<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

/**
 * Repository Admin Console
 * 
 * Community Summary GET method
 */
function main()
{
   model.tools = Admin.getConsoleTools("admin-communitysummary");
   model.metadata = Admin.getServerMetaData();
}

main();