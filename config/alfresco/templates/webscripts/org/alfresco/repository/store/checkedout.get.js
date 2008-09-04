script:
{
    // locate (optional) folder
    model.folder = null;
    var folderId = cmis.findArg(args.folderId, headers["CMIS-folderId"]);
    if (folderId !== null)
    {
        model.folder = search.findNode(folderId);
        if (model.folder === null)
        {
            status.code = 400;
            status.message = "Folder " + folderId + " not found";
            status.redirect = true;
            break script;
        }
        if (!model.folder.isContainer)
        {
            status.code = 400;
            status.message = "Folder id " + folderId + " does not refer to a folder";
            status.redirect = true;
            break script;
        }
    }
    // NOTE: includeDescendants is an extension of CMIS
    model.includeDescendants = (args.includeDescendants == "true") ? true : false;
 
    // property filter 
    model.filter = cmis.findArg(args.filter, headers["CMIS-filter"]);
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // TODO: includeAllowableActions 
   
    // retrieve checked-out
    var page = paging.createPageOrWindow(args, headers);
    var paged = cmis.queryCheckedOut(person.properties.userName, model.folder, model.includeDescendants, page);
    model.results = paged.results;
    model.cursor = paged.cursor;
}
