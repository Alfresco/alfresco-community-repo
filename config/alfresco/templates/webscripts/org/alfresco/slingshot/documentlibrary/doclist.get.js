/*
 * doclist
 *
 * Inputs:
 *  mandatory: nodeRef = parent space nodeRef
 *
 * Outputs:
 *  doclist - object containing list of child folders and documents in the parent space
 */
model.doclist = getDoclist(args["nodeRef"]);

/* Create collection of documents and folders in the given space */
function getDoclist(nodeRef)
{
   var items = new Array();
   
   /* nodeRef input */
   var parentSpace = null;
   if ((nodeRef != null) && (nodeRef != ""))
   {
      parentSpace = search.findNode(nodeRef);
   }
   if (parentSpace == null)
   {
      return jsonError("Parent space nodeRef not supplied");
   }
   
   for each(item in parentSpace.children)
   {
      items.push(item);
   }
   
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