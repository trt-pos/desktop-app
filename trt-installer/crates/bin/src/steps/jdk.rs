use iced::{widget, Element, Task};
use iced::widget::column;
use crate::controllers::{Controller, StepController};
use crate::Message;

#[derive(Default)]
pub struct JdkStepController {
    progress: f32
}

impl Controller for JdkStepController {
    type Msg = Message;

    fn view(&self) -> Element<Self::Msg> {
        column![
            widget::Space::new(iced::Fill, iced::Fill),
            widget::progress_bar(0f32..=1f32, self.progress),
            widget::Space::new(iced::Fill, iced::Fill),
        ].into()
    }

    fn update(&mut self, message: Self::Msg) -> Task<Self::Msg> {
        Task::none()
    }
}

impl StepController for JdkStepController {
    fn apply(&self) -> Task<Self::Msg> {
        Task::future(async {
            download_jdk().await;
            Message::Next
        })
    }
fn icon(&self) -> &'static [u8] { 
    include_bytes!("../../resources/download.png")
}

    fn title(&self) -> &'static str {
        "Download JDK"
    }
}

async fn download_jdk() {

}