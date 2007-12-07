/*
 * calendarSummary
 *
 * Inputs:
 *    nodeRef = Calendar space nodeRef
 *
 * Outputs:
 *    calendarSummary - object containing
 *       eventsthis - Number of events occuring this
 *       eventsTomorrow - Number of events occuring tomorrow
 */
model.calendarSummary = main(args["nodeRef"]);
 
function main(nodeRef)
{
   var thisWeek = 0,
       nextWeek = 0;

   if (nodeRef != null)
   {
      var calSpace = search.findNode(nodeRef);
      if (calSpace != null)
      {
         // generate lucene PATH to calendar events
         var path = calSpace.qnamePath + "/cm:CalEvents/*";
         
         // find the events scheduled for this week
         var fromDate = new Date();
         // go back to previous Sunday
         fromDate.setDate(fromDate.getDate() - fromDate.getDay());
         var fromQuery = fromDate.getFullYear() + "\\-" + (fromDate.getMonth()+1) + "\\-" + fromDate.getDate();
         var toDate = new Date();
         toDate.setDate(fromDate.getDate() + 7);
         var toQuery = toDate.getFullYear() + "\\-" + (toDate.getMonth()+1) + "\\-" + toDate.getDate();
         var events = search.luceneSearch("+PATH:\"" + path + '"' +
            " +@ia\\:fromDate:[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]");
         thisWeek = events.length;
         
         // increment to and from dates to cover next week
         fromDate.setDate(toDate.getDate());
         fromQuery = fromDate.getFullYear() + "\\-" + (fromDate.getMonth()+1) + "\\-" + fromDate.getDate();
         toDate.setDate(toDate.getDate() + 7);
         toQuery = toDate.getFullYear() + "\\-" + (toDate.getMonth()+1) + "\\-" + toDate.getDate();
         events = search.luceneSearch("+PATH:\"" + path + '"' +
            " +@ia\\:fromDate:[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59]");
         nextWeek = events.length;
      }
   }
   
   var calendarSummary =
   {
      "thisWeek": thisWeek,
      "nextWeek": nextWeek
   };
   return calendarSummary
}