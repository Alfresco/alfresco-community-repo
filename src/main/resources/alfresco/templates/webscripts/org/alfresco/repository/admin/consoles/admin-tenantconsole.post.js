<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

/**
 * Repository Admin Console
 * 
 * Tenant Console POST method
 */
function main()
{
   // execute supplied command
   var cmd = args["tenant-cmd"];
   tenantInterpreter.executeCmd(cmd);
   
   // generate the return URL
   status.code = 301;
   status.location = url.service;
   status.redirect = true;
}

main();