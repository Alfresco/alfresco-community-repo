/**
 * Update group
 */

function main()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   var group = groups.getGroup(shortName);
   if (group == null)
   {
      // Group cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The group :" + shortName + ", does not exist.");
      return;
   }
   
   if (json.has("displayName") == true)
   {
      group.setDisplayName(json.get("displayName"));
   }
   
   model.group = group;
}

main();