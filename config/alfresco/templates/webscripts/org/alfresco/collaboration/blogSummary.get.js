/*
 * blogSummary
 *
 * Inputs:
 *    nodeRef = blog space nodeRef
 *
 * Outputs:
 *    blogSummary - object containing
 *       numUpdates - number of articles updated since being published
 *       numPending - number of unpublished articles
 */
model.blogSummary = main(args["nodeRef"]);
 
function main(nodeRef)
{
   var numUpdates = 0,
      numPending = 0;

   if (nodeRef != null)
   {
      var space = search.findNode(nodeRef);
      
      if (space != null)
      {
         for each(node in space.children)
         {
            if ((node.hasAspect("blg:blogPost")) && (node.properties["blg:published"] == true))
            {
               if (node.properties["cm:modified"] > node.properties["blg:lastUpdate"])
               {
                  ++numUpdates;
               }
            }
            else
            {
               ++numPending;
            }
         }
      }
   }
   
   var blogSummary =
   {
      "numUpdates": numUpdates,
      "numPending": numPending
   };
   return blogSummary;
}
