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

   // MNT-21150 LDAP synced attributes can be changed using REST API
   var qname = "{http://www.alfresco.org/model/content/1.0}";
   var immutableProperties = people.getImmutableProperties(userName);

   var personProperties = ["firstName", "lastName", "email", "title", "jobtitle", "location", "telephone",
       "mobile", "companyaddress1", "companyaddress2", "companyaddress3", "companypostcode", "companytelephone", "companyfax",
       "companyemail", "skype", "instantmsg", "persondescription"]

   // assign new values to the person's properties
   for (var index=0; index<personProperties.length; index++)
   {
     if ((!json.isNull(personProperties[index])) && (!immutableProperties.hasOwnProperty((qname + personProperties[index]).toString())))
     {
       person.properties[personProperties[index]] = json.get(personProperties[index]);
     }
   }
   // Special case for organisation vs organization
   // Expected organisation in Json but property saved in person model as organization
   if ((!json.isNull("organisation")) && (!immutableProperties.hasOwnProperty((qname + "organization").toString())))
   {
     person.properties["organization"] = json.get("organisation");
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