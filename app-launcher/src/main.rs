use std::fs::File;
use std::process::{Command, Stdio};

fn main() {
    let app_dir = get_app_dir();

    let stdout_file = File::create("trt-output.log").expect("Failed to create stdout file");
    let stderr_file = File::create("trt-error.log").expect("Failed to create stderr file");


    let mut child = Command::new(format!("{}/jdk/bin/java", app_dir))
        .arg("-jar".to_string())
        .arg(format!("{}/bin/desktop-app.jar", app_dir))
        .stdout(Stdio::from(stdout_file))
        .stderr(Stdio::from(stderr_file))
        .spawn()
        .expect("Failed to execute command");

    // Imprime la salida del comando como texto
    child.wait().expect("Failed to wait on child");
}

fn get_app_dir() -> String
{
    let exec_file = std::env::current_exe().expect("Should exist");
    exec_file.parent().expect("Should have parent").to_str().expect("Should be a valid string").to_string()
}
