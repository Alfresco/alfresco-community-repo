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
   if (tagName == null || tagName == "") 
   {
      // TagName missing
      model.msg = "manage-tags.tag-name-missing";
      model.result = false;
   } 
   else 
   {
      tagName = tagName.toLowerCase();
      
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
         // Get the new name for the tag
         var newTagName;
         if (json.has("name") == false || json.get("name").length() == 0)
         {
            // Empty new name
            status.code = 400;
            status.redirect = true;
            status.message = "manage-tags.empty-name";
         } 
         else 
         {
            newTagName = json.get("name").toLowerCase();
            if (newTagName == tagName) 
            {
               // Duplciate name
               status.code = 400;
               status.redirect = true;
               status.message = "manage-tags.duplicate-name";
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
                     // Add new tag
                     taggedNodes[i].addTag(newTagName);
                  }
                  
                  // Remove the tag node
                  taggingService.deleteTag(store,tagName);
                  
                  // Successfully removing tag
                  model.msg = "manage-tags.edit-success";
                  model.result = true;
               } 
               catch(e)
               {
                  // Unable to remove Tag
                  model.msg = "manage-tags.edit-failure";
                  model.result = false;
               }
            }
         }
      }
   }
}

main();