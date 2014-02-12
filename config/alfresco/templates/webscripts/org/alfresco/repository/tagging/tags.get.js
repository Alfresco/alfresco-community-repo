function main()
{
   // Get the store reference
   var store = url.templateArgs.store_type + "://" + url.templateArgs.store_id;
   
   var tagNames;
   var tags = [];
   
   var filter = args["tf"];
   var details = args["details"];
   var from = args["from"];
   var size = args["size"];
   
   if (filter === null)
   {
      if(from === null || size === null)
      {
          // Get all the tags
          tagNames = taggingService.getTags(store);
          model.totalRecords = tagNames.length;
      }
      else
      {
          // Get page of the tags
          var pagedTagsWrapper = taggingService.getPagedTags(store, from, size);
          tagNames = pagedTagsWrapper.getTagNames();
          model.totalRecords = pagedTagsWrapper.getTotal();
      }
   }
   else
   {
      if(from === null || size === null)
      {
          // Get a list of filtered tags
          tagNames = taggingService.getTags(store, filter);
          model.totalRecords = tagNames.length;
      }
      else
      {
          // Get a page of filtered tags
          var pagedTagsWrapper = taggingService.getPagedTags(store, filter, from, size);
          tagNames = pagedTagsWrapper.getTagNames();
          model.totalRecords = pagedTagsWrapper.getTotal();
      }
   }
   
   // Sort by tag name
   tagNames.sort(sortByName);
   
   if (details === "true")
   {
      model.details = true;         
      for each (var tagName in tagNames)
      { 
         var tag = taggingService.getTag(store, tagName);
         tags.push(tag);
      }     
   }
   else
   {
      model.details = false;
      tags = tagNames;
   }
   
   model.tags = tags;
}

/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.toLowerCase() > a.toLowerCase() ? -1 : 1);
}

main();