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
   if (!json.isNull("location")) 
   { 
      person.properties["location"] = json.get("location"); 
   } 
   if (!json.isNull("telephone")) 
   { 
      person.properties["telephone"] = json.get("telephone"); 
   } 
   if (!json.isNull("mobile")) 
   { 
      person.properties["mobile"] = json.get("mobile"); 
   } 
   if (!json.isNull("companyaddress1")) 
   { 
      person.properties["companyaddress1"] = json.get("companyaddress1"); 
   } 
   if (!json.isNull("companyaddress2")) 
   { 
      person.properties["companyaddress2"] = json.get("companyaddress2"); 
   } 
   if (!json.isNull("companyaddress3")) 
   { 
      person.properties["companyaddress3"] = json.get("companyaddress3"); 
   } 
   if (!json.isNull("companypostcode")) 
   { 
      person.properties["companypostcode"] = json.get("companypostcode"); 
   } 
   if (!json.isNull("companytelephone")) 
   { 
      person.properties["companytelephone"] = json.get("companytelephone"); 
   } 
   if (!json.isNull("companyfax")) 
   { 
      person.properties["companyfax"] = json.get("companyfax"); 
   } 
   if (!json.isNull("companyemail")) 
   { 
      person.properties["companyemail"] = json.get("companyemail"); 
   } 
   if (!json.isNull("skype")) 
   { 
      person.properties["skype"] = json.get("skype"); 
   } 
   if (!json.isNull("instantmsg")) 
   { 
      person.properties["instantmsg"] = json.get("instantmsg"); 
   } 
   if (!json.isNull("persondescription")) 
   { 
      person.properties["persondescription"] = json.get("persondescription"); 
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