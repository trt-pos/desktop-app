use iced::Task;
use std::fmt::Debug;

pub trait Controller: Sync + Send {
    type Msg: Debug + Clone + Copy;
    
    fn view(&self) -> iced::Element<Self::Msg>;
    fn update(&mut self, message: Self::Msg) -> Task<Self::Msg>;
}

pub trait StepController: Controller {
    fn title(&self) -> &'static str;
    fn icon(&self) -> &'static [u8];
    fn apply(&self) -> Task<Self::Msg>;
}