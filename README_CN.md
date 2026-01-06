# MusicKit

<p align="left">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-blue?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android-15-green?logo=android" alt="Android">
  <img src="https://img.shields.io/badge/Compose-2025.12.01-orange?logo=jetpackcompose" alt="Compose">
  <img src="https://img.shields.io/badge/License-Apache_2.0-red.svg" alt="License">
</p>

[English Version](README.md)

一个基于 **Jetpack Compose** 和 **Material 3** 开发的现代 Android NCM 音乐转换工具。

---

## 🛠 开发环境

| 项目                        | 版本                 |
|:--------------------------|:-------------------|
| **Kotlin**                | `2.3.0`            |
| **Android Gradle Plugin** | `8.13.2`           |
| **Compose BOM**           | `2025.12.01`       |
| **最低支持版本**                | `24 (Android 7.0)` |
| **目标 SDK 版本**             | `35 (Android 15)`  |

## 📖 项目简介

**MusicKit** 是一款专注于高性能、原生体验的 NCM 转换工具。它能将 `.ncm` 加密文件还原为标准的 `.mp3` 或
`.flac` 格式。它不仅是简单的格式转换，还会自动补全：

- **元数据**：完整支持 ID3 标签（标题、艺术家、专辑）。
- **封面**：提取原始高分辨率专辑封面。
- **歌词**：自动匹配并嵌入同步 LRC 歌词。

## ✨ 功能特性

- **高效解密**：快速转换加密文件，不损失音频质量。
- **标签修复**：集成 TagLib，确保转换后的文件在各类播放器中完美显示。
- **在线集成**：自动从网络获取并嵌入缺失的歌词信息。
- **现代 UI**：完全采用 Material 3 设计规范，支持 **动态配色** 与 **全面屏** 沉浸体验。
- **批量处理**：支持全目录扫描，一键处理数百首歌曲。
- **隐私安全**：所有解密与转换过程均在本地设备完成，无需上传。

## 🚀 技术栈

- **界面**：Jetpack Compose（现代响应式 UI）
- **架构**：MVVM + Kotlin Coroutines & Flow
- **网络**：OkHttp & Kotlinx Serialization
- **多媒体**：TagLib (io.github.kyant0:taglib)
- **数据存储**：Jetpack DataStore (Preferences)

## 📝 使用方法

1. **初始设置**：进入 **设置** 页面，授权并选择你的 **输入目录**（NCM 存放处）和 **输出目录**。
2. **扫描文件**：返回 **首页**，应用将自动索引所有可转换的 NCM 文件。
3. **选择歌曲**：点击单选或使用“全选”。
4. **开始转换**：点击转换按钮，应用将自动完成解密、打码、封面和歌词嵌入。

## 🗺 开发路线图

- [x] NCM 解密核心逻辑实现
- [x] 元数据与封面自动写入
- [x] 网络歌词匹配与嵌入
- [x] 多语言支持 (i18n)
- [x] Material 3 界面适配

## ⚖️ 开源协议

本项目采用 **Apache License 2.0** 协议。详情请参阅 `LICENSE` 文件。

## ⚠️ 免责声明

本工具仅供 **学习与研究** 使用。请尊重版权法，支持正版音乐。开发者对本工具的任何误用不承担责任。
