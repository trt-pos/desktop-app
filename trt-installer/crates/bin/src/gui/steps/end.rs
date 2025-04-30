use crate::config::InstallationConfig;
use crate::gui::app::Message;
use crate::gui::steps::Step;
use crate::privileges::execute_script_with_privileges;
use iced::{widget, Element, Task};
use std::io;
use std::path::PathBuf;

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
            widget::text("The installation its almost done!")
                .align_x(iced::Alignment::Center),
            widget::text("Pressing 'Finish' will copy the files to the final directory and you will \
            be ready to start the application and let your business grow with the best POS software \
            in the market!")
                .align_x(iced::Alignment::Center),
        ]
            .spacing(5)
        .padding(5)
        .into()
    }

    fn update(&mut self, _: Message) -> Task<Message> {
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
    let final_jdk_dir = final_app_dir.join("jdk");

    #[cfg(target_os = "linux")]
    {
        let final_app_dir = final_app_dir.to_string_lossy();
        let final_jdk_dir = final_jdk_dir.to_string_lossy();

        let tmp_app_dir = tmp_app_dir.to_string_lossy();
        let tmp_jdk_dir = tmp_jdk_dir.to_string_lossy();

        let shortcut_block = if config.create_shortcut {
            format!(
                r#"
# Create the desktop entry
cat <<EOF > /usr/share/applications/the-round-table.desktop
[Desktop Entry]
Version=1.0
Type=Application
Name=The Round Table
Exec={final_app_dir}/start
Icon={final_app_dir}/images/icon.png
Categories=Application;
EOF
chmod +x /usr/share/applications/the-round-table.desktop
            "#
            )
        } else {
            String::new()
        };

        let installation_script = format!(
            r#"
#!/bin/bash
mkdir -p {final_app_dir}
mkdir -p {final_jdk_dir}

cp -r {tmp_app_dir}/* {final_app_dir}
cp -r {tmp_jdk_dir}/* {final_jdk_dir}

chmod +x {final_app_dir}/start

{shortcut_block}
        "#
        );

        execute_script_with_privileges(&installation_script)?;
        return Ok(());
    }

    panic!("Not implemented for this platform");
}
