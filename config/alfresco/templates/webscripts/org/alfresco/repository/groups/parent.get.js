/**
 * Get parent groups
 */

function main ()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   var level = args["level"];
   var maxItems = args["maxItems"];
   var skipCount = args["skipCount"];
   
   var group = groups.getGroup(shortName);
   if (group == null)
   {
      // Group cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The group: " + shortName + " does not exist.");
      return;
   }
   
   model.group = group;
   if(maxItems == null)
   {
      maxItems = -1;
   }
   if(skipCount == null)
   {
      skipCount = -1;
   }
   
   if (level != null)
   {
      if (!level.match("[ALL]"))
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The level argument has does not have a correct value.");
         return;
      }
      model.parents = group.getAllParentGroups(maxItems, skipCount);
   }
   else
   {
      model.parents = group.getParentGroups(maxItems, skipCount);
   }
}

main();