# work-oa-parent
该仓库只包含了系统的后端代码

•项目描述: 一套权限管理、微信公众号菜单管理、依托微信为载体实现申请和审批的自动化办公系统。
•系统包含平台管理端和微信公众号端:
1. 平台管理端:用户登录、权限管理、个人信息管理、审批管理、公众号菜单管理
2. 微信公众号端:办公审批、微信授权登录、消息推送等功能
•技术选型:SpringBoot、Redis、MybatisPlus、MySQL、SpringSecurity、Acitiviti7、Knife4j

该仓库包含了common、model、service-oa模块
common模块:定义了一系列用于开发应用程序的工具类，比如JWT框架、SpringSecurity的配置、异常处理类、定义了一个利用AOP思想构成的注解用于记录登陆者的操作行为等工具类
model模块:定义应用程序的模块数据类
service-oa：负责接受http请求、处理业务逻辑、访问数据库，以及处理Activiti等等类。
