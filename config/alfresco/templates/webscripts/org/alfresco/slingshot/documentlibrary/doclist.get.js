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
model.doclist = getDoclist(args["site"], args["path"], args["type"]);

/* Create collection of documents and folders in the given space */
function getDoclist(siteId, path, type)
{
   try
   {
      var items = new Array();
   
      /* siteId input */
      var site = siteService.getSite(siteId);
      if (site === null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Site not found: '" + siteId + "'");
         return;
      }
   
      var parentNode = site.getContainer("documentLibrary");
      if (parentNode === null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Document Library container not found in: " + siteId + ". (No write permission?)");
         return;
      }

      /* path input */
      if ((path !== null) && (path != ""))
      {
         parentSpace = parentNode.childByNamePath(path);
      }
      else
      {
         parentSpace = parentNode;
      }

      if (parentSpace === null)
      {
         parentSpace = parentNode;
      }

      var showDocs = true;
      var showFolders = true;
      
      if ((type !== null) && (type != ""))
      {
         showDocs = (type == "documents");
         showFolders = (type == "folders");
      }
   
      for each(item in parentSpace.children)
      {
         if ((item.isContainer && showFolders) || (item.isDocument && showDocs))
         {
            items.push(item);
         }
      }
   
      items.sort(sortByType);
   
      return ({
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