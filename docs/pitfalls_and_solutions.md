# 开发踩坑记录

本文档记录开发过程中遇到的重大问题、障碍及其解决方案，供后续开发者参考避坑。

---

## 1. PoweredRailBlock 构造函数参数问题

**日期**: 2026-01-21

**症状**:
- 自定义动力铁轨（如铜铁轨）上的矿车剧烈晃动
- 矿车会像经过激活铁轨一样弹射乘客
- 加速功能可能正常工作，但视觉效果异常

**根本原因**:
`PoweredRailBlock` 有两个构造函数：
- `PoweredRailBlock(Properties properties)` - 默认行为可能不正确
- `PoweredRailBlock(Properties properties, boolean isStraight)` - 第二个参数决定铁轨类型

**错误写法**:
```java
public CopperRailBlock(Properties properties, ...) {
    super(properties);  // ❌ 缺少第二个参数
}
```

**正确写法**:
```java
public CopperRailBlock(Properties properties, ...) {
    super(properties, true);  // ✓ true = 动力铁轨，false = 激活铁轨
}
```

**参考来源**: `Modern_Minecarts` 模组的 `CopperRailBlock.java`

---

## 2. 数据包标签文件夹命名问题 (1.21+)

**日期**: 2026-01-21

**症状**:
- 自定义方块/物品的标签不生效
- 客户端不识别自定义铁轨为有效铁轨
- 服务端正常但客户端行为异常（如矿车脱轨或物理不同步）

**根本原因**:
Minecraft 1.21+ 的数据包结构使用**单数形式**的文件夹名：
- ✓ `data/minecraft/tags/block/` (正确)
- ✓ `data/minecraft/tags/item/` (正确)
- ❌ `data/minecraft/tags/blocks/` (错误 - 多了 "s")
- ❌ `data/minecraft/tags/items/` (错误 - 多了 "s")

**错误目录结构**:
```
src/main/resources/data/minecraft/tags/
├── blocks/          ❌ 错误
│   └── rails.json
└── items/           ❌ 错误
    └── rails.json
```

**正确目录结构**:
```
src/main/resources/data/minecraft/tags/
├── block/           ✓ 正确
│   └── rails.json
└── item/            ✓ 正确
    └── rails.json
```

**注意**: 这与旧版本 (1.20.x) 可能不同，升级时需特别注意检查。

---

## 贡献指南

遇到类似的重大问题时，请按以下格式添加记录：

```markdown
## [序号]. [问题简述]

**日期**: YYYY-MM-DD

**症状**:
- 观察到的具体表现

**根本原因**:
问题的技术原因

**错误写法**:
[代码示例]

**正确写法**:
[代码示例]

**参考来源**: (可选)
```
