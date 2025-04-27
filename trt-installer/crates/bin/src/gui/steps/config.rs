use crate::config::InstallationConfig;
use crate::gui::app::Message;
use crate::gui::steps::Step;
use iced::widget::{column, image, row, value};
use iced::{Border, ContentFit, Element, Task, color, widget};
use std::fs;
use std::fs::File;
use std::path::PathBuf;
use std::time::{Instant, SystemTime, UNIX_EPOCH};
use futures::future::ok;

#[derive(Clone)]
pub struct ConfigStep {
    installation_dir: String,
    create_checkbox: bool,
    application_dir_name: String,
    tmp_installation_dir: PathBuf,
}

impl Default for ConfigStep {
    fn default() -> Self {
        let mut installation_dir = None;

        #[cfg(target_os = "windows")]
        {
            installation_dir = Some("C:\\Program Files".to_string());
        }

        #[cfg(target_os = "linux")]
        {
            installation_dir = Some("/opt/".to_string());
        }

        let installation_dir = if let Some(d) = installation_dir {
            d
        } else {
            panic!("Application is running in an unexpected OS")
        };

        let tmp_installation_dir = std::env::temp_dir().join(format!(
            "theroundtable-{}",
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .expect("UNIX_EPOCH is before SystemTime::now()")
                .as_secs()
        ));
        
        let _ = fs::remove_dir(&tmp_installation_dir);
        let _ = fs::create_dir_all(&tmp_installation_dir);

        Self {
            installation_dir,
            create_checkbox: false,
            application_dir_name: "theroundtable".to_string(),
            tmp_installation_dir,
        }
    }
}

impl Into<InstallationConfig> for ConfigStep {
    fn into(self) -> InstallationConfig {
        InstallationConfig {
            installation_dir: self.installation_dir,
            create_shortcut: self.create_checkbox,
            application_dir_name: self.application_dir_name,
        }
    }
}

impl Step for ConfigStep {
    fn title(&self) -> &'static str {
        "Installation configuration"
    }

    fn icon(&self) -> &'static [u8] {
        include_bytes!("../../../resources/icons/settings.png")
    }

    fn view(&self) -> Element<Message> {
        column!(
            widget::text("Installation directory"),
            row![
                widget::text_input("", &self.installation_dir),
                widget::button::Button::new(
                    widget::image(image::Handle::from_bytes(
                        include_bytes!("../../../resources/icons/open-folder.png").as_slice(),
                    ))
                    .content_fit(ContentFit::Fill)
                )
                .width(50)
                .height(50)
                .style(|_t, _s| {
                    widget::button::Style {
                        background: Some(color!(0, 0, 0, 0f32).into()),
                        border: Border::default().rounded(5.0),
                        ..Default::default()
                    }
                })
                .on_press(Message::FolderSelection)
                .width(60)
                .height(50),
            ]
            .width(iced::Fill)
            .height(50)
            .align_y(iced::Alignment::Center)
            .spacing(5),
            widget::Space::new(10, 0),
            widget::text("Application directory name"),
            widget::text_input("", &self.application_dir_name).on_input(Message::ApplicationDirNameInputText),
            widget::Space::new(10, 0),
            widget::checkbox("Create app shortcut   ", self.create_checkbox)
                .on_toggle(|value| { Message::CreateShortcutCheckbox(value) }),
        )
        .spacing(5)
        .into()
    }

    fn update(&mut self, message: Message) -> Task<Message> {
        match message {
            Message::FolderSelected(path) => {
                self.installation_dir = path;
            }
            Message::FolderSelection => {
                let installation_dir = self.installation_dir.clone();

                return Task::future(async move {
                    let selected_folder = select_folder(&installation_dir).await;
                    if let Some(path) = selected_folder {
                        Message::FolderSelected(path)
                    } else {
                        Message::Error("No folder selected".to_string())
                    }
                });
            }
            Message::ApplicationDirNameInputText(value) => self.application_dir_name = value,
            Message::CreateShortcutCheckbox(value) => self.create_checkbox = value,
            _ => {}
        }

        Task::none()
    }

    fn apply(&self) -> Task<Message> {
        let config = self.clone();

        Task::future(async move {
            let dir = PathBuf::from(&config.installation_dir);

            let _ = fs::create_dir_all(&dir);
            let test_file_path = dir.join("trt-write-test-file");
            if let Err(e) = File::create(&test_file_path) {
                return Message::Error(format!("Failed to create test file: {}", e));
            };

            let _ = fs::remove_file(&test_file_path);

            InstallationConfig::set_config(config.into());

            Message::NextStep
        })
    }
}

async fn select_folder(start_dir: &str) -> Option<String> {
    let file = rfd::AsyncFileDialog::new()
        .set_title("Select installation directory")
        .set_directory(start_dir)
        .pick_folder()
        .await?;

    Some(file.path().to_str()?.to_string())
}
