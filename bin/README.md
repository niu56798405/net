## nebula-kernal
network io, ioc, modular, action

#### IO
```java
NetServer server = new NetServer(Port, LifecycleListener, CommandContext, MessageInterceptor);

NetCleint client = new NetClient(LifecycleListener, CommandContext, HearbeatMessage);
Session session = client.connect(host, port, sessionId);

Session //每一个连接会封装成一个Session给应用程序使用
	.sendMessage() //sendMessageAndClose()
	.reconnect()   //client session
	
Command .execute(Session, IMessage) //每一个请求会转换成一个Command调用

IMessage: id, code, body  //每一个请求内容转换成一个IMessage对象
```

#### IOC
```java
Injections: //按以下先后顺序加载(@Bean, @Prototype同时加载)
    @Factory           //标识为一个工厂类,根据@Factory.value的Annotation来生成对应的对象
    @Configurator      //配置 优先于其他Bean加载
    @Repository        //数据库
    @Tempaltes         //模板数据(静态数据)
    @Bean              //单例
    @Prototype         //Prototype, 使用new关键字(构造函数中增加一行代码,该Class不能静态加载其他@Prototype Class)
    @Eventual          //加载完其他所有Bean之后加载
    
    @Inject            //使用该注释的字段才会被注入相关Bean
    @InjectDependence  //加载的依赖关系(@Bean中依赖@Prototype时需要,使用new关键字暂时无法分析得到)
    Loadable           //实现该接口的Bean会在实例化之后调用load()方法, 暂时@Prototype不支持该方法

@Modular @Cmd @Http    //可以使用@Inject来注入相关Bean
```

#### Module
```java
@Modular(ModularType) 标识一个类为Module, 由ModuleContainer加载和卸载
	每一个业务模块对应一个@Modular类(Module)

Module.share() 共享一个接口, 其他Module只能通过该接口访问该Module的相关内容

ModularCommand 继承该类可以通过泛型(方法参数)注入对应的Module实例
```

#### Action
```java
ActionExecutor(基于ThreadsPoolExecutor),用于真正执行Action的Threads Pool
                            //复杂的(:战斗)或者需要在同一线程中处理(:交易)的逻辑可以创建一个ActionExectuor.
ActionQueue (Executor)      //队列保证每一个Action的执行顺序(例如:每一个玩家需要一个队列,同一场战斗需要一个队列)
Action (ActionQueue, delay) //真正执行的主体
	.checkin()              //放入队列中
	.exec()                 //真实执行的逻辑入口
```