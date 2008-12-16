<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">
const LINK_INTERNAL_ASPECT = "lnk:internal";
const ASPECT_INTERNAL = "lnk:isInternal";

function getLinksData(node)
{
   var itm = {};
   itm.node = node;
   itm.name  = node.name || "";
   itm.title = node.properties["lnk:title"] || "";
   itm.description = node.properties["lnk:description"] || "";
   itm.url = node.properties["lnk:url"] || "";
   itm.createdOn = node.properties.created || "";
   itm.modifiedOn = node.properties.modified || "";
   itm.creator = people.getPerson(node.properties["cm:creator"]);

   itm.tags = node.tags;
   itm.internal = node.hasAspect("lnk:internal");
         
   return itm;
}

/**
 * Fetches the link properties from the json object and adds them to an array
 * using the correct property names as indexes.
 */
function getLinkProperties()
{

   var data = [];

   if (json.has("title"))
   {
     data["lnk:title"] = json.get("title");
   }

   if (json.has("url"))
   {
     data["lnk:url"] = json.get("url");
   }

   if (json.has("description"))
   {
     data["lnk:description"] = json.get("description");
   }
   
   return data;
}

function isLinkInternal()
{
   return (json.has("internal") ? true : false);
}
