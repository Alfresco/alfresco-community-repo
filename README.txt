Configuring and starting Alfresco/Share:
----------------------------------------

- Clone the project (e.g. git clone git@gitlab.alfresco.com:records-management/records-management.git)

- Import the project as a maven project

- Start the Alfresco/Share instances with the following commands:

  mvn clean install -Pstart-repo
  mvn clean install -Pstart-share

  (these commands work best if run from the specific directories, e.g. start share from
  rm-enterprise/rm-enterprise-share/ or rm-community/rm-community-share/ )


Configuring a different DB other than H2 (e.g. MySQL or PostgreSQL):
--------------------------------------------------------------------

- Create a file called "local.properties" under src/main/resources in alfresco-rm-enterprise-repo

- Add the following properties in this new file
  my.db.name -> The name of the database schema
  my.db.port -> The port number for your database (default port number for postgres is 5432 and for mysql it is 3306)

- Run the following commands to start your Alfresco instance:

  to start Alfresco (using Postgres):
  mvn clean install -Pstart-repo,use-postgres

  to start Alfresco (using MySQL):
  mvn clean install -Pstart-repo,use-mysql


Technical documentation:
------------------------

Technical documentation is available at rm-community/documentation/index.md and rm-enterprise/documentation/index.md.
This should be particularly useful for anyone wanting to integrate with or extend RM.


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

To use Chrome instead of Firefox:
   - copy webdriver.properties from https://github.com/AlfrescoTestAutomation/selenium-grid/tree/master/src/main/resources
    - put it under src/test/resource in rm-automation-ui project
    - download the chrome driver from http://chromedriver.storage.googleapis.com/ and extract it
    - change the following properties in webdriver.properties: webdriver.browser (Chrome) and webdriver.chrome.server.path (path/to/chrome/driver)
    - run the tests as usual


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


Install lombok plugin for IDEs:
-------------------------------

To allow automation and benchmark projects to be built within an IDE the lombok 'plugin' needs to be installed.

Execute lombok.jar (doubleclick it, or run java -jar lombok.jar). Follow instructions.


Use Solr 6 with Alfresco 5.2.x:
-------------------------------
In alfresco-global.properties (depending on the RM edition /records-management/rm-community/rm-community-repo/src/test/properties/local or /records-management/rm-enterprise/rm-enterprise-repo/src/test/properties/local)
change the value for "index.subsystem.name" from "solr4" to "solr6".
Add also the following property "solr.port=8983".

Download the latest Alfresco Search Services from
https://nexus.alfresco.com/nexus/#nexus-search;gav~~alfresco-search-services~~~
Currently it's 1.0.0 (alfresco-search-services-1.0.0.zip)

Unzip it and change to the "solr" folder within it. Start the Solr server using the following command:
solr start -a "-Dcreate.alfresco.defaults=alfresco,archive"

Start your repository
