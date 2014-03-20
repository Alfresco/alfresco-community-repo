function main() {
   var queryDef = {
      query: alfQuery,
      language: "fts-alfresco",
      page: {maxItems: 50},
      templates: []
   };

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
      var alfQuery = 'TYPE:"{http://www.alfresco.org/model/content/1.0}content" AND ' + 
                     'PATH:"/app:company_home/app:dictionary/cm:QuADDS/cm:' + url.templateArgs.name + '/*" '+ 
                     'AND @cm\:name:"' + url.templateArgs.item_name + '"';
      queryDef.query = alfQuery;
      
      var QuADDS_Items = search.query(queryDef);
      if (QuADDS_Items.length > 0)
      {
         var node = QuADDS_Items[0];
         var workingCopy = node.checkout();
         workingCopy.content = data;
         node = workingCopy.checkin();
         model.nodeRef = node.nodeRef.toString();
         return true;
      }
      else
      {
         status.code = 500;
         model.errorMessage = "Culd not find QuADDS item";
         return false;
      }

      // Shouldn't get to here - there should be a return at every code path...
      model.errorMessage = "Unexpected error occurred";
      status.code = 500;
      return false;
   }
}

model.success = main();
