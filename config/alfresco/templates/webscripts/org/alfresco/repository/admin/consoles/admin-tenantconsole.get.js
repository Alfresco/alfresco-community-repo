<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

/**
 * Repository Admin Console
 * 
 * Tenant Admin Console GET method
 */

function main()
{
   model.attributes = [];
   
   // cmd info
   model.cmd = {
      description: "Last command: " + Admin.encodeHtml(tenantInterpreter.command) + "<br>" + "Duration: " + tenantInterpreter.duration + "ms",
      output: tenantInterpreter.result
   };
   
   // mandatory model values for UI
   model.tools = Admin.getConsoleTools("admin-tenantconsole");
   model.metadata = Admin.getServerMetaData();
}

main();