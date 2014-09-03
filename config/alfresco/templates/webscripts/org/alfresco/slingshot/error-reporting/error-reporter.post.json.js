function main() {
   var callerName = json.get("callerName");
   var messageArgs = json.get("messageArgs");
   var userName = json.get("userName");
   var location = json.get("location");

   // Construct query to find the folder for adding errors...
   var errorFolder;
   var nodes = search.selectNodes('/app:company_home/cm:ShareErrors');
   if (nodes.length == 0)
   {
      // Create the folder...
      nodes = search.selectNodes('/app:company_home');
      errorFolder = nodes[0].createNode("ShareErrors", "cm:folder");
   }
   else
   {
      // Found the error folder... save a reference to it so we can create the item in it...
      errorFolder = nodes[0];
   }

   // Create the file...
   try
   {
      var item = errorFolder.createNode(null, "cm:content");
      if (item == null)
      {
         // Couldn't create the item for some reason...
         status.code = 500;
         model.errorMessage = "Could not create error report item";
         return false;
      }
      else
      {
         item.content = json;
         item.mimetype = "application/json";
         model.nodeRef = item.nodeRef.toString();
         return true;
      }
   }
   catch (e)
   {
      status.code = 500;
      model.errorMessage = "Could not create error report";
      return false;
   }
   
   // Shouldn't get to here - there should be a return at every code path...
   model.errorMessage = "Unexpected error occurred";
   status.code = 500;
   return false;
}
model.success = main();
