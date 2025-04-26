mod download_jdk;
mod shortcut;

pub use download_jdk::DownloadJDKAction;
pub use shortcut::CreateShortcutAction;
use std::future::Future;
use std::pin::Pin;

pub trait Action {
    fn action_name(&self) -> &'static str;
    fn execute(&self) -> Pin<Box<dyn Future<Output = Result<(), crate::Error>> + Send>>;
}
