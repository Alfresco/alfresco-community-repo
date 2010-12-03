<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/links/links.lib.js">


function getRequestNodes()
{
   var siteId = url.templateArgs.site;
   var containerId = url.templateArgs.container;
   var items = [];
   var nodes = [];
   if (json.has("items"))
   {
      var tmp = json.get("items");
      for (var x=0; x < tmp.length(); x++)
      {
          items.push(tmp.get(x));
      }
   }

   // fetch site
   var site = siteService.getSite(siteId);
   if (site === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Site " + siteId + " does not exist");
      return null;
   }

   // fetch container
   var node = site.getContainer(containerId);
   if (node === null)
   {
      node = site.createContainer(containerId);
      if (node === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Unable to fetch container '" + containerId + "' of site '" + siteId + "'. (No write permission?)");
         return null;
      }
   }

   for (var i in items)
   {
      if (i)
      {
         var tmpNode = node.childByNamePath(items[i]);
         if (tmpNode)
         {
            nodes.push(tmpNode);
         }
      }
   }

   return nodes;
}

/**
 * Deletes a link node
 */
function deleteLink(linkNode)
{
   // delete the node
   var nodeRef = linkNode.nodeRef;
   var linkData = getLinksData(linkNode);
   var isDeleted = linkNode.remove();
   if (! isDeleted)
   {
      var mes = "Unable to delete node: " + nodeRef; 
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, mes);
      model.message = mes; 
      return;
   }
   model.message = "Node " + nodeRef + " deleted";

   var siteId = url.templateArgs.site;
   var data =
   {
      title: linkData.title,
      page: "links"
   };

   activities.postActivity("org.alfresco.links.link-deleted", siteId, "links", jsonUtils.toJSONString(data));
}

function main()
{
   // get requested node
   var nodes = getRequestNodes();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

 for (var i in nodes)
 {
   if (i)
   {
    if (!nodes[i].hasPermission("Delete"))
    {
      status.code = 403;
      var mes = "Permission to delete is denied";
      status.message = mes;
      model.message = mes;
      return;
    }

    deleteLink(nodes[i]);
   }
 }

}

main();