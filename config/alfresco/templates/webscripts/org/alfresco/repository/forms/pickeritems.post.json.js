function main()
{
   var count = 0,
      items = [],
      results = [];

   // extract mandatory data from request body
   if (!json.has("items"))
   {
       status.setCode(status.STATUS_BAD_REQUEST, "items parameter is not present");
       return;
   }
   
   // convert the JSONArray object into a native JavaScript array
   var jsonItems = json.get("items"),
      numItems = jsonItems.length(),
      item, result, resultObj;
   
   for (count = 0; count < numItems; count++)
   {
      item = jsonItems.get(count);
      if (item != "")
      {
         result = search.findNode(item);
         if (result != null)
         {
            results.push(
            {
               item: result
            });
         }
      }
   }

   if (logger.isLoggingEnabled())
       logger.log("#items = " + count + ", #results = " + results.length);

   model.results = results;
}

main();
