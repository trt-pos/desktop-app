use crate::config::InstallationConfig;
use crate::gui::app::Message;
use crate::gui::steps::Step;
use iced::widget::row;
use iced::{Element, Task, widget};
use log::info;
use std::io;
use std::io::Write;
use std::path::PathBuf;

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

    fn update(&mut self, _: Message) -> Task<Message> {
        Task::none()
    }

    fn apply(&self) -> Task<Message> {
        Task::batch([
            Task::future(async {
                if let Err(e) = copy_files().await {
                    return Message::Error(format!("Failed to copy files: {}", e));
                }
                Message::NextStep
            }),
            Task::done(Message::BlockBackButton),
        ])
    }
}

async fn copy_files() -> io::Result<()> {
    let config = InstallationConfig::get_config();
    let installation_dir = &config.tmp_installation_dir;
    let application_dir_name = &config.application_dir_name;

    let extract_dir = installation_dir.join(application_dir_name);

    let zip_file_path = installation_dir.join("app.zip");
    let mut file = std::fs::File::create(&zip_file_path)?;
    file.write_all(crate::COMPRESSED_APP_FILES)?;

    info!(
        "Writing app zip file to {}",
        zip_file_path.to_string_lossy()
    );
    info!(
        "Extracting app zip file to {}",
        extract_dir.to_string_lossy()
    );

    // Unzip the file
    let mut archive = zip::ZipArchive::new(std::fs::File::open(&zip_file_path)?)?;
    for i in 0..archive.len() {
        let mut file = archive.by_index(i)?;
        let outpath = extract_dir.join(
            file.mangled_name()
                .components()
                .skip(1)
                .collect::<PathBuf>(),
        );

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
