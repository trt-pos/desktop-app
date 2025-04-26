use iced::{window, Size};
use std::path::PathBuf;
use std::sync::LazyLock;
mod actions;
mod error;
mod gui;
mod translations;
pub mod config;

use crate::gui::TrtInstallerApp;
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
