var results = [];

var alfQuery;
if (url.templateArgs.item_name != null)
{
   // Gets the items for a specific QuADDS...
   var QuADDS_Items = search.selectNodes('/app:company_home/app:dictionary/cm:QuADDS/cm:' + search.ISO9075Encode(url.templateArgs.name) + '/cm:' + search.ISO9075Encode(url.templateArgs.item_name));
   if (QuADDS_Items.length > 0)
   {
      var node = QuADDS_Items[0];
      var item = 
      {
         nodeRef: node.nodeRef.toString(),
         name: node.name,
         data: node.content
      };
      results.push(item);
   }
}
else if (url.templateArgs.name != null)
{
   // Gets the items for a specific QuADDS...
   var QuADDS_Items = search.selectNodes('/app:company_home/app:dictionary/cm:QuADDS/cm:' + search.ISO9075Encode(url.templateArgs.name) + '/*[subtypeOf("cm:content")]');
   for (var i = 0, j = QuADDS_Items.length; i < j; i++)
   {
      // Create core object
      var node = QuADDS_Items[i];
      var item = 
      {
         nodeRef: node.nodeRef.toString(),
         name: node.name,
         data: node.content
      };
      results.push(item);
   }
}
else
{
   // Gets all the QuADDS...
   var QuADDS = search.selectNodes('/app:company_home/app:dictionary/cm:QuADDS/*[subtypeOf("cm:folder")]');

   for (var i = 0, j = QuADDS.length; i < j; i++)
   {
      // Create core object
      var node = QuADDS[i];
      var item =
      {
         nodeRef: node.nodeRef.toString(),
         name: node.name
      };
      results.push(item);
   }
}
model.data = results;