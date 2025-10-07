# AREX Plugin for Eva Adapter

Custom AREX agent plugin for capturing and replaying traffic in the `com.eva.adapter` package and methods annotated with `@UseObjectPhase`.

## 🎯 Features

- **Package-based Capture**: Automatically records all public methods in `com.eva.adapter` package
- **Annotation-based Capture**: Records any method annotated with `@UseObjectPhase` across your entire codebase
- **Smart Filtering**: Excludes getters, setters, constructors, and synthetic methods
- **Exception Handling**: Captures both successful results and exceptions
- **Debug Support**: Comprehensive logging for troubleshooting

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- [AREX Agent](https://github.com/arextest/arex-agent-java) installed locally
- AREX Storage Service running

## 🚀 Quick Start

### 1. Install AREX Agent

```bash
git clone https://github.com/arextest/arex-agent-java.git
cd arex-agent-java
mvn clean install -DskipTests
```

### 2. Build This Plugin

```bash
git clone https://github.com/YOUR_USERNAME/arex-plugin-eva-adapter.git
cd arex-plugin-eva-adapter
mvn clean package
```

### 3. Deploy Plugin

```bash
# Create extensions directory if it doesn't exist
mkdir -p /path/to/arex-agent-jar/extensions

# Copy plugin JAR
cp target/arex-plugin-eva-adapter-1.0.0.jar /path/to/arex-agent-jar/extensions/
```

### 4. Configure Your Application

Add these VM options when starting your application:

```bash
-javaagent:/path/to/arex-agent-jar/arex-agent-0.4.8.jar
-Darex.service.name=your-service-name
-Darex.storage.service.host=localhost:8093
-Darex.enable.debug=true
```

### 5. Create the Annotation

Add this annotation to your project:

```java
package com.eva.adapter;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseObjectPhase {
}
```

## 📦 What Gets Captured

### Package-based Capture

All public methods in `com.eva.adapter.*`:

```java
package com.eva.adapter;

public class UserAdapter {
    // ✅ Captured
    public User getUser(String id) {
        return userRepository.findById(id);
    }
    
    // ✅ Captured
    public List<User> findUsers(SearchCriteria criteria) {
        return userRepository.search(criteria);
    }
    
    // ❌ Not captured (getter)
    public String getName() {
        return name;
    }
    
    // ❌ Not captured (private)
    private void internalMethod() {
        // ...
    }
}
```

### Annotation-based Capture

Any method with `@UseObjectPhase` (anywhere in your codebase):

```java
package com.example.service;

import com.eva.adapter.UseObjectPhase;

public class PaymentService {
    @UseObjectPhase
    public Payment processPayment(PaymentRequest request) {
        // This method will be captured
        return paymentGateway.process(request);
    }
}
```

## 🔍 Verification

### Check Plugin Loading

When your application starts, look for:

```
[arex] installed instrumentation module: plugin-eva-adapter
```

### Verify Recording

Make a request and check logs:

```
[AREX] Recorded com.eva.adapter.UserAdapter.getUser
[AREX] Recorded @UseObjectPhase method: com.example.service.PaymentService.processPayment
```

### Test Replay

Add the `arex-record-id` header:

```bash
curl -H "arex-record-id: YOUR-RECORD-ID" \
     http://localhost:8080/api/endpoint
```

Check logs for:

```
[AREX] Replayed com.eva.adapter.UserAdapter.getUser
```

## 🎛️ Customization

### Exclude Specific Methods

Edit `EvaAdapterPackageInstrumentation.java`:

```java
ElementMatcher<MethodDescription> matcher = isPublic()
        .and(not(isConstructor()))
        .and(not(isGetter()))
        .and(not(isSetter()))
        .and(not(named("healthCheck")))  // Add exclusions
        .and(not(nameStartsWith("internal")));
```

### Capture Only Specific Subpackages

```java
@Override
public ElementMatcher<TypeDescription> typeMatcher() {
    return nameStartsWith("com.eva.adapter.external")
            .or(nameStartsWith("com.eva.adapter.api"))
            .and(not(nameStartsWith("io.arex")));
}
```

### Add Custom Metadata

In `EvaAdapterMethodAdvice.java`:

```java
private static Mocker buildMocker(String className, String methodName, Object[] args) {
    String operationName = className + "." + methodName;
    Mocker mocker = MockUtils.createDynamicClass(operationName);
    
    // Add custom attributes
    mocker.getTargetRequest().setAttribute("environment", System.getProperty("env"));
    mocker.getTargetRequest().setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
    
    return mocker;
}
```

## 🐛 Debugging

### View Transformed Bytecode

With `-Darex.enable.debug=true`, check:

```bash
ls arex-agent-jar/bytecode-dump/com/eva/adapter/
```

### Debug in IDE

1. Import AREX agent project to your IDE
2. Import this plugin project to your IDE
3. Set breakpoints in Advice classes
4. Run your application in debug mode

### Enable Verbose Logging

Add more logging in Advice classes:

```java
System.out.println("[AREX-DEBUG] Recording: " + className + "." + methodName);
System.out.println("[AREX-DEBUG] Arguments: " + Arrays.toString(args));
```

## 📊 Performance Considerations

- Exclude high-frequency methods (healthchecks, metrics)
- Monitor serialization overhead for large objects
- Use exclusion patterns to reduce instrumentation scope
- Test thoroughly in non-production environments first

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🔗 Related Projects

- [AREX Agent](https://github.com/arextest/arex-agent-java) - Main AREX agent
- [AREX Storage](https://github.com/arextest/arex-storage) - Storage service
- [AREX Documentation](https://doc.arextest.com) - Official documentation

## 📧 Contact

For questions or support:
- Create an issue in this repository
- Join AREX QQ Group: 656108079
- Check [AREX Documentation](https://doc.arextest.com)

## 🙏 Acknowledgments

- AREX Team for the amazing testing framework
- ByteBuddy for bytecode manipulation capabilities
