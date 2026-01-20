# Technical Insights & Solutions Log

本文档记录了在模组开发过程中遇到的关键技术难点、错误原因及最终解决方案，旨在为未来类似问题提供参考。

---

## 1. Mixin 无限递归死锁 (Infinite Loop / Recursion)

### 场景
尝试修改马铠的渲染类型。使用了 `@Redirect` 拦截 `RenderType.armorCutoutNoCull(ResourceLocation)` 静态工厂方法。

### 问题描述
游戏启动后在加载或渲染阶段卡死（无响应），日志无明显报错。

### 根源分析
在使用 `@Redirect` 拦截静态方法时，如果在重定向的处理方法（Handler）内部**再次调用了该静态方法**（例如作为 `else` 分支的默认返回值），Mixin 可能会再次拦截这个调用，从而形成无限递归循环：
`Original Call` -> `Mixin Handler` -> `Original Call (Intercepted)` -> `Mixin Handler` -> ...

### 解决方案
**避免直接重定向工厂方法**。
改为拦截该对象的**消费端/使用端**。
*   **Failed**: Redirect `RenderType.armorCutoutNoCull(...)`
*   **Success**: Redirect `MultiBufferSource.getBuffer(RenderType)`

通过拦截 `getBuffer`，我们可以拿到已经创建好的 `RenderType` 对象，根据逻辑决定是使用它，还是使用一个新的（修改过的） `RenderType` 去请求 Buffer。这避免了调用原始工厂方法产生的递归风险。

---

## 2. Mixin 本地变量注入失败 (LVT Injection Failure)

### 场景
为了在 `getBuffer` 拦截点获取纹理（Texture），尝试使用 `@ModifyVariable` 捕获 `render` 方法中的 `ResourceLocation` 局部变量。

### 问题描述
游戏启动崩溃，提示 `InvalidInjectionException` 或卡死，指示无法在指定的 Local Variable Table (LVT) 中找到变量。

### 根源分析
*   **LVT 不一致**: 生产环境（Obfuscated）和开发环境的 LVT 可能不同。编译器优化可能会移除或重用局部变量槽位。
*   **作用域问题**: 目标变量可能在某些注入点不可见，或者 Mixin 处理器无法正确推断其索引 (Ordinal)。依赖局部变量注入通常是脆弱的 (Brittle)。

### 解决方案
**手动获取 (Manual Retrieval)**。
放弃依赖局部变量，转而在 Mixin 方法内部重新获取所需数据。
*   例如：直接从 `Horse entity` 获取 `ItemStack`，再从 `ArmorItem` 获取 `ResourceLocation`。
*   虽然这增加了一点点微不足道的 CPU 开销（重复获取），但极大地提高了代码的**稳定性**和**兼容性**。

---

## 3. RenderType 兼容性选择

### 经验
*   **RenderType.armorTranslucent**: 在某些版本或特定上下文中可能不存在或不公开。
*   **RenderType.entityTranslucent**: 是最通用、安全的半透明实体渲染类型，支持 Alpha 混合，适用于绝大多数生物和附属层（Layers）。在处理透明化需求时，优先考虑使用通用类型。
