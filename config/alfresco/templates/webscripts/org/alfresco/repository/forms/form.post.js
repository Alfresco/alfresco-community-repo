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
	   // The model.data is set here to allow the rendering of a simple result page.
	   // TODO This should be removed when the POST is working.
	   model.data = formdata;
	   
	   // Note: This formdata is org/alfresco/web/scripts/servlet/FormData.java
      logger.log("Saving form with formdata, " + formdata.fields.length + " fields.");
   	  //TODO At this point, for multipart, the field names are e.g. prop_cm_name

      // N.B. This repoFormData is a different FormData class to that used above.
      var repoFormData = new Packages.org.alfresco.repo.forms.FormData();
      for (var i = 0; i < formdata.fields.length; i++)
      {
    	  repoFormData.addData(formdata.fields[i].name, formdata.fields[i].value);
      }
      
      //TODO How to handle false booleans? They are omitted from POST
      formService.saveForm(nodeRef, repoFormData);
   }
   else
   {
      logger.log("Saving form with args = " + args);
      formService.saveForm(nodeRef, args);
   }
   
   model.message = "Successfully updated node " + nodeRef;
}

main();