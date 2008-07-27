function main()
{
   // Extract the person's user name from the URL
   var userName = url.extension;
   
   // Get the person we wish to delete
   var person = people.getPerson(userName);

   // if person is found matching the given user name
   // then get that person's details to pass through
   // to the template and then delete that person
   if (person != null)
   {
      // Get the person's details
      
      var personDetails =
      {
         properties: []
      }
      
      personDetails.properties["userName"] = userName;
      personDetails.properties["title"] = person.properties["title"];
      personDetails.properties["firstName"] = person.properties["firstName"];
      personDetails.properties["lastName"] = person.properties["lastName"];
      personDetails.properties["organization"] = person.properties["organization"];
      personDetails.properties["jobtitle"] = person.properties["jobtitle"];
      personDetails.properties["email"] = person.properties["email"];
      personDetails.assocs = {}; // fake the assocs object
      
      // delete the person
      people.deletePerson(userName);

      // Put the person's details on the model to pass to the template
      model.personDetails = personDetails;
   }
   // else if no person was found matching the given user name,
   // then return HTTP error status "not found"
   else
   {
      status.setCode(status.STATUS_NOT_FOUND, "Person " + userName
            + " does not exist and thus can't be deleted");
      return;
   }
}

main();