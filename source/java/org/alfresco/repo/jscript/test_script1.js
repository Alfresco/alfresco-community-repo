var id = root.id;
var name = root.name;
out.println("Name: " + name);
var type = root.type;
out.println("ID: " + id + " of type: " + type);
var noderef = root.nodeRef;
out.println("NodeRef: " + noderef);
var childList = root.children;
out.println("Has " + childList.length + " child nodes");
var properties = root.properties;
out.println("Property Count: " + properties.length);
var assocs = root.assocs;
out.println("Assoc Count: " + assocs.length);

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
