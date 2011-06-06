<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/constants.lib.js">

script:
{
    // locate node
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    model.node = object.node;

    // property filter
    var filter = args[cmisserver.ARG_FILTER];
    if (filter === null)
    {
        filter = "*";
    }
    
    // retrieve policies
    var page = paging.createPageOrWindow(args);
    var paged = cmisserver.getAppliedPolicies(model.node, filter, page);
    model.results = paged.results;
    model.cursor = paged.cursor;
}
