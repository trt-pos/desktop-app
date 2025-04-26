mod files;
mod jdk;
mod config;
mod welcome;

use crate::gui::app::Message;
pub use config::ConfigStep;
pub use files::FilesStep;
use iced::Task;
pub use jdk::JdkStep;
pub use welcome::WelcomeStep;

pub trait Step {
    fn title(&self) -> &'static str;
    fn icon(&self) -> &'static [u8];
    fn view(&self) -> iced::Element<Message>;
    fn update(&mut self, message: Message) -> Task<Message>;
    fn validate(&self) -> bool;
    fn apply(&self) -> Task<Message>;
}