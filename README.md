# Mall项目

是一套电商系统，包括 前台商城系统及后台管理系统



前台商城系统包含首页门户、商品推荐、商品搜索、商品展示、购物车、订单流程、会员中心、客户服务、帮助中心等模块。

后台管理系统包含商品管理、订单管理、会员管理、促销管理、运营管理、内容管理、统计报表、财务管理、权限管理、设置等模块。 



### 应用到的技术框架有：

1. 首先这个项目是**基于 `Spring Boot，MySQL，Mybatis` 开发**的

2. 然后对于**登录来说使用了 `JWT` ,以及`百度的人脸识别功能`**

3. 在做**缓存和分布式锁的时候用到了 `Redisson和Redis`**

4. 做**微服务用到了Zookeeper以及Dubbo**

5. 在**对订单进行异步解耦操作时用到了RabbitMQ**

6. **数据挖掘是配置了Flume，读取数据发送到Kafka上，然后通过Flink获取Kafka的数据，最终将数据发送到Redis中**

7. 最后用**Nginx做了反向代理和负载均衡**

   

















## 一、登录模块（JWT人脸识别）

- 在做用户登录时采用**JWT做登录认证**，通过JWT采用的加密算法生成的token来进行验证。

  - **JSON Web Token**因为在设置的时候已经把时间戳加进去了，因此不支持续签，续签会导致时间更改从而导致token更改

  - JWT和Session都是存储信息的，但是JWT**存储在浏览器端**，**每次请求时附带token**

  - JWT存储在浏览器端，**可以减轻服务器的内存压力**，Session不行

  - JWT本身包含认证信息，因此**一旦信息泄露，任何人都可以获得令牌的所有权限**。为了减少盗用，JWT的**有效期不宜设置太长**。对于某些**重要操作**，用户在使用时**应该每次都进行进行身份验证**。

  - ```java
    token=JWT.create().withAudience(userId).withExpiresAt(date).sign(Algorithm.HMAC256(password));
    
    jwt: //JWT配置文件
      tokenHeader: Authorization #JWT存储的请求头
      #secret: mall-admin-secret #JWT加解密使用的密钥
      expiration: 604800 #JWT的超期限时间(60*60*24) 24小时
      tokenHead: Bearer@ #JWT负载中拿到开头
    ```



- 使用**百度人脸识别**完成人脸识别功能，通过**单例的饿汉模式的DCL**（double check lock）加载百度SDK的**APPID、API Key和Secret Key**。

  - **单例DCL**：**volatile，getInstance() null synchronized null** 

  - ```java
    JSONObject resultObject=aipFace.search(faceBean.getImgdata(),"BASE64",groupList,null);
            AIBaiduFaceBean faceSearchResponse=JSON.parseObject(resultObject.toString(), AIBaiduFaceBean.class);
            if((faceSearchResponse.getError_code().equals("0"))&&(faceSearchResponse.getError_msg().equals("SUCCESS"))){
            	if(faceSearchResponse.getResult().getUser_list().get(0).getScore()>80f){
    
    ```







## 二、功能模块（红包，点赞）



- 通过Redis分布式锁实现商家与用户之间的**收、发红包功能**、以及实现用户给**商品的点赞、取消点赞功能**。

  - **二倍均值法：**
    二倍均值算法的核心思想是根据**每次剩余的总金额M**和**剩余人数N**,**执行M/N再乘以2**的操作**得到一个边界值E**,然后制定一个从**0到E的随机区间**,在这个随机区间内将**产生一个随机金额R**,此时**总金额M将更新为M-R**,**剩余人数N更新为N-1**,再**继续重复**上述执行流程,以此类推,**直至最终剩余人数N-1为0**,即代表随机数已经产生完毕。 

  - **点赞防止并发点赞，导致点赞数出现问题**

  - **如果客户端获取到锁之后因为某些原因宕掉而没有去释放锁，一定时间之后锁应该要自动释放，避免死锁**

  - **setnx会导致其他进程不能获得锁，在一个循环中不断的尝试SetNX操作，设置ex可以预防简单死锁**

  - **Redisson的rlock**，使用与lock类似，相较于Redis分布式锁的优化为：

    - **WatchDog**机制，自动延续锁生命周期
    - 使用**lua**脚本保证了性能的同时，保证了整体的**原子性**
    - **Redisson为可重入锁**
      - ``1、Redisson存储锁的数据类型是Hash类型``
      - `2、Hash数据类型的key值包含了当前线程信息。`
      - 当前线程来获取锁可以直接进入，而不需要原地等待
    - **Redisson分布式锁缺点：**
      - 刚在**master节点加锁后**，master会**异步复制给slave**节点，此时**master节点宕机**，主从切换，**slave变为master**，此时客户端2尝试加锁时，**仍能在新的master加锁成功**，导致出现各种脏数据
      - 哨兵和主从模式下都会产生该情况
      - Redisson的分布式锁的数据类型为Hash

  - ZooKeeper基于Watcher机制，实现分布式锁

  - **如果redis宕机，会导致请求全部打在数据库，而且对于使用redis的分布式锁会全部失效（单点问题）**

  - ```java
    @Transactional(rollbackFor = Exception.class)//出现exception的子类时回滚
    @Async//异步
    RLock rLock = redissonClient.getLock(lockKey);
    rLock.lock(10, TimeUnit.SECONDS);
    ```









