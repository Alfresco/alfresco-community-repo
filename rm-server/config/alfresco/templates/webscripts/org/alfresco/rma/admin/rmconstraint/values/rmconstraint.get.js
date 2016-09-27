/**
 * Get the detail of the rm constraint
 */ 
function main()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   // Get the constraint
   var constraint = caveatConfig.getConstraint(shortName);
   
   if (constraint != null)
   {
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