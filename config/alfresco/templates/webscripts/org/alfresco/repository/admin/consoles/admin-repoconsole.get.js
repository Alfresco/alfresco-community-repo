<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

/**
 * Repository Admin Console
 * 
 * Repo Admin Console GET method
 */

function main()
{
   model.attributes = [];
   
   // cmd info
   model.cmd = {
      description: "Last command: " + Admin.encodeHtml(repoInterpreter.command) + "<br>" + "Duration: " + repoInterpreter.duration + "ms",
      output: repoInterpreter.result
   };
   
   // mandatory model values for UI
   model.tools = Admin.getConsoleTools("admin-repoconsole");
   model.metadata = Admin.getServerMetaData();
}

main();