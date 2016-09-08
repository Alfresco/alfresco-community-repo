/**
 * Update the details of a value in an rm constraint
 */ 
function main()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   var values = null;
   
   if (json.has("values"))
   {
      values = json.getJSONArray("values");
   }
   
   if (values == null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Values missing");
      return;
   }
  
   // Get the constraint
   var constraint = caveatConfig.getConstraint(shortName);
   
   if (constraint != null)
   {
      constraint.updateValues(values); 
      model.constraint = caveatConfig.getConstraint(shortName);
      model.constraintName = shortName; 
   }
   else
   {
      // Return 404
      status.setCode(404, "Constraint List " + shortName + " does not exist");
      return;
   }
}

main();