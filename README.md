# 量股化金

本项目聚焦金融科技赛道，研发“量股化金 AI 股市量化预测系统”，核心亮点在于将自然物理定理与 LSTM、Transformer 多模型融合算法深度耦合，创新构建“物理约束 + 数据驱动”的智能预测体系，力求打造 “低门槛、高精准、全场景” 的股市决策解决方案。

------------

## 核心亮点

+ **LSTM 模型预测：**集成了基于 PyTorch 实现的 LSTM (长短期记忆网络) 模型，专门用于处理时间序列数据，预测未来 30 天的股票价格走势。
+ **微服务架构：**预测模型被封装为独立的 FastAPI 微服务，与 Java 后端解耦，既保证了高性能计算的独立性，又便于模型迭代更新。
+ **数据清洗与归一化：**实现了完善的数据预处理流程（归一化/反归一化），确保模型输入的准确性。
+ **分布式锁机制：**引入 Redisson 分布式锁，在处理订单撤销、交易撮合等关键业务时，防止并发导致的超卖、资金扣减错误等“脏数据”问题。
- **乐观锁设计：**在数据库层面，对资金账户和持仓表使用 乐观锁 （版本号机制），在保证数据强一致性的同时，最大化系统的并发吞吐量。
- **内存撮合队列：**使用 OrderQueueManager 将订单异步放入内存队列，削峰填谷，平滑处理高并发下单请求。
- **多源数据采集：**灵活对接 AKShare（开源财经数据接口）和其他实时 API，获取包括日/周/月 K 线、分时数据、个股新闻等多维度信息。
- **Elasticsearch 全文检索：**引入 Elasticsearch 搜索引擎，实现了对 A 股 5000+ 股票的毫秒级全文检索（支持代码前缀、名称模糊搜索），极大提升了用户体验。
- **多级缓存策略：**核心数据（如实时股价、用户自选股）采用 Redis 缓存，并针对高频访问的接口（如分时数据）实现了本地缓存或短时 Redis 缓存，减轻数据库压力。
- **互动生态：**内置博客系统，支持用户发表投资心得、评论互动、点赞，形成了“看行情 -> 做交易 -> 聊心得”的闭环。



## 技术栈

-------------------------

+ **后端：**SpringBoot 2.7.12 + Fastapi
+ **数据库：**MySQL、Timescale(后期升级引入)
+ **缓存：**Redis
+ **前端：**Vue3、Vite、ElementUI
+ **搜索：**Elasticsearch
+ **消息队列：**RocketMQ(后期升级引入)
+ **表格处理：**EasyExcel
+ **预测模型：**Pytorch



## 快速开始

------------------------------

### 1. 环境准备

确保您的系统中存在 JDK11、Python3.12、Node.js、MySQL、PostgerSQL



### 2. 克隆与安装

在 feng_lghj 系统中编译 maven 文件

将项目拉取到本地，在 stockPredict 根目录下安装依赖

```cmd
# 安装依赖
pip install -r requirements.txt
```

在 lghj_web 中安装项目依赖【报错就切换为管理员权限】

```cmd
# 安装依赖
npm install
```



### 3. 项目配置

+ 数据库：打开 feng-lghj/lghj-server 下的 application.yml 根据其配置 MySQL 与 timescale的数据库信息，修改为您的配置信息

​	运行 sql 目录下的 init.sql 与 add.sql 初始化 MySQL 数据库

+ 在 Linux 虚拟机中拉取 Elasticsearch、kibana 镜像并部署在同一网络中（推荐使用 docker）

+ Redis 可选在 windows 中运行或部署在 Linux 中



### 4. 运行项目

在 IDE 中运行 LiangGuHuaJinApplication.java、prediction_service.py、stock_data_service.py

优先使用管理端的A股导入功能进行数据库A股基础信息导入、以及 Elasticsearch 导入

或者启动、登录后访问 localhost:8080/api/admin/stock/import、localhost:8080/api/admin/stock/sync-es

前端在根目录下运行

```
npm run dev
```



>本项目仅为学习使用，预测结果仅供参考
>
>尚有缺陷后续将持续优化