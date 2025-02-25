use std::pin::Pin;

mod java_jdk;

trait Action {
    fn execute(&self) -> Pin<Box<dyn Future<Output = ()> + Send>>;
}

#[tokio::main]
async fn main() {
    let actions: Vec<&dyn Action> = vec![&java_jdk::DownloadJDKAction];

    for action in actions {
        action.execute().await;
    }
}
