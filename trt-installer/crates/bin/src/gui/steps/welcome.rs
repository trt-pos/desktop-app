use crate::gui::app::Message;
use crate::gui::steps::Step;
use iced::{Element, Task, widget};
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

    fn button_text(&self) -> &'static str {
        "Continue"
    }

    fn view(&self) -> Element<Message> {
        column![
            widget::text("Thank you for choosing The Round Table! The best free and open source POS software in the market")
            .align_x(iced::Alignment::Center),
            widget::text("Through this wizard you will be able to configure and install The Round Table, the open source POS software!")
            .align_x(iced::Alignment::Center),
            widget::text("Get ready to start your journey with us!")
        ].spacing(5)
         .padding(5)
         .into()
    }

    fn update(&mut self, _: Message) -> Task<Message> {
        Task::none()
    }

    fn apply(&self) -> Task<Message> {
        Task::future(async move { Message::NextStep })
    }
}
