use crate::Action;
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
    fn execute(&self) -> Pin<Box<dyn Future<Output = Result<(), crate::Error>> + Send>> {
        Box::pin(async {
            // Downloading the compressed JDK folder
            let url = JDK_DOWNLOAD_URL.deref();
            let response = reqwest::get(url).await.expect("Failed to download JDK");

            let bin_path = std::env::current_exe().expect("Failed to get current executable path");

            let installation_dir = bin_path.parent().expect("Failed to get parent directory");

            let mut file =
                File::create(installation_dir.join(format!("jdk.{}", &*RESOURCE_EXTENSION)))
                    .await
                    .expect("Failed to create file");
            let content = response.bytes().await.expect("Failed to read response");

            file.write_all(&content)
                .await
                .expect("Failed to write to file");

            // Extracting the compressed JDK folder
            match RESOURCE_EXTENSION.deref().as_str() {
                "zip" => {
                    let file = std::fs::File::open(installation_dir.join("jdk.zip"))
                        .expect("Failed to open file");
                    let reader = std::io::BufReader::new(file);
                    let mut archive =
                        zip::ZipArchive::new(reader).expect("Failed to open zip file");
                    archive
                        .extract(bin_path.join("jdk"))
                        .expect("Failed to extract zip file");
                }
                "tar.gz" => {
                    let file = std::fs::File::open(installation_dir.join("jdk.tar.gz"))
                        .expect("Failed to open file");
                    let tar = GzDecoder::new(file);
                    let mut archive = tar::Archive::new(tar);
                    archive
                        .unpack(installation_dir.join("jdk"))
                        .expect("Failed to unpack tar file");
                }
                _ => panic!("Unsupported extension: {}", &*RESOURCE_EXTENSION),
            };

            // Moving the inner folder inside the decompressed folder to the parent folder and renaming it to "jdk"
            let inner_folder = installation_dir
                .join("jdk")
                .read_dir()
                .expect("Failed to read directory")
                .next()
                .expect("Failed to get next entry")
                .expect("Failed to get entry")
                .path();
            std::fs::rename(&inner_folder, installation_dir.join("jdk_2"))
                .expect("Failed to rename folder");
            fs::remove_dir(installation_dir.join("jdk"))
                .await
                .expect("Failed to remove directory");
            std::fs::rename(installation_dir.join("jdk_2"), installation_dir.join("jdk"))
                .expect("Failed to rename folder");

            Ok(())
        })
        
    }
}
