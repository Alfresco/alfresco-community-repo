<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    // locate (optional) folder
    model.folder = null;
    var folderId = args[cmisserver.ARG_FOLDER_ID];
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
    model.filter = args[cmisserver.ARG_FILTER];
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // rendition filter
    model.renditionFilter = args[cmisserver.ARG_RENDITION_FILTER];
    if (model.renditionFilter === null || model.renditionFilter.length == 0)
    {
        model.renditionFilter = "cmis:none";
    }

    // include allowable actions
    var includeAllowableActions = args[cmisserver.ARG_INCLUDE_ALLOWABLE_ACTIONS];
    model.includeAllowableActions = (includeAllowableActions == "true" ? true : false);   

    // include relationships
    model.includeRelationships = args[cmisserver.ARG_INCLUDE_RELATIONSHIPS];
    if (model.includeRelationships == null || model.includeRelationships.length == 0)
    {
        model.includeRelationships = "none";
    }    

    // retrieve checked-out
    var page = paging.createPageOrWindow(args);
    var paged = cmisserver.queryCheckedOut(person.properties.userName, model.folder, model.includeDescendants, page);
    model.results = paged.results;
    model.cursor = paged.cursor;
}
