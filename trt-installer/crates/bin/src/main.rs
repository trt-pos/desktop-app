use iced::{window, Size};
mod actions;
mod error;
mod gui;
mod translations;
pub mod config;

use crate::gui::TrtInstallerApp;
pub use error::Error;

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

    iced::application(
        "TRT Installer",
        TrtInstallerApp::update,
        TrtInstallerApp::view,
    )
        .theme(TrtInstallerApp::theme)
        .window(window_settings)
        .settings(settings)
        .subscription(TrtInstallerApp::subscription)
        .run()
        .expect("Error running the application");
}
