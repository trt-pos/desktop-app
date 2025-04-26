use crate::Message;
use crate::controllers::{Controller, StepController};
use iced::{Element, Task, widget};
use widget::column;

#[derive(Default)]
pub struct WelcomeStepController {}

impl Controller for WelcomeStepController {
    type Msg = Message;

    fn view(&self) -> Element<Self::Msg> {
        column![
            widget::text("Through this application you will be able to configure and install The Round Table, the open source POS software!").align_x(iced::Alignment::Center),
            widget::Space::new(0, 5),
            widget::image("crates/bin/resources/icon.png")
                .width(200)
                .height(200),
        ].padding(5)
         .into()
    }

    fn update(&mut self, message: Self::Msg) -> Task<Self::Msg> {
        Task::none()
    }
}

impl StepController for WelcomeStepController {
    fn title(&self) -> &'static str {
        "Welcome to the Trt Installer!"
    }

    fn icon(&self) -> &'static [u8] {
        include_bytes!("../../resources/icons/logo.png")
    }

    fn apply(&self) -> Task<Self::Msg> {
        Task::future(async move { Message::Next })
    }
}
