mod config;
mod end;
mod files;
mod jdk;
mod welcome;

use crate::gui::app::Message;
pub use config::ConfigStep;
pub use end::EndStep;
pub use files::FilesStep;
use iced::Task;
pub use jdk::JdkStep;
pub use welcome::WelcomeStep;

pub trait Step {
    fn title(&self) -> &'static str;
    fn icon(&self) -> &'static [u8];
    fn button_text(&self) -> &'static str;

    fn skip(&self) -> bool {
        false
    }

    fn view(&self) -> iced::Element<Message>;
    fn update(&mut self, message: Message) -> Task<Message>;
    fn apply(&self) -> Task<Message>;
}
