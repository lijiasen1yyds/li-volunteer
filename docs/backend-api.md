# 后端接口文档

## 基本信息

- 服务基地址：`http://localhost:8123/api`
- 在线接口文档：后端启动后访问 `http://localhost:8123/api/doc.html`
- 接口统一返回：

```json
{
  "code": 0,
  "data": {},
  "message": "ok"
}
```

- 常见错误码：

| code | 含义 |
| --- | --- |
| `0` | 成功 |
| `40000` | 参数错误 |
| `40100` | 未登录 |
| `40101` | 无权限 |
| `40300` | 禁止访问 |
| `40400` | 数据不存在 |
| `50000` | 系统错误 |
| `500001` | 操作失败 |

- 登录态由 Sa-Token 管理。登录成功后，前端需要保留会话 Cookie，或携带 `satoken`。
- 所有 `Long` 类型字段都会被序列化成字符串，前端不要按 `number` 精度敏感字段处理。
- 时间字段在代码里都是 `Date`。前端建议统一按 ISO 8601 日期时间字符串处理。

## 通用约定

### 分页请求字段

以下分页接口都继承同一套分页参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `current` | `number` | 当前页，默认 `1` |
| `pageSize` | `number` | 每页条数，默认 `10`，后端限制 `<= 20` |
| `sortField` | `string` | 排序字段 |
| `sortOrder` | `string` | 排序方向，建议传 `ascend` 或 `descend` |

### 分页响应结构

分页接口返回 `BaseResponse<Page<T>>`，前端重点使用这些字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `records` | `T[]` | 当前页数据 |
| `current` | `number` | 当前页 |
| `size` | `number` | 每页条数 |
| `total` | `number` | 总条数 |
| `pages` | `number` | 总页数 |

## 角色与枚举

### 用户角色

| 值 | 含义 |
| --- | --- |
| `volunteer` | 志愿者 |
| `organizer` | 组织者 |
| `admin` | 管理员 |

### 活动分类 `category`

| 值 | 含义 |
| --- | --- |
| `community` | 社区帮扶 |
| `competition` | 赛事保障 |
| `environment` | 环保宣传 |
| `education` | 教育支持 |
| `other` | 其他 |

### 活动状态 `activity.status`

| 值 | 含义 |
| --- | --- |
| `0` | 待审核 |
| `1` | 已发布 |
| `2` | 审核拒绝 |
| `3` | 进行中 |
| `4` | 已完成 |
| `5` | 已取消 |

### 报名状态 `registration.status`

| 值 | 含义 |
| --- | --- |
| `0` | 待审核 |
| `1` | 已通过 |
| `2` | 已拒绝 |
| `3` | 已取消 |

### 签到状态 `checkIn.status`

| 值 | 含义 |
| --- | --- |
| `0` | 已签到 |
| `1` | 已签退 |

### 志愿时长认证状态 `volunteerHours.status`

| 值 | 含义 |
| --- | --- |
| `0` | 待认证 |
| `1` | 已认证 |
| `2` | 认证拒绝 |

## 返回模型

### `LoginUserVO`

| 字段 |
| --- |
| `id` |
| `userAccount` |
| `userName` |
| `userAvatar` |
| `userPhone` |
| `userEmail` |
| `studentId` |
| `college` |
| `major` |
| `className` |
| `userProfile` |
| `userRole` |
| `userStatus` |
| `totalVolunteerHours` |
| `createTime` |
| `updateTime` |

### `ActivityVO`

| 字段 |
| --- |
| `id` |
| `title` |
| `description` |
| `coverImage` |
| `category` |
| `location` |
| `startTime` |
| `endTime` |
| `registrationDeadline` |
| `maxParticipants` |
| `currentParticipants` |
| `volunteerHours` |
| `organizerId` |
| `organizerName` |
| `status` |
| `createTime` |

### `ActivityRegistrationVO`

| 字段 |
| --- |
| `id` |
| `activityId` |
| `userId` |
| `status` |
| `reviewMessage` |
| `createTime` |
| `userName` |
| `activityTitle` |

### `CheckInRecordVO`

| 字段 |
| --- |
| `id` |
| `activityId` |
| `userId` |
| `registrationId` |
| `checkInTime` |
| `checkOutTime` |
| `actualHours` |
| `status` |
| `createTime` |
| `userName` |
| `activityTitle` |

### `VolunteerHoursVO`

| 字段 |
| --- |
| `id` |
| `userId` |
| `activityId` |
| `hours` |
| `status` |
| `certifierId` |
| `certifyTime` |
| `remark` |
| `createTime` |
| `userName` |
| `activityTitle` |
| `certifierName` |

