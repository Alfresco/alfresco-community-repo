# alfresco-community-repo

[![Build Status](https://github.com/Alfresco/alfresco-community-repo/actions/workflows/master_release.yml/badge.svg?branch=release/7.2.1)](https://github.com/Alfresco/alfresco-community-repo/actions/workflows/master_release.yml)

#### Alfresco Core

Alfresco Core is a library packaged as a jar file which contains the following:
* Various helpers and utils
* Canned queries interface and supporting classes
* Generic encryption supporting classes

#### Alfresco Data Model
Data model is a library packaged as a jar file which  contains the following:
* Dictionary, Repository and Search Services interfaces
* Models for data types and Dictionary implementation
* Parsers

#### Alfresco Repository

Repository is a library packaged as a jar file which contains the following:
* DAOs and SQL scripts
* Various Service implementations
* Utility classes

#### Alfresco Remote API

Remote API is a library packaged as a jar file which contains the following:
* REST API framework
* WebScript implementations including [V1 REST APIs](https://community.alfresco.com/community/ecm/blog/2017/05/02/v1-rest-api-10-things-you-should-know)
* [OpenCMIS](https://chemistry.apache.org/java/opencmis.html) implementations

#### Artifacts
The artifacts can be obtained by:
* downloading from [Alfresco maven repository](https://artifacts.alfresco.com/nexus/content/groups/public)
* as Maven dependency by adding the dependency to your pom file:
~~~
<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>alfresco-core</artifactId>
  <version>version</version>
</dependency>

<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>alfresco-data-model</artifactId>
  <version>version</version>
</dependency>

<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>alfresco-repository</artifactId>
  <version>version</version>
</dependency>

<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>alfresco-remote-api</artifactId>
  <version>version</version>
</dependency>

<dependency>
    <groupId>org.alfresco</groupId>
    <artifactId>content-services-community</artifactId>
    <version>version</version>
    <type>war</type>
</dependency>
~~~
and Alfresco maven repository:
~~~
<repository>
  <id>alfresco-maven-repo</id>
  <url>https://artifacts.alfresco.com/nexus/content/groups/public</url>
</repository>
~~~
The SNAPSHOT versions of the artifact are not published.

## Setting up and building your development environment
See the [Development Tomcat Environment](https://github.com/Alfresco/acs-community-packaging/tree/master/dev/README.md)
page which will show you how to try out your repository changes in a local tomcat instance.
If you wish to use Docker images, take a look at the aliases ending in `D` and the docker-compose files in this
project's test modules.    

## Branches
This project has a branch for each ACS release. For example the code in ACS 6.2.1 is a
branch called `releases/6.2.2`. In addition to the original 6.2.2 release it will also contain Hot Fixes
added later. The latest unreleased code is on the `master` branch. There are also `.N` branches, such as 
`releases/7.1.N` on which we gather unreleased fixes for future service pack releases. They do not indicate
that one is planned.

For historic reasons the version of artifacts created on each branch do not match the ACS version.
For example artifact in ACS 7.2.0 will be `14.<something>`.

The enterprise projects which extend the `alfresco-community-repo` use the same branch names and leading
artifact version number.

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.