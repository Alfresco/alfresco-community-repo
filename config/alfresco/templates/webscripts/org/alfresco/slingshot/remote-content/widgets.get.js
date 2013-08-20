
var alfQuery = 'ASPECT:"{http://www.alfresco.org/model/surf/1.0}widget"' +
                  ' AND PATH:"/app:company_home/app:dictionary//*"';
   
var queryDef = {
   query: alfQuery,
   language: "fts-alfresco",
   page: {maxItems: 50},
   templates: []
};

// Get article nodes
var widgets = [],
    item,
    nodes = search.query(queryDef);

for (var i = 0, j = nodes.length; i < j; i++)
{
   // Create core object
   node = nodes[i];
   item =
   {
      nodeRef: node.nodeRef.toString(),
      widgetType: node.properties["surf:widgetType"],
      mid: node.properties["surf:mid"],
      label: node.properties["surf:label"]
   };
   widgets.push(item);
}


model.data = widgets;