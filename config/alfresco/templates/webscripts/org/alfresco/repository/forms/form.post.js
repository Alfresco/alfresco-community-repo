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
	   logger.log("POST request received for nodeRef: " + nodeRef);
   }
   
   
   
   
   // TODO: check the given nodeRef is real
   
   
   
   
   // persist the submitted data using the most appropriate data set
   if (typeof formdata !== "undefined")
   {
	   // The model.data is set here to allow the rendering of a simple result page.
	   // TODO This should be removed when the POST is working.
	   model.data = formdata;
	   
	   // Note: This formdata is org/alfresco/web/scripts/servlet/FormData.java
	   if (logger.isLoggingEnabled())
	   {
		   logger.log("Saving form with formdata, " + formdata.fields.length + " fields.");
	   }

      // N.B. This repoFormData is a different FormData class to that used above.
      var repoFormData = new Packages.org.alfresco.repo.forms.FormData();
      for (var i = 0; i < formdata.fields.length; i++)
      {
    	  // Replace the first 2 underscores with colons.
    	  var alteredName = formdata.fields[i].name.replaceFirst("_", ":").replaceFirst("_", ":");
    	  repoFormData.addData(alteredName, formdata.fields[i].value);
      }
      
      formService.saveForm(nodeRef, repoFormData);
   }
   else
   {
	   if (logger.isLoggingEnabled())
	   {
		   logger.log("Saving form with args = " + args);
	   }
      formService.saveForm(nodeRef, args);
   }
   
   model.message = "Successfully updated node " + nodeRef;
}

main();