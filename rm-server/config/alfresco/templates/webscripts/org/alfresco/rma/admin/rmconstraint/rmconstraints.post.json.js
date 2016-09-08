/**
 * Create a new RM Constraint List
 */ 
function main()
{
   // Parse the passed in details
   var title = null;
   var name = null;
   var allowedValues = {};
   
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
   

   var constraints = caveatConfig.allConstraints;
   
   // Check for existing constraint...
   var alreadyExists = false;
   for (var i=0; i<constraints.length; i++)
   {
      var currTitle = constraints[i].title;
      if (currTitle + "" == title)
      {
         alreadyExists = true;
         break;
      }
   }
   
   var existingConstraint = caveatConfig.getConstraint(title);
   if (!alreadyExists)
   {
      var constraint = caveatConfig.createConstraint(name, title, allowedValues);
      model.constraint = constraint;
   }
   else
   {
      status.code = 400;
      model.errorMessage = "rm.admin.list-already-exists";
      model.title = title;
   }
}

main();