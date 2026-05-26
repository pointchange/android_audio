# Audio

一款基于 jetpack compose 、借助 vlc player 插件开发的几乎适配全格式音频的音乐软件

## 功能特性

- **基本功能**: 音频播放与搜索、音频列表、扫描文件、定时器、自选主题
- **本地缓存**: 使用 Room 数据库记忆扫描位置
- **后台服务与小部件**: 基本通知栏音乐服务与桌面小部件

## 运行截图

| 首页 | 扫描 | 播放页 |
| --- | --- |--- |
| <img src="screenshots/Screenshot_20260526_194626.png" width="300" alt="主页"> | <img src="screenshots/Screenshot_20260526_194645.png" width="300" alt="扫描"> |<img src="screenshots/Screenshot_20260526_194742.png" width="300" alt="播放页-歌词"> |

## 技术栈

```mermaid
graph TD
G[Room] -->|提供数据| F[repository]--> 
       |临时数据与提供状态| A[vlcManager 控制器] --> B[viewmodel] --> C[ui]
        A --> D[service]
        A --> E[widget]
```

本项目采用现代 Android 开发规范和主流架构

**开发语言**: [kotlin](https://kotlinlang.org/)

**UI**: jetpack compose

**架构模式**: MVVM

**本地存储**: Room

this project uses libvlc-android licensed under the LGPLv2.1 or later. 

## 快速开始

### 运行环境

- android studio、jdk 21、gradle9.3.1

### 本地编译步骤

1. 克隆项目到本地

2. 使用 android studio 打开项目

3. 本项目使用了APi key 请在 local.properties 文件配置

4. Run ▷

## 开源协议

```text
Copyright [2026] [pointchange]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
```
