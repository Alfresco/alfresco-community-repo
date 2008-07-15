function main()
{
   // Extract the person's user name from the URL
   var userName = url.extension;
   var person = people.getPerson(userName);

   // if person is found matching the given user name
   // then update that person's details with the details
   // provided within the JSON object passed in
   if (person != null)
   {
      // Update the person's details
      person.properties["title"] = json.get("title");
      person.properties["firstName"] = json.get("firstName");
      person.properties["lastName"] = json.get("lastName");
      person.properties["organization"] = json.get("organisation");
      person.properties["jobtitle"] = json.get("jobtitle");
      person.properties["email"] = json.get("email");
      person.save();

      // Put the updated person on the model to pass to the template
      model.person = person;
   }
   // else if no person was found matching the given user name,
   // then return HTTP error status "not found"
   else
   {
      status.setCode(status.STATUS_NOT_FOUND, "Person " + userName
            + " does not exist");
      return;
   }
}

main();