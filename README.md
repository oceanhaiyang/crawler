# crawler
简单的京东爬虫

仅以手机商品为例：
主要包括 id，title，image，price和sellpoint

难点：price和sellpoint是通过异步回显的，不能通过简单的解析页面标签获取

需要通过jackson解析，使用hashmap将解析的price值放进相应的id中。


改进：
1.后期需要线程池进行多个爬虫并发，
2.将数据保存到数据库中

