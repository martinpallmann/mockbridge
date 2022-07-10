---
layout: default
---

<img src="https://martinpallmann.de/mockbridge/gfx/logo.svg" alt="MockBridge" style="width: 200px;">

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
