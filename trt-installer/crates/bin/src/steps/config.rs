use crate::controllers::{Controller, StepController};
use crate::Message;
use iced::{widget, Element, Task};

#[derive(Default)]
pub struct ConfigStepController {}

impl Controller for ConfigStepController {
    type Msg = Message;

    fn view(&self) -> Element<Self::Msg> {
        widget::text("Configuration Step")
            .into()
    }

    fn update(&mut self, message: Self::Msg) -> Task<Self::Msg> {
        Task::none()
    }
}

impl StepController for ConfigStepController {
    fn title(&self) -> &'static str {
        "Installation configuration"
    }

    fn icon(&self) -> &'static [u8] {
        include_bytes!("../../resources/settings.png")
    }

    fn apply(&self) -> Task<Self::Msg> {
        Task::future(async move { Message::Next })
    }
}