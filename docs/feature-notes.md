# 功能点与技术改造记录

这份文档用于记录从原始学习项目开始，后续所有重构和新增功能的设计意图、技术选型、核心流程和关键代码位置。以后维护项目时，每完成一个功能点或重要重构，都要在这里追加记录，保证项目不仅能运行，也能被快速理解和讲清楚。

## 文档维护约定

- 每个功能点都记录：业务目标、使用技术、完整流程、关键代码、可靠性设计、可测试点、面试讲解点。
- 每次修改高并发链路、缓存、MQ、状态机、鉴权、规则引擎、支付、通知、部署配置时，都同步更新本文档。
- 如果只是小修样式或文案，可以不单独新增功能点，但如果影响业务流程或接口语义，需要记录。
- 文档优先描述“为什么这样设计”和“请求如何流转”，避免只罗列文件名。
- README 保持项目总览；本文档负责保存更细的功能点细节。

## 1. 高并发赛事报名

### 业务目标

原始项目已有赛事报名功能，但报名逻辑主要依赖服务层查询、Redis 分布式锁和 RabbitMQ，技术点存在但链路不够清晰，也没有区分真实马拉松常见的两种报名形态。

本次改造将报名功能定位为项目的核心技术卖点：在热门马拉松赛事报名开始时，大量跑者同时提交报名请求，系统需要做到快速响应、不重复报名、异步削峰落库，并根据赛事报名模式决定是否需要扣减名额库存。

当前支持两种报名模式：

- `LOTTERY`：大型赛事，超额报名后抽签。报名阶段不扣参赛名额，`participantNumber` 表示最终可参赛容量。
- `FIRST_COME`：小型赛事，限额报名，报完即止。报名阶段按 `participantNumber` 扣减库存。

### 使用技术

- Caffeine：本地热点赛事缓存，减少 Redis 访问压力。
- Redis：缓存赛事详情、赛事报名元信息、报名模式、剩余名额、用户报名标记。
- Redis Lua：原子完成报名临界区操作；抽签模式只去重和写标记，先到先得模式额外扣库存。
- RabbitMQ：报名请求异步落库，削峰填谷。
- MyBatis-Plus + MySQL：报名最终持久化。
- 数据库唯一索引：`registration(user_id, race_id)` 作为重复报名的最终兜底。
- Spring 定时任务：维护赛事状态，并触发报名截止后的抽签。

### 完整流程

1. 跑者调用 `/registration/add`，提交 `raceId` 和 `userId`。
2. 后端先做基础参数校验。
3. 读取赛事信息，优先走 Caffeine 本地缓存。
4. 本地缓存未命中时读取 Redis 赛事详情缓存。
5. Redis 未命中时回源 MySQL，并写回 Redis 与 Caffeine。
6. 校验赛事是否处于可报名时间窗口和可报名状态。
7. 查询用户健康资格，按赛事类型判断是否允许报名：
   - 全马要求最高健康资格。
   - 半马允许中等风险以下。
   - 10K/5K 允许更低门槛。
8. 预热 Redis 报名 key：
   - `race:{raceId}:meta`：报名开始时间、结束时间、赛事状态。
   - `race:{raceId}:meta.registrationMode`：报名模式，`1` 为抽签，`2` 为先到先得。
   - `race:{raceId}:stock`：先到先得模式的剩余报名名额。
9. 执行 Redis Lua 脚本，在 Redis 内部原子完成：
   - 判断赛事元信息是否存在。
   - 判断是否未开始。
   - 判断是否已结束。
   - 判断用户是否已经报名。
   - 抽签模式：直接写入用户报名标记。
   - 先到先得模式：判断库存是否充足并扣减库存。
   - 写入用户报名标记。
10. Lua 成功后，后端生成 `RegistrationMessage`，发送到 RabbitMQ。
11. 接口立即返回“报名已受理”，不等待数据库落库完成。
12. RabbitMQ 消费端异步创建报名记录。
13. 消费端先查询是否已报名，再插入数据库。
14. 数据库唯一索引作为最终幂等兜底，重复消息不会生成重复报名。
15. 如果 Lua 成功但 RabbitMQ 发送失败，服务端回滚 Redis 用户报名标记；先到先得模式额外归还库存。

### 双模式流程

抽签模式适合大型城市马拉松。报名阶段系统只受理申请，不承诺参赛资格。报名截止后，抽签服务从 `REGISTERED` 状态的报名记录中按 `participantNumber` 选出中签用户，状态变为“中签待支付”；未中签用户状态变为“未中签”。

