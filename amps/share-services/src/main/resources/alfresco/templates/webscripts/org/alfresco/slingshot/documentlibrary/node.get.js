<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/evaluator.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

/**
 * Main entry point: Return single document or folder given it's nodeRef
 *
 * @method getDoclist
 */
function getDoclist()
{
   // Use helper function to get the arguments
   var parsedArgs = ParseArgs.getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }
   
   parsedArgs.pathNode = ParseArgs.resolveNode(parsedArgs.nodeRef);
   parsedArgs.location = Common.getLocation(parsedArgs.pathNode, parsedArgs.libraryRoot);

   var filter = args.filter,
      items = [];

   var favourites = Common.getFavourites(),
      node = parsedArgs.pathNode,
      parent = null;

   var thumbnail = null,
       item = Evaluator.run(node);

   item.isFavourite = (favourites[node.nodeRef] === true);
   item.likes = Common.getLikes(node);

   item.location =
   {
      site: parsedArgs.location.site,
      siteTitle: parsedArgs.location.siteTitle,
      container: parsedArgs.location.container,
      path: parsedArgs.location.path,
      file: node.name
   };

   item.location.parent = {};
   if (node.parent != null && node.parent.isContainer && node.parent.hasPermission("Read"))
   {
      parent =
      {
         node: node.parent,
         userAccess: Evaluator.run(node.parent, true).actionPermissions
      };
      item.location.parent.nodeRef = String(node.parent.nodeRef.toString());  
   }

   // Special case for container and libraryRoot nodes
   if ((parsedArgs.location.containerNode && String(parsedArgs.location.containerNode.nodeRef) == String(node.nodeRef)) ||
      (parsedArgs.libraryRoot && String(parsedArgs.libraryRoot.nodeRef) == String(node.nodeRef)))
   {
      item.location.file = "";
   }
   
   return (
   {
      parent: parent,
      onlineEditing: utils.moduleInstalled("org.alfresco.module.vti"),
      items: [item]
   });
}

/**
 * Document List Component: doclist
 */
model.doclist = getDoclist();