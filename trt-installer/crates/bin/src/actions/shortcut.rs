use crate::actions::Action;
use crate::{Error};
use std::pin::Pin;

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
                let file_content = format!(
                    r#"[Desktop Entry]
Version=1.0
Type=Application
Name=The Round Table
Exec="asd/start"
Icon=asd/images/icon.png
Categories=Game;
"#,
                );

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
