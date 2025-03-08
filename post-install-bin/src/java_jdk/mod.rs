use crate::{Action, INSTALLATION_DIR};
use flate2::read::GzDecoder;
use std::ops::Deref;
use std::pin::Pin;
use std::sync::LazyLock;
use tokio::fs;
use tokio::fs::File;
use tokio::io::AsyncWriteExt;

/// JDK 23.0.2 download URLs:
/// Linux/AArch64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_linux-aarch64_bin.tar.gz
/// Linux/x64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_linux-x64_bin.tar.gz
/// macOS/Aarch64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_macos-aarch64_bin.tar.gz
/// macOS/x64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_macos-x64_bin.tar.gz
/// Windows/x64: https://download.java.net/java/GA/jdk23.0.2/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-23.0.2_windows-x64_bin.zip
static JDK_DOWNLOAD_URL: LazyLock<String> = LazyLock::new(|| {
    let version = "23.0.2";
    let os = std::env::consts::OS;
    let arch = std::env::consts::ARCH;

    let platform = match os {
        "windows" => "windows",
        "macos" => "macos",
        "linux" => "linux",
        _ => panic!("Unsupported OS: {}", os),
    };

    let extension = &*RESOURCE_EXTENSION;

    let arch = match arch {
        "aarch64" => "aarch64",
        "x86_64" => "x64",
        _ => panic!("Unsupported arch: {}", arch),
    };

    format!(
        "https://download.java.net/java/GA/jdk{}/6da2a6609d6e406f85c491fcb119101b/7/GPL/openjdk-{}_{}-{}_bin.{}",
        version, version, platform, arch, extension
    )
});

static RESOURCE_EXTENSION: LazyLock<String> = LazyLock::new(|| {
    let os = std::env::consts::OS;

    match os {
        "windows" => "zip",
        "macos" => "tar.gz",
        "linux" => "tar.gz",
        _ => panic!("Unsupported OS: {}", os),
    }
    .to_string()
});

pub struct DownloadJDKAction;

impl Action for DownloadJDKAction {
    fn action_name(&self) -> &'static str {
        "Downloading and extracting JDK"
    }

    fn execute(&self) -> Pin<Box<dyn Future<Output = Result<(), crate::Error>> + Send>> {
        
        Box::pin(async {
            // Downloading the compressed JDK folder
            let url = JDK_DOWNLOAD_URL.deref();
            let response = reqwest::get(url).await?;

            let compressed_file_path = INSTALLATION_DIR.join(format!("jdk.{}", *RESOURCE_EXTENSION));
            let mut file = File::create(&compressed_file_path).await?;
            let content = response.bytes().await?;

            file.write_all(&content).await?;

            let file = std::fs::File::open(&compressed_file_path)?;
            let output_path = INSTALLATION_DIR.join("jdk");
            
            // Extracting the compressed JDK folder
            match RESOURCE_EXTENSION.deref().as_str() {
                "zip" => {
                    let reader = std::io::BufReader::new(file);
                    let mut archive = zip::ZipArchive::new(reader)?;
                    archive.extract(&output_path)?;
                }
                "tar.gz" => {
                    let tar = GzDecoder::new(file);
                    let mut archive = tar::Archive::new(tar);
                    archive.unpack(&output_path)?;
                }
                _ => panic!("Unsupported extension: {}", *RESOURCE_EXTENSION),
            };

            if let Err(e) = std::fs::remove_file(&compressed_file_path) {
                eprintln!("Failed to remove the downloaded file after extracting its contents: {}", e);
            };

            // Moving the inner folder inside the decompressed folder to the parent folder and renaming it to "jdk"
            let inner_folder = output_path
                .read_dir()?
                .next()
                .expect("Failed to get next entry")?
                .path();
            
            let tmp_path = INSTALLATION_DIR.join("tmp");
            std::fs::rename(&inner_folder, &tmp_path)?;
            
            fs::remove_dir(&output_path).await?;
            std::fs::rename(&tmp_path, &output_path)?;

            Ok(())
        })
    }
}
