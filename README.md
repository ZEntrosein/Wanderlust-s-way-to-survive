# Wanderlust's Way To Survive (漫游者的生存之道)

**Minecraft Version:** 1.21.1  
**Mod Loader:** NeoForge

一个专注于增强生存体验的 Minecraft 模组。

---

## 已实现功能

### 1. 旅者附魔 (Trek Enchantment)

**附魔 ID:** `wanderlusts_way_to_survive:trek`  
**适用装备:** 靴子  
**最大等级:** III

#### 功能描述
在**相同方块材质**上疾跑时，逐渐提升移动速度。速度上限受**玩家经验等级**和**附魔等级**影响。

#### 速度计算公式
```
最终速度上限 = (基础上限 + 经验等级 × XP倍率) × (1 + 附魔等级 × 附魔倍率)
```

#### 特性
- **动量累积**：持续疾跑积累动量，速度逐渐提升
- **材质敏感**：更换脚下方块类型时动量重置
- **方块特定上限**：可为特定方块（如泥土小径、灵魂土、浮冰）配置独立速度上限
- **客户端边缘保护**：高速跑动时防止意外坠落

#### 配置项
| 配置项 | 描述 | 默认值 |
|--------|------|--------|
| `baseSpeedCap` | 基础速度上限 | 0.2 |
| `xpSpeedMultiplier` | 每经验等级增加的速度上限 | 0.005 |
| `enchantmentLevelMultiplier` | 附魔等级速度倍率 | 0.1 |
| `blockSpeedCaps` | 方块特定速度上限列表 | 见配置文件 |

---

### 2. 苦力怕动态爆炸 (Creeper Dynamic Explosion)

#### 功能描述
苦力怕的爆炸威力根据其**当前生命值**动态调整：
- **满血时**：爆炸威力较弱（默认 0.5 倍）
- **残血时**：爆炸威力较强（默认 2.5 倍）

#### 设计理念
鼓励玩家在苦力怕满血时主动引爆以减少破坏，或者将其打残后再引爆以获得更大范围的爆炸效果。

#### 配置项
| 配置项 | 描述 | 默认值 |
|--------|------|--------|
| `enableCreeperScaling` | 启用苦力怕动态爆炸 | true |
| `maxHealthMultiplier` | 满血时爆炸倍率 | 0.5 |
| `minHealthMultiplier` | 残血时爆炸倍率 | 2.5 |

---

### 3. 骑马穿叶 (Horse Leaf Passthrough)

#### 功能描述
骑马时可以**穿过树叶方块**，方便在森林中骑行。但仍然可以从上方**落到树叶上站稳**。

#### 工作原理
- 马站在**非树叶地面**上时 → 可以穿过树叶
- 马站在**树叶或空中**时 → 树叶保持碰撞（可以站住）
- 离开地面后有**宽限期**（默认500ms），在此期间仍可穿过树叶（支持跳跃穿过）

#### 配置项
| 配置项 | 描述 | 默认值 |
|--------|------|--------|
| `enableHorseLeafPassthrough` | 启用骑马穿叶 | true |
| `horseLeafGracePeriod` | 穿叶宽限期（毫秒） | 500 |

### 4. 马匹游泳 (Horse Swimming)

#### 功能描述
允许骑乘的马匹在**深水区**游泳，防止因进入深水而沉底或迫使玩家下马。

#### 工作原理
- 检测马匹是否被骑乘且处于水中
- 如果脚下也是水（深水），给予马匹向上的浮力
- 保持马匹头部在水面之上

#### 配置项
| 配置项 | 描述 | 默认值 | 范围 |
|--------|------|--------|------|
| `enableHorseSwim` | 启用马匹游泳 | true | - |
| `horseSwimMode` | 游泳模式 (运动模式/定高模式) | PHYSICAL | - |
| `horseSwimUpwardDrift` | 游泳上浮速度 (Y轴) | 0.1 | 0.0 - 1.0 |
| `horseSwimHorizontalMultiplier` | 游泳水平速度倍率 | 1.0 | 0.0 - 5.0 |
| `horseSwimDeepWaterCheck` | 启用深水检测（防止浅水乱跳） | true | - |

#### 骑马视角优化
当骑在马背上视角向下移时，马匹会逐渐变透明，提供更好的视野。

| 配置项 | 描述 | 默认值 | 范围 |
|--------|------|--------|------|
| `enableHorseTransparency` | 启用马匹透明化 | true | - |
| `horseTransparencyFadeStart` | 透明化开始角度（开始变淡） | 25.0 | 0.0 - 90.0 |
| `horseTransparencyFadeEnd` | 透明化最大角度（达到最淡） | 45.0 | > Start - 90.0 |
| `horseMinOpacity` | 最小不透明度（最淡状态） | 0.25 | 0.0 - 1.0 |

---

### 5. 骑乘末影珍珠传送 (Mounted Pearl Teleport)

#### 功能描述
骑乘坐骑（马、骆驼等）时使用末影珍珠，**坐骑会跟随玩家一起传送**到目的地，传送后仍保持骑乘状态。

#### 工作原理
1. 检测到玩家骑乘时使用末影珍珠
2. 记录坐骑信息，玩家暂时下马
3. 传送后在数个 tick 内同步坐骑位置
4. 自动让玩家重新骑上坐骑

#### 配置项
| 配置项 | 描述 | 默认值 |
|--------|------|--------|
| `enableMountedPearlTeleport` | 启用骑乘传送 | true |
| `mountedPearlSyncTicks` | 坐骑位置同步 tick 数 | 3 |

---

## 配置文件

配置文件位于：`config/wanderlusts_way_to_survive-common.toml`

支持**中英文双语**配置界面（需要模组菜单支持）。

---

## 技术细节

### 项目结构
```
src/main/java/com/zeno/wanderlustswaytosurvive/
├── WanderlustsWayToSurvive.java    # 模组主类
├── attachment/
│   ├── MomentumData.java           # 动量数据附件
│   └── MountedPearlData.java       # 骑乘传送数据附件
├── config/
│   └── MomentumConfig.java         # 配置类
├── handler/
│   ├── MomentumHandler.java        # 旅者附魔逻辑（服务端）
│   ├── EdgeProtectionHandler.java  # 边缘保护（客户端）
│   ├── MountedPearlHandler.java    # 骑乘传送逻辑
│   └── HorseSwimHandler.java       # 马匹游泳逻辑
├── mixin/
│   ├── CreeperMixin.java           # 苦力怕爆炸 Mixin
│   └── LeavesBlockMixin.java       # 骑马穿叶 Mixin
└── registries/
    ├── ModAttachmentTypes.java     # 数据附件注册
    └── ModEnchantments.java        # 附魔注册
```

### 数据驱动
- 附魔定义：`data/wanderlusts_way_to_survive/enchantment/trek.json`
- 附魔标签：`data/minecraft/tags/enchantment/`

---

## 待开发功能

- [ ] 更多附魔类型
- [ ] 新物品/方块
- [ ] 世界生成特性
- [ ] 更多实体交互

---

## 许可证

[待定]
