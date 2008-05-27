/**
 * Document List Component: treenode
 *
 * Inputs:
 *  mandatory: site = the site containing the document library
 *   optional: path = folder relative to root store
 *
 * Outputs:
 *  treenode - object containing list of child folder nodes for a TreeView widget
 */
model.treenode = getTreenode(args["site"], args["path"]);

/* Create collection of folders in the given space */
function getTreenode(siteId, path)
{
   try
   {
      var items = new Array();
   
      /* siteId input */
      var site = siteService.getSite(siteId);
      if (site === null)
      {
         return jsonError("Site not found: " + siteId);
      }
   
      // var parentNode = site.getComponentContainer("documentLibrary");
      var parentNode = companyhome; // TODO: Remove hack
      if (parentNode === null)
      {
         return jsonError("Document Library container not found in: " + siteId);
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

      for each(item in parentSpace.children)
      {
         if (item.isContainer)
         {
            items.push(item);
         }
      }
   
      items.sort(sortByName);
   
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

/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.name.toLowerCase() > a.name.toLowerCase() ? -1 : 1);
}