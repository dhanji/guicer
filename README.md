Interesting Guice modules.


# Options

Type-safe configuration module that allows multiple sources of config to be collected and exposed in a Guice project. Primarily: Yaml files, .properties files and Unix-style command-line flags.

```
./java MyGuiceApp --a_flag=1 --another_flag=true --third_flag=a,b,c
```

Is bound and exposed as:

```java
@Options
public class Config {
  int aFlag();
  boolean anotherFlag();
  Set<String> thirdFlag();
}
```

Similarly, you can use a YAML file for custom environment configuration:

```yaml
staging:
  a_flag: 1
  another_flag: true
  third_flag: a,b,c
  
production:
  a_flag: 3
  another_flag: false
  third_flag: a,b,c,d,e,f
```

# Stats

Provides a way for Guice objects to expose internal statistics as a snapshot. The snapshots can either be exposed at a URL via the provided Guice Servlet module or be posted to an external service (such as Graphite or Librato) via a Broadcast module.

Standard publishers are provided for JSON, text and HTML. They can be extended via a simple interface for any format.

```java
public class MyService {
  @Stat("rpc-call-rate")
  private AtomicInteger rpcCalls;
}
```

Broadcast module:
```java
install(new StatBroadcastModule("http://graphite.url",
                                StatBroadcastModule.basicAuthOf("user", "pass"),
                                new GraphiteJsonPublisher(),
                                TimeUnit.SECONDS.toMillis(30));  // publish every 30 sec
```

