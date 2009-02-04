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
   	//TODO Need to test this path.
   	nodeRef = ta_path;
   }
   else
   {
   	nodeRef = ta_storeType + '://' + ta_storeId + '/' + ta_id;
   }
   
   logger.log("POST request received for nodeRef: " + nodeRef);
   
   // TODO: check the given nodeRef is real
   
   // persist the submitted data using the most appropriate data set
   if (typeof formdata !== "undefined")
   {
      model.data = formdata;
      formService.saveForm(nodeRef, formdata);
   }
   else if (typeof json !== "undefined")
   {
      formService.saveForm(nodeRef, json);
   }
   else
   {
      formService.saveForm(nodeRef, args);
   }
   
   model.message = "Successfully updated node " + nodeRef;
}

main();