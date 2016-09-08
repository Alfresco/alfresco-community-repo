== Alfresco Records Management - Development Environment Setup == 

Prerequisites

 - Gradle 1.0 milestone 8a (optional unless native gradle support is required)
 - Alfresco Repository 4.0.1 (or higher).  Specifically you need the alfresco.war and share.war files.
 - Any prerequisties required for an Alfresco installation, including Java 1.5, Tomcat and a suitable database.
 
 NOTE:  throughout these instructions we will use the 'gradlew' wrapper command.  This wrapper command downloads the required Gradle libraries automatically.  Should you prefer to use the native 'gradle' command instead you will need to install Gradle manually.
 
Initial Setup

 - Checkout Records Management code from https://svn.alfresco.com/repos/alfresco-enterprise/modules/recordsmanagement/HEAD
   Note:  the RM branch structure reflects the main repository branch structure, so HEAD is the current development branch and BRACHES contains the release branches.
 - Run "gradlew fetchWarFile".  This will use Maven to download the dependant Alfresco War's, currently version 4.0.1 (from the code directory)
 - Run "gradlew amp" in root directory.  This will unpack the dependancies, build the RM source and finally assemble the RM amps.
   Note:  the first execution of this task may take serveral minutes, because it will unpack the required build dependancies from the alfresco and share WAR.  It will also pull any external dependancies from Maven or 
 - You will now find rm-server\build\dist\alfresco-rm-2.0.amp and rm-share\build\dist\alfresco-rm-share-2.0.amp have been built.  
 
Using Eclipse

  - Run "gradlew eclipse".  This will generate the eclipse project files.
  - Start Eclipse in the usual way.
    Note: make sure the WAR dependancies have been exploded before opening Eclispe.
  - Import projects found in rm-server and rm-share directories.
  
For users of the Alfresco DevEnv

 - Create a normal project using "create-project".
 - Check out RM code into the "code" directory (checkout https://svn.alfresco.com/repos/alfresco-enterprise/modules/recordsmanagement/HEAD)
 - The devEnv will automatically set the TOMCAT_HOME and APP_TOMCAT_HOME environment variables to point to the Tomcat instances created by the use-tomcat6 and use-app-tomcat6 scipts.  Magic!
 - Copy the JDBC driver to <TOMCAT_HOME>/lib/
 - You can use the dev-context.xml (found in <PROJECT_HOME>/code/root/projects/repository/config/alfresco/extension) generated for you to configure the repository. Place it in <TOMCAT_HOME>/shared/classes/alfresco/extension and <PROJECT_NAME>/code/rm-server/config/alfresco/extension.
    
Deploying the RM AMPs

  - Set the envoronment variables TOMCAT_HOME and APP_TOMCAT_HOME to the home directory of the repository and share Tomcat instances respectively.
    NOTE: these can be the same tomcat instance, but it is recommended that two are used.
  - Configure your repository Tomcat so that your repository.properties settings will be successfully picked up when Alfresco is started.
  - Run "gradlew deployAmp" in the root directory.  This will use the MMT to apply the RM AMPs to the Alfresco and Share WARs respectively.  The modified WARs will then be copied to the set Tomcat instances, cleaning any exisiting exploded WARs.
  - Start Tomcat(s). (with start_tomcat and start_app-tomcat)
 

Summary Of Available Gradle Tasks

  Note:  All these tasks can be executed in the root directory or in either of the sub-project directories.
  Note:  Use the command "gradle <taskName>" when executing.
  Note:  The RM Gradle scripts import the standard "Java" package so those associated standard tasks are available, for example "jar", "compileJava", "clean", etc
  
  - explodeDeps 	: checks for existance of the projects dependant WAR (either alfresco.war or share.war).  If not already exploded, unpacks the required depedancies from the WAR files.
  - cleanDeps 		: cleans the projects exploded dependancies.
  - amp 			: builds the projects AMP and places it in build/dist.
  - installAmp		: installs the AMP into a copy of the projects dependant WAR using the MMT.
					  NOTE: the installed WAR can be found in build/dist.
  - deployAmp       : depolys the project AMP to the configured Tomcat instance.
  - fetchWarFile    : fetches the dependant Alfresco WAR files
  - eclipse         : generates eclipse projects for repository and share projects