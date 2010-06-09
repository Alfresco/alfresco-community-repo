/**
 * User Profile - Reset user avatar REST method
 * 
 * Current user can only modify their own settings or an admin can reset all.
 * 
 * @method PUT
 */

function main()
{
   // Get the person details and ensure they exist for update
   var userName = url.extension;
   var user = people.getPerson(userName);
   if (user == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Person " + userName + " does not exist");
      return;
   }
   
   // ensure we found a valid user and that it is the current user or we are an admin
   if (user == null ||
       (people.isAdmin(person) == false && user.properties.userName != person.properties.userName))
   {
      status.code = 500;
      status.message = "Failed to locate user to modify or permission denied.";
      status.redirect = true;
      return;
   }
   
   // remove old image child node if we have one
   var assocs = user.childAssocs["cm:preferenceImage"];
   if (assocs != null && assocs.length == 1)
   {
      assocs[0].remove();
   }
   // remove 'cm:avatar' target association - backward compatible with JSF web-client avatar
   assocs = user.associations["cm:avatar"];
   if (assocs != null && assocs.length == 1)
   {
      user.removeAssociation(assocs[0], "cm:avatar");
   }
   
   model.success = true;
}

main();