Configuring and starting Alfresco/Share:
----------------------------------------

- Clone the project (e.g. git clone git@gitlab.alfresco.com:records-management/records-management.git)

- Import the project as a maven project

- Start the Alfresco/Share instances with the following commands:

  mvn clean install -Pstart-repo
  mvn clean install -Pstart-share



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



Running integration test:
-------------------------

In order to execute the integration tests run the following command (unit tests will be executed every time before you start Alfresco/Share):

mvn clean install -Dskip.integrationtests=false