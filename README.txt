Configuring and starting Alfresco/Share:
----------------------------------------

- Clone the project from git

- Import the project as a maven project

- Start the Alfresco/Share instances with the following commands:

  To start the repo:
  cd rm-community/rm-community-repo
  mvn clean install -Pstart-repo
  
  To start Share:
  cd rm-community/rm-community-share
  mvn clean install -Pstart-share

  NOTE: If you have the enterprise code, see rm-enterprise/README.txt for instructions on how to build/start the enterprise code.


Configuring a different DB other than H2 (e.g. MySQL or PostgreSQL):
--------------------------------------------------------------------

- Create a file called "local.properties" under src/main/resources in rm-community-repo (you may need to create the directory)

- Add the following properties in this new file
  my.db.name -> The name of the database schema
  my.db.port -> The port number for your database (default port number for postgres is 5432 and for mysql it is 3306)

- Run the following commands to start your Alfresco instance:

  to start Alfresco (using Postgres):
  mvn clean install -Pstart-repo,use-postgres

  to start Alfresco (using MySQL):
  mvn clean install -Pstart-repo,use-mysql


Running integration test:
-------------------------

In order to execute the integration tests run the following command (unit tests will be executed every time before you start Alfresco/Share):

mvn clean install -Dskip.integrationtests=false


Running UI Automation tests:
----------------------------

To run the automated UI tests, change to the rm-automation directory and run:

   mvn clean install -Dskip.automationtests=false

Note: due to Selenium Firefox driver changes, the highest supported Firefox version for UI tests is 43.0.4 (with Selenium 2.52.0). 

It is possible to have multiple versions of Firefox installed onto your workstation (e.g. one for running the UI tests and the other, kept
up to date, for everyday browsing) but beware Firefox auto-updates. In this scenario the best approach is to create a non-default profile 
(default profiles will be shared between your Firefox installations!) for which auto-updates are disabled and forcing the use of this 
profile in your tests (-Dwebdriver.firefox.profile="ProfileName"). If your Firefox 43 install isn't in your path, you can use the 
-Dwebdriver.firefox.profile option set to the full path of its "firefox-bin" executable.

MacOS X Sierra users: if you experience by order of magnitude slower performance when connected to a WiFi network (e.g. office WiFi)
add your workstation to your local /etc/hosts file as described on https://github.com/SeleniumHQ/selenium/issues/2824.


Updating License Headers:
-------------------------

In order to refesh out of date license source headers run the following command:

mvn clean install -Dlicense.update.dryrun=false


Running tests against latest Aikau snapshot:
--------------------------------------------

The latest Aikau snapshot can be pulled by running the following command in rm-community:

   mvn clean install -DskipTests -Dalfresco.aikau.version=LATEST -U

Thereafter start the Share instance and run automation tests as described above.


Configuring Outlook Integration:
-------------------------------

To download and run RM with the Outlook Integration AMPs installed on the repo and Share use the following commands:

  mvn clean install -Pstart-repo,outlook-integration
  mvn clean install -Pstart-share,outlook-integration

Follow these instructions install licence and Outlook plugin:
  
  - http://docs.alfresco.com/outlook2.1/tasks/Outlook-license.html
  - http://docs.alfresco.com/outlook2.1/tasks/Outlook-install_v2.html 





SNAPSHOT dependencies:
----------------------

If you're building Enterprise RM, the base project (Community) is pulled in via a snapshot dependency configured in maven.
This dependency will either be loaded from your local .m2 cache or from Nexus if the version in your .m2 doesn't exist or is old
(Old in maven terms is anything over 24hrs old). If maven fetches it from Nexus, your code it's unlikely to be the correct version.
You want to always use the version in your local cache - this means either doing a daily build at the root project level
that pushes a new copy of the correct version into your cache, or alternatively you could run mvn with the
--no-snapshot-dependency (or -nsu) option, which won't try to download a newer version.


Code Formatting:
----------------

This project follows the usual Alfresco Coding Standards. If you use Eclipse or IntelliJ, there are settings inside the ide-config directory for you to import.

Surf build errors:
------------------

If you get: 
[ERROR] Failed to execute goal on project alfresco-rm-community-share: Could not resolve dependencies for project org.alfresco:alfresco-rm-community-share:amp:2.6-SNAPSHOT: Failed to collect dependencies at org.alfresco.surf:spring-surf-api:jar:6.3 -> org.alfresco.surf:spring-surf:jar:${dependency.surf.version}: Failed to read artifact descriptor for org.alfresco.surf:spring-surf:jar:${dependency.surf.version}: Could not transfer artifact org.alfresco.surf:spring-surf:pom:${dependency.surf.version} from/to alfresco-internal (https://artifacts.alfresco.com/nexus/content/groups/private): Not authorized , ReasonPhrase:Unauthorized. -> [Help 1]

then please re-run with  -Ddependency.surf.version=6.3
