mod shortcut;

use std::future::Future;
use std::pin::Pin;

pub trait Action {
    fn action_name(&self) -> &'static str;
    fn execute(&self) -> Pin<Box<dyn Future<Output = Result<(), crate::Error>> + Send>>;
}
