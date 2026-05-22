# Tasks

- [ ] Task 1: 替换应用图标为xingxi.png
  - [ ] 1.1 将xingxi.png放到mipmap目录
  - [ ] 1.2 更新AndroidManifest.xml中的icon
  - [ ] 1.3 更新mipmap资源文件

- [ ] Task 2: 更新SettingsRepository存储头像和昵称
  - [ ] 2.1 添加aiAvatarUri字段
  - [ ] 2.2 添加userAvatarUri字段
  - [ ] 2.3 添加aiNickname字段
  - [ ] 2.4 添加保存和加载方法

- [ ] Task 3: 更新Avatar组件支持图片
  - [ ] 3.1 添加Uri参数
  - [ ] 3.2 使用Coil或Glide加载网络/本地图片
  - [ ] 3.3 添加点击事件回调

- [ ] Task 4: 创建昵称编辑对话框
  - [ ] 4.1 创建NameEditDialog组件
  - [ ] 4.2 添加TextField输入框
  - [ ] 4.3 添加确定和取消按钮

- [ ] Task 5: 实现相册选择功能
  - [ ] 5.1 添加权限（READ_EXTERNAL_STORAGE）
  - [ ] 5.2 使用ActivityResultContracts.GetContent
  - [ ] 5.3 在ChatScreen中处理选择结果

- [ ] Task 6: 更新ChatScreen集成功能
  - [ ] 6.1 AI头像点击打开相册
  - [ ] 6.2 AI昵称点击显示编辑对话框
  - [ ] 6.3 用户头像点击打开相册
  - [ ] 6.4 从SettingsRepository加载保存的头像和昵称

- [ ] Task 7: 添加页面切换动画
  - [ ] 7.1 使用AnimatedContent或NavHost
  - [ ] 7.2 添加淡入淡出/滑动动画
  - [ ] 7.3 确保动画流畅自然

- [ ] Task 8: 更新依赖
  - [ ] 8.1 添加Coil图片加载库
  - [ ] 8.2 添加必要的权限

- [ ] Task 9: 重新构建验证
  - [ ] 9.1 执行gradlew assembleDebug
  - [ ] 9.2 验证APK生成

# Task Dependencies
- Task 2 依赖 Task 1
- Task 3 依赖 Task 2
- Task 4 独立
- Task 5 独立
- Task 6 依赖 Task 2, 3, 4, 5
- Task 7 独立
- Task 8 依赖 Task 3, 5
- Task 9 依赖所有任务
