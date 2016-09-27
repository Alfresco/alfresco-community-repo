/**
 * Delete the rm constraint list
 */ 
function main()
{
   // Get the shortname
   var shortName = url.extension;
   
   // Get the constraint
   var constraint = caveatConfig.getConstraint(shortName);
   
   if (constraint != null)
   {
      caveatConfig.deleteConstraintList(shortName);
      
      // Pass the constraint name to the template
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