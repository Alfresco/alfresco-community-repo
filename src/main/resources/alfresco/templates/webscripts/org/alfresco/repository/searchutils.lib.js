/*
 * Search related utility functions
 */

const FUTURE_DATE = "3000\\-12\\-31T00:00:00";
const ZERO_DATE = "1970\\-01\\-01T00:00:00";

/**
 * Returns the date object for the date "numdays" ago
 */
function getTodayMinusXDays(numdays)
{
   var date = new Date();
   var dateMillis = new Date().getTime();
   dateMillis -= 1000 * 60 * 60 * 24 * numdays;
   date.setTime(dateMillis);
   
   // PENDING: should it be from the beginning of the date or exactly x days back?
   
   return date;
}


/**
 * Returns the date string as required by Lucene,
 * thus in the format "1970\\-01\\-01T00:00:00"
 *
 * Note: hours/minutes/seconds are currently NOT taken into account
 */
function getLuceneDateString(date)
{
   var temp = new Date();
   temp.setTime(Date.parse(date));
   return temp.getFullYear() + "-" + (temp.getMonth() + 1) + "\\-" +
     (temp.getDate() < 10 ? "0" + temp.getDate() : temp.getDate()) + "T00:00:00";
}

/**
 * Returns the creation date range query for the given dates.
 */
function getCreationDateRangeQuery(fromDate, toDate)
{
   var luceneQuery = " +@cm\\:created:[";
   if (fromDate !== null && ! isNaN(fromDate.getTime()))
   {
      luceneQuery += getLuceneDateString(fromDate);
   }
   else
   {
      luceneQuery += ZERO_DATE;
   }
   luceneQuery += " TO ";
   if (toDate !== null && ! isNaN(toDate.getTime()))
   {
      luceneQuery += getLuceneDateString(toDate);
   }
   else
   {
      luceneQuery += FUTURE_DATE;
   }
   luceneQuery += "] ";
   return luceneQuery;
}

