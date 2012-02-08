/**
 * Example JSON:
 * {
 *    "name" : "myCategory",
 *    "aspect" : "{http://www.alfresco.org/model/content/1.0}generalclassifiable"
 * }
 * 
 * where "name" is the name of the new category
 *       "aspect" is an optional field used if creating a root category, it defaults to "{http://www.alfresco.org/model/content/1.0}generalclassifiable" 
 *       if none specified 
 */

// JSON property values
var PROP_NAME = "name";
var PROP_ASPECT = "aspect";

// Default property values
var DEFAULT_ASPECT = "{http://www.alfresco.org/model/content/1.0}generalclassifiable";

function main()
{
   // Check we have a json request
   if (typeof json === "undefined")
   {
      if (logger.isWarnLoggingEnabled() == true)
      {
         logger.warn("Could not create category, because json object was undefined.");
      }
      
      status.setCode(501, "Could not create category, because json object was undefined.");
      return;
   }
   
   // Try and retrieve the name of the new category
   var name = json.get(PROP_NAME);
   if (name == null || name.length === 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Could not create category, because 'prop_cm_name' parameter is missing from json request.");
      return;
   }
    
   // Build the node reference string
   var nodeRef = null;
   var aspect = null;
   if (url.templateArgs.length != 0)
   {       
      nodeRef = url.templateArgs.store_protocol + "://" + url.templateArgs.store_id + "/" + url.templateArgs.node_id;
      
      // Log message
      if (logger.isLoggingEnabled() == true)
      {
         logger.log("Attempting to create new category. (nodeRef=" + nodeRef + ", name=" + name + ")");
      }
   }
   else
   {
      // See if an aspect has been specified
      var aspect = DEFAULT_ASPECT;
      if (json.has(PROP_ASPECT) == true)
      {
         aspect = json.get(PROP_ASPECT);
      }
   }
    
   if (nodeRef != null)
   {
      var category = classification.getCategory(nodeRef);
      if (category !== null)
      {
         model.persistedObject = category.createSubCategory(name);
      }
      else
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Unable to create category, because category node was found or is not a category.  (nodeRef=" + nodeRef + ", name=" + name + ")");
         return;
      }
   }
   else
   {
      model.persistedObject = classification.createRootCategory(aspect, name);
   }

   // The message depends on the search service in use
   var messageKey = "message.addCategory.success";
   if (search.searchSubsystem == "solr")
   {
      messageKey = "message.addCategory.solr.success";
   }
   
   // Build the rest of the model 
   model.messageKey = messageKey;
   model.name = name;
}

main();
