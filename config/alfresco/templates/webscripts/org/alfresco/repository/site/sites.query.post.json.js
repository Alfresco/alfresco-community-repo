function main()
{
    var sites = null;
    
    // NOTE:
    // This implementation is sufficient for the 3.1 requirement, but when site query is expanded
    // it should be formalised in the underlying JS and JAVA API's.  Currently it is very simplistic
    // as the current use case is very limited.    
    if (json.has("shortName") == true)
    {
        var shortNameQuery = json.getJSONObject("shortName");
        
        // Get the matching mode required (default is "exact")
        var matchMode = shortNameQuery.get("match")
        var isMembershipMode = (matchMode == "exact-membership");
        
        // Get each short name and put the associated site in the list
        if (shortNameQuery.has("values") == true)
        {
            var values = shortNameQuery.getJSONArray("values");
            var len = values.length();
            var username = person.properties.userName;
            sites = new Array(len);
            for (var index=0; index<len; index++)
            {
               var shortName = values.getString(index);
               var site = siteService.getSite(shortName);
               if (site != null && (!isMembershipMode || site.isMember(username)))
               {
                  sites.push(site);
               }
            }
        }
        // If the query has no values just continue as there will be no matches
    }
    
    // Set the sites collection in the model
    model.sites = sites != null ? sites : new Array(0);    
}

main();	