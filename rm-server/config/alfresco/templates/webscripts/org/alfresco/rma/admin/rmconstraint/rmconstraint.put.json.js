<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/rma/admin/rmconstraint/rmconstraint-utils.js">

/**
 * Update an rm constraint
 */
function main()
{
   // Get the shortname
   var shortName = url.extension;

   // Get the constraint
   var constraint = caveatConfig.getConstraint(shortName);

   if (constraint != null)
   {
      var allowedValues,
         title = null;

      if (json.has("constraintTitle"))
      {
         title = json.get("constraintTitle");
         if (existsTitle(caveatConfig, title))
         {
            status.code = 400;
            model.errorMessage = "rm.admin.list-already-exists";
            model.title = title;
            return;
         }
         constraint.updateTitle(title);
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
         constraint.updateAllowedValues(allowedValues);
      }

      // Pass the constraint detail to the template
      model.constraint = constraint;
   }
   else
   {
      // Return 404
      status.setCode(404, "Constraint List " + shortName + " does not exist");
      return;
   }
}

main();