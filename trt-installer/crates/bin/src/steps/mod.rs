mod files;
mod jdk;
mod config;
mod welcome;

pub use welcome::WelcomeStepController;
pub use config::ConfigStepController;
pub use files::FilesStepController;
pub use jdk::JdkStepController;

#[derive(Debug, Clone, Copy)]
pub enum StepMessage {
    Progress(f32)
}