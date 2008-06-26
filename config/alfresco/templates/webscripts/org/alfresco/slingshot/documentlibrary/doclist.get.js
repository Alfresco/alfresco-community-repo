<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/filters.lib.js">

/**
 * Document List Component: doclist
 *
 * Inputs:
 *  mandatory: site = the site containing the document library
 *   optional: path = folder relative to root store
 *
 * Outputs:
 *  doclist - object containing list of child folders and documents
 */
model.doclist = getDoclist(args["site"], args["path"], args["type"], args["filter"]);

/* Create collection of documents and folders in the given space */
function getDoclist(siteId, path, type, filter)
{
   try
   {
      var items = new Array();
   
      /* siteId input */
      var siteNode = siteService.getSite(siteId);
      if (siteNode === null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Site not found: '" + siteId + "'");
         return;
      }
   
      var containerNode = siteNode.getContainer("documentLibrary");
      if (containerNode === null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Document Library container not found in: " + siteId + ". (No write permission?)");
         return;
      }

      /* path input */
      if ((path !== null) && (path != ""))
      {
         pathNode = containerNode.childByNamePath(path);
      }
      else
      {
         pathNode = containerNode;
      }

      if (pathNode === null)
      {
         pathNode = containerNode;
      }

      var showDocs = true;
      var showFolders = true;
      
      // Default to all children of pathNode
      var assets = pathNode.children;

      // Try to find a filter query based on the passed-in argument
      var filterParams =
      {
         siteNode: siteNode,
         containerNode: containerNode,
         pathNode: pathNode
      }
      var filterQuery = getFilterQuery(filter, filterParams);
      if (filterQuery != null)
      {
         assets = search.luceneSearch("workspace://SiteStore", filterQuery);
      }

      // Documents and/or folders?
      if ((type !== null) && (type != ""))
      {
         showDocs = (type == "documents");
         showFolders = (type == "folders");
      }
      
      for each(asset in assets)
      {
         if ((asset.isContainer && showFolders) || (asset.isDocument && showDocs))
         {
            items.push(asset);
         }
      }
   
      items.sort(sortByType);
   
      return (
      {
         "luceneQuery": filterQuery,
         "items": items
      });
   }
   catch(e)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
      return;
   }
}


function sortByType(a, b)
{
   if (a.isContainer == b.isContainer)
   {
      return (b.name.toLowerCase() > a.name.toLowerCase() ? -1 : 1);
   }
   return (a.isContainer ? -1 : 1);
}