use std::fs::{File, OpenOptions};
use std::os::unix::process::CommandExt;
use std::path::Path;
use std::process::{Command, Stdio};

fn main() {
    let app_dir = get_app_dir();

    let stdout_path = Path::new(&app_dir).join("trt-output.log");
    let stderr_path = Path::new(&app_dir).join("trt-error.log");

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

fn get_app_dir() -> String {
    let exec_file = std::env::current_exe().expect("Should exist");
    exec_file
        .parent()
        .expect("Should have parent")
        .to_str()
        .expect("Should be a valid string")
        .to_string()
}
