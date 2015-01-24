== Alfresco Records Management - Development Environment Setup ==

Prerequisites

 - Maven 3.0.4 (or higher)
 - Eclipse Maven Plugin (m2e)
 - See Maven setup instructions https://ts.alfresco.com/share/page/site/eng/wiki-page?title=Maven_Setup


Initial Setup

 - Create a normal project using "create-project"
 - Check out RM code into the "code" directory (eg "checkout https://svn.alfresco.com/repos/alfresco-enterprise/modules/recordsmanagement/HEAD")
 - Create the tomcat instances using "use-tomcat7" and "use-app-tomcat7"
 - If you've got SOLR config in your $DEV_HOME/projects/repository.properties file, then you'll need to remove it or fix it in the RM project level properties file.

Using Eclipse (or IntelliJ)

 - Import projects as Maven projects ("Import > Maven > Existing Maven Projects")
 - Browse to the code directory of your project and select "rm-server/pom.xml" and "rm-share/pom.xml". DO NOT select the parent "/pom.xml"
 - Open the Ant view and add the build files for both modules ("build.xml")
 - If you are not working on a Windows machine you need to change the value of a property called "mvn.exec".
   To do this create a file called "build.local.properties" under the code directory and change the value in that new file.
 - Run the "prepareEnv" target for rm-server which will prepare the development environment. This target must be run just once.
 - Now "fullBuildCommunity" or "fullBuildEnterprise" target (depending which version of Alfresco you want as dependency) can be run which will create the amp file, apply it to the war file and copy the war file to webapps folder.


Summary of Available Ant Targets

 - fullBuildCommunity           : Creates the amp file and applies it to the Community war file
 - fullBuildEnterprise          : Creates the amp file and applies it to the Enterprise war file
 - incrementalBuild             : Creates the jar file and copies the jar file with other files like css, js, ftl, etc. files


Summary of Available Internal Ant Targets

 - alfresco:amp                 : Creates the amp file using alfresco maven plugin
 - alfresco:install             : Installs the amp file to the Community war file
 - alfresco:installEnterprise   : Installs the amp file to the Enterprise war file
 - assembleIconPackage          : Assembles an icons package for the module
 - configureSolr4               : Configures Solr4 for Alfresco (creates two directories "solr4" and "solr4config" under the "data" directory)
 - copyDBDriver                 : Copies the DB driver
 - copyDevContextFile           : Copies the dev-context.xml file
 - copyWarFileToTomcat          : Copies the war file (amp applied) to the webapp folder
 - copyWebDirectory             : Copies the source/web folder
 - deleteExplodedWar            : Deletes the exploded war file
 - deleteWarFile                : Deletes the war file
 - fetchSolr                    : Gets the the Solr artifact
 - fetchWarFile                 : Gets the "original" Community war file
 - fetchEntpriseWarFile         : Gets the "original" Enterprise war file
 - install                      : Executes the "mvn install" command
 - package                      : Executes the "mvn package" command
 - prepareEnv                   : Prepares the development environment (must be run just once)
 - unitTest                     : Runs the unit tests