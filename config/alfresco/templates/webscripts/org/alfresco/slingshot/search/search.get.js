/**
 * Search Component: search
 *
 * Inputs:
 *   optional: site = the site to search into.
 *   optional: container = the component the search in
 *
 * Outputs:
 *  data.items/data.error - object containing list of search results
 */

const DEFAULT_MAX_RESULTS = 100;
const SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";

function getDocumentItem(siteId, containerId, restOfPath, node)
{
   // check whether this is a folder or a file
   if (node.isContainer)
   {
      // PENDING - return a folder result
      return null;
   }
   else if (node.isDocument)
   {
      var item = {};
      item.site = siteId;
      item.container = containerId;
      item.nodeRef = node.nodeRef.toString();
      item.type = "file";
      item.icon32 = node.icon32;
      item.qnamePath = node.qnamePath;
      item.viewUrl = "/proxy/alfresco/api/node/content/" + node.nodeRef.toString().replace('://', '/') + "/" + node.name;
      item.detailsUrl = "page/site/" + siteId + "/" + containerId;
      item.containerUrl = "page/site/" + siteId + "/" + containerId;
      item.name = node.name;
      item.title = node.properties["cm:title"];
      return item;
   }
   else
   {
      return null;
   }
}

/**
 * Delegates the extraction to the correct extraction function
 * depending site/container id.
 */
function getItem(siteId, containerId, restOfPath, node)
{
   if (containerId == "documentLibrary")
   {
      return getDocumentItem(siteId, containerId, restOfPath, node);
   }
}

/**
 * Returns an array with [0] = site and [1] = container or null if the node does not match
 */
function splitQNamePath(node)
{
   var path = node.qnamePath;
   
   if (path.match("^"+SITES_SPACE_QNAME_PATH) != SITES_SPACE_QNAME_PATH)
   {
      return null;
   }
   var tmp = path.substring(SITES_SPACE_QNAME_PATH.length);
   var pos = tmp.indexOf('/');
   if (pos < 1)
   {
      return null;
   }
   var siteId = tmp.substring(0, pos);
   siteId = siteId.substring(siteId.indexOf(":") + 1);
   tmp = tmp.substring(pos + 1);
   pos = tmp.indexOf('/');
   if (pos < 1)
   {
      return null;
   }
   var containerId = tmp.substring(0, pos);
   containerId = containerId.substring(containerId.indexOf(":") + 1);
   var restOfPath = tmp.substring(pos + 1);
   
   return [ siteId, containerId, restOfPath ];
}

/**
 * Processes the search results. Filters out unnecessary nodes
 * 
 * @return the final search results object
 */
function processResults(nodes, maxResults)
{    
   var results = new Array();
   
   var added = 0;
   for (var x=0; x < nodes.length && added < maxResults; x++)
   {
      // for each node we extract the site/container qname path and then
      // let the per-container helper function decide what to do
      var parts = splitQNamePath(nodes[x]);
      if (parts == null)
      {
         continue;
      }
      
      var item = getItem(parts[0], parts[1], parts[2], nodes[x]);
      if (item != null)
      {
         results.push(item);
         added++;
      }
   }
   
   return ({
         "items": results
   });
}

/* Create collection of documents */
function getSearchResults(term, maxResults, siteId, containerId)
{
   //try
   //{
      var path = SITES_SPACE_QNAME_PATH; // "/app:company_home/st:sites/";
      if (siteId != null && siteId.length > 0)
      {
         path += "cm:" + siteId + "/";
      }
      else
      {
         path += "*/";
      }
      if (containerId != null && containerId.length > 0)
      {
         path += "cm:" + containerId + "/";
      }
      else
      {
         path += "*/";
      }
	  
      var luceneQuery = "+PATH:\"" +path     + "/*\"";
      if (term.length > 0)
      {
         luceneQuery += " +(" +
                        "    TEXT:\"" + term + "\"" +
                        "    @cm\\:name:\"" + term + "\"" +
                        "  )";
      }
         
      var nodes = search.luceneSearch(luceneQuery);
       
      return processResults(nodes, maxResults);
   //}
   //catch(e)
   //{
   //   return { error: e.toString() };
   //}
}


function main()
{
   var siteId = (args["site"] != undefined) ? args["site"] : null;
   var containerId = (args["container"] != undefined) ? args["container"] : null;
   var term = args["term"];
   var maxResults = (args["maxResults"] != undefined) ? parseInt(args["maxResults"]) : DEFAULT_MAX_RESULTS;
   
   model.data = getSearchResults(term, maxResults, siteId, containerId);
   var x=0;
}

main();



/*
      // put together a search path
      
      // siteId input
      var site = siteService.getSite(siteId);
      if (site === null)
      {
         return jsonError("Site not found: " + siteId);
      }
   
      var containerNode = site.getContainer(containerId);
      if (containerNode === null)
      {
         return jsonError("Document Library container not found in: " + siteId + ". (No write permission?)");
      }

      return { path : containerNode.qnamePath };
      
      / *return ({
         "items": items
      });* /
      
*/