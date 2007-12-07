/*
 * docSummary
 *
 * Inputs:
 *    nodeRef = doc space nodeRef
 *
 * Outputs:
 *    docSummary - object containing
 *       numNew - number new documents added in the last 7 days
 *       numModified - number of documents modified in the last 7 days
 */
model.docSummary = main(args["nodeRef"]);
 
function main(nodeRef)
{
   var numNew = 0,
      numModified = 0;

   if (nodeRef != null)
   {
      var space = search.findNode(nodeRef);
      
      if (space != null)
      {
         // generate lucene PATH to get all child documents
         var path = space.qnamePath + "//*";
         
         var date = new Date();
         var toQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();
         date.setDate(date.getDate() - 7);
         var fromQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();

         // documents created in the last 7 days
         var docs = search.luceneSearch("+PATH:\"" + path + "\"" +
            " +@cm\\:created:[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]");
         numNew = docs.length;
         
         // documents modified in the last 7 days
         docs = search.luceneSearch("+PATH:\"" + path + "\"" +
            " +@cm\\:modified:[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]");
         
         numModified = 0;
         for each(doc in docs)
         {
            if (doc.properties["cm:modified"] - doc.properties["cm:created"] > 1000)
            {
               ++numModified;
            }
         }
      }
   }
   
   var docSummary =
   {
      "numNew": numNew,
      "numModified": numModified
   };
   return docSummary;
}
