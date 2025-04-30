use crate::config;
use std::ffi::OsStr;
use std::process::ExitStatus;
use std::time::{SystemTime, UNIX_EPOCH};
use std::{fs, io};

pub fn is_running_as_root() -> bool {
    #[cfg(target_os = "linux")]
    {
        use std::process::Command;
        let output = Command::new("id")
            .arg("-u")
            .output()
            .expect("Failed to execute command");
        let uid = String::from_utf8_lossy(&output.stdout);
        return uid.trim() == "0";
    }

    panic!("This function is not implemented for this OS")
}

pub fn execute_script_with_privileges(script: &str) -> io::Result<ExitStatus> {
    let config = config::InstallationConfig::get_config();
    let epoch_time = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .expect("UNIX_EPOCH is before SystemTime::now()")
        .as_secs();
    let tmp_script_file = &config
        .tmp_installation_dir
        .join(format!("trt_installer-{epoch_time}.sh"));

    fs::write(tmp_script_file, script)?;

    #[cfg(target_os = "linux")]
    {
        return execute_with_privileges(["bash", &tmp_script_file.to_string_lossy()]);
    }

    panic!("This function is not implemented for this OS")
}

pub fn execute_with_privileges<S, I>(args: I) -> io::Result<ExitStatus>
where
    I: IntoIterator<Item = S>,
    S: AsRef<OsStr>,
{
    #[cfg(target_os = "linux")]
    {
        use std::process::Command;

        let exit_status = Command::new("pkexec").args(args).spawn()?.wait()?;

        return Ok(exit_status);
    }

    panic!("This function is not implemented for this OS")
}
