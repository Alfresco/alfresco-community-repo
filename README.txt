== Alfresco Records Management - Development Environment Setup ==

Prerequisites

 - Maven 3.0.4 (or higher)
 - Eclipse Maven Plugin (m2e)
 - See Maven setup instructions https://ts.alfresco.com/share/page/site/eng/wiki-page?title=Maven_Setup


Initial Setup

 - Create a normal project using "create-project"
 - Check out RM code into the "code" directory (eg "checkout https://svn.alfresco.com/repos/alfresco-enterprise/modules/recordsmanagement/HEAD")
 - Create the tomcat instances using "use-tomcat7" and "use-app-tomcat7"


Using Eclipse

 - Import projects as Maven projects ("Import > Maven > Existing Maven Projects")
 - Browse to the code directory of your project and select "rm-server/pom.xml" and "rm-share/pom.xml". DO NOT select the parent "/pom.xml"
 - Open the Ant view and add the build files for both modules ("build.xml")
 - Run the "prepareEnv" target for rm-server which will prepare the development environment. This target must be run just once.
 - Now "buildAndDeploy" target can be run which will create the amp file, apply it to the war file and copy the war file to webapps folder.


Summary of Available Ant Targets

 - assembleIconPackage          : Assembles an icons package for the module
 - assembleLocalisationPackage  : Assembles an i18n package for the module
 - buildAndDeploy               : Creates the amp file and applies it to the war file
 - prepareEnv                   : Prepares the development environment (must be run just once)
 - tomcat-start                 : Starts a tomcat instance
 - tomcat-start-debug           : Starts a tomcat instance in debug mode
 - tomcat-stop                  : Stops the running tomcat instance


Summary of Available Internal Ant Targets

 - alfresco:amp                 : Creates the amp file using alfresco maven plugin
 - alfresco:install             : Installs the amp file to the war file
 - cleanTarget                  : Deletes the "target" folder
 - copyDBDriver                 : Copies the DB driver
 - copyDevContextFile           : Copies the dev-context.xml file
 - copyWarFileToTomcat          : Copies the war file (amp applied) to the webapp folder
 - deleteExplodedWar            : Deletes the exploded war file
 - deleteWarFile                : Deletes the war file
 - fetchWarFile                 : Gets the "original" war file
 - install                      : Executes the "mvn install" command