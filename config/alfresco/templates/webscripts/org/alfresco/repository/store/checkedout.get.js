<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/read.lib.js">

script:
{
    // locate (optional) folder
    model.folder = null;
    var folderId = args[cmis.ARG_FOLDER_ID];
    if (folderId !== null)
    {
        var folder = getObjectFromObjectId(folderId);
        if (folder.node === null)
        {
            break script;
        }
        model.folder = folder.node;
        if (!model.folder.isContainer)
        {
            status.code = 400;
            status.message = "Folder id " + folder.ref + " does not refer to a folder";
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
