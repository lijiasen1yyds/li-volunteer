# 登录注册功能实现计划

## Context
项目已有 User 实体、UserMapper、SA-Token 依赖、完整的异常处理体系，但缺少 Service 层、Controller 层和 DTO/VO 类。需要实现用户注册、登录、登出、获取当前用户四个核心接口。

## 前置修复

**修改 `pom.xml`**：将 `sa-token-core` 改为 `sa-token-spring-boot-starter`，否则 StpUtil 无法绑定 HTTP 请求。

## 需创建/修改的文件（共 8 个）

### 1. DTO 类
- **`model/dto/user/UserRegisterRequest.java`** — 字段：userAccount, userPassword, checkPassword
- **`model/dto/user/UserLoginRequest.java`** — 字段：userAccount, userPassword

### 2. VO 类
- **`model/vo/LoginUserVO.java`** — User 实体去除 userPassword、isDelete 的安全视图
- **`model/vo/UserVO.java`** — 公开视图，仅含 id、userName、userAvatar、userProfile、userRole、college、major、className、totalVolunteerHours、createTime

### 3. Service 层
- **`service/UserService.java`** — 接口，继承 IService<User>
  - `long userRegister(String userAccount, String userPassword, String checkPassword)`
  - `LoginUserVO userLogin(String userAccount, String userPassword)`
  - `boolean userLogout()`
  - `User getLoginUser()`
  - `LoginUserVO getLoginUserVO(User user)`
  - `UserVO getUserVO(User user)`

- **`service/impl/UserServiceImpl.java`** — 实现类
  - 密码加密：`DigestUtil.md5Hex(SALT + userPassword)`，SALT = `"ljs_volunteer"`
  - 注册校验：参数非空、账号≥4位、密码≥8位、两次密码一致、账号不重复（synchronized + DB 唯一约束）
  - 登录：加密后匹配、检查 userStatus、调用 `StpUtil.login(userId)`
  - 登出：`StpUtil.logout()`
  - 获取当前用户：从 `StpUtil.getLoginId()` 获取 userId，查库并检查状态
  - VO 转换：使用 Hutool `BeanUtil.copyProperties`

### 4. Controller 层
- **`controller/UserController.java`**
  - `POST /user/register` → 返回 `BaseResponse<Long>`
  - `POST /user/login` → 返回 `BaseResponse<LoginUserVO>`
  - `POST /user/logout` → 返回 `BaseResponse<Boolean>`
  - `GET /user/get/login` → 返回 `BaseResponse<LoginUserVO>`

### 5. 修改 pom.xml
- `sa-token-core` → `sa-token-spring-boot-starter`

## 验证方式
启动应用后通过 Knife4j 文档（http://localhost:8123/api/doc.html）测试：
1. 注册新用户 → 返回 userId
2. 用该账号登录 → 返回 LoginUserVO
3. 获取当前登录用户 → 返回用户信息
4. 登出 → 返回 true
5. 再次获取当前用户 → 返回未登录错误
