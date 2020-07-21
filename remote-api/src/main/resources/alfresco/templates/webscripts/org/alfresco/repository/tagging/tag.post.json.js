function main()
{
   // Get the store reference
   var store = url.templateArgs.store_type + "://" + url.templateArgs.store_id;

   // Get the details of the tag
   if (json.has("name") == false || json.get("name").length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Name missing when creating tag");
      return;
   }
   var tagName = json.get("name");
   
   // See if the tag already exists
   var tag = taggingService.getTag(store, tagName),
      tagExists = (tag != null);
   
   if (!tagExists)
   {
      tag = taggingService.createTag(store, tagName);
      if (tag == null)
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "error.cannotCreateTag");
         return;
      }
   }
   
   // Put the created tag into the model
   model.tag = tag;
   model.tagExists = tagExists;
}

main();