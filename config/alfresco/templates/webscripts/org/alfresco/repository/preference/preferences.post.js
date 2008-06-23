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
	
	// Set the preferences
	preferenceService.setPreferences(userid, preferences);
}

main();