# Structure Maven Plugin

## Overview

This plugin is used to generate a graph of the Maven project structure in JSON format so that it can be analyzed by other applications.

This plugins differs from others in that it does not follow transitive dependencies for artifacts that are not part of the project.

## Usage

```
mvn org.opennms.maven.plugins:structure-maven-plugin:1.0-SNAPSHOT:structure
``` 

## Example output

```
[
  {
    "pom": "/home/jesse/git/opennms/pom.xml",
    "dependencies": [],
    "artifactId": "opennms",
    "groupId": "org.opennms",
    "version": "25.0.0-SNAPSHOT"
  },
  {
    "pom": "/home/jesse/git/opennms/checkstyle/pom.xml",
    "dependencies": [],
    "artifactId": "org.opennms.checkstyle",
    "groupId": "org.opennms",
    "version": "25.0.0-SNAPSHOT"
  },
  {
    "pom": "/home/jesse/git/opennms/dependencies/activemq/pom.xml",
    "parent": {
      "artifactId": "dependencies",
      "groupId": "org.opennms",
      "version": "25.0.0-SNAPSHOT"
    },
    "dependencies": [
      {
        "artifactId": "activemq-karaf",
        "groupId": "org.apache.activemq",
        "version": "5.14.5"
      },
      {
        "artifactId": "activemq-jaas",
        "groupId": "org.apache.activemq",
        "version": "5.14.5"
      },
      {
        "artifactId": "spring-dependencies",
        "groupId": "org.opennms.dependencies",
        "version": "25.0.0-SNAPSHOT"
      },
      {
        "artifactId": "jcl-over-slf4j",
        "groupId": "org.slf4j",
        "version": "1.7.26"
      },
      {
        "artifactId": "log4j-over-slf4j",
        "groupId": "org.slf4j",
        "version": "1.7.26"
      }
    ],
    "artifactId": "activemq-dependencies",
    "groupId": "org.opennms.dependencies",
    "version": "25.0.0-SNAPSHOT"
  },
...
```