### `ActivityEvaluationVO`

| 字段 |
| --- |
| `id` |
| `activityId` |
| `userId` |
| `rating` |
| `content` |
| `createTime` |
| `userName` |
| `userAvatar` |
| `activityTitle` |

## 用户模块

### `POST /user/register`

- 鉴权：无需登录
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "userAccount": "zhangsan",
  "userPassword": "12345678",
  "checkPassword": "12345678"
}
```

- 返回：`BaseResponse<string>`，`data` 为用户 ID
- 约束：
  - `userAccount` 长度至少 4
  - `userPassword` 长度至少 8
  - 两次密码必须一致

### `POST /user/login`

- 鉴权：无需登录
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "userAccount": "zhangsan",
  "userPassword": "12345678"
}
```

- 返回：`BaseResponse<LoginUserVO>`

### `POST /user/logout`

- 鉴权：需登录
- 返回：`BaseResponse<boolean>`

### `GET /user/get/login`

- 鉴权：需登录
- 返回：`BaseResponse<LoginUserVO>`

## 活动模块

### `POST /activity/add`

- 鉴权：`organizer`
- `Content-Type`：`multipart/form-data`
- 表单字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | `string` | 是 | 活动标题 |
| `description` | `string` | 否 | 活动描述 |
| `category` | `string` | 否 | 活动分类，见上方枚举 |
| `location` | `string` | 否 | 活动地点 |
| `startTime` | `string` | 是 | 开始时间 |
| `endTime` | `string` | 是 | 结束时间 |
| `registrationDeadline` | `string` | 否 | 报名截止时间 |
| `maxParticipants` | `number` | 否 | 最大人数 |
| `volunteerHours` | `number` | 否 | 活动可认证时长 |
| `file` | `file` | 否 | 活动封面图 |

- 返回：`BaseResponse<string>`，`data` 为活动 ID
- 约束：
  - 标题不能为空，长度不超过 256
  - 描述长度不超过 10000
  - `startTime` 不能晚于 `endTime`
  - 图片仅支持 `jpg/jpeg/png/webp`
  - 图片大小不能超过 `2MB`
- 备注：
  - `organizerId` 由后端按当前登录用户自动写入

### `POST /activity/delete`

- 鉴权：需登录
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "id": "1"
}
```

- 返回：`BaseResponse<boolean>`
- 备注：
  - 管理员会直接删除活动
  - 组织者删除自己的活动时，后端会把活动状态改成 `5=已取消`

### `POST /activity/update`

- 鉴权：`admin`
- `Content-Type`：`multipart/form-data`
- 表单字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `string` | 是 | 活动 ID |
| `title` | `string` | 否 | 活动标题 |
| `description` | `string` | 否 | 活动描述 |
| `coverImage` | `string` | 否 | 直接传封面 URL |
| `category` | `string` | 否 | 活动分类 |
| `location` | `string` | 否 | 活动地点 |
| `startTime` | `string` | 否 | 开始时间 |
| `endTime` | `string` | 否 | 结束时间 |
| `registrationDeadline` | `string` | 否 | 报名截止时间 |
| `maxParticipants` | `number` | 否 | 最大人数 |
| `volunteerHours` | `number` | 否 | 活动可认证时长 |
| `file` | `file` | 否 | 新封面图 |

- 返回：`BaseResponse<boolean>`
- 备注：
  - 如果同时传 `coverImage` 和 `file`，后端最终会以上传图片地址覆盖 `coverImage`

### `GET /activity/get/vo?id={id}`

- 鉴权：无需登录
- 返回：`BaseResponse<ActivityVO>`

### `POST /activity/list/page/vo`

- 鉴权：无需登录
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "title": "社区",
  "category": "community",
  "status": 1,
  "organizerId": "1",
  "current": 1,
  "pageSize": 10,
  "sortField": "createTime",
  "sortOrder": "descend"
}
```

- 返回：`BaseResponse<Page<ActivityVO>>`

### `POST /activity/my/list/page/vo`

- 鉴权：需登录
- `Content-Type`：`application/json`
- 请求体：同 `ActivityQueryRequest`
- 返回：`BaseResponse<Page<ActivityVO>>`
- 备注：
  - `organizerId` 会被后端强制改成当前登录用户

### `POST /activity/review`

