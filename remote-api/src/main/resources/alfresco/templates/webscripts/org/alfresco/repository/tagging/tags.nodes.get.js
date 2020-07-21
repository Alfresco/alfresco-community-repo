function main()
{
   // Get the store reference
   var store = url.templateArgs.store_type + "://" + url.templateArgs.store_id;
   
   // Get the tag
   var tag = url.templateArgs.tag;
   if (tag == null)
   {
      // Error since no tag specified on the URL
      status.setCode(404, "No tag specified");
      return;
   }
   
   // do the search
   model.nodes = search.tagSearch(store, tag);
}

main();