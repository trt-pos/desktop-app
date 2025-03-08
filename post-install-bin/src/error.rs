#[derive(thiserror::Error, Debug)]
pub enum Error {
    #[error("Failed to download: {0}")]
    DownloadingError(#[from] reqwest::Error),
    
    #[error("I/O Error: {0}")]
    IOError(#[from] std::io::Error),
    
    #[error("Failed to extract: {0}")]
    ZipError(#[from] zip::result::ZipError),
    
    #[error("Ignorable error: {0}")]
    IgnorableError(Box<Error>),
    
    #[error("Critical error: {0}")]
    CriticalError(Box<Error>),
}