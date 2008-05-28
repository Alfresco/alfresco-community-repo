/**
 * Document List Component: action
 *
 * Inputs:
 *  mandatory: action = the action to perform
 *             site = the site containing the document library
 *   optional: path = folder relative to root store
 *             file = the file involved in the action
 *
 * Outputs:
 *  action - object containing result of performing action
 */
model.action = doAction(args["action"], args["site"], args["path"], args["file"]);

/* Perform the requested action */
function doAction(action, siteId, path, file)
{
   try
   {
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
