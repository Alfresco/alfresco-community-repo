
var alfQuery = 'TYPE:"{http://www.alfresco.org/model/surf/1.0}amdpage"' +
                  ' AND PATH:"/app:company_home/app:dictionary//*"';

if (url.templateArgs.pagename != null)
{
   alfQuery = alfQuery + ' AND @cm\:name:"' + url.templateArgs.pagename + '"';
}

var queryDef = {
   query: alfQuery,
   language: "fts-alfresco",
   page: {maxItems: 50},
   templates: []
};

// Get article nodes
var pages = [],
    item,
    nodes = search.query(queryDef);

var includeContent = (nodes.length == 1);
for (var i = 0, j = nodes.length; i < j; i++)
{
   // Create core object
   node = nodes[i];
   item =
   {
      nodeRef: node.nodeRef.toString(),
      name: node.name
   };
   if (includeContent)
   {
      item.content = node.content;
   }
   pages.push(item);
}


model.data = pages;