# 头像上传功能测试指南

## 🔧 功能说明

前端头像上传功能已经完成，包括：

### ✅ 已实现的功能：

1. **FmAvatar组件** - 头像显示组件
   - 支持多种尺寸（sm, md, lg, xl）
   - 支持点击交互
   - 支持上传状态显示
   - 自动处理相对路径和完整URL

2. **FmAvatarUpload组件** - 头像上传组件
   - 文件类型验证（jpg, jpeg, png, gif, webp）
   - 文件大小限制（5MB）
   - 上传进度显示
   - 错误处理和用户提示

3. **API集成**
   - `uploadAvatar()` - 上传头像文件
   - `getFileUrl()` - 获取文件访问URL
   - 自动更新用户信息

4. **Settings页面集成**
   - 使用FmAvatarUpload组件
   - 自动更新用户store
   - 表单数据同步

## 🎯 测试步骤：

### 1. 启动后端服务
```bash
cd 代码/xinli
mvn spring-boot:run
```

### 2. 启动前端服务
```bash
cd 代码/basic
npm run dev
```

### 3. 测试头像上传
1. 访问 http://localhost:9000
2. 登录账号（使用测试账号：13800138000 / 123456）
3. 进入"我的" -> "设置"页面
4. 点击头像或"更换头像"按钮
5. 选择图片文件（支持jpg, png, gif, webp格式，最大5MB）
6. 查看上传进度和结果

### 4. 验证功能
- ✅ 文件类型验证：尝试上传txt文件，应该显示错误提示
- ✅ 文件大小验证：上传超过5MB的文件，应该显示错误提示
- ✅ 上传成功：上传有效图片，应该显示成功提示并更新头像
- ✅ 头像显示：刷新页面后头像应该正确显示
- ✅ URL访问：可以通过 http://localhost:8080/uploads/avatars/文件名 直接访问

## 🔗 相关文件：

### 前端文件：
- `src/ui/components/FmAvatar/index.vue` - 头像显示组件
- `src/ui/components/FmAvatarUpload/index.vue` - 头像上传组件
- `src/views/profile/settings.vue` - 设置页面
- `src/api/modules/auth.ts` - API接口
- `src/store/modules/user.ts` - 用户状态管理

### 后端文件：
- `FileUploadController.java` - 文件上传控制器
- `FileUploadService.java` - 文件上传服务
- `FileUploadConfig.java` - 文件上传配置
- `WebConfig.java` - 静态资源配置
- `application.yml` - 应用配置

## 🐛 常见问题：

1. **上传失败** - 检查后端服务是否启动，JWT token是否有效
2. **文件访问404** - 检查uploads目录是否存在，WebConfig配置是否正确
3. **头像不显示** - 检查环境变量VITE_APP_API_BASEURL配置
4. **权限错误** - 确保uploads目录有写入权限

## 📁 文件存储结构：
```
项目根目录/
└── uploads/
    └── avatars/
        ├── avatar_1_20241219120000_abc12345.jpg
        ├── avatar_2_20241219120100_def67890.png
        └── ...
```

## 🎨 UI效果：
- 头像显示为圆形
- 支持hover效果
- 上传时显示loading状态
- 成功/失败有toast提示
- 响应式设计，适配移动端
