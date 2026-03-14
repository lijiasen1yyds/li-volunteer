# 活动增删改查功能实现计划

## Context
项目已有 User 模块完整实现（实体、Mapper、Service、Controller），数据库中已建好 activity 表（26 个字段）。需要实现活动的创建、删除、更新、查询（单条+分页）五个核心接口，以及"我的活动"查询接口。

## 需创建的文件（共 11 个）

### 1. Entity
- **`model/entity/Activity.java`** — 活动实体类，映射 activity 表全部字段
  - `@TableId(type = IdType.AUTO)` 自增主键
  - `@TableLogic` 逻辑删除字段 isDelete
  - 关键字段：title, description, coverImage, category, location, startTime, endTime, registrationDeadline, maxParticipants, currentParticipants, volunteerHours, organizerId, status, reviewMessage, reviewerId, reviewTime

### 2. Enum 枚举
- **`model/enums/ActivityStatusEnum.java`** — 活动状态枚举
  - PENDING(0, "待审核"), PUBLISHED(1, "已发布"), REJECTED(2, "审核拒绝"), IN_PROGRESS(3, "进行中"), COMPLETED(4, "已完成"), CANCELLED(5, "已取消")
- **`model/enums/ActivityCategoryEnum.java`** — 活动类别枚举
  - COMMUNITY("community"), COMPETITION("competition"), ENVIRONMENT("environment"), EDUCATION("education"), OTHER("other")

### 3. DTO 类
- **`model/dto/activity/ActivityAddRequest.java`** — 创建活动请求
  - 字段：title, description, coverImage, category, location, startTime, endTime, registrationDeadline, maxParticipants, volunteerHours
  - 不含 organizerId（由后端从登录用户获取）
- **`model/dto/activity/ActivityUpdateRequest.java`** — 更新活动请求
  - 字段：id + 与 AddRequest 相同的可修改字段
- **`model/dto/activity/ActivityQueryRequest.java`** — 查询活动请求
  - 继承 PageRequest，支持分页
  - 筛选条件：title（模糊搜索）、category、status、organizerId

### 4. VO 类
- **`model/vo/ActivityVO.java`** — 活动视图对象
  - 包含活动所有公开字段 + organizerName（组织者名称，关联查询填充）
  - 不含审核相关敏感字段（reviewMessage, reviewerId, reviewTime）

### 5. Mapper 层
- **`mapper/ActivityMapper.java`** — 继承 `BaseMapper<Activity>`，`@Mapper` 注解
- **`resources/mapper/ActivityMapper.xml`** — BaseResultMap 映射全部字段

### 6. Service 层
- **`service/ActivityService.java`** — 接口，继承 `IService<Activity>`
  - `void validActivity(Activity activity, boolean add)` — 参数校验
  - `QueryWrapper<Activity> getQueryWrapper(ActivityQueryRequest)` — 构建查询条件
  - `ActivityVO getActivityVO(Activity)` — 单条转 VO
  - `Page<ActivityVO> getActivityVOPage(Page<Activity>)` — 分页转 VO

- **`service/impl/ActivityServiceImpl.java`** — 实现类
  - 校验逻辑：创建时标题、开始/结束时间必填；标题≤256字符；描述≤10000字符；开始时间不能晚于结束时间
  - 查询条件：title 用 `like`，category/status/organizerId 用 `eq`，支持排序
  - VO 转换：使用 `BeanUtil.copyProperties`，批量查询组织者信息避免 N+1 问题

### 7. Controller 层
- **`controller/ActivityController.java`** — REST 接口

## API 接口设计

| 方法 | 路径 | 说明 | 权限要求 |
|------|------|------|----------|
| POST | `/api/activity/add` | 创建活动 | 需登录，organizerId 自动设为当前用户 |
| POST | `/api/activity/delete` | 删除活动 | 需登录，仅活动创建者或管理员 |
| POST | `/api/activity/update` | 更新活动 | 需登录，仅活动创建者或管理员 |
| GET  | `/api/activity/get/vo` | 根据 ID 获取活动详情 | 无需登录 |
| POST | `/api/activity/list/page/vo` | 分页查询活动列表 | 无需登录 |
| POST | `/api/activity/my/list/page/vo` | 查询我创建的活动 | 需登录，自动过滤 organizerId |

## 权限控制策略
- **创建**：任何登录用户均可创建活动
- **删除/更新**：仅活动的 organizerId 对应用户或 admin 角色可操作
- **查询**：公开接口，无需登录
- **我的活动**：需登录，后端强制覆盖 organizerId 为当前用户 ID

## 关键设计决策
1. **防 N+1 查询**：分页查询时批量获取所有组织者信息，通过 Map 关联，避免逐条查询
2. **防爬虫**：分页接口限制每页最大 20 条
3. **参数安全**：创建活动时 organizerId 从 session 获取，不允许前端传入
4. **统一响应**：所有接口返回 `BaseResponse<T>`，复用项目已有的异常处理体系

## 验证方式
启动应用后通过 Knife4j 文档（http://localhost:8123/api/doc.html）测试：
1. 登录用户 → 创建活动 → 返回活动 ID
2. 根据 ID 查询活动详情 → 返回 ActivityVO（含组织者名称）
3. 分页查询活动列表 → 支持按标题/类别/状态筛选
4. 更新活动信息 → 仅创建者可操作
5. 删除活动 → 仅创建者或管理员可操作
6. 查询我的活动 → 仅返回当前用户创建的活动
