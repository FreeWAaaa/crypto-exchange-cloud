# Lombok配置指南

## 问题描述
在使用Lombok注解（如@Data、@Slf4j等）时出现"程序包lombok.extern.slf4j不存在"错误。

## 解决方案

### 1. 检查Maven依赖
确保父POM和子模块POM中都正确配置了Lombok依赖：

**父POM (pom.xml):**
```xml
<properties>
    <lombok.version>1.18.28</lombok.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**子模块POM:**
```xml
<dependencies>
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 2. IntelliJ IDEA配置

#### 2.1 安装Lombok插件
1. 打开IntelliJ IDEA
2. 进入 `File` -> `Settings` (Windows/Linux) 或 `IntelliJ IDEA` -> `Preferences` (Mac)
3. 选择 `Plugins`
4. 搜索 "Lombok"
5. 安装 "Lombok" 插件
6. 重启IDEA

#### 2.2 启用注解处理
1. 进入 `File` -> `Settings` -> `Build, Execution, Deployment` -> `Compiler` -> `Annotation Processors`
2. 勾选 `Enable annotation processing`
3. 点击 `Apply` 和 `OK`

#### 2.3 检查项目设置
1. 进入 `File` -> `Project Structure` -> `Modules`
2. 选择你的模块
3. 在 `Dependencies` 标签页中确保Lombok依赖存在
4. 在 `Sources` 标签页中确保源码目录正确

### 3. Maven配置

#### 3.1 清理并重新编译
```bash
# 清理项目
mvn clean

# 重新编译
mvn compile

# 或者强制更新依赖
mvn clean compile -U
```

#### 3.2 检查Maven版本
确保使用Maven 3.6+版本：
```bash
mvn -version
```

### 4. 验证配置

#### 4.1 创建测试类
```java
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class TestClass {
    private String name;
    private Integer age;
    
    public void test() {
        log.info("测试Lombok日志功能");
    }
}
```

#### 4.2 检查生成的代码
1. 编译项目后，在 `target/classes` 目录下查看生成的class文件
2. 使用反编译工具查看是否生成了getter/setter方法

### 5. 常见问题排查

#### 5.1 依赖冲突
如果存在依赖冲突，可以排除冲突的依赖：
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.28</version>
    <scope>provided</scope>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

#### 5.2 版本兼容性
确保Lombok版本与JDK版本兼容：
- JDK 8: Lombok 1.18.x
- JDK 11: Lombok 1.18.x
- JDK 17: Lombok 1.18.28+

#### 5.3 清理IDEA缓存
如果问题仍然存在，尝试清理IDEA缓存：
1. `File` -> `Invalidate Caches and Restart`
2. 选择 `Invalidate and Restart`

### 6. 项目特定配置

#### 6.1 Spring Boot配置
在 `application.yml` 中添加：
```yaml
spring:
  main:
    allow-bean-definition-overriding: true
```

#### 6.2 Maven编译器配置
在POM中添加编译器配置：
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <encoding>UTF-8</encoding>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.28</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## 验证步骤

1. **检查插件安装**: 在IDEA中查看是否安装了Lombok插件
2. **检查注解处理**: 确认启用了注解处理
3. **重新编译**: 执行 `mvn clean compile`
4. **测试注解**: 创建测试类使用Lombok注解
5. **查看生成代码**: 检查是否生成了getter/setter方法

## 注意事项

1. Lombok插件必须安装并启用
2. 注解处理必须启用
3. Maven依赖必须正确配置
4. 项目需要重新编译才能看到效果
5. 某些IDE可能需要重启才能生效

如果按照以上步骤操作后仍有问题，请检查：
- IDEA版本是否支持Lombok
- JDK版本是否兼容
- 项目结构是否正确
- 是否有其他插件冲突
