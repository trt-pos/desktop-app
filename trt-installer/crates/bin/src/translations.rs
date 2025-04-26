use std::collections::HashMap;
use std::sync::LazyLock;

static TRANSLATIONS_CSV: &str = include_str!("../resources/translations.csv");

static TRANSLATIONS: LazyLock<HashMap<&str, &'static str>> = LazyLock::new(|| {
    let first_line = TRANSLATIONS_CSV.lines().next().unwrap_or("");
    let keys = first_line.split(',');
    let locale = std::env::var("LANG")
        .ok()
        .map(|lang| lang.split('.').next().unwrap_or("").to_string())
        .unwrap_or("en_US".to_string());

    let mut locale_index = 0;

    for (index, key) in keys.enumerate().skip(1) {
        if key == locale {
            locale_index = index;
            break;
        }
    }

    if locale_index == 0 {
        locale_index = 1; // Default to English
    }

    let mut translations = HashMap::new();

    for line in TRANSLATIONS_CSV.lines().skip(1) {
        let parts = line.split(',').collect::<Vec<&'static str>>();

        if parts.len() > locale_index {
            let key = parts[0];
            let translation = parts[locale_index];
            translations.insert(key, translation);
        } else {
            eprintln!("Warning: Missing translation for key '{}'", parts[0]);
        }
    }
    translations
});

fn translate(key: &'static str) -> &'static str {
    #[cfg(debug_assertions)]
    {
        return translate_or_panic(key);
    }

    TRANSLATIONS.get(key).unwrap_or(&key)
}

fn translate_or_panic(key: &'static str) -> &'static str {
    TRANSLATIONS
        .get(key)
        .unwrap_or_else(|| panic!("Translation for key '{}' not found", key))
}
