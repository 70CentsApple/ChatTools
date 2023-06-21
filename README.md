[>>English<<](README_en.md)

# Chat Tools
Chat Tools 是一个 Minecraft Fabric 模组，为玩家提供众多的实用聊天功能。\
请安装前置模组 [Cloth Config](https://modrinth.com/mod/cloth-config)。

Chat Tools的大部分功能支持高度个性化，请在配置页面进行配置。\
配置页面启动方式（请确保安装好 [Cloth Config](https://modrinth.com/mod/cloth-config) ）：
- 输入指令 `/chattools opengui`
- Chat Tools 与 [Mod Menu](https://modrinth.com/mod/modmenu) 联动，在 Mod Menu 中打开Chat Tools 配置界面

# 功能介绍
## 基本（General Section）
包含模组基本设定
- 显示聊天时间（Show Timestamp）\
在信息前面插入一个时间戳\
![Timestamp](<images/Timestamp.png>)
- 隐藏自己昵称（Nickname Hider）\
在自己视角里隐藏自己真实昵称\
![Nickname Hider](<images/Nickname Hider.png>)
- 最大聊天记录数量（Max History Length）\
调整游戏保留聊天记录上限\
![Max History Length](<images/Max History Length.png>)

## 聊天提醒（Notifier Section）
各种聊天提醒功能
- 后台弹窗提醒（Toast）\
![Toast](<images/Toast.gif>)
- 声音选项（Sound）\
支持自定义音效
- 动作栏选项（Actionbar）\
在动作栏提醒关注的消息
- 高亮选项（Highlight）\
支持自定义高亮前缀（匹配到的消息前面加前缀）
![Highlight Function](<images/Highlight Function.png>)
- 匹配白名单（Allow List）\
列表中的内容将会被匹配
- 匹配黑名单（Ban List）\
列表中的内容将不会被匹配（优先级大于白名单列表）

## 注入聊天（Formatter Section）
使用指定样式格式化自己的消息
- 注入文本（Pattern）\
即自动格式化替换的样式\
例如：\
`&e{text}` 在支持以 & 作为自定义颜色前缀的服务器中将会让您的消息变成金色\
`&e{text} ~(ovo)~` 将额外为您加上个性化后缀（小尾巴）\
`我的坐标是：{pos}` 将为您自动替换 `{pos}` 为当前坐标
- 匹配黑名单（Auto-Disable when matches...）\
在有些情况下，我们**不希望**自己的文本被格式化。\
这些情况包括（但不限于）：\
向箱子商店插件售卖物品时在聊天栏输入的物品数量（或all）；\
以各种特殊字符开头的指令。\
Chat Tools的默认正则表达式字串 `^\d+$|^[.#%$/].*|\ball\b` 即可满足需求，\
当然，您也可以更改或自行添加更多。

## 快捷发言（Chat Keybindings Section）
用快捷键来代替常用的指令
- 一键复读（Trigger Last Command Hotkey）\
按下设置的热键即可将您上一条指令重复一遍\
例如：\
在跑酷地图中 F3+C 记录坐标并发送一次后，此后每按下一次快捷键即可快速回溯到记录点位置。
- 指令宏（Command Keybindings）\
为常用指令设置热键\
![Command Keybindings](<images/Command Keybindings.png>)

## 聊天气泡（Bubble Section）
- 启用聊天气泡（Enable Chat Bubbles）\
在玩家头上渲染聊天气泡\
![Chat Bubbles](<images/Chat Bubbles.png>)
