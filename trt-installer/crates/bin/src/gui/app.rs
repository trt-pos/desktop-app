use crate::gui::steps;
use crate::gui::steps::Step;
use iced::widget::image::Handle;
use iced::widget::{button, column, row};
use iced::{Subscription, Task, Theme, widget};
use std::time::Duration;

pub struct TrtInstallerApp {
    actual_panel: usize,
    panels: [Box<dyn Step>; 5],
    last_error: String,
    waiting_apply: bool,
    block_back_button: bool,
}

impl Default for TrtInstallerApp {
    fn default() -> Self {
        Self {
            actual_panel: 0,
            panels: [
                Box::new(steps::WelcomeStep::default()),
                Box::new(steps::ConfigStep::default()),
                Box::new(steps::FilesStep::default()),
                Box::new(steps::JdkStep::default()),
                Box::new(steps::EndStep::default()),
            ],
            last_error: String::new(),
            waiting_apply: false,
            block_back_button: false,
        }
    }
}

#[derive(Clone, Debug)]
pub struct ProgressTaskStatus {
    pub message: String,
    pub progress: f32,
    pub length: f32,
}

#[derive(Debug, Clone)]
pub enum Message {
    None,
    AppTick,

    BlockBackButton,
    AcceptStep,
    PreviousStep,
    NextStep,

    Error(String),

    FolderSelection,
    FolderSelected(String),
    ApplicationDirNameInputText(String),
    CreateShortcutCheckbox(bool),
    // SendAnalyticsCheckbox(bool),
    DownloadProgress(ProgressTaskStatus),
    DownloadStarted,
}

impl TrtInstallerApp {
    pub fn view(&self) -> iced::Element<Message> {
        let actual_panel = self
            .panels
            .get(self.actual_panel)
            .expect("Index out of bounds");

        let button_text = actual_panel.button_text();

        let continue_button = if !self.waiting_apply {
            widget::button(button_text)
                .style(button::primary)
                .on_press(Message::AcceptStep)
                .width(125)
        } else {
            widget::button(button_text)
                .style(button::primary)
                .width(125)
        };

        let back_button = if self.block_back_button {
            row![]
        } else {
            row![
                widget::button("Back")
                    .style(button::secondary)
                    .on_press(Message::PreviousStep)
                    .width(125)
            ]
        };

        let version = env!("CARGO_PKG_VERSION");

        row![
            column![
                widget::image(Handle::from_bytes(actual_panel.icon()))
                    .width(75)
                    .height(75),
                widget::Space::new(0, iced::Fill),
                widget::text(format!("v{version}")).size(10)
            ],
            column![
                widget::text(actual_panel.title())
                    .size(30)
                    .height(75)
                    .width(iced::Fill)
                    .align_x(iced::Alignment::Center)
                    .align_y(iced::Alignment::Center),
                widget::container(actual_panel.view())
                    .width(iced::Fill)
                    .height(iced::Fill),
                widget::text(&self.last_error).color(iced::Color::from_rgb8(255, 31, 31)),
                row![
                    widget::Space::new(iced::Fill, 0),
                    back_button,
                    continue_button,
                ]
                .spacing(5)
            ]
            .spacing(10)
        ]
        .spacing(15)
        .padding(10)
        .into()
    }

    pub fn update(&mut self, message: Message) -> Task<Message> {
        let actual_panel = self
            .panels
            .get_mut(self.actual_panel)
            .expect("Index out of bounds");

        match message.clone() {
            Message::AcceptStep => {
                self.waiting_apply = true;
                return actual_panel.apply();
            }
            Message::NextStep => {
                self.waiting_apply = false;
                self.actual_panel += 1;
                self.last_error = String::new();

                if self.panels.len() == self.actual_panel {
                    self.actual_panel -= 1;
                    return iced::exit();
                }

                let mut actual_panel = self
                    .panels
                    .get_mut(self.actual_panel)
                    .expect("Index out of bounds");

                while actual_panel.skip() {
                    self.actual_panel += 1;

                    actual_panel = self
                        .panels
                        .get_mut(self.actual_panel)
                        .expect("Index out of bounds");
                }
            }
            Message::PreviousStep => {
                self.actual_panel -= 1;
            }
            Message::BlockBackButton => {
                self.block_back_button = true;
            }
            Message::Error(error) => {
                self.waiting_apply = false;
                self.last_error = error;
                return actual_panel.update(message);
            }
            other => {
                return actual_panel.update(other);
            }
        }

        Task::none()
    }

    pub fn subscription(&self) -> Subscription<Message> {
        Subscription::batch([self.tick_subscription()])
    }

    fn tick_subscription(&self) -> Subscription<Message> {
        let tick_rate = Duration::from_millis(33);
        iced::time::every(tick_rate).map(move |_| Message::AppTick)
    }

    pub fn theme(&self) -> Theme {
        Theme::TokyoNight
    }
}
