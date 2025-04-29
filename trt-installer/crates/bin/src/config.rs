use std::path::PathBuf;
use std::sync::OnceLock;

static CONFIG: OnceLock<InstallationConfig> = OnceLock::new();

#[derive(Default, Debug)]
pub struct InstallationConfig {
    pub installation_dir: String,
    pub create_shortcut: bool,
    pub application_dir_name: String,
    pub tmp_installation_dir: PathBuf,
    pub send_analytics: bool,
}

impl InstallationConfig {
    pub fn get_config() -> &'static InstallationConfig {
        if CONFIG.get().is_none() {
            #[cfg(debug_assertions)]
            {
                panic!("Config is not set");
            }
            
            Self::set_config(InstallationConfig::default());
        }
        
        CONFIG.get().expect("Config should be initialized")
    }
    
    pub fn set_config(config: InstallationConfig) {
        let result = CONFIG.set(config);
        
        #[cfg(debug_assertions)]
        {
            if result.is_err() {
                panic!("Failed to set config: {:?}", result.err());
            }
        }
    }
}