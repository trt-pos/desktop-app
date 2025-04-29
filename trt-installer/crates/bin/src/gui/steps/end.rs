use crate::config::InstallationConfig;
use crate::gui::app::Message;
use crate::gui::steps::Step;
use iced::{widget, Element, Task};
use std::path::{Path, PathBuf};
use std::{fs, io};

#[derive(Default)]
pub struct EndStep {}

impl Step for EndStep {
    fn title(&self) -> &'static str {
        "Congratulations!"
    }

    fn icon(&self) -> &'static [u8] {
        include_bytes!("../../../resources/icons/logo.png")
    }

    fn button_text(&self) -> &'static str {
        "Finish"
    }

    fn view(&self) -> Element<Message> {
        iced::widget::column![
            widget::text("You have successfully installed The Round Table!!")
                .align_x(iced::Alignment::Center),
            widget::text("You can now start the application and let your business grow with the best POS software in the market!")
                .align_x(iced::Alignment::Center),
        ]
        .padding(5)
        .into()
    }

    fn update(&mut self, message: Message) -> Task<Message> {
        Task::none()
    }

    fn apply(&self) -> Task<Message> {
        Task::future(async {
            if let Err(e) = move_files_to_final_dir().await {
                return Message::Error(format!("Failed to copy files: {}", e));
            }
            Message::NextStep
        })
    }
}

async fn move_files_to_final_dir() -> io::Result<()> {
    let config = InstallationConfig::get_config();
    let tmp_app_dir = config
        .tmp_installation_dir
        .join(&config.application_dir_name);
    let tmp_jdk_dir = config.tmp_installation_dir.join("jdk");

    let final_app_dir = PathBuf::from(&config.installation_dir).join(&config.application_dir_name);
    let final_jdk_dir = PathBuf::from(&config.installation_dir).join("jdk");

    fs::create_dir_all(&final_app_dir)?;
    fs::create_dir_all(&final_jdk_dir)?;

    // Copy the app files
    copy_dir_recursive(&tmp_app_dir, &final_app_dir)?;
    copy_dir_recursive(&tmp_jdk_dir, &final_jdk_dir)?;

    Ok(())
}

fn copy_dir_recursive(src: impl AsRef<Path>, dst: impl AsRef<Path>) -> io::Result<()> {
    let src = src.as_ref();
    let dst = dst.as_ref();

    if !dst.exists() {
        fs::create_dir_all(dst)?;
    }

    for entry in fs::read_dir(src)? {
        let entry = entry?;
        let path = entry.path();
        let dest_path = dst.join(entry.file_name());

        if path.is_dir() {
            copy_dir_recursive(path, &dest_path)?;
        } else {
            fs::copy(path, dest_path)?;
        }
    }

    Ok(())
}
