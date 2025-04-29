mod files;
mod jdk;
mod config;
mod welcome;
mod end;

use crate::gui::app::Message;
pub use config::ConfigStep;
pub use files::FilesStep;
use iced::Task;
pub use jdk::JdkStep;
pub use welcome::WelcomeStep;
pub use end::EndStep;

pub trait Step {
    fn title(&self) -> &'static str;
    fn icon(&self) -> &'static [u8];
    fn button_text(&self) -> &'static str;
    
    fn view(&self) -> iced::Element<Message>;
    fn update(&mut self, message: Message) -> Task<Message>;
    fn apply(&self) -> Task<Message>;
}