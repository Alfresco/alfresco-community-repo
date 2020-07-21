<import resource="classpath:alfresco/enterprise/webscripts/org/alfresco/enterprise/repository/admin/admin-common.lib.js">

/**
 * Repository Admin Console
 * 
 * Root page POST method
 */
function main()
{
   var returnParams = "m=admin-console.success";
   try
   {
      Admin.persistJMXFormData();
   }
   catch (e)
   {
      returnParams = "e=" + e.message;
   }
   // generate the return URL - using the supplied tool ID or default tool if not specified
   // redrawing the appropriate tool page will retrieve the updated attribute values
   status.code = 301;
   status.location = url.serviceContext + (args.t ? args.t : Admin.getDefaultToolURI()) + "?" + returnParams;
   status.redirect = true;
}

function debug()
{
   // dump each form field in a name/value pair for easy log output
   var params = [];
   for each (field in formdata.fields)
   {
      params.push({
         name: field.name,
         value: field.value
      });
   }
   model.params = params;
}

if (!args.debug)
{
   main();
}
else
{
   debug();
}
