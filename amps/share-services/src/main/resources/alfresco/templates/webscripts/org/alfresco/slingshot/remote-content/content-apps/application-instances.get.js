
var alfQuery = 'TYPE:"{http://www.alfresco.org/model/surf/1.0}applicationInstance"' +
                  ' AND PATH:"/app:company_home/cm:ContentApps//*"';

// if (url.templateArgs.pagename != null)
// {
//    alfQuery = alfQuery + ' AND @cm\:name:"' + url.templateArgs.pagename + '"';
// }

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

for (var i = 0, j = nodes.length; i < j; i++)
{
   // Create core object
   node = nodes[i];
   item =
   {
      nodeRef: node.nodeRef.toString(),
      name: node.name
   };
   pages.push(item);
}


model.data = pages;