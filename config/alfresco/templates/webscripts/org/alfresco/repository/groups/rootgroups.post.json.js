/**
 * Create new root group.
 */

function main()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   var group = groups.getGroup(shortName);
   if (group != null)
   {
      // Group already exists
      status.setCode(status.STATUS_BAD_REQUEST, "The root group :" + shortName + ", already exixts.");
      return;
   }
   
   var displayName = shortName;
   
   if (json.has("displayName") == true)
   {
      displayName = json.get("displayName");
   }
   
   model.group = groups.createRootGroup(shortName, displayName);
   status.code = status.STATUS_CREATED;   
}

main();