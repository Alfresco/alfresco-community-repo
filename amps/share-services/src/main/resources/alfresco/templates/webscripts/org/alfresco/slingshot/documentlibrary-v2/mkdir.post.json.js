/**
 * Create folder paths under a node, ignoring existing folders and creating as needed
 * Emulating the functionality provided by "mkdir -p" unix command.
 * 
 * @param destination (string) - NodeRef destination for the paths i.e. parent node for all paths
 * @param paths (Array) - List of path Strings to be created under the given parent node
 * @since 5.2
 */

function main()
{
   // get params and check destination nodeRef exists
   if (json.has("destination"))
   {
      var destination = json.get("destination");
   }
   if (json.has("paths"))
   {
      var paths = json.get("paths");
   }
   if (!destination || !paths)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Required parameters are missing");
      return;
   }
   var destNode = search.findNode(destination);
   if (destNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Cannot find specified destination node");
   }
   
   // process each path and create the folder, store created noderefs
   var nodeRefs = [];
   
   for (var i=0, path; i<paths.length(); i++)
   {
      // coerce to JavaScript String object
      path = "" + paths.get(i);
      // Remove any leading "/" from the path
      if (path.charAt(0) === '/')
      {
         path = path.substr(1);
      }
      
      nodeRefs.push(destNode.createFolderPath(path).nodeRef);
   }
   
   model.nodeRefs = nodeRefs;
}
   
main();