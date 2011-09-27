function main()
{
    // Get the user name of the person to get
    var userName = url.templateArgs.userid;
    
    // Get the person who has that user name
    var person = people.getPerson(userName);
    
    if (person === null)  
    {
        // Return 404 - Not Found      
        status.setCode(status.STATUS_NOT_FOUND, "Person " + userName + " does not exist");
        return;
    }
    
    // Get the list of sites
    var size = 0,
        sizeString = args["size"];
    if (sizeString != null)
    {
        size = parseInt(sizeString);
    }
    model.sites = siteService.listUserSites(userName, size);
    model.roles = (args["roles"] !== null ? args["roles"] : "managers");
}

main();