# TradeFlow
此项目主要还是本人不太会包装简历，所以想通过项目的方式让招聘方对本人有个了解，
目前项目已经完成了gateway,naocs 以及基于Spring Security结合jwt完成身份验证
后续会加一些高并发场景以及es,redis,mq的应用，有觉得合适的招聘方请联系我
杭州 java后端 吴锦涛 15140649621 


```


## 模块

```text
trade-flow/
├── trade-flow-parent/               # 统一依赖和构建配置
├── trade-flow-server/               # 启动类及后续 HTTP 接口
├── trade-flow-entity/               # 请求、响应、业务模型和枚举
├── trade-flow-common-service/       # 业务接口、实现及辅助组件
├── trade-flow-common-dao/           # 后续正式 Mapper、XML 和 SQL
└── trade-flow-utils/                # 通用工具
```
