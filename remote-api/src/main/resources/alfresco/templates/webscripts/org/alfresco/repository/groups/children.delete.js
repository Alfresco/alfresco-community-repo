/**
 * Remove authority from group
 */

function main ()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   var fullAuthorityName = urlElements[2];
   
   var group = groups.getGroup(shortName);
   if (group == null)
   {
      // Group cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The group :" + shortName + ", does not exist.");
      return;
   }
   
   group.removeAuthority(fullAuthorityName);
}

main();