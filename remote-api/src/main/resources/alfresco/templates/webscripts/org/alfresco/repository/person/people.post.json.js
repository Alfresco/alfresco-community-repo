function main()
{
   //
   // Get the person details
   //
   
   if ((json.isNull("userName")) || (json.get("userName").length() == 0)) 
   {
      status.setCode(status.STATUS_BAD_REQUEST, "User name missing when creating person");
      return;
   }
   
   if ((json.isNull("firstName")) || (json.get("firstName").length() == 0))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "First name missing when creating person");
      return;
   }
   
   if ((json.isNull("email")) || (json.get("email").length() == 0))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Email missing when creating person");
      return;
   }
   
   var password = "password";
   if (json.has("password"))
   {
      password = json.get("password");
   }
   
   // Create the person with the supplied user name
   var userName = json.get("userName");
   var enableAccount = ((json.has("disableAccount") && json.get("disableAccount")) == false);
   var person = people.createPerson(userName, json.get("firstName"), json.get("lastName"), json.get("email"), password, enableAccount);
   
   // return error message if a person with that user name could not be created
   if (person === null)
   {
      status.setCode(status.STATUS_CONFLICT, "User name already exists: " + userName);
      return;
   }
   
   // assign values to the person's properties
   if (json.has("title"))
   {
      person.properties["title"] = json.get("title");
   }
   if (json.has("organisation"))
   {
      person.properties["organization"] = json.get("organisation");
   }
   if (json.has("jobtitle"))
   {
      person.properties["jobtitle"] = json.get("jobtitle");
   }
   person.save();
   
   // set quota if any - note that only Admin can set this and will be ignored otherwise
   var quota = (json.has("quota") ? json.get("quota") : -1);
   people.setQuota(person, quota.toString());
   
   // apply groups if supplied - note that only Admin can successfully do this
   if (json.has("groups"))
   {
      var groups = json.get("groups");
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