# 项目介绍
仿牛客网实现的社区论坛项目，不仅实现了用户注册、登录、帖子发表、评论、点赞、回帖等基本功能，同时使用了前缀树实现了敏感词过滤功能，并且使用了elasticsearch实现帖子高亮搜索，使用wkhtmltopdf生成长图，对网站的UV以及DAU进行统计，还使用了七牛云服务器存储用户头像。

# 功能简介
1、使用Spring Security 做权限控制，替代拦截器的拦截控制，并使用自己的认证方案替代Security 认证流程，使权限认证和控制更加方便灵活。  
2、使用Redis的set实现点赞，zset实现关注，并使用Redis存储登录ticket和验证码，解决分布式session问题。  
3、使用Redis高级数据类型HyperLogLog统计UV(Unique Visitor),使用Bitmap统计DAU(Daily Active User)。  
4、使用Kafka处理发送评论、点赞和关注等系统通知，并使用事件进行封装，构建了强大的异步消息系统。  
5、使用Elasticsearch做全局搜索，并通过事件封装，增加关键词高亮显示等功能。  
6、对热帖排行模块，使用分布式缓存Redis和本地缓存Caffeine作为多级缓存，避免了缓存雪崩，将QPS提升了20倍(10-200)，大大提升了网站访问速度。并使用Quartz定时更新热帖排行。  
# 开发环境
| 工具 | 版本号 | 地址 |
| --- | --- | --- |
| JDK | 13 | [https://openjdk.java.net/install/](https://openjdk.java.net/install) |
| Mysql | 5.5 | [https://www.mysql.com/](https://www.mysql.com) |
| Redis | 3.2 | [https://redis.io/download/](https://redis.io/download) |
| Elasticsearch | 6.4.3 | [https://www.elastic.co/downloads](https://www.elastic.co/downloads) |
| Kafka | 2.3.0 | [https://kafka.apache.org/downloads](https://kafka.apache.org/downloads) |
| nginx | 1.10 | [http://nginx.org/en/download.html](http://nginx.org/en/download.html) |

# 后续更新功能
1、增加帖子收藏以及帖子包含图片发布功能  
2、增加用户之间的对话框功能  
