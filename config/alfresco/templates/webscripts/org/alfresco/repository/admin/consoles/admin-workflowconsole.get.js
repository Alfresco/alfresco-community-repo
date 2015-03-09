<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

/**
 * Repository Admin Console
 * 
 * Workflow Admin Console GET method
 */

function main()
{
   model.attributes = [];
   
   // cmd info
   model.cmd = {
      description: "Last command: " + Admin.encodeHtml(workflowInterpreter.command) + "<br>" + "Duration: " + workflowInterpreter.duration + "ms",
      output: workflowInterpreter.result
   };
   
   // mandatory model values for UI
   model.tools = Admin.getConsoleTools("admin-workflowconsole");
   model.metadata = Admin.getServerMetaData();
}

main();