- 鉴权：`admin`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "id": "1",
  "status": 1,
  "reviewMessage": "审核通过"
}
```

- 返回：`BaseResponse<boolean>`
- 约束：
  - `status` 只能传 `1=已发布` 或 `2=审核拒绝`
  - 拒绝时必须填写 `reviewMessage`
  - 只能审核当前状态为 `0=待审核` 的活动

## 文件模块

### `POST /file/upload`

- 鉴权：需登录，且必须是该活动的组织者或管理员
- `Content-Type`：`multipart/form-data`
- 表单字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | `file` | 是 | 图片文件 |
| `activityId` | `string` | 是 | 活动 ID |

- 返回：`BaseResponse<string>`，`data` 为图片 URL
- 约束：
  - 图片仅支持 `jpg/jpeg/png/webp`
  - 图片大小不能超过 `2MB`
- 备注：
  - 上传成功后，后端会同步更新活动封面图 `coverImage`

## 报名模块

### `POST /registration/add`

- 鉴权：标注为 `volunteer`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "activityId": "1"
}
```

- 返回：`BaseResponse<string>`，`data` 为报名记录 ID
- 约束：
  - 活动必须存在且状态为 `1=已发布`
  - 未超过报名截止时间
  - 未超过最大报名人数
- 备注：
  - 同一用户同一活动只能保留一条报名记录

### `POST /registration/cancel`

- 鉴权：需登录
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "id": "1"
}
```

- 返回：`BaseResponse<boolean>`
- 约束：
  - 只能取消自己的报名
  - 只能取消 `0=待审核` 的报名

### `POST /registration/review`

- 鉴权：`organizer`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "id": "1",
  "status": 1,
  "reviewMessage": "通过"
}
```

- 返回：`BaseResponse<boolean>`
- 约束：
  - `status` 只能传 `1=已通过` 或 `2=已拒绝`
  - 拒绝时必须填写 `reviewMessage`
  - 只能审核 `0=待审核` 的报名
- 备注：
  - 审核通过后，活动 `currentParticipants` 会自动加 1
  - 当前实现里，管理员也可审核

### `GET /registration/get/vo?id={id}`

- 鉴权：需登录
- 返回：`BaseResponse<ActivityRegistrationVO>`

### `POST /registration/list/page/vo`

- 鉴权：`organizer`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "activityId": "1",
  "userId": "1",
  "status": 0,
  "current": 1,
  "pageSize": 10,
  "sortField": "createTime",
  "sortOrder": "descend"
}
```

- 返回：`BaseResponse<Page<ActivityRegistrationVO>>`
- 备注：
  - 管理员可看全部
  - 组织者必须指定 `activityId`，且只能查看自己活动的报名

### `POST /registration/my/list/page/vo`

- 鉴权：需登录
- `Content-Type`：`application/json`
- 请求体：同 `RegistrationQueryRequest`
- 返回：`BaseResponse<Page<ActivityRegistrationVO>>`
- 备注：
  - `userId` 会被后端强制改成当前登录用户

## 签到模块

### `POST /checkIn/do`

- 鉴权：标注为 `volunteer`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "activityId": "1"
}
```

- 返回：`BaseResponse<string>`，`data` 为签到记录 ID
- 约束：
  - 活动必须存在且状态为 `3=进行中`
  - 当前用户必须有该活动的 `1=已通过` 报名记录
  - 不能重复签到

### `POST /checkIn/doCheckOut`

- 鉴权：标注为 `volunteer`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "activityId": "1"
}
```

- 返回：`BaseResponse<boolean>`
- 备注：
  - 签退成功后，后端会自动生成一条待认证的志愿时长记录

### `GET /checkIn/get/vo?id={id}`

- 鉴权：需登录
- 返回：`BaseResponse<CheckInRecordVO>`

### `POST /checkIn/list/page/vo`

- 鉴权：`organizer`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "activityId": "1",
  "userId": "1",
  "status": 1,
  "current": 1,
  "pageSize": 10,
  "sortField": "createTime",
  "sortOrder": "descend"
}
```

- 返回：`BaseResponse<Page<CheckInRecordVO>>`
- 备注：
  - 管理员可看全部
  - 组织者必须指定 `activityId`，且只能查看自己活动的签到记录

### `POST /checkIn/my/list/page/vo`

- 鉴权：需登录
- `Content-Type`：`application/json`
- 请求体：同 `CheckInQueryRequest`
- 返回：`BaseResponse<Page<CheckInRecordVO>>`
- 备注：
  - `userId` 会被后端强制改成当前登录用户

## 志愿时长认证模块

### `GET /volunteerHours/get/vo?id={id}`

