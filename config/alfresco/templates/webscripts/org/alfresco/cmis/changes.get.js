<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    // property filter 
    model.filter = args[cmisserver.ARG_FILTER];
    if (model.filter === null || model.filter == "")
    {
        model.filter = "*";
    }

    // ACL
    model.includeACL = args[cmisserver.ARG_INCLUDE_ACL] == "true";
    
    // Change log
    var changeLogToken = args[cmisserver.ARG_CHANGE_LOG_TOKEN];
    if (changeLogToken == "")
    {
        changeLogToken = null;
    }
    var maxItems = args[cmisserver.ARG_MAX_ITEMS];
    var maxItemsInt = maxItems != null && maxItems != "" ? parseInt(maxItems) : null;
    model.changeLog = cmisserver.getChangeLog(changeLogToken, maxItemsInt);
    
    // return value
    1;
}