先到先得模式适合小型赛事。报名阶段 `participantNumber` 被视为报名库存，Redis Lua 在同一个原子操作中完成查重和扣库存。库存为 0 后直接拒绝新报名，因此该模式不需要抽签。

### Lua 返回码

- `0`：报名已受理。
- `1`：赛事不存在。
- `2`：报名未开始。
- `3`：报名已结束。
- `4`：用户已报名。
- `5`：名额不足，仅先到先得模式会返回。
- `6`：系统繁忙或未知错误。

Java 侧通过 `RegistrationLuaResult` 将返回码转换为清晰业务语义，避免 Controller 继续使用魔法数字。

### 关键代码

- `marathon-service/src/main/resources/scripts/register.lua`：报名 Lua 脚本。
- `marathon-service/src/main/java/org/example/marathonservice/service/RegistrationLuaExecutor.java`：Java 侧 Lua 执行封装。
- `marathon-service/src/main/java/org/example/marathonservice/service/RaceCacheService.java`：Caffeine + Redis + MySQL 多级缓存。
- `marathon-service/src/main/java/org/example/marathonservice/service/RegistrationService.java`：报名入口、MQ 生产、MQ 消费、抽签逻辑。
- `marathon-service/src/main/java/org/example/marathonservice/enums/RegistrationMode.java`：报名模式枚举。
- `marathon-service/src/main/java/org/example/marathonservice/config/MQConfig.java`：报名队列、交换机、路由键、死信队列。
- `marathon-common/src/main/java/org/example/marathoncommon/constants/RedisKeyConstant.java`：Redis key 统一定义。
- `docs/sql/001_high_concurrency_registration.sql`：报名唯一索引与订单索引。

### 可靠性设计

- 防重复报名：Redis 用户报名标记前置拦截，数据库唯一索引最终兜底。
- 防名额超扣：仅先到先得模式启用，Redis Lua 脚本将库存判断和扣减放在同一个原子操作中。
- 削峰：接口线程只负责快速校验和发送 MQ，落库由消费者异步处理。
- 幂等消费：消费端先查重，数据库唯一索引兜底。
- 失败补偿：MQ 发送失败时回滚 Redis 标记；先到先得模式同时回滚库存。
- 缓存一致性：赛事新增或更新后主动刷新缓存；赛事状态定时变化后同步刷新缓存。
- 缓存雪崩缓解：Redis 赛事详情 TTL 加随机抖动。
- 缓存穿透缓解：非法 ID 直接返回；空赛事短 TTL 缓存。

### 状态设计

赛事状态通过 `RaceStatus` 枚举表达：

- 草稿
- 待审核
- 报名未开始
- 报名中
- 报名截止
- 抽签完成
- 比赛结束
- 取消

报名状态通过 `RegistrationStatus` 枚举表达：

- 受理中
- 已报名
- 已取消
- 未中签
- 中签待支付
- 已支付
- 支付超时

### 测试与验证

已补充轻量测试：

- `RegistrationLuaResultTest`：验证 Lua 返回码到业务结果的映射。
- `RegistrationLuaResultTest`：验证历史赛事默认按抽签模式处理。
- `RegistrationLuaScriptTest`：验证 Lua 脚本包含报名模式分支和关键原子操作。
- `MarathonWebappApplicationTests`：验证 JWT 生成、校验、用户角色和用户 ID 解析。

已执行验证：

- `mvn -pl marathon-webapp -am test`：通过。
- `npm run build`：通过。

### 压测建议

使用 `hey` 或 `wrk` 压测 `/registration/add`：

```bash
hey -n 10000 -c 200 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -m POST \
  -d '{"raceId":1,"userId":1001}' \
  http://localhost:8700/registration/add
```

重点观察：

- 抽签模式下报名人数可以超过参赛名额，最终通过抽签筛选。
- 先到先得模式下 Redis 库存不会扣成负数。
- 同一用户不会重复报名。
- 先到先得模式下数据库报名数不超过赛事名额。
- RabbitMQ 消费积压时接口仍能快速返回。
- 消费恢复后报名记录最终落库。

### 面试讲解点

