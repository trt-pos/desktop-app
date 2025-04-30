// build.rs
use std::env;
use std::path::Path;

fn main() {
    let path = env::var("APP_ZIP_PATH").unwrap_or("../resources/app-files.zip".to_string());

    // Validate that the file exists from the main.rs file
    if !Path::new("src").join(&path).exists() {
        panic!("The file APP_ZIP_PATH doesn't exist: {}", path);
    }

    // Make the env var visible for the macro env!("APP_ZIP_PATH") used in the main.rs file
    println!("cargo:rustc-env=APP_ZIP_PATH={}", path);
}
