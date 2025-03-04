use std::pin::Pin;

mod java_jdk;
mod error;

pub use error::Error;

trait Action {
    fn execute(&self) -> Pin<Box<dyn Future<Output = Result<(), Error>> + Send>>;
}

#[tokio::main]
async fn main() {
    let actions: Vec<&dyn Action> = vec![&java_jdk::DownloadJDKAction];

    for action in actions {
        action.execute().await.expect("Failed to execute action");
    }
}