- 为什么多级缓存选择 Caffeine + Redis + MySQL，而不是只用 Redis。
- 为什么真实马拉松不能简单套用秒杀防超卖模型。
- 为什么大型赛事采用超额报名后抽签，小型赛事采用限额报名。
- 为什么 Lua 脚本只做查重和必要扣库存，不做复杂健康资格判断。
- 为什么 Redis 成功后还需要数据库唯一索引兜底。
- 为什么报名接口返回“已受理”，而不是同步等数据库插入完成。
- MQ 发送失败时如何按报名模式补偿 Redis 库存和用户标记。
- 如何解释削峰、幂等、最终一致性和不同报名模式下的并发控制。

## 2. 配置安全与本地环境

### 业务目标

原始项目配置文件中包含真实外部 IP、数据库账号、Redis 密码、RabbitMQ 密码、邮箱授权码和 MinIO 密钥。作为求职作品，这类信息必须移除，同时项目需要能在本地快速启动。

### 使用技术

- Spring Boot 配置占位符。
- 环境变量。
- Docker Compose。
- MySQL、Redis、RabbitMQ、MinIO 本地容器。

### 改造内容

- `application.yml` 中所有敏感配置改为环境变量。
- 新增 `application-local.yml.example`，作为本地配置样例。
- 新增 `docker-compose.yml`，一键启动 MySQL、Redis、RabbitMQ、MinIO。
- 新增 `docs/sql` 初始化目录，Docker MySQL 启动时可执行初始化脚本。

### 关键代码

- `marathon-webapp/src/main/resources/application.yml`
- `marathon-webapp/src/main/resources/application-local.yml.example`
- `docker-compose.yml`
- `docs/sql/001_high_concurrency_registration.sql`

### 面试讲解点

- 为什么不能把真实密钥提交到仓库。
- 如何用环境变量区分本地、测试、生产环境。
- 为什么求职项目也需要本地一键启动能力。

## 3. JWT 鉴权与基础角色权限

### 业务目标

原始项目生成了 JWT，但后端缺少真正的统一拦截校验，主要依靠前端路由守卫保护页面。此次改造增加基础后端鉴权，让接口访问更符合真实项目习惯。

### 使用技术

- JWT。
- Spring MVC `HandlerInterceptor`。
- Spring Boot 配置外部化。

### 流程

1. 登录成功后，后端签发 JWT。
2. JWT 包含账号、用户 ID、角色类型和过期时间。
3. 前端请求时通过 `Authorization: Bearer <token>` 携带 token。
4. `AuthInterceptor` 校验 token 是否存在、是否合法、是否过期。
5. 根据接口路径做基础角色校验：
   - 规则管理、健康审核、主办方申请审核：管理员。
   - 抽签、成绩上传、赛事维护、路线维护：主办方或管理员。
   - 普通用户接口：登录用户。
   - 登录、注册、赛事列表、赛事详情、新闻、评论等：公开访问。

### 关键代码

- `marathon-webapp/src/main/java/org/example/marathonwebapp/utils/Jwt.java`
- `marathon-webapp/src/main/java/org/example/marathonwebapp/config/AuthInterceptor.java`
- `marathon-webapp/src/main/java/org/example/marathonwebapp/config/WebMvcConfig.java`

### 面试讲解点

- 前端路由守卫不能替代后端鉴权。
- JWT 密钥和过期时间需要外部化。
- 当前是轻量角色拦截，后续可升级 Spring Security。

## 4. 支付与参赛号一致性

### 业务目标

原始项目支付成功后会生成参赛号，但状态约束不够清晰。此次改造明确：只有中签待支付的报名才能创建订单，只有支付成功后才分配正式参赛号。

### 使用技术

- MySQL 订单表。
- Redis 自增计数器。
- 报名状态机。

### 流程

1. 用户中签后，报名状态变为“中签待支付”。
2. 用户创建订单时，后端校验报名是否处于“中签待支付”。
3. 支付成功后，更新订单为已支付。
4. 使用 Redis `INCR` 按赛事生成递增参赛号。
5. 更新报名状态为“已支付”，写入参赛号。
6. 如果订单取消或超时，可将报名状态标记为“支付超时”。

### 关键代码

- `marathon-service/src/main/java/org/example/marathonservice/service/OrderService.java`
- `marathon-service/src/main/java/org/example/marathonservice/service/RegistrationService.java`

### 面试讲解点

- 为什么不能报名成功就分配参赛号。
- 为什么参赛号按赛事维度使用 Redis 自增。
- 支付和报名状态如何保持一致。

## 后续建议记录的功能点

- Drools 健康资格规则重构。
- 抽签算法公平性与可复现性优化。
- WebSocket 通知可靠性。
- 管理端赛事审核流程。
- 压测报告与性能指标。
- 前端用户流程重构。
