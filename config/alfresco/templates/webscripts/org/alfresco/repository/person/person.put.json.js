function main()
{
   // Get the person details and ensure they exist for update
   var userName = url.extension;
   var person = people.getPerson(userName);
   if (person == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Person " + userName + " does not exist");
      return;
   }
   
   // assign new values to the person's properties
   if (!json.isNull("firstName"))
   {
      person.properties["firstName"] = json.get("firstName");
   }
   if (!json.isNull("lastName"))
   {
      person.properties["lastName"] = json.get("lastName");
   }
   if (!json.isNull("email"))
   {
      person.properties["email"] = json.get("email");
   }
   if (!json.isNull("title"))
   {
      person.properties["title"] = json.get("title");
   }
   if (!json.isNull("organisation"))
   {
      person.properties["organization"] = json.get("organisation");
   }
   if (!json.isNull("jobtitle"))
   {
      person.properties["jobtitle"] = json.get("jobtitle");
   }
   
   // Update the person node with the modified details
   person.save();
   
   // Enable or disable account? - note that only Admin can set this
   if (json.has("disableAccount"))
   {
      var disableAccount = (json.get("disableAccount") == true);
      if (disableAccount && people.isAccountEnabled(userName))
      {
         people.disableAccount(userName);
      }
      else if (!disableAccount && !people.isAccountEnabled(userName))
      {
         people.enableAccount(userName);
      }
   }
   
   // set quota if supplied - note that only Admin can set this and will be ignored otherwise
   if (json.has("quota"))
   {
      var quota = json.get("quota");
      people.setQuota(person, quota.toString());
   }
   
   // remove from groups if supplied - note that only Admin can do this
   if (json.has("removeGroups"))
   {
      var groups = json.get("removeGroups");
      for (var index=0; index<groups.length(); index++)
      {
         var groupId = groups.getString(index);
         var group = people.getGroup(groupId);
         if (group != null)
         {
            people.removeAuthority(group, person);
         }
      }
   }
   
   // app to groups if supplied - note that only Admin can do this
   if (json.has("addGroups"))
   {
      var groups = json.get("addGroups");
      for (var index=0; index<groups.length(); index++)
      {
         var groupId = groups.getString(index);
         var group = people.getGroup(groupId);
         if (group != null)
         {
            people.addAuthority(group, person);
         }
      }
   }
   
   // Put the created person into the model
   model.person = person;
}

main();