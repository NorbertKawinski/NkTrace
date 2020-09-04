# NkTrace

NkTrace is a Java library for making deep stacktraces easier to read in the log files.

It\'s mainly done by wrapping methods in NkTrace blocks.
```
try(NkTrace trace = NkTrace.info(logger)) { 
    logger.info("Some code here...");
}
```
Such block automatically creates "entry" and "exit" log entries.
Additionally, all logs inside this block will be automatically indented to easily follow the span of the function.
Resulting logs:
```
 INFO >> ExampleTest.example:42
 INFO    Some code here...
 INFO << ExampleTest.example:42
```

You can see more detailed examples in later sections of this readme.

## Download
You can find the binaries here:
- https://github.com/NorbertKawinski/NkTrace/releases

## Setup
### Requirements:
* Java8+
* SLF4J
* SLF4J-compatible logging framework

#### Note on compatibility with SLF4J
Not all SLF4J-compatible frameworks works well with NkTrace as it requires:
* MDC (Mapped Diagnostic Context) support required for indentation feature
* (Optional) Marker support required if you want to use custom entry/exit message patterns

Logback and Log4J implement all these features. 
Before using any other logging framework, please confirm the support for MDC and Markers.

### Compiling NkTrace from sources
Just run the following command in the commandline
```
gradlew clean jar
```
You should see the NkTrace-xxx.jar generated in the ***./build/libs/*** subdirectory.

### Adding NkTrace to your project
* Add NkTrace-x.x.x.x.jar as a dependency

### Setting up standard formatter
To make the indentation work, you\'ll need to configure the message format to include indentation from the MDC.
For example, you can configure the following format in Logback:
```
%date [%thread] %5level %mdc{indent}%msg \(%file:%line\) %n
```
This readme used the following (simplified) format:
```
%5level %mdc{indent}%msg%n
```

### (Optional) Setting up custom formatter (Logback)
Depending on your setup of the pattern layout, you might want to display entry/exit logs differently.
It\'s certainly possible because NkTrace uses special markers "NkTraceEntry" and "NkTraceExit" for its messages.
Thanks to this, you can apply different log format only for these messages.

Since Logback doesn't allow setting different layouts based on the marker, we have to do it ourselves.

Please copy the NkPatternLayout class to your project. This class is located in "test/java/net/kawinski/logging/utils/NkPatternLayout"

This class holds 3 pattern layouts inside and switches on them based on the marker in the logging event.

Example XML appender configuration: 
```
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
        <layout class="net.kawinski.logging.utils.NkPatternLayout">
            <entryPattern>%5level %mdc{indent}Entering %method in %class{0} %msg .\(%file:%line\)%n</entryPattern>
            <regularPattern>%5level %mdc{indent}%msg .\(%file:%line\)%n</regularPattern>
            <exitPattern>%5level %mdc{indent}Exiting %method in %class{0} %msg .\(%file:%line\)%n</exitPattern>
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

calling "example01" produces logs similar to:
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
As you can see, each function is marked with ">>" and "<<" entries along with inner logs being indented.

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

calling "example02" produces logs similar to:
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
