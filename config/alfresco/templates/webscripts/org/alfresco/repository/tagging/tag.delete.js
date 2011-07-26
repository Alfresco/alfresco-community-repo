function main()
{
   // Get the store from the template args
   var store = "workspace://SpacesStore";
   if (url.templateArgs.store_type != null && url.templateArgs.store_id != null)
   {
      store = url.templateArgs.store_type + "://" + url.templateArgs.store_id;
   }
   
   // Get the tagName
   var tagName = url.templateArgs.tagName;
   if (tagName == null || tagName.length == 0)
   {
      // TagName missing
      model.msg = "manage-tags.tag-name-missing";
      model.result = false;
   }
   else
   {
      // Search the tag
      var tag = taggingService.getTag(store, url.templateArgs.tagName);
      if (tag == null)
      {
         // Tag not found
         model.msg = "manage-tags.tag-not-found";
         model.result = false;
      }
      else
      {
         try
         {
            // Get list of tagged node
            var taggedNodes = search.tagSearch(store,tagName); 
            for (var i=0; i < taggedNodes.length; i++)
            {
               // Remove tag from the node
               taggedNodes[i].removeTag(tagName);
            }
            // Remove the tag node
            taggingService.deleteTag(store,tagName);
            // Successfully removing tag
            model.msg = "manage-tags.delete-success";
            model.result = true;
         }
         catch(e)
         {
            // Unable to remove Tag
            model.msg = "manage-tags.delete-failure";
            model.result = false;
         }
      }
   }
}

main();