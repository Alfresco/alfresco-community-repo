<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">
const LINK_INTERNAL_ASPECT = "lnk:internal";
const ASPECT_INTERNAL = "lnk:isInternal";


function getLinksData(node)
{
    var itm = {};
    itm.node = node;
    itm.name  = node.name||"";
    itm.title = node.properties["lnk:linkTitle"]||"";
    itm.description = node.properties["lnk:description"]||"";
    itm.url = node.properties["lnk:url"]||"";

      // get the tags JSONArray and copy it into a real javascript array object
    itm.tags = node.tags;
    itm.internal = (node.hasAspect("lnk:internal")?"true":"false");
    
    itm.author = people.getPerson(node.properties["cm:creator"]);
    // (re-)enable permission
    node.setInheritsPermissions(true);
    /*itm.isUpdated = node.properties.isUpdated || "false";*/
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
      data["lnk:linkTitle"] = json.get("title");
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
    return (json.has("isinternal")?true:false);
}
