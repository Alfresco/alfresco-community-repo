function main()
{
   var ta_storeType = url.templateArgs['store_type'];
   var ta_storeId = url.templateArgs['store_id'];
   var ta_id = url.templateArgs['id'];
   var ta_mode = url.templateArgs['mode'];
   var ta_path = url.templateArgs['path'];
   
   var nodeRef = '';
   // The template argument 'path' only appears in the second URI template.
   if (ta_path != null)
   {
      nodeRef = ta_path;
   }
   else
   {
   	nodeRef = ta_storeType + '://' + ta_storeId + '/' + ta_id;
   }
   
   if (logger.isLoggingEnabled())
   {
	   logger.log("JSON POST request received for nodeRef: " + nodeRef);
   }

   //TODO Add check whether nodeRef exists.
   
   
   if (typeof json !== "undefined")
   {
      // At this point the field names are e.g. prop_cm_name
      // and there are some extra values - hidden fields? These are fields from YUI's datepicker(s)
      // e.g. "template_x002e_form-ui_x002e_form-test_prop_my_date-entry":"2/19/2009"
   }
   else
   {
      if (logger.isWarnLoggingEnabled())
      {
         logger.warn("json object was undefined.");
      }
      status.setCode(501, message);
      return;
   }
   
   var repoFormData = new Packages.org.alfresco.repo.forms.FormData();
   var jsonKeys = json.keys();
   for ( ; jsonKeys.hasNext(); )
   {
	   // Replace the first 2 underscores with colons.
	   var nextKey = jsonKeys.next();
	   var alteredKey = nextKey.replaceFirst("_", ":").replaceFirst("_", ":");
	   
	   repoFormData.addData(alteredKey, json.get(nextKey));
   }

   formService.saveForm(nodeRef, repoFormData);
   
   model.message = "Successfully updated node " + nodeRef;
}

main();