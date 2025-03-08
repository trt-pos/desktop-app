use std::path;
use crate::{Action, Error, INSTALLATION_DIR};
use std::pin::Pin;
use tokio::fs;
use tokio::io::AsyncWriteExt;

pub struct CreateShortcutAction;

impl Action for CreateShortcutAction {
    fn action_name(&self) -> &'static str {
        "Creating shortcut"
    }

    #[allow(unreachable_code)]
    fn execute(&self) -> Pin<Box<dyn Future<Output = Result<(), Error>> + Send>> {
        #[cfg(target_os = "linux")]
        {
            return Box::pin(async {
                let shortcut_file = path::PathBuf::from(&*INSTALLATION_DIR).join("theroundtable.desktop");

                let file_content = format!(
                    r#"[Desktop Entry]
Version=1.0
Type=Application
Name=The Round Table
Exec="{}/start"
Icon={}/images/icon.png
Categories=Game;
"#,
                    INSTALLATION_DIR.to_string_lossy(),
                    INSTALLATION_DIR.to_string_lossy()
                );

                fs::File::create(shortcut_file)
                    .await
                    .map_err(|e| Error::IgnorableError(Box::new(e.into())))?
                    .write_all(file_content[..].as_bytes())
                    .await
                    .map_err(|e| Error::IgnorableError(Box::new(e.into())))?;

                Ok(())
            });
        }

        Box::pin(async {
            Err(Error::IgnorableError(Box::new(Error::NotSupported(
                "Shortcut creation is not supported on this OS".to_string(),
            ))))
        })
    }
}
