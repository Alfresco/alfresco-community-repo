function main()
{
   // Get the user name of the person to get
   var userName = url.extension;
   
   // Get the person who has that user name
   var person = people.getPerson(userName);
   if (person != null) 
   {
      model.person = person;
   }
   else 
   {
      status.setCode(status.STATUS_NOT_FOUND, "Person " + userName + " does not exist");
   }
}

main();