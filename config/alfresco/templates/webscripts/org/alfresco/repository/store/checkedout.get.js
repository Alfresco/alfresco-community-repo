script:
{
    // locate (optional) folder
    model.folder = null;
    var folderId = args[cmis.ARG_FOLDER_ID];
    if (folderId !== null)
    {
        model.folder = cmis.findNode(folderId);
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
    model.filter = args[cmis.ARG_FILTER];
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // include allowable actions
    var includeAllowableActions = args[cmis.ARG_INCLUDE_ALLOWABLE_ACTIONS];
    model.includeAllowableActions = (includeAllowableActions == "true" ? true : false);   

    // retrieve checked-out
    var page = paging.createPageOrWindow(args);
    var paged = cmis.queryCheckedOut(person.properties.userName, model.folder, model.includeDescendants, page);
    model.results = paged.results;
    model.cursor = paged.cursor;
}
