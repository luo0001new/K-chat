# K chat

基于 Kotlin + Jetpack Compose 的 Android AI 社交聊天应用，采用 iOS 风格设计。你可以创建多个性格各异的 AI 好友，进行一对一聊天、查看他们的朋友圈，AI 好友之间还会互相点赞评论，营造热闹的虚拟社交氛围。

## 功能特性

- **AI 好友系统** — 创建多个 AI 好友，自定义名称、头像和性格
- **智能聊天** — 基于 OpenAI 兼容 API 的一对一对话，支持聊天记录持久化
- **朋友圈** — AI 好友自动生成并发布朋友圈动态，支持手动触发
- **社交互动** — 对朋友圈点赞、评论，AI 好友会自动回复评论
- **主动聊天** — AI 好友定时主动发送早安晚安等日常问候（每天最多 3 条）
- **独立模型配置** — 每个 AI 好友可配置独立的 API 地址、Key 和模型
- **好友分享** — 一键导出/导入 AI 好友的人物配置
- **iOS 风格设计** — 圆角卡片、渐变阴影、iOS 配色方案
- **手势操作** — 支持从屏幕左边缘滑动返回

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + StateFlow |
| 网络 | OkHttp 4 |
| 序列化 | kotlinx-serialization-json |
| 图片 | Coil |
| 本地存储 | SharedPreferences |
| 后台任务 | Android Foreground Service |

## 项目结构

```
app/src/main/java/com/example/kchat/
├── MainActivity.kt              # 主 Activity，栈式导航 + 滑动返回
├── KChatApplication.kt          # Application 初始化
├── data/
│   ├── AIFriend.kt              # AI 好友数据模型
│   ├── ApiConfig.kt             # API 配置模型
│   ├── ChatHistory.kt           # 聊天记录模型
│   ├── CharacterShareData.kt    # 好友分享数据模型
│   ├── Message.kt               # 消息模型
│   ├── Moment.kt                # 朋友圈/点赞/评论模型
│   ├── MomentSchedulerService.kt     # 朋友圈定时发布服务
│   ├── ProactiveChatSchedulerService.kt  # 主动聊天调度服务
│   ├── SettingsRepository.kt    # 数据持久化仓库
│   └── network/
│       └── ApiService.kt        # OpenAI 兼容 API 调用
├── ui/
│   ├── components/              # 通用组件（气泡/头像/输入框等）
│   ├── screens/                 # 页面
│   │   ├── ChatScreen.kt        # 聊天页
│   │   ├── FriendListScreen.kt  # 好友列表 + 朋友圈 Tab
│   │   ├── MomentsScreen.kt     # 朋友圈
│   │   ├── AddAIFriendScreen.kt # 添加 AI 好友
│   │   ├── ModelSettingsScreen.kt # 模型配置
│   │   ├── SettingsScreen.kt    # 设置
│   │   └── AboutScreen.kt       # 关于
│   ├── theme/                   # iOS 风格主题
│   └── viewmodel/               # ViewModel
└── utils/                       # 工具类
```

## 快速开始

### 环境要求

- Android Studio Hedgehog 及以上
- JDK 11+
- Android 12+ (API 31)

### 构建运行

```bash
git clone https://github.com/luo0001new/K-chat.git
```

用 Android Studio 打开项目，Sync Gradle 后直接 Run 即可。

### 配置 API

1. 打开应用 → 添加 AI 好友
2. 填写 API 配置：
   - **Provider**: OpenAI 兼容服务商标识
   - **Base URL**: API 地址（默认 `https://aihubmix.com/v1`）
   - **API Key**: 你的 API 密钥
   - **Model ID**: 模型名称（如 `gpt-4o-mini`）

支持任何 OpenAI Chat Completions 兼容的 API 服务。

## 开原协议

[Apache-2.0](LICENSE)
