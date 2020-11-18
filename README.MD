# bunch-of-crafts [![JDK8](https://github.com/serge2nd/bunch-of-crafts/workflows/JDK8/badge.svg)](https://github.com/serge2nd/bunch-of-crafts/actions?query=workflow%3A%22JDK8%22) [![JDK11](https://github.com/serge2nd/bunch-of-crafts/workflows/JDK11/badge.svg)](https://github.com/serge2nd/bunch-of-crafts/actions?query=workflow%3A%22JDK11%22) [![JDK15](https://github.com/serge2nd/bunch-of-crafts/workflows/JDK15/badge.svg)](https://github.com/serge2nd/bunch-of-crafts/actions?query=workflow%3A%22JDK15%22) [![Coverage Status](https://coveralls.io/repos/github/serge2nd/bunch-of-crafts/badge.svg?branch=master)](https://coveralls.io/github/serge2nd/bunch-of-crafts?branch=master)
Some Java classes for the general purpose use with *at least* Java 8. Contains such things as:
- Spring bean definition building helpers ([`ru.serge2nd.bean.definition`](./src/main/java/ru/serge2nd/bean/definition))
- Spring bean post-processors ([`ru.serge2nd.bean.processor`](./src/main/java/ru/serge2nd/bean/processor))
- Collection Framework extensions such as abstract extensible unmodifiable containers ([`ru.serge2nd.collection`](./src/main/java/ru/serge2nd/collection/package-info.java))
- Function providers ([`ru.serge2nd.function`](./src/main/java/ru/serge2nd/function))
- Stream API extensions, mainly lightweight collectors and collecting utils ([`ru.serge2nd.stream`](./src/main/java/ru/serge2nd/stream/package-info.java))
- Utility methods working with types ([`ru.serge2nd.type`](./src/main/java/ru/serge2nd/type))
- [`ru.serge2nd.type.TypeWrap`](./src/main/java/ru/serge2nd/type/TypeWrap.java)
- [`ru.serge2nd.ObjectAssist`](./src/main/java/ru/serge2nd/ObjectAssist.java)

## Apply in a project
This library is not yet available in a public repository. Perhaps will be done soon.

## Build
#### Package (with the sources JAR)
```
./mvnw clean package
```
#### Local install (with the sources JAR)
```
./mvnw clean install
```
