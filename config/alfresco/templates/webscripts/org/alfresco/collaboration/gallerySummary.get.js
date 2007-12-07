/*
 * gallerySummary
 *
 * Inputs:
 *    nodeRef = gallery space nodeRef
 *
 * Outputs:
 *    gallerySummary - object containing
 *       numNew - number of new images in the last 7 days
 *       numTotal - total number of images
 */
model.gallerySummary = main(args["nodeRef"]);
 
function main(nodeRef)
{
   var numNew = 0,
      numTotal = 0;

   if (nodeRef != null)
   {
      var space = search.findNode(nodeRef);
      
      if (space != null)
      {
         // generate lucene PATH to all gallery images
         var path = space.qnamePath + "//*";
         
         var date = new Date();
         var toQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();
         date.setDate(date.getDate() - 7);
         var fromQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();

         // images added in the last 7 days
         var images = search.luceneSearch("+PATH:\"" + path + "\"" +
            " +@cm\\:created:[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]");
         numNew = images.length;
         
         // total images
         images = search.luceneSearch("+PATH:\"" + path + "\"");
         numTotal = images.length;
      }
   }
   
   var gallerySummary =
   {
      "numNew": numNew,
      "numTotal": numTotal
   };
   return gallerySummary;
}
