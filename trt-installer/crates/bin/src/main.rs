#![allow(unreachable_code)]

use iced::{window, Size};
pub mod config;
mod error;
mod gui;
mod translations;
mod privileges;

use crate::gui::TrtInstallerApp;
pub use error::Error;

static COMPRESSED_APP_FILES: &[u8] = include_bytes!(env!("APP_ZIP_PATH"));

fn main() {
    env_logger::init();
    
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
