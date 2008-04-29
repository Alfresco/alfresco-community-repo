/*
 * projectSpace
 *
 * Inputs:
 *  mandatory: nodeRef = parent space nodeRef
 *
 * Outputs: projectSpace - object containing pproject space details
 */
model.projectSpace = main(args["nodeRef"]);

function main(nodeRef)
{
   var title = "";
   var subSpaces = {};
   var space = search.findNode(nodeRef);
   
   if (space != null)
   {
      title = space.name;
      // Discover the nodeRef of each project subspace
      for each(node in space.children)
      {
         if (node.hasAspect("{http://www.alfresco.org/model/content/1.0}projectsummary"))
         {
            subSpaces[node.name] = node;
         }
      }
   }
   
   var projectSpace =
   {
      "title": title,
      "subSpaces": subSpaces
   };
   return projectSpace;
}
