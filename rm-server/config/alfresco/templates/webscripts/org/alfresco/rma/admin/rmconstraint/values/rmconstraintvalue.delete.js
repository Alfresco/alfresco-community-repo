/**
 * Delete the rm constraint list
 */ 
function main()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   var authorityName = urlElements[1];
   
   if (shortName == null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "shortName missing");
      return;
   }
   if (valueName == null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "value missing");
      return;
   }
   
   // Get the constraint
   var constraint = caveatConfig.getConstraint(shortName);
   
   if (constraint != null)
   {
      caveatConfig.deleteRMConstraintListValue(shortName, valueName);
      
      var constraint = caveatConfig.getConstraint(shortName);
      
      // Pass the constraint name to the template
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