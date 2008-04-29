/*
 * forumSummary
 *
 * Inputs:
 *    nodeRef = forum space nodeRef
 *
 * Outputs:
 *    forumSummary - object containing
 *       numPosts - posts in last 7 days
 *       numTopics - new topics in last 7 days
 */
model.forumSummary = main(args["nodeRef"]);
 
function main(nodeRef)
{
   var numPosts = 0,
      numTopics = 0;

   if (nodeRef != null)
   {
      var space = search.findNode(nodeRef);
      
      if (space != null)
      {
         // generate lucene PATH to get forums
         var path = space.qnamePath + "//*";
         
         var date = new Date();
         var toQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();
         date.setDate(date.getDate() - 7);
         var fromQuery = date.getFullYear() + "\\-" + (date.getMonth()+1) + "\\-" + date.getDate();

         // posts created in the last 7 days
         var docs = search.luceneSearch("+PATH:\"" + path + "\"" +
            " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"" +
            " +@cm\\:created:[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]");
         numPosts = docs.length;
         
         // topics created in the last 7 days
         docs = search.luceneSearch("+PATH:\"" + path + "\"" +
            " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}topic\"" +
            " +@cm\\:created:[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]");
         numTopics = docs.length;
      }
   }
   
   var forumSummary =
   {
      "numPosts": numPosts,
      "numTopics": numTopics
   };
   return forumSummary;
}

// var query = "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\"";


// 	{http://www.alfresco.org/model/forum/1.0}topic
// 	{http://www.alfresco.org/model/forum/1.0}post