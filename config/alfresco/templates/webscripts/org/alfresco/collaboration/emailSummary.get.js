/*
 * emailSummary
 *
 * Inputs:
 *    nodeRef = email space nodeRef
 *
 * Outputs:
 *    emailSummary - object containing
 *       numToday - number of articles updated since being published
 *       numWeek - number of unpublished articles
 */
model.emailSummary = main(args["nodeRef"]);
 
function main(nodeRef)
{
   var numToday = 0,
      numWeek = 0;

   if (nodeRef != null)
   {
      var space = search.findNode(nodeRef);
      
      if (space != null)
      {
         // generate lucene PATH to get all child documents
         var path = space.qnamePath + "//*";
         
         var date = new Date();
         var toQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();
         var fromQuery = toQuery;

         // emails today
         var docs = search.luceneSearch("+PATH:\"" + path + "\"" +
            " +@cm\\:created:[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]");
         numToday = docs.length;
         
         date.setDate(date.getDate() - 7);
         fromQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();

         // documents modified in the last 7 days
         docs = search.luceneSearch("+PATH:\"" + path + "\"" +
            " +@cm\\:modified:[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]");
         
         numWeek = docs.length;
      }
   }
   
   var emailSummary =
   {
      "numToday": numToday,
      "numWeek": numWeek
   };
   return emailSummary;
}
