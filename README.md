# Desktop app

## Versions
The version fields should follow the same pattern:
- major.minor.patch

The major indicates the version of the app,
the minor indicates the version of the features,
and the patch indicates the version of the bug fixes.

The core only loads plugins that have the same major version
because it is not backward compatible between major versions, 
but it is between minor and path versions. 
The same rules apply to the plugins versionioning.

## ENV vars
- TRT_HOME: Determines where should be placed the config dir .round-table
- TRT_ENV: ["test", "dev", "prod", null]
- TRT_TEST_SERVER_URL: ["https://....", null]