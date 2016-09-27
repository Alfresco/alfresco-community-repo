/**
 * Get the detail of the rm constraint
 */ 
function main()
{
   var urlElements = url.extension.split("/");
   var shortName = decodeURIComponent(urlElements[0]);
   var valueName = decodeURIComponent(urlElements[2])
   
   // Get the constraint
   var constraint = caveatConfig.getConstraint(shortName);
   
   if (constraint != null)
   {
      // Pass the constraint detail to the template
      var value = constraint.getValue(valueName);
      
      if(value == null)
      {
         // Return 404
         status.setCode(404, "Constraint List: " + shortName + " value: " + valueName + "does not exist");
         return;
      }
      
      model.value = value;
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