# iOS iMessage风格UI优化规格

## Why
当前聊天应用UI与iMessage风格仍有差距，需要优化界面元素、动画效果和交互细节，使其更接近原生iMessage体验。

## What Changes
- 优化消息气泡样式（圆角、阴影、颜色）
- 添加用户头像显示
- 优化顶部导航栏（联系人信息、返回按钮）
- 重构底部输入区域（相机、照片、语音按钮）
- 添加消息状态指示（已读/未读）
- 实现丰富的动画效果（消息进入、发送按钮、打字指示器）
- 添加消息时间分组显示
- 优化整体配色和字体

## Impact
- Affected specs: UI组件、动画系统、消息显示
- Affected code: ChatScreen, MessageBubble, MessageInput, 主题配置

## ADDED Requirements

### Requirement: iMessage风格消息气泡
The system SHALL 提供iMessage风格的消息气泡组件

#### Scenario: 用户消息气泡
- **WHEN** 显示用户发送的消息
- **THEN** 气泡为蓝色(#007AFF)，右侧圆角较小，带阴影效果

#### Scenario: 对方消息气泡
- **WHEN** 显示AI/对方消息
- **THEN** 气泡为灰色(#E9E9EB)，左侧圆角较小，带阴影效果

### Requirement: 用户头像显示
The system SHALL 在消息旁显示用户头像

#### Scenario: 对方头像显示
- **WHEN** 显示AI消息
- **THEN** 在消息左侧显示圆形头像

#### Scenario: 用户头像显示
- **WHEN** 显示用户消息
- **THEN** 在消息右侧显示圆形头像（可选）

### Requirement: 消息状态指示
The system SHALL 显示消息已读/未读状态

#### Scenario: 已读状态
- **WHEN** 消息已被阅读
- **THEN** 在消息下方显示"read"和时间戳

### Requirement: 底部输入栏重构
The system SHALL 提供iMessage风格的底部输入栏

#### Scenario: 功能按钮显示
- **WHEN** 显示输入栏
- **THEN** 显示相机、照片、语音等功能按钮

#### Scenario: 发送按钮样式
- **WHEN** 输入框有内容
- **THEN** 显示蓝色圆形发送按钮

### Requirement: 动画效果
The system SHALL 提供丰富的动画效果

#### Scenario: 消息进入动画
- **WHEN** 新消息出现
- **THEN** 从底部滑入并淡入，带弹性效果

#### Scenario: 发送按钮动画
- **WHEN** 点击发送
- **THEN** 按钮缩放反馈动画

#### Scenario: 打字指示器
- **WHEN** AI正在回复
- **THEN** 显示三个跳动的圆点动画

#### Scenario: 消息列表滚动
- **WHEN** 发送新消息
- **THEN** 平滑滚动到底部

### Requirement: 时间分组显示
The system SHALL 按时间分组显示消息

#### Scenario: 时间戳分组
- **WHEN** 消息跨时间段
- **THEN** 显示时间分隔线（如"今天 12:30"）

## MODIFIED Requirements
### Requirement: 顶部导航栏
**现有**: 简单的标题栏
**修改**: 显示联系人头像、名称、返回按钮、视频/更多按钮

## REMOVED Requirements
无