- 鉴权：需登录
- 返回：`BaseResponse<VolunteerHoursVO>`
- 备注：
  - 管理员可看全部
  - 普通用户只能看自己的记录
  - 非管理员在不是本人时，仅活动组织者可查看

### `POST /volunteerHours/review`

- 鉴权：`organizer`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "id": "1",
  "status": 1,
  "hours": 2.5,
  "remark": "认证通过"
}
```

- 返回：`BaseResponse<boolean>`
- 字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `string` | 是 | 认证记录 ID |
| `status` | `number` | 是 | `1=已认证`，`2=认证拒绝` |
| `hours` | `number` | 否 | 最终认证时长，通过时可选 |
| `remark` | `string` | 否 | 备注，拒绝时必填 |

- 约束：
  - 只能审核 `0=待认证` 的记录
  - `hours` 不传时，后端默认使用系统已生成的待认证时长
  - 如果传了 `hours`，不能小于等于 0，也不能超过实际签到时长和活动可认证时长的上限
  - 拒绝时必须传 `remark`
- 备注：
  - 审核通过后，后端会累加用户总志愿时长
  - 当前实现里，管理员也可审核

### `POST /volunteerHours/list/page/vo`

- 鉴权：`organizer`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "activityId": "1",
  "userId": "1",
  "status": 0,
  "current": 1,
  "pageSize": 10,
  "sortField": "createTime",
  "sortOrder": "descend"
}
```

- 返回：`BaseResponse<Page<VolunteerHoursVO>>`
- 备注：
  - 管理员可看全部
  - 组织者必须指定 `activityId`，且只能查看自己活动的认证记录

### `POST /volunteerHours/my/list/page/vo`

- 鉴权：需登录
- `Content-Type`：`application/json`
- 请求体：同 `VolunteerHoursQueryRequest`
- 返回：`BaseResponse<Page<VolunteerHoursVO>>`
- 备注：
  - `userId` 会被后端强制改成当前登录用户

## 评价模块

### `POST /evaluation/add`

- 鉴权：标注为 `volunteer`
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "activityId": "1",
  "rating": 5,
  "content": "活动很好"
}
```

- 返回：`BaseResponse<string>`，`data` 为评价 ID
- 约束：
  - 活动必须存在且状态为 `4=已完成`
  - 当前用户必须有该活动的 `1=已通过` 报名记录
  - 同一用户对同一活动只能评价一次
  - `rating` 只能为 `1-5`
  - `content` 长度不能超过 `1024`

### `POST /evaluation/update`

- 鉴权：需登录
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "id": "1",
  "rating": 4,
  "content": "修改后的评价"
}
```

- 返回：`BaseResponse<boolean>`
- 约束：
  - 只能修改自己的评价
  - `rating` 只能为 `1-5`
  - `content` 长度不能超过 `1024`

### `POST /evaluation/delete`

- 鉴权：需登录
- `Content-Type`：`application/json`
- 请求体是一个原始数字，不是对象：

```json
1
```

- 返回：`BaseResponse<boolean>`
- 备注：
  - 只能删除自己的评价
  - 管理员也可删除

### `GET /evaluation/get/vo?id={id}`

- 鉴权：无需登录
- 返回：`BaseResponse<ActivityEvaluationVO>`

### `POST /evaluation/list/page/vo`

- 鉴权：无需登录
- `Content-Type`：`application/json`
- 请求体：

```json
{
  "activityId": "1",
  "userId": "1",
  "minRating": 3,
  "maxRating": 5,
  "current": 1,
  "pageSize": 10,
  "sortField": "createTime",
  "sortOrder": "descend"
}
```

- 返回：`BaseResponse<Page<ActivityEvaluationVO>>`

### `POST /evaluation/my/list/page/vo`

- 鉴权：需登录
- `Content-Type`：`application/json`
- 请求体：同 `EvaluationQueryRequest`
- 返回：`BaseResponse<Page<ActivityEvaluationVO>>`
- 备注：
  - `userId` 会被后端强制改成当前登录用户

## 前端对接备注

- 详情接口全部是查询参数 `id`，不是 RESTful 路径参数。
- 除 `activity/add`、`activity/update`、`file/upload` 外，其余写接口都按 JSON 传参。
- `evaluation/delete` 是当前项目里唯一一个“请求体是原始值”的接口，前端调用时要单独处理。
- `my/list/page/vo` 系列接口不要手动传 `userId` 或 `organizerId` 作为真实筛选依据，后端会覆盖。
- 如果前端严格按角色控制页面展示，建议仍以前端角色做限制；但最终权限以后端校验为准。
