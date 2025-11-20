# 高校实习管理系统 - 前端项目

## 项目简介

高校实习过程跟踪与评价管理系统的前端项目，使用 Vue 3 + Vite + Element Plus 开发。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios

## 项目结构

```
internship-web/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口定义
│   │   └── system.js      # 系统管理API
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   │   └── common/        # 通用组件
│   │       └── PageLayout.vue  # 页面布局组件
│   ├── router/            # 路由配置
│   │   └── index.js       # 路由入口
│   ├── store/             # 状态管理
│   │   ├── modules/       # 状态模块
│   │   │   └── auth.js    # 认证状态
│   │   └── index.js       # Store入口
│   ├── utils/             # 工具函数
│   │   ├── request.js     # 请求封装
│   │   ├── auth.js        # 认证工具
│   │   └── permission.js  # 权限工具
│   ├── views/             # 页面组件
│   │   ├── admin/         # 管理员端页面
│   │   │   ├── SchoolManagement.vue      # 学校管理
│   │   │   ├── CollegeManagement.vue     # 学院管理
│   │   │   ├── MajorManagement.vue       # 专业管理
│   │   │   ├── ClassManagement.vue       # 班级管理
│   │   │   ├── SemesterManagement.vue    # 学期管理
│   │   │   └── SystemConfigManagement.vue # 系统配置
│   │   ├── Dashboard.vue  # 首页
│   │   ├── Login.vue      # 登录页
│   │   └── error/         # 错误页面
│   ├── App.vue            # 根组件
│   ├── main.js            # 入口文件
│   └── style.css          # 全局样式
├── package.json
└── vite.config.js
```

## 已实现功能

### 系统管理模块

1. **学校管理**
   - 学校列表查询（分页、搜索）
   - 添加学校
   - 编辑学校信息
   - 停用学校（软删除）

2. **学院管理**
   - 学院列表查询（分页、搜索）
   - 添加学院
   - 编辑学院信息
   - 停用学院（软删除）

3. **专业管理**
   - 专业列表查询（分页、搜索）
   - 添加专业
   - 编辑专业信息
   - 停用专业（软删除）

4. **班级管理**
   - 班级列表查询（分页、搜索）
   - 添加班级
   - 编辑班级信息
   - 停用班级（软删除）
   - **分享码功能**：
     - 生成分享码
     - 重新生成分享码
     - 查看分享码信息
     - 复制分享码

5. **学期管理**
   - 学期列表查询（分页、搜索）
   - 添加学期
   - 编辑学期信息
   - 设置当前学期
   - 删除学期

6. **系统配置管理**
   - 配置列表查询（分页、搜索）
   - 添加配置
   - 编辑配置
   - 删除配置

## 设计风格

- **配色方案**: 使用 Element Plus 的柔和蓝色主题（#409eff）
- **设计风格**: 简约、清爽、现代化
- **避免**: 渐变背景、紫色配色
- **特点**: 
  - 柔和的蓝色主色调
  - 简洁的卡片式布局
  - 统一的圆角和阴影
  - 清晰的层次结构

## 开发规范

### 代码规范
- 使用 Composition API
- 组件命名使用大驼峰
- 文件命名使用大驼峰
- 统一的代码格式（Prettier）

### API调用规范
- 统一使用 `request` 工具进行HTTP请求
- API接口按模块划分文件
- 统一错误处理

### 样式规范
- 使用 scoped 样式
- 统一的颜色变量
- 响应式设计

## 环境配置

### 开发环境
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

### 环境变量
创建 `.env` 文件：
```
VITE_API_BASE_URL=http://localhost:8080/api
```

## 路由配置

### 系统管理模块路由
- `/admin/system/school` - 学校管理
- `/admin/system/college` - 学院管理
- `/admin/system/major` - 专业管理
- `/admin/system/class` - 班级管理
- `/admin/system/semester` - 学期管理
- `/admin/system/config` - 系统配置

## 权限控制

- 路由守卫：检查登录状态和Token有效性
- 接口权限：后端接口使用 `@PreAuthorize` 注解控制
- 前端权限：根据用户角色显示/隐藏功能（待完善）

## 待完善功能

1. 用户管理模块前端页面
2. 实习管理模块前端页面
3. 评价管理模块前端页面
4. 校企协同模块前端页面
5. 数据统计与分析模块前端页面
6. 完整的权限控制系统（前端）
7. 布局组件（顶部导航、左侧菜单）

## 注意事项

1. 所有接口需要后端支持，确保后端服务已启动
2. Token存储在 localStorage 或 sessionStorage
3. 请求拦截器自动添加 Token
4. 响应拦截器统一处理错误

## 开发计划

根据详细开发计划文档，当前已完成：
- ✅ 第一阶段：项目基础搭建
- ✅ 第二阶段：系统管理模块开发（后端 + 前端）

下一步：
- 第三阶段：用户管理模块开发
- 第四阶段：实习过程跟踪模块开发
- ...

