<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/rma/admin/rmconstraint/rmconstraint-utils.js">

/**
 * Create a new RM Constraint List
 */
function main()
{
   // Parse the passed in details
   var title = null,
      name = null,
      allowedValues = {};

   if (json.has("constraintName"))
   {
      name = json.get("constraintName");
   }

   if (json.has("constraintTitle"))
   {
      title = json.get("constraintTitle");
   }
   else
   {
      title = name;
   }

   if (existsTitle(caveatConfig, title))
   {
      status.code = 400;
      model.errorMessage = "rm.admin.list-already-exists";
      model.title = title;
      return;
   }

   if (json.has("allowedValues"))
   {
      values = json.getJSONArray("allowedValues");

      var i = 0;
      allowedValues = new Array();

      if (values != null)
      {
         for (var x = 0; x < values.length(); x++)
         {
            allowedValues[i++] = values.get(x);
         }
      }
   }

   model.constraint = caveatConfig.createConstraint(name, title, allowedValues);
}

main();