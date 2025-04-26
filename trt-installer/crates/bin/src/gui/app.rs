use crate::gui::steps;
use crate::gui::steps::Step;
use iced::widget::image::Handle;
use iced::widget::{button, column, row};
use iced::{widget, Subscription, Task, Theme};
use std::time::{Duration, Instant};

pub struct TrtInstallerApp {
    actual_panel: usize,
    panels: [Box<dyn Step>; 4],
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
            ],
        }
    }
}

#[derive(Debug, Clone)]
pub enum Message {
    AppTick,
    
    AcceptStep,
    PreviousStep,
    NextStep,
    
    Error(String),
    
    FolderSelection,
    FolderSelected(String),
    CreateShortcutCheckbox(bool),
    
    DownloadProgress(f32),
    DownloadComplete,
    DownloadStarted,
}

impl TrtInstallerApp {
    pub fn view(&self) -> iced::Element<Message> {
        let actual_panel = self
            .panels
            .get(self.actual_panel as usize)
            .expect("Index out of bounds");

        row![
            widget::image(Handle::from_bytes(actual_panel.icon()))
                .width(75)
                .height(75),
            column![
                widget::text(actual_panel.title())
                    .size(30)
                    .height(75)
                    .width(iced::Fill)
                    .align_x(iced::Alignment::Center)
                    .align_y(iced::Alignment::Center),
                widget::Space::new(0, 20),
                widget::container(actual_panel.view())
                    .width(iced::Fill)
                    .height(iced::Fill),
                row![
                    widget::Space::new(iced::Fill, 0),
                    widget::button("Back")
                        .style(button::secondary)
                        .on_press(Message::PreviousStep)
                        .width(125),
                    widget::button("Continue")
                        .style(button::primary)
                        .on_press(Message::AcceptStep)
                        .width(125),
                ]
                .spacing(5)
            ]
        ]
            .spacing(15)
            .padding(10)
            .into()
    }

    pub fn update(&mut self, message: Message) -> Task<Message> {
        let actual_panel = self
            .panels
            .get_mut(self.actual_panel as usize)
            .expect("Index out of bounds");

        match message {
            Message::AcceptStep => {
                if !actual_panel.validate() {
                    return Task::none();
                }

                return  actual_panel.apply();
            }
            Message::NextStep => {
                self.actual_panel += 1;
                
                if self.panels.len() == self.actual_panel { 
                    self.actual_panel -= 1;
                    return iced::exit();
                }
                
            }
            Message::PreviousStep => {
                self.actual_panel -= 1;
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
        let tick_rate = Duration::from_millis(100);
        let now = Instant::now();
        iced::time::every(tick_rate).map(move |_| {
            Message::AppTick
        })
    }

    pub fn theme(&self) -> Theme {
        Theme::TokyoNight
    }
}
