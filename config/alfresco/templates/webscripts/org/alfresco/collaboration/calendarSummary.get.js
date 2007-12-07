/*
 * calendarSummary
 *
 * Inputs:
 *    nodeRef = Calendar space nodeRef
 *
 * Outputs:
 *    calendarSummary - object containing
 *       eventsToday - Number of events occuring today
 *       eventsTomorrow - Number of events occuring tomorrow
 */
model.calendarSummary = main(args["nodeRef"]);
 
function main(nodeRef)
{
   var eventsToday = 0,
       eventsTomorrow = 0;

   if (nodeRef != null)
   {
      var calSpace = search.findNode(nodeRef);
      if (calSpace != null)
      {
         // generate lucene PATH to calendar events
         var path = calSpace.qnamePath + "/cm:CalEvents/*";
         
         // find the events scheduled for today
         var t = new Date();
         var todayQuery = t.getFullYear() + "\\-" + (t.getMonth()+1) + "\\-" + t.getDate();
         var events = search.luceneSearch("+PATH:\"" + path + '"' +
            " +@ia\\:fromDate:[" + todayQuery + "T00\\:00\\:00 TO " + todayQuery + "T23\\:59\\:59]");
         eventsToday = events.length;
         
         // inc date to tommorow
         t.setDate(t.getDate() + 1);
         var tomQuery = t.getFullYear() + "\\-" + (t.getMonth()+1) + "\\-" + t.getDate();
         events = search.luceneSearch("+PATH:\"" + path + '"' +
            " +@ia\\:fromDate:[" + tomQuery + "T00\\:00\\:00 TO " + tomQuery + "T23\\:59\\:59]");
         eventsTomorrow = events.length;
      }
   }
   
   var calendarSummary =
   {
      "eventsToday": eventsToday,
      "eventsTomorrow": eventsTomorrow
   };
   return calendarSummary
}