/**
 * Document List Component: activity
 */
postActivity();

/* Posts to the activities service after a Document Library action */
function postActivity()
{
   var obj = {};
   
   var type = json.get("type");
	if (type == null || type.length == 0)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "Activity 'type' parameter missing when posting activity");
		return;
	}

   var siteId = json.get("site");
	if (siteId == null || siteId.length == 0)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "'site' parameter missing when posting activity");
		return;
	}

   switch (String(type).toLowerCase())
   {
      case "file-added":
         obj.browseURL = json.get("browseURL");
         obj.contentURL = json.get("contentURL");
         obj.fileName = json.get("fileName");
         break;
      
      case "files-added":
         obj.browseURL = json.get("browseURL");
         obj.fileCount = json.get("fileCount");
         break;
   }
   
	try 
	{
      // Log to activity service
		activities.postActivity("org.alfresco.documentlibrary." + type, siteId, "documentlibrary", jsonUtils.toJSONString(obj));
	}
	catch(e)
	{
		if (logger.isLoggingEnabled())
		{
			logger.log(e);
		}
	}

}
