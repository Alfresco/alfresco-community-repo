/*
JavaException: org.springframework.dao.DataIntegrityViolationException: could not update: [org.alfresco.repo.domain.hibernate.ChildAssocImpl#567]; nested exception is org.hibernate.exception.ConstraintViolationException: could not update: [org.alfresco.repo.domain.hibernate.ChildAssocImpl#567]
*/

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
         return jsonError("Site not found: " + siteId);
      }
   
      var parentNode = site.getContainer("documentLibrary");
      if (parentNode === null)
      {
         return jsonError("Document Library container not found in: " + siteId + ". (No write permission?)");
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

      var showDocs = true,
         showFolders = true;
      
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

function sortByType(a, b)
{
   if (a.isContainer == b.isContainer)
   {
      return (b.name.toLowerCase() > a.name.toLowerCase() ? -1 : 1);
   }
   return (a.isContainer ? -1 : 1);
}