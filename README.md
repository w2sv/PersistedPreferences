# DataStoreUtils

![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/w2sv/DataStoreUtils?include_prereleases)
[![Build](https://github.com/w2sv/DataStoreUtils/actions/workflows/workflow.yaml/badge.svg)](https://github.com/w2sv/DataStoreUtils/actions/workflows/workflow.yaml)
![GitHub](https://img.shields.io/github/license/w2sv/DataStoreUtils)

A small set of utilities for exposing Preferences DataStore values as typed flows and persisted preference handles.

## 🚀 Installation

### Inline

```kotlin
dependencies {
    implementation("io.github.w2sv:datastoreutils-preferences:<version>")
}
```

---

### Version Catalog (`libs.versions.toml`)

```toml
[versions]
w2sv-datastoreutils = "<version>"

[libraries]
w2sv-datastoreutils-preferences = { module = "io.github.w2sv:datastoreutils-preferences", version.ref = "w2sv-datastoreutils" }
```

**build.gradle.kts:**

```kotlin
dependencies {
    implementation(libs.w2sv.datastoreutils.preferences)
}
```

---

## Usage

`PersistedPreference<T>` keeps the read and write side of a preference together: consumers collect `flow` to observe
changes and call `save(value)` to persist updates. This keeps repository APIs small and typed without exposing
`DataStore`, keys, serialization details, or separate setter methods.

Create an accessor from your `DataStore<Preferences>`:

```kotlin
val accessor = PreferencesDataStoreAccessor(dataStore)
```

Expose concrete preferences from a repository:

```kotlin
class PreferencesRepository(preferences: PreferencesDataStoreAccessor) {
    val launchCount = preferences.persistedPreference(
        key = intPreferencesKey("launch_count"),
        default = { 0 }
    )

    val theme = preferences.enumPreference(
        keyName = "theme",
        default = { Theme.System },
        savePolicy = EnumSavePolicy.byName()
    )
}
```

You can also subclass the accessor:

```kotlin
class PreferencesRepository(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreAccessor(dataStore) {
    val theme = enumPreference(
        keyName = "theme",
        default = { Theme.System },
        savePolicy = EnumSavePolicy.byName()
    )
}
```

Nullable enum preferences use the policy's null representation when saving `null`:

```kotlin
val selectedTheme = preferences.nullableEnumPreference(
    keyName = "selected_theme",
    default = { null },
    savePolicy = EnumSavePolicy.byName<Theme>()
)
```

Custom enum storage is supported through `EnumSavePolicy`:

```kotlin
val theme = preferences.enumPreference(
    keyName = "theme",
    default = { Theme.System },
    savePolicy = EnumSavePolicy.byCustomString(
        toSavable = { it.id },
        toExternal = { id, default -> Theme.entries.firstOrNull { it.id == id } ?: default() }
    )
)
```

## 📄 License

Licensed under the Apache License 2.0.
