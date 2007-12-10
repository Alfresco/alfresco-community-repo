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
         // generate lucene PATH to get all child documents
         var path = space.qnamePath + "//*";
         var nodes = search.luceneSearch("+PATH:\"" + path + "\"" +
            " +(@cm\\:content.mimetype:\"text/plain\" OR @cm\\:content.mimetype:\"text/html\")");

         for each(node in nodes)
         {
            if ((node.hasAspect("blg:blogPost")) && (node.properties["blg:published"] == true))
            {
               if (node.properties["cm:modified"] - node.properties["blg:lastUpdate"] > 5000)
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
