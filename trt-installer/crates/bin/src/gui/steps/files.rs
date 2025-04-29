use crate::config::InstallationConfig;
use crate::gui::app::Message;
use crate::gui::steps::Step;
use iced::widget::row;
use iced::{Element, Task, widget};
use std::io;
use std::io::Write;

static COMPRESSED_FILES: &[u8] = include_bytes!("../../../resources/app-files.zip");

#[derive(Default)]
pub struct FilesStep {
    progress: f32,
}

impl Step for FilesStep {
    fn title(&self) -> &'static str {
        "Install application files"
    }

    fn icon(&self) -> &'static [u8] {
        include_bytes!("../../../resources/icons/copy.png")
    }

    fn button_text(&self) -> &'static str {
        "Install"
    }

    fn view(&self) -> Element<Message> {
        let text = "The app files will be written to disk.";

        iced::widget::column![
            widget::Space::new(iced::Fill, iced::Fill),
            widget::column![
                widget::text(text),
                row![
                    widget::progress_bar(0f32..=1f32, self.progress),
                    widget::text(format!("{} %", self.progress))
                        .align_x(iced::Center)
                        .width(60)
                ]
                .align_y(iced::Center),
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
        Task::none()
    }

    fn apply(&self) -> Task<Message> {
        Task::future(async {
            if let Err(e) = copy_files().await {
                return Message::Error(format!("Failed to copy files: {}", e));
            }
            Message::NextStep
        })
    }
}

async fn copy_files() -> io::Result<()> {
    let config = InstallationConfig::get_config();
    let installation_dir = &config.tmp_installation_dir;

    let zip_file_path = installation_dir.join("app.zip");
    let mut file = std::fs::File::create(&zip_file_path)?;
    file.write_all(COMPRESSED_FILES)?;

    // Unzip the file
    let mut archive = zip::ZipArchive::new(std::fs::File::open(&zip_file_path)?)?;
    for i in 0..archive.len() {
        let mut file = archive.by_index(i)?;
        let outpath = installation_dir.join(file.mangled_name());

        if file.name().ends_with('/') {
            std::fs::create_dir_all(&outpath)?;
        } else {
            if let Some(parent) = outpath.parent() {
                std::fs::create_dir_all(parent)?;
            }
            let mut outfile = std::fs::File::create(&outpath)?;
            io::copy(&mut file, &mut outfile)?;
        }
    }
    std::fs::remove_file(zip_file_path)?;

    Ok(())
}
