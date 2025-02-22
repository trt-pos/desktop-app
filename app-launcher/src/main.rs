use std::fs;
use std::fs::OpenOptions;
use std::os::unix::process::CommandExt;
use std::path::Path;
use std::process::{Command, Stdio};

const MAX_LOG_SIZE: u64 = 5 * 1024 * 1024;

fn main() {
    let app_dir = get_app_dir();

    let home_dir = std::env::var(if cfg!(windows) { "USERPROFILE" } else { "HOME" })
        .expect("Failed to get user HOME");

    let log_dir = Path::new(&home_dir).join(".round-table");
    fs::create_dir_all(&log_dir).expect("Failed to create log directory");

    let stdout_path = log_dir.join("trt-output.log");
    let stderr_path = log_dir.join("trt-error.log");

    rotate_log(&stdout_path);
    rotate_log(&stderr_path);

    let stdout_file = OpenOptions::new()
        .create(true)
        .append(true)
        .open(&stdout_path)
        .expect("Failed to open stdout file");

    let stderr_file = OpenOptions::new()
        .create(true)
        .append(true)
        .open(&stderr_path)
        .expect("Failed to open stderr file");

    let err = Command::new(format!("{}/jdk/bin/java", app_dir))
        .arg("-jar")
        .arg(format!("{}/bin/desktop-app.jar", app_dir))
        .stdout(Stdio::from(stdout_file))
        .stderr(Stdio::from(stderr_file))
        .exec();

    println!("Error: {:?}", err);
}

fn rotate_log(log_path: &Path) {
    if let Ok(metadata) = fs::metadata(log_path) {
        if metadata.len() >= MAX_LOG_SIZE {
            let rotated_log = log_path.with_extension("log.old");
            let _ = fs::remove_file(&rotated_log);
            fs::rename(log_path, &rotated_log).expect("log couldn't be rotated");
        }
    }
}

fn get_app_dir() -> String {
    let exec_file = std::env::current_exe().expect("Should exist");
    exec_file
        .parent()
        .expect("Should have parent")
        .to_str()
        .expect("Should be a valid string")
        .to_string()
}
