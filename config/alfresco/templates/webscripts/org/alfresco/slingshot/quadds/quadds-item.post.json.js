function main() {
   // Get the name and data for the QuADDS item...
   var name = json.get("name");
   var data = json.get("data");

   if (url.templateArgs.name == null)
   {
      // Can't create a QuADDS item without knowing where to place the item...
      status.code = 500;
      model.errorMessage = "No QuADDS name provided";
      return false;
   }
   else if (name == null)
   {
      // Can't create a QuADDS item without knowing what to call it
      // TODO: Could we just use a timestamp here if nothing is provided?
      status.code = 500;
      model.errorMessage = "No QuADDS name provided for item to create";
      return false;
   }
   else if (data == null)
   {
      status.code = 500;
      model.errorMessage = "No QuADDS data provided for item to create";
      return false;
   }
   else
   {
      try
      {
         // Try to parse the data to check that it's valid JSON...
         var validJson = JSON.parse(data);
      }
      catch(e)
      {
         status.code = 500;
         model.errorMessage = "Invalid QuADDS data provided - must be JSON";
         return false;
      }

      // Construct query to find the requested QuADDS...
      var QuADDS_Folder;
          // nodes = search.query(queryDef);
      var nodes = search.selectNodes('/app:company_home/app:dictionary/cm:QuADDS/cm:' + search.ISO9075Encode(url.templateArgs.name));
      if (nodes.length == 0)
      {
         // Need to create the QuADDS Folder...
         // Get the root folder...
         nodes = search.selectNodes('/app:company_home/app:dictionary/cm:QuADDS');
         if (nodes.length == 0)
         {
            // TODO: Create the QuADDS folder
            status.code = 500;
            model.errorMessage = "'QuADDS' folder has not been created";
            return false;
         }
         else
         {
            // Create the folder...
            QuADDS_Folder = nodes[0].createNode(url.templateArgs.name, "cm:folder");
         }
      }
      else
      {
         // Found the QuADDS folder... save a reference to it so we can create the item in it...
         QuADDS_Folder = nodes[0];
      }

      // Create the file...
      try
      {
         var item = QuADDS_Folder.createNode(json.get("name"), "cm:content");
         if (item == null)
         {
            // Couldn't create the item for some reason...
            status.code = 500;
            model.errorMessage = "Could not create QuADDS item";
            return false;
         }
         else
         {
            item.addAspect("cm:versionable");
            item.content = data;
            item.mimetype = "application/json";
            model.nodeRef = item.nodeRef.toString();
            return true;
         }
      }
      catch (e)
      {
         status.code = 500;
         model.errorMessage = "Could not create QuADDS item";
         return false;
      }
      
      // Shouldn't get to here - there should be a return at every code path...
      model.errorMessage = "Unexpected error occurred";
      status.code = 500;
      return false;
   }
}

model.success = main();
