/**
 * User Status REST Update method
 * 
 * @method POST
 * @param json {string}
 *    {
 *       status: "value"
 *    }
 */

function main()
{
   model.success = false;
   
   if (json.has("status"))
   {
      var newStatus = json.get("status");
      if (newStatus != null)
      {
         var statusTime = new Date();
         person.properties["cm:userStatus"] = newStatus;
         person.properties["cm:userStatusTime"] = statusTime;
         person.save();

         model.success = true;
         model.userStatus = newStatus;
         model.userStatusTime = statusTime;
         
         if (newStatus.trim() != "")
         {
            var activity = {};
            activity.status = newStatus;
            activities.postActivity("org.alfresco.profile.status-changed", null, "profile", jsonUtils.toJSONString(activity));
         }
      }
   }
}

main();