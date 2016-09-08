/**
 * Get the detail of the rm constraint
 */ 
function main()
{
   // Get the shortname
   var shortName = url.extension;
   
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