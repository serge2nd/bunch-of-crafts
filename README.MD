# bunch-of-crafts [![LATEST](https://img.shields.io/github/v/release/serge2nd/bunch-of-crafts?color=green&label=latest)](https://serge2nd.jfrog.io/artifactory/maven/ru/serge2nd/bunch-of-crafts/0.9.0) [![SNAPSHOT](https://img.shields.io/github/v/tag/serge2nd/bunch-of-crafts)](https://serge2nd.jfrog.io/artifactory/maven/ru/serge2nd/bunch-of-crafts/0.10.0-SNAPSHOT)
[![JDK8](https://github.com/serge2nd/bunch-of-crafts/workflows/JDK8/badge.svg)](https://github.com/serge2nd/bunch-of-crafts/actions?query=workflow%3A%22JDK8%22)
[![JDK11](https://github.com/serge2nd/bunch-of-crafts/workflows/JDK11/badge.svg)](https://github.com/serge2nd/bunch-of-crafts/actions?query=workflow%3A%22JDK11%22)
[![JDK15](https://github.com/serge2nd/bunch-of-crafts/workflows/JDK15/badge.svg)](https://github.com/serge2nd/bunch-of-crafts/actions?query=workflow%3A%22JDK15%22)
[![Coverage Status](https://coveralls.io/repos/github/serge2nd/bunch-of-crafts/badge.svg?branch=master)](https://coveralls.io/github/serge2nd/bunch-of-crafts?branch=master)

Some Java classes for the general purpose use with *at least* Java 8. Contains such things as:
- Spring bean definition building helpers ([`ru.serge2nd.bean.definition`](./src/main/java/ru/serge2nd/bean/definition))
- Spring bean post-processors ([`ru.serge2nd.bean.processor`](./src/main/java/ru/serge2nd/bean/processor))
- Collections Framework extensions such as abstract extensible unmodifiable containers ([`ru.serge2nd.collection`](./src/main/java/ru/serge2nd/collection/package-info.java))
  and unmodifiable final `Properties` ([`ru.serge2nd.collection.HardProperties`](./src/main/java/ru/serge2nd/collection/HardProperties.java))
- Function providers ([`ru.serge2nd.function`](./src/main/java/ru/serge2nd/function))
- Stream API extensions, mainly lightweight collectors and collecting utils ([`ru.serge2nd.stream`](./src/main/java/ru/serge2nd/stream/package-info.java))
- Utility methods working with types ([`ru.serge2nd.type`](./src/main/java/ru/serge2nd/type))
- [`ru.serge2nd.type.TypeWrap`](./src/main/java/ru/serge2nd/type/TypeWrap.java)
- [`ru.serge2nd.ObjectAssist`](./src/main/java/ru/serge2nd/ObjectAssist.java)
- [`ru.serge2nd.misc.BitsResolver`](./src/main/java/ru/serge2nd/misc/BitsResolver.java)

## Apply in a project
First add the repository:
```xml
<repositories>
    <repository>
        <id>serge2nd-artifactory</id>
        <url>https://serge2nd.jfrog.io/artifactory/maven</url>
    </repository>
</repositories>
```
Then declare the dependency:
- release
```xml
<dependency>
  <groupId>ru.serge2nd</groupId>
  <artifactId>bunch-of-crafts</artifactId>
  <version>0.9.0</version>
</dependency>
```
- snapshot
```xml
<dependency>
    <groupId>ru.serge2nd</groupId>
    <artifactId>bunch-of-crafts</artifactId>
    <version>0.10.0-SNAPSHOT</version>
</dependency>
```