## 三、商品品牌订单管理（反射，缓存，分页，返回类型类）



### 3.1、反射

- 通过**反射机制**处理了99%相似的Dao层调用方法。

  - 对每个商品增加**会员价格 阶梯价格 满减价格 库存信息等信息时**，由于**插入的方法名相同**，商品表中对应的**价格列表获取相应的类**，**分别到*每个表*中插入该商品的id和productId**
  - 在JVM的层面，java的对象引用不仅要可以直接或间接的接触到对象类型，更应该**可以根据索引能得到这个对象的类型数据**（对象的Class对象）。这样的JVM设计使得JAVA可以拥有反射功能。
  - **反射可以绕过类的访问权限而直接访问到方法**，对于**IDEA**等集成编译器，就是通过反射，来获取到当前对象可实现的方法等，然后通过前端页面展示出来，以供我们选择
  - 虽然反射机制很灵活，但是在**性能上却带来了一些开销**，
    - 在反射调用方法的例子中，我们先后调用了Class.forName,Class.getMethod,以及Method.invoke 三个操作。其中Class.forName 会调用本地方法，Class.getMethod 会遍历该类的公有方法。如果没有匹配到它还会遍历父级的公有方法，可以知道这两个操作非常耗费时间。

  ```java
  for (Object item : dataList) {
  	Method setId = item.getClass().getMethod("setId", Long.class);
      setId.invoke(item, (Long) null);
      Method setProductId = item.getClass().getMethod("setProductId", Long.class);
      setProductId.invoke(item, productId);
  }//把要增加的商品id 弄进去 自己的id 自动加一 不用管
  Method insertList = dao.getClass().getMethod("insertList", List.class);
  insertList.invoke(dao, dataList);
  ```





### 3.2、Redis缓存

- 利用**Redis做分布式缓存**，在对商品进行搜索时，使用缓存，增加用户体验。

  - 在重复获取品牌列表时使用Cacheable注解来进行缓存，在使用时要注意在主启动类上加上@EnableCaching

  - 缓存实际上使用的string，对应的键和值为当前查询数据的json格式，可以直接从缓存中拿到

  - 使用sha256Hex进行加密

    ```Java
    在品牌管理页面，使用redis缓存，首先判断搜索框的数据是否是null，
        如果不是缓存的值为 ： keyword+'-'+#pageNum+'-'+#pageSize
        如果是 ： #pageNum+'-'+#pageSize
        
    缓存值内容为CommonPage类型
    public class CommonPage<T> implements Serializable {
        private Integer pageNum;//当前页号
        private Integer pageSize;//每页数据数量
        private Integer totalPage;//总共多少页
        private Long total;//总数据量
        private List<T> list;//数据
    ```






### 3.3、PageHelper分页

- 使用**PageHelper**对于需要的订单列表进行物理分页。

  - 在Mybatis配置文件中，bean.setPlugins(pageInterceptor);

    - ```Java
      PageInterceptor pageInterceptor=new PageInterceptor();
      Properties properties=new Properties();
      properties.setProperty("helperDialect","mysql");//
      properties.setProperty("offsetAsPageNum", "false");
      properties.setProperty("rowBoundsWithCount", "false");
      properties.setProperty("reasonable", "false");
      pageInterceptor.setProperties(properties);
      ```






### 3.4、自定义返回类型

- 定义**CommonResult、CodeMsg以及CommonPage**等类，统一方法返回值及类型，便于管理项目。

  - ```Java
    public class CommonResult<T> {
        private long code;
        private String message;
        private T data;
    
    ```





### 3.5、Rabbit自动取消订单

使用**RabbitMQ**来对订单消息进行处理，在自动完成**订单超时取消功能**。

- 场景描述：当用户下单后，状态为待支付，假如在规定的过期时间内尚未支付金额，那么就应该设置订单状态为取消。在不用MQ的情况下，我们可以设置一个定时器，每秒轮询数据库查找超出过期时间且未支付的订单，然后修改状态，但是这种方式会占用很多资源，所以在这里我们可以利用RabbitMQ的死信队列。

