use crate::controllers::{Controller, StepController};
use crate::Message;
use iced::{widget, Element, Task};

#[derive(Default)]
pub struct FilesStepController {
    progress: f32
}

impl Controller for FilesStepController {
    type Msg = Message;

    fn view(&self) -> Element<Self::Msg> {
        widget::progress_bar(0f32..=1f32, self.progress)
            .into()
    }

    fn update(&mut self, message: Self::Msg) -> Task<Self::Msg> {
        Task::none()
    }
}

impl StepController for FilesStepController {
    fn title(&self) -> &'static str {
        "Copy application files"
    }

    fn icon(&self) -> &'static [u8] {
        include_bytes!("../../resources/copy.png")
    }

    fn apply(&self) -> Task<Self::Msg> {
        Task::future(async {
            copy_files().await;
            Message::Next
        })
    }
}

async fn copy_files() {

}