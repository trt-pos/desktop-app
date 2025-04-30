use std::path::PathBuf;
use std::sync::RwLock;
use std::sync::{Arc, OnceLock};

static CONFIG: OnceLock<RwLock<Option<Arc<InstallationConfig>>>> = OnceLock::new();

#[derive(Default, Debug)]
pub struct InstallationConfig {
    pub installation_dir: String,
    pub create_shortcut: bool,
    pub application_dir_name: String,
    pub tmp_installation_dir: PathBuf,
    pub send_analytics: bool,
}

impl InstallationConfig {
    pub fn get_config() -> Arc<InstallationConfig> {
        let lock = CONFIG.get_or_init(|| RwLock::new(None));

        let read_guard = lock.read().unwrap();
        if let Some(config) = &*read_guard {
            Arc::clone(config)
        } else {
            drop(read_guard); 
            panic!("Configuration not set. Please set the configuration before accessing it.");
        }
    }

    pub fn set_config(config: InstallationConfig) {
        let lock = CONFIG.get_or_init(|| RwLock::new(None));
        let mut guard = lock.write().unwrap();
        *guard = Some(Arc::new(config));
    }
}
