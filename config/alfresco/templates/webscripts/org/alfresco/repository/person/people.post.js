function main()
{
   // Get the person details
   var userName = json.get("userName");
   if ((userName === null) || (userName.length == 0)) 
   {
      status.setCode(status.STATUS_BAD_REQUEST, "User name missing when creating person")
      return;
   }
   
   var title = json.get("title");
   var firstName = json.get("firstName");
   var lastName = json.get("lastName");
   var organisation = json.get("organisation");
   var jobTitle = json.get("jobTitle");
   var email = json.get("email");
   var bio = json.get("bio");
   var avatarUrl = json.get("avatarUrl");
   
   // Create the person 
   var person = people.createPerson(userName);
   person.properties.title = title;
   person.properties.firstName = firstName;
   person.properties.lastName = lastName;
   person.properties.organisation = organisation;
   person.properties.jobTitle = jobTitle;
   person.properties.email = email;
   person.properties.bio = bio;
   person.properties.avatarUrl = avatarUrl;
   person.save();
   
   // Put the created person into the model
   model.person = person;
}

main();
