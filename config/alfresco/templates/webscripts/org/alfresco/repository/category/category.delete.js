/**
 * Deletes a given category.
 */

function main()
{
   // Extract template args and build the node reference
   var nodeRef = url.templateArgs.store_protocol + "://" + url.templateArgs.store_id + "/" + url.templateArgs.node_id;
   
   // Log information
   if (logger.isLoggingEnabled() == true)
   {
      logger.log("Attempting to remove category (nodeRef=" + nodeRef + ")");
   }
   
   // Try and find the category node
   var category = classification.getCategory(nodeRef);
   
   // Return 404 if category is not found
   if (category == null)
   {
      // Log information
      if (logger.isLoggingEnabled() == true)
      {
         logger.log("Category being removed could not be found. (nodeRef=" + nodeRef + ")");
      }
      
      // Set status code
      status.setCode(status.STATUS_NOT_FOUND, "Category could not be found. (nodeRef=" + nodeRef + ")");
      return;
   }
   
   // Remove the category
   category.removeCategory();
   
   // Log information
   if (logger.isLoggingEnabled() == true)
   {
      logger.log("Category successfully removed. (nodeRef=" + nodeRef + ")");
   }
   
   // The message depends on the search service in use
   var messageKey = "message.removeCategory.success";
   if (search.searchSubsystem == "solr")
   {
      messageKey = "message.removeCategory.solr.success";
   }

   // Set model properties
   model.messageKey = messageKey;
}

main();
