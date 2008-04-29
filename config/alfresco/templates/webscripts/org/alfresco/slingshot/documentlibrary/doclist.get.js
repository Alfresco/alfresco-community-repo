/*
 * doclist
 *
 * Inputs:
 *  mandatory: nodeRef = parent space nodeRef
 *
 * Outputs:
 *  doclist - object containing list of child folders and documents in the parent space
 */
model.doclist = getDoclist(args["nodeRef"], args["path"], args["type"]);

/* Create collection of documents and folders in the given space */
function getDoclist(nodeRef, path, type)
{
   var items = new Array();
   
   /* nodeRef input */
   var parentSpace = null;
   if ((nodeRef != null) && (nodeRef != ""))
   {
      parentSpace = search.findNode(nodeRef);
   }
   else if ((path != null) && path != "")
   {
      parentSpace = companyhome.childByNamePath(path);
   }
   if (parentSpace == null)
   {
      // return jsonError("Parent space nodeRef not supplied");
      parentSpace = companyhome;
   }

   var showDocs = true,
      showFolders = true;
      
   if ((type != null) && (type != ""))
   {
      showDocs = (type == "documents");
      showFolders = (type == "folders");
   }
   
   for each(item in parentSpace.children)
   {
      if ((item.isContainer && showFolders) || (!item.isContainer && showDocs))
      {      
         items.push(item);
      }
   }
   
   items.sort(sortByType);
   
   return ({
      "items": items
   });
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