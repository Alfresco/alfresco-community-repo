# alfresco-community-repo

[![Build Status](https://travis-ci.com/Alfresco/alfresco-community-repo.svg?branch=master)](https://travis-ci.com/Alfresco/alfresco-community-repo)

This project contains the bulk of the [Alfresco Content Services Repository](https://community.alfresco.com/docs/DOC-6385-project-overview-repository) code.

To make the development process simpler, it brings together code historically in `alfresco-core`,
`alfresco-data-madel`, `alfresco-repository`, `alfresco-remote-api` and repository specific
tests and war file creation from `acs-community-packaging`. They exist as sub projects within the Maven Reactor and still
create the same artifacts.

#### Alfresco Core

Alfresco Core is a library packaged as a jar file which contains the following:
* Various helpers and utils
* Canned queries interface and supporting classes
* Generic encryption supporting classes

Version 7 of the library uses Spring 5, Quartz 2.3 and does not have Hibernate dependency.

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

Tests are combined into test classes split by test type or Spring application context used in the test, see classes
in _src/test/java/org/alfresco_. All of these classes as well as individual tests can be run by specifying the test
class name and a set of DB connection properties. Check the travis.yml file for docker images that should be started
to provide a sutable test environment. For example:
~~~
mvn clean test -Dtest=SomeRepoTest -Ddb.driver=org.postgresql.Driver -Ddb.name=alfresco -Ddb.url=jdbc:postgresql:alfresco -Ddb.username=alfresco -Ddb.password=alfresco
~~~

#### Alfresco Remote API

Remote API is a library packaged as a jar file which contains the following:
* REST API framework
* WebScript implementations including [V1 REST APIs](https://community.alfresco.com/community/ecm/blog/2017/05/02/v1-rest-api-10-things-you-should-know)
* [OpenCMIS](https://chemistry.apache.org/java/opencmis.html) implementations

Like the `alfresco-repository` tests are combined in test classes split by test type or Spring application context used
in the test.

#### Artifacts
The artifacts can be obtained by:
* downloading from [Alfresco maven repository](https://artifacts.alfresco.com/nexus/content/groups/public)
* getting as Maven dependency by adding the dependency to your pom file:
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

All current source versions are held in github. Historic versions can be found in [Alfresco SVN](https://svn.alfresco.com/repos/alfresco-open-mirror/services/alfresco-core/)

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.

## Setting up your development environment
Although it is possible to work on individual github projects, we recommend working on
the `alfresco-community-repo`, `alfresco-enterprise-repo`, `acs-packaging` and `acs-community-packaging`
in a single Intellij IDEA project. They depend on each other and typically you 
will want to make changes to all of them if you are changing the repository code.
In the case of older branches, there is generally no need for the `acs-community-packaging` as you will
not be creating a community release.


Although it is possible to work on individual github projects, we recommend working on the `alfresco-community-repo`
and `acs-community-packaging` in a single Intellij IDEA project. They depend on each other and typically you will
want to make changes to both of them if you are changing the repository code.

~~~
$ mkdir work
$ cd work
$ git clone git@github.com:Alfresco/alfresco-community-repo.git
$ git clone git@github.com:Alfresco/acs-community-packaging.git
~~~
If you wish to build these projects from the command line, use the following commands.
~~~
$ cd alfresco-community-repo
$ mvn clean install -PcommunityDocker -DskipTests=true -Dversion.edition=Community
$ cd ..

$ cd acs-community-packaging
$ mvn clean install -PcommunityDocker -Dmaven.javadoc.skip=true
$ cd ..
~~~
In Itellij IDEA, create a new project using the `work` directory as the source.
* File > New Project from Existing Sources > .../work > Maven

## Branches
As multiple projects have been combined, branch names use the ACS version they are targeting.
For example the code used to create the repository in ACS 6.2.1 in a branch called `releases/6.2.1`.

The actual version number of the **repository artifacts** created by `alfresco-community-repo` are however different.
For example `release/6.2.1` artifacts are `7.183.x`. This adds some complexity, but ensures that
version numbers do not go backwards in existing releases. It also provides some level of 
independence between the repository and other ACS components.
