/**
 * Get parent groups
 */

function main ()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   var level = args["level"];
   var sortBy = args["sortBy"];
   var paging = utils.createPaging(args);
   
   var group = groups.getGroup(shortName);
   if (group == null)
   {
      // Group cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The group: " + shortName + " does not exist.");
      return;
   }
   
   model.group = group;

   if(sortBy == null)
   {
      sortBy = "authorityName";
   }
   
   if (level != null)
   {
      if (!level.match("[ALL]"))
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The level argument has does not have a correct value.");
         return;
      }
      model.parents = group.getAllParentGroups(paging, sortBy);
      model.paging = paging;
   }
   else
   {
      model.parents = group.getParentGroups(paging, sortBy);
      model.paging = paging;
   }
}

main();