- 使用MQ的死信队列，每个订单设置超时时间，**超过超时时间**时，将**消息发送到死信交换机**，然后**再转发到死信队列**中，然后到数据库中查询当前订单状态，如果未支付，那么更新订单的状态为取消状态

- 其实对于延迟消息队列来说也是队列，满足先进先出的原则，那么此时前一笔数据未被消费，后面的消息都不能被消费

- 解决办法：

  1. 设置所有消息的TTL一致，那么按照时间顺序发送到交换机当中就可以保证数据的正确发送
  2. 使用 **`x-delayed-message`插件**，使用该插件生产者生产数据会先持久化到Mnesia(分布式数据库管理系统)，该插件会尝试确认消息是否过期，如果消息过期，消息会通过**`x-delayed-type`**类型标记的交换机投递至目标队列，供消费者消费。

  

  

  ```Java
  采用direct（直接，当路由key与exchange绑定设置的key完全一致的时候，就将此消息发送到这个queue）
  @RabbitListener（声明了绑定的queue） 标注在类上面表示当有收到消息的时候，就交给 @RabbitHandler 的方法处理，具体使用哪个方法处理，根据 MessageConverter 转换后的参数类型
  ```

  



- 自定义**SystemLog**注解，通过AOP连接点前置后置增强，实现调用**Swagger**来自动生成配置文档。

  - 前置获取时间使用的时**ThreadLocal**来为每个线程保存各自的开始时间，放到**ThreadLocalMap**中存储，以便后续**统计每个操作用时**等信息。

  - ```Java
    如果不想每次都写private  final Logger logger = LoggerFactory.getLogger(当前类名.class); 可以用注解@Slf4j;（Lombox包下）
    @Target({ElementType.METHOD,ElementType.PARAMETER})//该注解应用的场合
    @Retention(RetentionPolicy.RUNTIME)//运行时生效
    @Documented
    public @interface SystemLog {
        //日志名称
        String description() default "";
        //日志类型
        LogType type();
    }
    @Before("controllerAspect()")
        public void before(JoinPoint joinPoint){//得到controller方法允许之前 取出开始时间
            Date beginTime=new Date();
            beginTimeThreadLocal.set(beginTime);
        }
        
    //多线程池存储日志数据
                ThreadPoolUtil.getPool().execute(new SaveSystemLogThread(log,logService));
    ```

    

- 自定义**UserLoginToken**注解，通过重写**preHandle**，进行登录后检查**token**及其他信息是否正确。

  - **JWT在信息泄露**时会发生不安全的现象，此时就需要**重复验证当前token**是否正确

  - ```Java
    @Override//前置增强
    public boolean preHandle(HttpServletRequest request,                           HttpServletResponse response,Object handler) throws Exception {
    	String token = 		         request.getHeader("Authorization");
    	//从http头部取出token
    	//判断该方法是否经过了token注解的修饰 		//如果不是映射的token注解进行修饰
    	if(!(handler instanceof 					HandlerMethod)){
                return true;
            }
    ```



- 配置**Nginx**服务器实现负载均衡。

  - 使用Nginx负载均衡需要开启并行启动选项，以便开启多个端口以供访问

  - ```Java
    由于使用了nginx服务器，因此我们需要启动nginx服务器，
    
    并且在E:\DOC\java\Nginx-ex\nginx-1.16.1\nginx-1.16.1\conf\nginx.conf的内容和你后端启动两台tomcat服务器的端口一致
    upstream tomcat {
            server 127.0.0.1:8080 weight=10;
            server 127.0.0.1:8081 weight=10;#可以在这里加权重
    }
    location / {
            #root   html;
            #index  index.html index.htm;
            proxy_pass http://tomcat;
            proxy_redirect default;
    }
    ```

    





```
@CrossOrigin//实现了跨域访问
```







## 四、数据挖掘（Flume+Kafka+Flink+Redis）



- 采用Apache **Flume**实现分布式海量日志采集、聚合和传输系统。

  - 在flume配置文件中指定数据库，查询语句，kafka队列名称，队列端口，主题等信息

    - 修改完配置文档后开启监听

    - ```
      $ flume-ng agent --conf conf -n a1 -f app/flume/conf/flume-sink-avro.conf >/dev/null 2>&1 &
      ```

  

