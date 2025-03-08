use std::path::PathBuf;
use std::pin::Pin;
use std::sync::LazyLock;

mod error;
mod java_jdk;
mod shortcut;

pub use error::Error;

static INSTALLATION_DIR: LazyLock<PathBuf> = LazyLock::new(|| {
    let bin_path = std::env::current_exe().expect("Failed to get current executable path");
    bin_path
        .parent()
        .expect("Binary file should have a parent")
        .to_path_buf()
});

trait Action {
    fn action_name(&self) -> &'static str;
    fn execute(&self) -> Pin<Box<dyn Future<Output = Result<(), Error>> + Send>>;
}

#[tokio::main]
async fn main() {
    let actions: Vec<&dyn Action> = vec![
        &java_jdk::DownloadJDKAction,
        &shortcut::CreateShortcutAction
    ];

    for action in actions {
        match action.execute().await {
            Ok(_) => {
                println!("{} executed successfully", action.action_name());
            }
            Err(e) => {
                eprintln!("{} failed: {}", action.action_name(), e);

                if let Error::IgnorableError(_) = e {
                    continue;
                } else {
                    println!("The error is not ignorable, exiting...");
                    let _ = std::fs::remove_dir_all(&*INSTALLATION_DIR);
                    std::process::exit(1);
                }
            }
        }
    }
}
