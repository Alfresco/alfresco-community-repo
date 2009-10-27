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
	
	preferenceService.clearPreferences(userid, args["pf"]);
}

main();