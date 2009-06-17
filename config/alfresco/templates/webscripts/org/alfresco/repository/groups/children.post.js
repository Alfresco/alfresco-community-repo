/** 
 * Link groups or users to group.
 * 
 * Will create sub-groups if they don't already exist.
 */  

function main()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   var fullAuthorityName = urlElements[2];
   
   var group = groups.getGroup(shortName);
   var GROUP_PREFIX = "GROUP_";
    
   if (group == null)
   {
      // Parent Group cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The group :" + shortName + ", does not exist.");
      return;
   }
   
   if (fullAuthorityName.match("^" + GROUP_PREFIX + "*"))
   {
      var subGroupName = fullAuthorityName.substr(GROUP_PREFIX.length);
      var child = groups.getGroup(subGroupName);
      
      // This is a group authority
      if(child == null)
      {
         // child does not exist
         child = group.createGroup(subGroupName, subGroupName);
         status.code = status.STATUS_CREATED;
         model.group = child;
         return;
      }
   }
   
   // Link an existing group or user
   group.addAuthority(fullAuthorityName);
   status.code = status.STATUS_OK;
   model.group 
}

main();