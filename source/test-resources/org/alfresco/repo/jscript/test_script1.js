var id = root.id;
var name = root.name;
logger.log("Name: " + name);
var type = root.type;
logger.log("ID: " + id + " of type: " + type);
var noderef = root.nodeRef;
logger.log("NodeRef: " + noderef);
var childList = root.children;
logger.log("Has " + childList.length + " child nodes");
var properties = root.properties;
logger.log("Property Count: " + properties.length);
var assocs = root.assocs;
logger.log("Assoc Count: " + assocs.length);

// test various access mechanisms
var childname1 = childList[0].name;
var childname2 = childList[0].properties.name
var childname3 = childList[0].properties["name"];
var childname4 = childList[0].properties["cm:name"];

function result()
{
   return (childname1 == childname2 && childname2 == childname3 && childname3 == childname4);
}
result();
