Alfresco Web Content Management Enabler
---------------------------------------

To enable Web Content Management in Alfresco, you need to install the
Virtualization server and configure WCM to bootstrap.  The WCM enabler
is distributed in a ZIP or tar.gz file that needs to be extracted in the
Alfresco home directory (e.g. C:\Alfresco or /opt/alfresco).  The
distribution includes virtual-tomcat and related scripts for the WCM
Virtualization server.  If your unzip program asks about directories
already existing, you are safe to allow this, as only new files are
provided.


Installing WCM Bootstrap
------------------------

Once unzipped, the wcm-bootstrap-context.xml file needs to be moved to
the Alfresco extension directory:
- for Tomcat, this is usually tomcat/shared/classes/alfresco/extension
- for JBoss, this is usually jboss/server/default/conf/alfresco/extension

Restart the Alfresco server for the bootstrap to take effect.  You will 
see information about the success of the bootstrap in the logs.


WCM Functionality
-----------------

You should now find 2 additional spaces in your Alfresco repository:
- Web Projects in Company Home
- Web Forms in Data Dictionary

If you wish to use the Website Preview feature, you will need to start
the Alfresco Virtualization server:
- on Windows run virtual_start.bat
- on Linux run sh virtual_alf.sh start
