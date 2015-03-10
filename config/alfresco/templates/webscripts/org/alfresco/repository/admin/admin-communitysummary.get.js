<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

/**
 * Repository Admin Console
 * 
 * Community System Summary GET method
 */
function main()
{
   // Memory and CPUs
   model.memoryAttributes = {
      // convert bytes into GB with 2 decimal places
      "FreeMemory": Math.round(java.lang.Runtime.getRuntime().freeMemory() / 1024 / 1024 / 1024 * 100) / 100,
      "MaxMemory": Math.round(java.lang.Runtime.getRuntime().maxMemory() / 1024 / 1024 / 1024 * 100) / 100,
      "TotalMemory": Math.round(java.lang.Runtime.getRuntime().totalMemory() / 1024 / 1024 / 1024 * 100) / 100,
      "AvailableProcessors": java.lang.Runtime.getRuntime().availableProcessors()
   };
   
   // System properties
   model.sysPropsAttributes = {};
   ["java.home", "java.version", "java.vm.vendor", "os.name", "os.version", "os.arch"].forEach(function(p) {
      model.sysPropsAttributes[p] = java.lang.System.getProperty(p);
   });
   
   // Alfresco properties
   model.alfrescoAttributes = {};
   ["edition", "version", "versionLabel", "schema", "id"].forEach(function(p) {
      model.alfrescoAttributes[p] = server[p];
   });
   
   model.tools = Admin.getConsoleTools("admin-communitysummary");
   model.metadata = Admin.getServerMetaData();
}

main();