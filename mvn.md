# Maven

## 🧱 项目构建（Build）

### 三大生命周期

- clean
- site
- default

#### clean 生命周期的阶段 phases

- pre-clean
- clean
- post-clean

#### site 生命周期的阶段 phases

- pre-site
- site
- post-site

#### default 生命周期的阶段 phases

- validate：检查 pom.xml
- compile：编译 Java 代码
- test：运行单元测试
- package：生成 .jar/.war
- install：安装到本地仓库
- deploy：发布到远程仓库
