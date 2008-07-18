<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">

function findTargetNode()
{
   if (url.templateArgs.site != undefined)
   {
      var siteId = url.templateArgs.site;
      var containerId = url.templateArgs.container;
      var path = url.templateArgs.path;

      // fetch site
      var site = siteService.getSite(siteId);
      if (site === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Site " + siteId + " does not exist");
         return null;
      }
      else if (containerId == undefined)
      {
         // get site node
         return site.node;
      }
      
      // fetch container
      var node = null;
      if (site.hasContainer(containerId))
      {
         node = site.getContainer(containerId);
      }
      if (node == null)
      {
         // Container might not be there as it hasn't been created yet!
         //status.setCode(status.STATUS_NOT_FOUND, "Unable to fetch container '" + containerId + "' of site '" + siteId + "'. (No write permission?)");
         return null;
      }
      else if (path == undefined)
      {
         return node;
      }
      
      node = node.childByNamePath(path);
      return node;
   }
   else
   {
      return findFromReference();
   }
}

function main()
{
   var node = findTargetNode();
   if (node == null)
   {
      model.noscopefound=true;
      return;
   }
   
   // fetch the nearest available tagscope
   var scope = node.tagScope;
   if (scope == null)
   {
      //status.setCode(status.STATUS_BAD_REQUEST, "No tag scope could be found for the given resource");
      //return null;
      model.noscopefound=true;
   }
   else
   {
      var topN = args["topN"] != undefined ? parseInt(args["topN"]) : -1;
      if (topN > -1)
      {
         // PENDING:
         // getTopTags currently throws an AIOOB exception if topN > tags.length() :-/
         if (scope.tags.length < topN) topN = scope.tags.length;
         model.tags = scope.getTopTags(topN);
      }
      else
      {
         model.tags = scope.tags;
      }
   }
}

main();
