use std::io;
use std::io::Write;
use std::sync::LazyLock;
use crate::gui::app::Message;
use crate::gui::steps::Step;
use iced::{widget, Element, Task};
use crate::config::InstallationConfig;

static COMPRESSED_FILES: &[u8] = include_bytes!("../../../resources/app-files.zip");

#[derive(Default)]
pub struct FilesStep {
    progress: f32,
}

impl Step for FilesStep {
    fn title(&self) -> &'static str {
        "Copy application files"
    }

    fn icon(&self) -> &'static [u8] {
        include_bytes!("../../../resources/icons/copy.png")
    }

    fn view(&self) -> Element<Message> {
        widget::progress_bar(0f32..=1f32, self.progress).into()
    }

    fn update(&mut self, message: Message) -> Task<Message> {
        Task::none()
    }

    fn validate(&self) -> bool {
        true
    }

    fn apply(&self) -> Task<Message> {
        Task::future(async {
            copy_files().await;
            Message::NextStep
        })
    }
}

// TODO: Use a tmp folder
async fn copy_files() -> io::Result<()> {
    let installation_dir = &InstallationConfig::get_config().installation_dir;
    let installation_dir = std::path::PathBuf::from(installation_dir);
    
    let zip_file_path = installation_dir.join("theroundtable.zip");
    let mut file = std::fs::File::create(&zip_file_path)?;
    file.write_all(COMPRESSED_FILES)?;
    
    // Unzip the file
    let mut archive = zip::ZipArchive::new(std::fs::File::open(&zip_file_path)?)?;
    for i in 0..archive.len() {
        let mut file = archive.by_index(i)?;
        let outpath = installation_dir.join(file.mangled_name());
        
        if file.name().ends_with('/') {
            std::fs::create_dir_all(&outpath)?;
        } else {
            if let Some(parent) = outpath.parent() {
                std::fs::create_dir_all(parent)?;
            }
            let mut outfile = std::fs::File::create(&outpath)?;
            io::copy(&mut file, &mut outfile)?;
        }
    }
    std::fs::remove_file(zip_file_path)?;
    
    Ok(())
}
