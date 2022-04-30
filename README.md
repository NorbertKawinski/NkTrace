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

Create ```src/main/resources/logback.xml``` file.  
Example configuration can be found in the link below:  
[https://github.com/NorbertKawinski/NkTraceExample/blob/master/src/main/resources/logback.xml](https://github.com/NorbertKawinski/NkTraceExample/blob/master/src/main/resources/logback.xml)

Or, if you\'d rather like to configure NkTrace programmatically, check out below example:  
[https://github.com/NorbertKawinski/NkTraceExample/blob/master/src/main/java/net/kawinski/logging/example/NkTraceExampleManual.java](https://github.com/NorbertKawinski/NkTraceExample/blob/master/src/main/java/net/kawinski/logging/example/NkTraceExampleManual.java)

Note: This readme used the following (simplified) layout format:
```
%5level %mdc{NkTrace_Indent}%msg%n
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
