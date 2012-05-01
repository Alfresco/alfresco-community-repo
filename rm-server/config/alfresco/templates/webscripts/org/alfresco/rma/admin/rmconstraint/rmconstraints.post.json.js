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
   
   var constraint = caveatConfig.createConstraint(name, title, allowedValues);
   
   // Pass the constraint detail to the template
   model.constraint = constraint;
}

main();