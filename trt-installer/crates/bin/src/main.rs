use iced::widget::image::Handle;
use iced::widget::{button, column, row};
use iced::{Alignment, Size, Task, Theme, widget, window};
use std::path::PathBuf;
use std::sync::LazyLock;

mod actions;
mod controllers;
mod error;
mod steps;
mod translations;

use crate::controllers::StepController;
use crate::steps::StepMessage;
pub use error::Error;

static INSTALLATION_DIR: LazyLock<PathBuf> = LazyLock::new(|| {
    let bin_path = std::env::current_exe().expect("Failed to get current executable path");
    bin_path
        .parent()
        .expect("Binary file should have a parent")
        .to_path_buf()
});

fn main() {
    let window_settings = window::Settings {
        size: Size::new(700f32, 500f32),
        min_size: Some(Size::new(700f32, 500f32)),
        resizable: true,
        decorations: true,
        ..Default::default()
    };

    let settings = iced::Settings {
        ..Default::default()
    };

    iced::application("TRT Installer", update, view)
        .theme(theme)
        .window(window_settings)
        .settings(settings)
        .run()
        .expect("Error running the application");

    // let actions: Vec<&dyn actions::Action> = vec![
    //     &actions::DownloadJDKAction,
    //     &actions::CreateShortcutAction
    // ];
    //
    // for action in actions {
    //     match action.execute().await {
    //         Ok(_) => {
    //             println!("{} executed successfully", action.action_name());
    //         }
    //         Err(e) => {
    //             eprintln!("{} failed: {}", action.action_name(), e);
    //
    //             if let Error::IgnorableError(_) = e {
    //                 continue;
    //             } else {
    //                 println!("The error is not ignorable, exiting...");
    //                 let _ = std::fs::remove_dir_all(&*INSTALLATION_DIR);
    //                 std::process::exit(1);
    //             }
    //         }
    //     }
    // }
}

type SharedStepController = Box<dyn StepController<Msg = Message>>;

struct TrtInstallerApp {
    actual_panel: u8,
    panels: [SharedStepController; 4],
}

impl Default for TrtInstallerApp {
    fn default() -> Self {
        Self {
            actual_panel: 0,
            panels: [
                Box::new(steps::WelcomeStepController::default()),
                Box::new(steps::ConfigStepController::default()),
                Box::new(steps::FilesStepController::default()),
                Box::new(steps::JdkStepController::default()),
            ],
        }
    }
}

#[derive(Debug, Clone, Copy)]
pub enum Message {
    Accept,
    Back,
    Next,
    Other(StepMessage),
}

fn view(app: &TrtInstallerApp) -> iced::Element<Message> {
    let actual_panel = app
        .panels
        .get(app.actual_panel as usize)
        .expect("Index out of bounds");
    
    row![
        widget::image(Handle::from_bytes(actual_panel.icon()))
            .width(75)
            .height(75),
        column![
            widget::text(actual_panel.title())
                .size(30)
                .height(75)
                .width(iced::Fill)
                .align_x(Alignment::Center)
                .align_y(Alignment::Center),
            widget::Space::new(0, 20),
            widget::container(actual_panel.view())
                .width(iced::Fill)
                .height(iced::Fill),
            row![
                widget::Space::new(iced::Fill, 0),
                widget::button("Back")
                    .style(button::secondary)
                    .on_press(Message::Back)
                    .width(125),
                widget::button("Continue")
                    .style(button::primary)
                    .on_press(Message::Next)
                    .width(125),
            ]
            .spacing(5)
        ]
    ]
    .spacing(15)
    .padding(10)
    .into()
}

fn update(app: &mut TrtInstallerApp, message: Message) -> Task<Message> {
    let actual_panel = app
        .panels
        .get_mut(app.actual_panel as usize)
        .expect("Index out of bounds");

    match message {
        Message::Accept => {
            return actual_panel.apply();
        }
        Message::Next => {
            app.actual_panel += 1;
        }
        Message::Back => {
            app.actual_panel -= 1;
        }
        other => {
            return actual_panel.update(other);
        }
    }

    Task::none()
}

fn theme(app: &TrtInstallerApp) -> Theme {
    Theme::TokyoNight
}
