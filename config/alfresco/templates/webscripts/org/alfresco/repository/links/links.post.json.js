<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/links/links.lib.js">

function ensureTagScope(node)
{
   if (!node.isTagScope)
   {
     node.isTagScope = true;
   }

   // also check the parent (the site!)
   if (!node.parent.isTagScope)
   {
      node.parent.isTagScope = true;
   }
}

/**
 * Creates a link
 */
function createLink(linkNode)
{
   var tags = [];
   if (json.has("tags"))
   {
      // get the tags JSONArray and copy it into a real javascript array object
      var tmp = json.get("tags");
      for (var x = 0; x < tmp.length(); x++)
      {
         tags.push(tmp.get(x));
      }
   }

   // get a unique name
   var nodeName = getUniqueChildName(linkNode, "link");
   linkNode = linkNode.createNode(nodeName, "lnk:link",getLinkProperties());

   if (isLinkInternal())
   {
      var pr = [];
      pr["lnk:isInternal"] = "true";
      linkNode.addAspect("lnk:internal", pr);
   }

   linkNode.tags = tags;
   linkNode.mimetype = "text/html";
   linkNode.content = linkNode.properties["lnk:url"];
   linkNode.save();

   var siteId = url.templateArgs.site;
   var data =
   {
      title: json.get("title"),
      page: json.get("page") + "?linkId=" + nodeName
   };

   model.message = linkNode.properties["name"];
   activities.postActivity("org.alfresco.links.link-created", siteId, "links", jsonUtils.toJSONString(data));
     
   return linkNode;
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }
      
   if (!node.hasPermission("CreateChildren"))
   {
      status.code = 403;
      var mes = "Permission to create is denied";
      status.message = mes;
      model.message = mes;
      return;
   }

   ensureTagScope(node);

   var link = createLink(node);
   model.item = link;    
}

main();