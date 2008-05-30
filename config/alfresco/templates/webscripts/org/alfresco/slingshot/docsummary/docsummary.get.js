
/**
 * Document Summary Component: docsummary
 *
 * Inputs:
 *  mandatory: site = the site to get documents from
 *   optional: filter = the filter to apply
 *
 * Outputs:
 *  docs - object containing list of documents
 */
model.docs = getDocs(args["site"], args["filter"]);

/* Create collection of documents */
function getDocs(siteId, filter)
{
   try
   {
      /* siteId input */
      var site = siteService.getSite(siteId);
      if (site === null)
      {
         return jsonError("Site not found: " + siteId);
      }
   
      var parentNode = site.getContainer("documentLibrary");
      if (parentNode === null)
      {
         return jsonError("Document Library container not found in: " + siteId + ". (No write permission?)");
      }

      // build up the query to get documents modified in the last 7 days
      var path = parentNode.qnamePath + "//*";
      
      var date = new Date();
      var toQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();
      date.setDate(date.getDate() - 7);
      var fromQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();
      
      search.setStoreUrl("workspace://SiteStore");
      var query = "+TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" +PATH:\"" + 
                  path + "\" +@cm\\:modified:[" + fromQuery + "T00\\:00\\:00 TO " + 
                  toQuery + "T23\\:59\\:59]";
      
      logger.log("docsummary query = " + query);
      
      var docs = search.luceneSearch(query, "cm:modified", false);
      
      logger.log("number of results = " + docs.length);
      
      var items = null;
      
      // restrict results to 10 items if necessary
      if (docs.length > 10)
      {
         items = new Array();
         for (var x = 0; x < 10; x++)
         {
            items.push(docs[x]);
         }
      }
      else
      {
         items = docs;
      }
      
      return ({
         "items": items
      });
   }
   catch(e)
   {
      return jsonError(e.toString());
   }
}


/* Format and return error object */
function jsonError(errorString)
{
   var obj =
   {
      "error": errorString
   };
   
   return obj;
}
