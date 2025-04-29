use crate::config;
use crate::gui::app::{Message, ProgressTaskStatus};
use crate::gui::steps::Step;
use flate2::read::GzDecoder;
use futures::StreamExt;
use iced::widget::row;
use iced::{Element, Task, widget};
use std::ops::Deref;
use std::path::PathBuf;
use std::sync::{Arc, LazyLock};
use tokio::fs;
use tokio::fs::File;
use tokio::io::AsyncWriteExt;
use tokio::sync::{Mutex, MutexGuard, RwLock, RwLockWriteGuard, TryLockError};

/// JDK 23.0.2 download URLs:
/// Linux/AArch64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_linux-aarch64_bin.tar.gz
/// Linux/x64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_linux-x64_bin.tar.gz
/// macOS/Aarch64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_macos-aarch64_bin.tar.gz
/// macOS/x64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_macos-x64_bin.tar.gz
/// Windows/x64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_windows-x64_bin.zip
static JDK_DOWNLOAD_URL: LazyLock<String> = LazyLock::new(|| {
    let version = "23.0.2";
    let os = std::env::consts::OS;
    let arch = std::env::consts::ARCH;

    let platform = match os {
        "windows" => "windows",
        "macos" => "macos",
        "linux" => "linux",
        _ => panic!("Unsupported OS: {}", os),
    };

    let extension = &*RESOURCE_EXTENSION;

    let arch = match arch {
        "aarch64" => "aarch64",
        "x86_64" => "x64",
        _ => panic!("Unsupported arch: {}", arch),
    };

    format!(
        "https://download.java.net/java/GA/jdk{}/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-{}_{}-{}_bin.{}",
        version, version, platform, arch, extension
    )
});

static RESOURCE_EXTENSION: LazyLock<String> = LazyLock::new(|| {
    let os = std::env::consts::OS;

    match os {
        "windows" => "zip",
        "macos" => "tar.gz",
        "linux" => "tar.gz",
        _ => panic!("Unsupported OS: {}", os),
    }
    .to_string()
});

static DOWNLOAD_STATUS: LazyLock<Arc<Mutex<ProgressTaskStatus>>> = LazyLock::new(|| {
    Arc::new(Mutex::new(ProgressTaskStatus {
        message: String::new(),
        progress: 0.0,
        length: 0.0,
    }))
});

#[derive(Default)]
pub struct JdkStep {
    download_progress: f32,
    download_total: f32,
    download_message: String,
    download_started: bool,
}

impl Step for JdkStep {
    fn title(&self) -> &'static str {
        "Download JDK"
    }

    fn icon(&self) -> &'static [u8] {
        include_bytes!("../../../resources/icons/download.png")
    }

    fn view(&self) -> Element<Message> {
        let text = if self.download_started {
            "Downloading JDK..."
        } else {
            "The JDK is required to run the application. Please wait while the wizard downloads it."
        };

        let status_bar = if self.download_started {
            let message = self.download_message.clone();
            let progress = self.download_progress * self.download_total;

            row![
                widget::Space::new(iced::Fill, 0),
                widget::text(message).width(150),
                widget::text(format!("{:.2} ", progress / 1_000_000.0,))
                    .width(60)
                    .align_x(iced::Right),
                widget::text(" / "),
                widget::text(format!("{:.2} MB", self.download_total / 1_000_000.0))
                    .width(95)
                    .align_x(iced::Left),
                widget::Space::new(40, 0),
            ]
            .spacing(5)
        } else {
            row![]
        };

        let progress_percentage = (self.download_progress * 100.0).round() as u32;

        iced::widget::column![
            widget::Space::new(iced::Fill, iced::Fill),
            widget::column![
                widget::text(text),
                row![
                    widget::progress_bar(0f32..=1f32, self.download_progress),
                    widget::text(format!("{} %", progress_percentage))
                        .align_x(iced::Center)
                        .width(60)
                ]
                .align_y(iced::Center),
                status_bar,
            ]
            .width(550)
            .spacing(10),
            widget::Space::new(iced::Fill, iced::Fill),
        ]
        .spacing(5)
        .align_x(iced::Center)
        .into()
    }

    fn update(&mut self, message: Message) -> Task<Message> {
        match message {
            Message::DownloadProgress(download_status) => {
                self.download_progress = download_status.progress;
                self.download_message = download_status.message;
                self.download_total = download_status.length;
            }
            Message::DownloadStarted => self.download_started = true,
            Message::DownloadComplete => {
                self.download_progress = 1.0;
            }
            Message::AppTick => {
                return Task::future(async {
                    let download_status = match DOWNLOAD_STATUS.try_lock() {
                        Ok(v) => v,
                        Err(_) => return Message::None,
                    };
                    Message::DownloadProgress(download_status.clone())
                });
            }
            _ => {}
        }

        Task::none()
    }

    fn apply(&self) -> Task<Message> {
        Task::batch([
            Task::done(Message::DownloadStarted),
            Task::future(async move {
                if let Err(e) = download_jdk().await {
                    return Message::Error(e.to_string());
                }

                Message::NextStep
            }),
        ])
    }
}

