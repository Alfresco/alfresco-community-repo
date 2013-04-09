function main()
{
   // Try and get the person
   var userid = url.templateArgs.userid;
   var person = people.getPerson(userid);
   if (person == null)
   {
      // 404 since person resource could not be found
      status.setCode(status.STATUS_NOT_FOUND, "The user " + userid
            + " could not be found");
      return;
   }
  
   try
   {
      // Get the preferences for the person
      var preferences = preferenceService.getPreferences(userid, args["pf"]);

      // Convert the preferences to JSON and place in the model
      model.preferences = jsonUtils.toJSONString(preferences);
   }
   catch (error)
   {
      var msg = error.message;

      if (logger.isLoggingEnabled())
      {
         logger.log(msg);
      }

      // determine if the exception was UnauthorizedAccessException, if so
      // return 401 status code
      if (msg.indexOf("AccessDeniedException") != -1)
      {
         status.setCode(status.STATUS_UNAUTHORIZED, msg);

         if (logger.isLoggingEnabled()) 
            logger.log("Returning 401 status code");
      }
      else
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, msg);

         if (logger.isLoggingEnabled()) 
            logger.log("Returning 500 status code");
      }
      return;
   }
}

main();