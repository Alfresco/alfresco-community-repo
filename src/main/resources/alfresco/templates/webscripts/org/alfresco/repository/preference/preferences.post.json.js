function main()
{
   // Try and get the person
   var userid = url.templateArgs.userid;
   var person = people.getPerson(userid);
   if (person == null)
   {
      // 404 since person resource could not be found
      status.setCode(status.STATUS_NOT_FOUND, "The user " + userid + " could not be found");
      return;
   }

   // Convert the passed json into a native JS object
   var preferences = jsonUtils.toObject(json);

   try
   {
      // Set the preferences
      if (preferenceService.getAllowWrite())
      {
         preferenceService.setPreferences(userid, preferences);
      }
   }
   catch (error)
   {
      var msg = error.message;

      if (logger.isLoggingEnabled())
         logger.log(msg);

      // determine if the exception was UnauthorizedAccessException, if so
      // return 401 status code
      if (msg.indexOf("AccessDeniedException") != -1)
      {
         status.setCode(status.STATUS_UNAUTHORIZED, msg);
      }
      else throw error;
   }
}

main();