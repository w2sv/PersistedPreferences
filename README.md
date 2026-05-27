# DataStoreUtils

![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/w2sv/DataStoreUtils?include_prereleases)
[![Build](https://github.com/w2sv/DataStoreUtils/actions/workflows/workflow.yaml/badge.svg)](https://github.com/w2sv/DataStoreUtils/actions/workflows/workflow.yaml)
![GitHub](https://img.shields.io/github/license/w2sv/DataStoreUtils)

A repository that facilitates working with the Preferences DataStore.

## 🚀 Installation

### Inline

```kotlin
dependencies {
    implementation("io.github.w2sv:datastoreutils-datastoreflow:<version>")
    implementation("io.github.w2sv:datastoreutils-preferences:<version>")
}
```

---

### Version Catalog (`libs.versions.toml`)

```toml
[versions]
w2sv-datastoreutils = "<version>"

[libraries]
w2sv-datastoreutils-datastoreflow = { module = "io.github.w2sv:kotlinutils-datastoreflow", version.ref = "w2sv-datastoreutils" }
w2sv-datastoreutils-preferences = { module = "io.github.w2sv:kotlinutils-preferences", version.ref = "w2sv-datastoreutils" }
```

**build.gradle.kts:**

```kotlin
dependencies {
    implementation(libs.w2sv.datastoreutils.datastoreflow)
    implementation(libs.w2sv.datastoreutils.preferences)
}
```

---

## 📄 License

Licensed under the Apache License 2.0.
