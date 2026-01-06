# MusicKit

<p align="left">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-blue?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android-15-green?logo=android" alt="Android">
  <img src="https://img.shields.io/badge/Compose-2025.12.01-orange?logo=jetpackcompose" alt="Compose">
  <img src="https://img.shields.io/badge/License-Apache_2.0-red.svg" alt="License">
</p>

[中文版 (Chinese Version)](README_CN.md)

A modern Android application for NCM music conversion, built with **Jetpack Compose** and **Material
3**.

---

## 🛠 Environment

| Requirement               | Version            |
|:--------------------------|:-------------------|
| **Kotlin**                | `2.3.0`            |
| **Android Gradle Plugin** | `8.13.2`           |
| **Compose BOM**           | `2025.12.01`       |
| **Min SDK**               | `24 (Android 7.0)` |
| **Target SDK**            | `35 (Android 15)`  |

## 📖 Overview

**MusicKit** is a high-performance tool designed to decrypt `.ncm` files into standard `.mp3` or
`.flac` formats. It doesn't just convert audio; it ensures your library stays organized by
automatically restoring:

- **Metadata**: Full ID3 tag support (Title, Artist, Album).
- **Visuals**: Original high-resolution album covers.
- **Lyrics**: Synchronized LRC lyrics fetched automatically.

## ✨ Features

- **High-speed Decryption**: Efficiently converts encrypted files without quality loss.
- **TagLib Integration**: Robust metadata writing for wide player compatibility.
- **Online Integration**: Automatically matches and embeds lyrics from web sources.
- **Material 3 UI**: A sleek, modern interface with support for **Dynamic Color** and **Edge-to-Edge
  ** display.
- **Batch Management**: Scan entire folders and process hundreds of files in one go.
- **Privacy First**: All decryption is done locally on your device.

## 🚀 Tech Stack

- **UI**: Jetpack Compose (Modern, reactive UI)
- **Architecture**: MVVM with Kotlin Coroutines & Flow
- **Network**: OkHttp & Kotlinx Serialization
- **Media**: TagLib for advanced audio tagging
- **Storage**: Jetpack DataStore for configuration

## 📝 Usage

1. **Setup**: Go to **Settings** and grant permission for your **Input** and **Output** directories.
2. **Scan**: Return to the **Home** screen; MusicKit will automatically index all `.ncm` files.
3. **Select**: Tap individual items or use "Select All".
4. **Convert**: Hit the primary action button to begin the automated conversion.

## 🗺 Roadmap

- [x] Core NCM decryption logic
- [x] Metadata and cover art injection
- [x] Online lyrics fetching
- [x] Multi-language support (i18n)
- [x] Material 3 Design implementation

## ⚖️ License

Distributed under the **Apache License 2.0**. See `LICENSE` for more information.

## ⚠️ Disclaimer

This project is for **educational and research purposes only**. Please respect copyright laws and
support official music platforms. The developers are not responsible for any misuse of this tool.
