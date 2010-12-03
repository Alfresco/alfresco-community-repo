<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/links/links.lib.js">

/**
 * Update a link
 */
function updateLink(linkNode)
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

   var isInternal = isLinkInternal();
   if (!linkNode.hasAspect("lnk:internal") && isInternal)
   {
      var pr = [];
      pr["lnk:isInternal"] = "true";
      linkNode.addAspect("lnk:internal", pr);
   }
   else if (linkNode.hasAspect("lnk:internal") && !isInternal)
   {
      linkNode.removeAspect("lnk:internal");
   }

   var prs = getLinkProperties();
   for (var propName in prs)
   {
      if (propName)
      {
         linkNode.properties[propName] = prs[propName];
      }
   }

   linkNode.mimetype = "text/html";
   linkNode.content = linkNode.properties["lnk:url"];
   linkNode.tags = tags;
   linkNode.save();
   model.message = "Node " + linkNode.nodeRef + " updated";
   var siteId = url.templateArgs.site;
   var data =
   {
      title: json.get("title"),
      page: json.get("page") + "?linkId=" + linkNode.name
   };

   activities.postActivity("org.alfresco.links.link-updated", siteId, "links", jsonUtils.toJSONString(data));

   return linkNode;
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (!node.hasPermission("WriteProperties") || !node.hasPermission("WriteContent"))
   {
      status.code = 403;
      var mes = "Permission to update is denied";
      status.message = mes;
      model.message = mes;
      return;
   }
   updateLink(node);
}

main();