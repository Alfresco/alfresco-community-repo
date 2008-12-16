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

    } else if (linkNode.hasAspect("lnk:internal") && !isInternal)
    {
        linkNode.removeAspect("lnk:internal");
    }

    var prs = getLinkProperties();
    for (var propName in prs)
    {
        linkNode.properties[propName] = prs[propName];
    }

    linkNode.mimetype = "text/html";
    linkNode.content = linkNode.properties["lnk:url"];
    linkNode.tags = tags;
    linkNode.save();

    var siteId = url.templateArgs.site;
    var containerId = url.templateArgs.container;
    var data = {
      title: json.get("title"),
      page: json.get("page") + "?container=links&linkId=" + linkNode.name
    }

   activities.postActivity("org.alfresco.links.link-updated", siteId, containerId, jsonUtils.toJSONString(data));

   return linkNode;
}

function main()
{
   // get requested node
   var node = getRequestNode();

   var link = updateLink(node);

}

main();