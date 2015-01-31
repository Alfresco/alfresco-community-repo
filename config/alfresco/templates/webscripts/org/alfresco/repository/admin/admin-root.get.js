<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

/**
 * Repository Admin Console
 * 
 * Root page GET method
 */
function main()
{
   var surl = url.service + (url.service.lastIndexOf('/') !== url.service.length() - 1 ? '/' : "");
   status.code = 301;
   status.location = surl + Admin.getDefaultTool();
   status.redirect = true;
}

main();