async fn download_jdk() -> Result<(), crate::Error> {
    // Downloading the compressed JDK folder
    let url = JDK_DOWNLOAD_URL.deref();
    let response = reqwest::get(url).await?;
    let total_size = response.content_length().unwrap_or(200_000_000);

    {
        let mut download_status = DOWNLOAD_STATUS.lock().await;
        download_status.length = total_size as f32;
        download_status.message = "Downloading...".to_string();
    }

    let installation_config = config::InstallationConfig::get_config();
    let tmp_dir = &(installation_config.tmp_installation_dir);
    let compressed_file_path = tmp_dir.join(format!("jdk.{}", *RESOURCE_EXTENSION));

    let mut file = File::create(&compressed_file_path).await?;
    let mut downloaded = 0u64;
    let mut stream = response.bytes_stream();

    while let Some(item) = stream.next().await {
        let chunk = item?;
        file.write_all(&chunk).await?;
        downloaded += chunk.len() as u64;

        {
            let mut download_status = match DOWNLOAD_STATUS.try_lock() {
                Ok(v) => v,
                Err(_) => continue,
            };
            download_status.length = total_size as f32;
            download_status.progress = downloaded as f32 / total_size as f32;
            download_status.message = "Downloading...".to_string();
        }
    }

    {
        let mut download_status = DOWNLOAD_STATUS.lock().await;
        download_status.length = 1.0;
        download_status.progress = 0.0;
        download_status.message = "Extracting files...".to_string();
    }

    let file = std::fs::File::open(&compressed_file_path)?;
    let output_path = PathBuf::from(tmp_dir).join("jdk");

    // Extracting the compressed JDK folder
    match RESOURCE_EXTENSION.deref().as_str() {
        "zip" => {
            let reader = std::io::BufReader::new(file);
            let mut archive = zip::ZipArchive::new(reader)?;
            archive.extract(&output_path)?;
        }
        "tar.gz" => {
            let tar = GzDecoder::new(file);
            let mut archive = tar::Archive::new(tar);
            archive.unpack(&output_path)?;
        }
        _ => {
            #[cfg(debug_assertions)]
            {
                panic!("Unsupported extension: {}", *RESOURCE_EXTENSION)
            }

            return Err(crate::Error::GenericError(RESOURCE_EXTENSION.to_string()));
        }
    };

    let _ = std::fs::remove_file(&compressed_file_path);

    // Moving the inner folder inside the decompressed folder to the parent folder and renaming it to "jdk"
    let inner_folder = output_path
        .read_dir()?
        .next()
        .expect("Failed to get next entry")?
        .path();

    let tmp_path = tmp_dir.join("tmp");
    std::fs::rename(&inner_folder, &tmp_path)?;

    fs::remove_dir(&output_path).await?;
    std::fs::rename(&tmp_path, &output_path)?;

    Ok(())
}
