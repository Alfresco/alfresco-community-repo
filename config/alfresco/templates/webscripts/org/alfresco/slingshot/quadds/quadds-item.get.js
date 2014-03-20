var queryDef = {
   language: "fts-alfresco",
   page: {maxItems: 50},
   templates: []
};

var results = [];

var alfQuery;
if (url.templateArgs.item_name != null)
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
   alfQuery = 'TYPE:"{http://www.alfresco.org/model/content/1.0}content" AND PATH:"/app:company_home/app:dictionary/cm:QuADDS/cm:' + 
            url.templateArgs.name + '/*"'
   queryDef.query = alfQuery;

   var QuADDS_Items = search.query(queryDef);
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
   alfQuery = 'TYPE:"{http://www.alfresco.org/model/content/1.0}folder" AND PATH:"/app:company_home/app:dictionary/cm:QuADDS/*"'
   queryDef.query = alfQuery;

   var QuADDS = search.query(queryDef);
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