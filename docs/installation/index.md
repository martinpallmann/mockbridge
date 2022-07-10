---
layout: default
---

# Installation

Maven:
```xml
<dependencies>{% for dependency in site.data.versions %}
    <dependency>
        <groupId>{{ dependency.groupId }}</groupId>
        <artifactId>{{ dependency.artifactId }}</artifactId>
        <version>{{ dependency.version }}</version>
    </dependency>{% endfor %}
</dependencies>
```
