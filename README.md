# NkTrace

NkTrace is a Java library for making deep stack traces easier to read in the log files.  
It\'s mainly done by wrapping methods in NkTrace blocks.
```
try(NkTrace trace = NkTrace.info(logger)) { 
    logger.info("Some code here...");
}
```
Such block automatically creates "entry" and "exit" log entries.  
Additionally, all logs inside this block will be automatically indented to easily follow the span of the function.  
With NkTrace, the logs will look like:
```
 INFO >> ExampleTest.example:42
 INFO    Some code here...
 INFO << ExampleTest.example:42
```

You can see more detailed examples in later sections of this readme.  
To see an example project using NkTrace, check out this repository:  
<https://github.com/NorbertKawinski/NkTraceExample>

# Installation
## Add NkTrace dependency
To start using NkTrace, we need to add NkTrace as a dependency.

You can download the binary yourself and add it manually to the classpath  
or use a dependency manager (like Maven or Gradle).

### Manual installation  
Pick a release from the [releases section](https://github.com/NorbertKawinski/NkTrace/releases)  

### Maven
```
<dependency>
    <groupId>net.kawinski.logging</groupId>
    <artifactId>nktrace</artifactId>
    <version>${nktrace_version}</version>
    <scope>compile</scope>
</dependency>
```
List of available NkTrace release versions is available on [Maven Central](https://search.maven.org/artifact/net.kawinski.logging/nktrace)

### Gradle
```
dependencies {
    implementation group: "net.kawinski.logging", name: "nktrace", version: "${nktrace_version}"
}
```
List of available NkTrace release versions is available on [Maven Central](https://search.maven.org/artifact/net.kawinski.logging/nktrace)

## Add SLF4J-compatible logging framework
NkTrace isn\'t a standalone library.  
Since NkTrace depends on SLF4J, you need to choose your preferred logging framework.

Not all SLF4J-compatible frameworks works well with NkTrace, because it requires:
* MDC (Mapped Diagnostic Context) support required for indentation feature
* (Optional) Marker support required if you want to use custom entry/exit message patterns

```Logback``` and ```Log4j``` implement all these features.  
Before using any other logging framework, please confirm the support for MDC and Markers.

## Setting up standard formatter (Logback example)
To make the indentation work, you\'ll need to configure the message format to include indentation from the MDC.

This section will show you Logback example.  
If you are using any other logging framework, check its formatter documentation.

Create ```src/main/resources/logback.xml``` file: 
```
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%date [%thread] %5level %mdc{NkTrace_Indent}%msg \(%file:%line\) %n</pattern>
        </encoder>
    </appender>
    <root level="all">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

Or, if you\'d rather like to configure NkTrace programmatically:
```
final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

final PatternLayoutEncoder ple = new PatternLayoutEncoder();
ple.setPattern("%date [%thread] %5level %mdc{NkTrace_Indent}%msg \\(%file:%line\\)%n");
ple.setContext(lc);
ple.start();

final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
consoleAppender.setEncoder(ple);
consoleAppender.setContext(lc);
consoleAppender.start();

final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
rootLogger.detachAndStopAllAppenders();
rootLogger.addAppender(consoleAppender);
rootLogger.setLevel(Level.ALL);
```

Note: This readme used the following (simplified) layout format:
```
%5level %mdc{NkTrace_Indent}%msg%n
```

### (Optional) Setting up custom formatter (Logback example)
Depending on your setup of the pattern layout, you might want to display entry/exit logs differently.  
It\'s certainly possible because NkTrace uses special markers ```NkTraceEntry``` and ```NkTraceExit``` for its messages.  
Thanks to this, you can apply different log format only for these messages.

Since Logback doesn't allow setting different layouts based on the marker out of the box, we have to do it ourselves.  
Please copy the ```NkPatternLayout``` class to your project. This class is located in ```test/java/net/kawinski/logging/utils``` package  
This class holds 3 pattern layouts inside and switches on them based on the marker in the logging event.

All that\'s left is XML appender configuration: 
```
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
        <layout class="net.kawinski.logging.utils.NkPatternLayout">
            <entryPattern>%5level %mdc{NkTrace_Indent}Entering %method in %class{0} %msg .\(%file:%line\)%n</entryPattern>
            <regularPattern>%5level %mdc{NkTrace_Indent}%msg .\(%file:%line\)%n</regularPattern>
            <exitPattern>%5level %mdc{NkTrace_Indent}Exiting %method in %class{0} %msg .\(%file:%line\)%n</exitPattern>
        </layout>
    </encoder>
</appender>
```

## Example 1
Following code presents the basic features of NkTrace
```
private void example01() {
    try (final NkTrace trace = NkTrace.info(logger)) {
        logger.info("Inside example01");
        example01A();
        example01B();
    }
}

private void example01A() {
    try (final NkTrace trace = NkTrace.debug(logger)) {
        logger.debug("Inside 01A");
    }
}

private void example01B() {
    try (final NkTrace trace = NkTrace.trace(logger)) {
        logger.trace("Inside 01B");
        logger.debug("Before 01C");
        example01C();
        logger.debug("After 01C");
    }
}

private void example01C() {
    try (final NkTrace trace = NkTrace.trace(logger)) {
        logger.trace("Inside 01C");
    }
}
```

calling ```example01``` produces logs similar to:
```
 INFO >> ExampleTest.example01:26
 INFO    Inside example01
DEBUG    >> ExampleTest.example01A:34
DEBUG       Inside 01A
DEBUG    << ExampleTest.example01A:34
TRACE    >> ExampleTest.example01B:40
TRACE       Inside 01B
DEBUG       Before 01C
TRACE       >> ExampleTest.example01C:49
TRACE          Inside 01C
TRACE       << ExampleTest.example01C:49
DEBUG       After 01C
TRACE    << ExampleTest.example01B:40
 INFO << ExampleTest.example01:26
```
As you can see, each function is marked with ```>>``` and ```<<``` entries along with inner logs being indented.

## Example 2
Following code shows how to add custom messages to entry/exit logs.
```
private void example02() {
    try (final NkTrace trace = NkTrace.trace(logger)) {
        logger.info("Received 1: {}", myMultiply1(3.0, 6.0));
        logger.info("Received 2: {}", myMultiply2(3.0, 6.0));
    }
}

private double myMultiply1(final double a, final double b) {
    try (final NkTrace trace = NkTrace.debug(logger, "a: {}, b: {}", a, b)) {
        logger.debug("You can log the return value into the exit trace instead of logging another line");
        final double result = a * b;
        trace.returning(result); // instead of logger.info("Returning({})", result);
        return result;
    }
}

private double myMultiply2(final double a, final double b) {
    try (final NkTrace trace = NkTrace.debug(logger, "a: {}, b: {}", a, b)) {
        logger.debug("You can shorten exit trace and return value into a single line");
        return trace.returning(a * b);
    }
}
```

calling ```example02``` produces logs similar to:
```
TRACE >> ExampleTest.example02:55
DEBUG    >> ExampleTest.myMultiply1:62 a: 3.0, b: 6.0
DEBUG       You can log the return value into the exit trace instead of logging another line
DEBUG    << ExampleTest.myMultiply1:62 returning(18.0)
 INFO    Received 1: 18.0
DEBUG    >> ExampleTest.myMultiply2:71 a: 3.0, b: 6.0
DEBUG       You can shorten exit trace and return value into a single line
DEBUG    << ExampleTest.myMultiply2:71 returning(18.0)
 INFO    Received 2: 18.0
TRACE << ExampleTest.example02:55

```

## License

MIT