- 利用Kafka集群处理大数据消息队列，将数据传到Flink进行数据挖掘。

  - 在我们的`Kafka`中，先创建一个`topic`，用于后面接收`Flume`采集过来的数据
  - FlinkKafkaConsumer的实现会建立一个到Kafka客户端的连接来查询topic的列表和分区。
  - Flink的checkpoint启用之后，Flink Kafka Consumer将会从一个topic中消费记录并以一致性的方式周期性地检查所有Kafka偏移量以及其他操作的状态。
  - Flink将保存流程序到状态的最新的checkpoint中，并重新从Kafka中读取记录，记录从保存在checkpoint中的偏移位置开始读取。
  - 通过定义RedisSink，重写RedisMapper方法来将数据以set的方式加入到redis中，以加快访问
  - 最后在实现改功能的方法中去0号数据库查询相应的key，最终将查询到的数据传给前端，生成图表







### 0、对于Flume中读取数据库中的数据Flink处理数据的原因

通过select * from UMSLog 那么对于每一条数据来说都是只有数据，并不是json格式的键值对，那么就需要对数据进行处理，首先先对UMSlog类取出所有属性，自己通过StringBuffer来实现UmsLog对象的json字符串对象，再通过Google的Gson，对字符串进行还原，还原成UmsLog对象，这样就可以对对象进行处理，取出其中的LogType，以及修改时间（将一天内的数据统计到一起），通过Flink的map方法去对这两个字段的数据进行数据统计，

```Java
//存储当前key对应的userId集合
private MapState<String,Boolean> userIdState;
//存储当前key对应的UV值
private ValueState<Long> uvState;
//userIdState是否不包含当前访问的userId,说明该用户今天还未访问过该页面
if(!userIdState.contains(umsLog.getUserid().toString())){
     userIdState.put(umsLog.getUserid().toString(),null);
     uvState.update(uvState.value()+1);
}
```



返回给Redis的值：

```Java
//生成Redis key 格式为 日期_logType
String redisKey=umsLog.getUpdateTime()+"_"+umsLog.getLogType();
return Tuple2.of(redisKey,uvState.value());
```

然后就可以通过service层通过读取Redis中字符串，

实现**取得指定范围日期和指定类型的出现次数**，**将所有数据保存在List中作为CommonResult返回给前端**







------------------------------------------------






### 1、Flume核心组件

　　Flume主要由3个重要的组件构成：
　　1）Source： 完成对日志数据的收集，分成transtion 和 event 打入到channel之中
　　　　 Flume提供了各种source的实现，包括Avro Source、 Exce Source、 Spooling
　　　　Directory Source、 NetCat Source、 Syslog Source、 Syslog TCP Source、
　　　　Syslog UDP Source、 HTTP Source、 HDFS Source， etc。
　　2）Channel： Flume Channel主要提供一个队列的功能，对source提供中的数据进行简单的缓存。
　　　　 Flume对于Channel， 则提供了Memory Channel、 JDBC Chanel、 File Channel，etc

　　3）Sink： Flume Sink取出Channel中的数据，进行相应的存储文件系统，数据库，或者提交到远程服务器。
　　　　包括HDFS sink、 Logger sink、 Avro sink、 File Roll sink、 Null sink、 HBasesink， etc。





![img](https://images2017.cnblogs.com/blog/999804/201711/999804-20171108162907622-1214907870.png)

这种情况应用的场景比较多，比如要收集Web网站的用户行为日志， Web网站为了可用性使用的负载集群模式，每个节点都产生用户行为日志，可以为
　　每 个节点都配置一个Agent来单独收集日志数据，然后多个Agent将数据最终汇聚到一个用来存储数据存储系统，如HDFS上。









### 2、为什么要集成Flume和Kafka

​        我们很多人在在使用Flume和kafka时，都会问一句为什么要将Flume和Kafka集成？那首先就应该明白业务需求，**一般使用Flume+Kafka架构都是希望完成实时流式的日志处理，后面再连接上Flink/Storm/Spark Streaming等流式实时处理技术，从而完成日志实时解析的目标。**

1. 生产环境中，往往是**读取日志进行分析**，而这往往是**多数据源**的，如果**Kafka构建多个生产者使用文件流的方式向主题写入数据再供消费者消费的话，无疑非常的不方便**。

2. 如果**Flume直接对接实时计算框架，当数据采集速度大于数据处理速度，很容易发生数据堆积或者数据丢失，**而**kafka可以当做一个消息缓存队列，从广义上理解，把它当做一个数据库，可以存放一段时间的数据**。

3. Kafka属于中间件，**一个明显的优势就是使各层解耦，使得出错时不会干扰其他组件**。

 

因此数据从数据源到flume再到Kafka时，数据一方面可以同步到HDFS做离线计算，另一方面可以做实时计算，可实现数据多分发。
