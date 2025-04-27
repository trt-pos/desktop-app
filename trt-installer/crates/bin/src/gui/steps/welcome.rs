use crate::gui::app::Message;
use crate::gui::steps::Step;
use iced::{widget, Element, Task};
use widget::column;

#[derive(Default)]
pub struct WelcomeStep {}

impl Step for WelcomeStep {
    fn title(&self) -> &'static str {
        "Welcome to the Trt Installer!"
    }

    fn icon(&self) -> &'static [u8] {
        include_bytes!("../../../resources/icons/logo.png")
    }

    fn view(&self) -> Element<Message> {
        column![
            widget::text("Through this application you will be able to configure and install The Round Table, the open source POS software!").align_x(iced::Alignment::Center),
        ].padding(5)
         .into()
    }

    fn update(&mut self, message: Message) -> Task<Message> {
        Task::none()
    }



    fn apply(&self) -> Task<Message> {
        Task::future(async move { Message::NextStep })
    }
}
