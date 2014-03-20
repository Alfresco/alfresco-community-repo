function main() {
   var queryDef = {
      language: "fts-alfresco",
      page: {maxItems: 50},
      templates: []
   };

   var results = [];
   var alfQuery;
   if (url.templateArgs.name == null)
   {
      // Can't create a QuADDS item without knowing where to place the item...
      status.code = 500;
      model.errorMessage = "No QuADDS name provided";
      return false;
   }
   else if (url.templateArgs.item_name == null)
   {
      // Can't create a QuADDS item without knowing where to place the item...
      status.code = 500;
      model.errorMessage = "No QuADDS item name provided";
      return false;
   }
   else
   {
      // Gets the items for a specific QuADDS...
      alfQuery = 'TYPE:"{http://www.alfresco.org/model/content/1.0}content" AND ' + 
                 'PATH:"/app:company_home/app:dictionary/cm:QuADDS/cm:' + url.templateArgs.name + '/*" '+ 
                 'AND @cm\:name:"' + url.templateArgs.item_name + '"';
      queryDef.query = alfQuery;

      var QuADDS_Items = search.query(queryDef);
      if (QuADDS_Items.length > 0)
      {
         var node = QuADDS_Items[0];
         var isDeleted = node.remove();
         if (!isDeleted)
         {
            status.code = 500;
            model.errorMessage = "No could not delete QuADDS item";
            return false;
         }
         else
         {
            return true;
         }
      }
      else
      {
         status.code = 500;
         model.errorMessage = "Culd not find QuADDS item";
         return false;
      }
   }
}

model.success = main();