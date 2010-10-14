/**
 * Document List Component: activity
 */
postActivity();

/* Posts to the activities service after a Document Library action */
function postActivity()
{
   var data = {};
   
   /*
    * Activity Type
    */
   var type = json.get("type");
   if (type == null || type.length === 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Activity 'type' parameter missing when posting activity");
      return;
   }

   /*
    * Site
    */
   var siteId = json.get("site");
   if (siteId == null || siteId.length === 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "'site' parameter missing when posting activity");
      return;
   }
   var site = siteService.getSite(siteId);
   if (site == null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "'" + siteId + "' is not a valid site");
      return;
   }

   var strParams = "";

   switch (String(type).toLowerCase())
   {
      case "file-added":
      case "file-updated":
      case "google-docs-checkout":
      case "google-docs-checkin":
      case "inline-edit":
         data.title = json.get("fileName");
         data.nodeRef = json.get("nodeRef");
         strParams = "?nodeRef=" + json.get("nodeRef");
         break;
      
      case "files-added":
      case "files-deleted":
      case "files-updated":
         data.title = json.get("fileCount");
         strParams = "?path=" + json.get("path");
         if (json.has("parentNodeRef"))
         {
            data.parentNodeRef = json.get("parentNodeRef");
         }
         break;
      
      case "file-deleted":
         data.title = json.get("fileName");
         data.nodeRef = json.get("nodeRef");
         strParams = "?path=" + json.get("path");
         break;
      
      default:
         status.setCode(status.STATUS_BAD_REQUEST, "'" + type + "' is not a valid activity type");
         return;
   }
   
   try 
   {
      // Log to activity service
      data.page = json.get("page") + strParams;
      activities.postActivity("org.alfresco.documentlibrary." + type, siteId, "documentlibrary", jsonUtils.toJSONString(data));
   }
   catch(e)
   {
      if (logger.isLoggingEnabled())
      {
         logger.log(e);
      }
   }

}
