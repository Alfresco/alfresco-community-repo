<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/constants.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/modify.lib.js">

script:
{
    // locate node
    var object = getObjectFromUrl();
    model.node = object.node;
    
    model.checkin = args[cmis.ARG_CHECKIN] == "true" ? true : false;
    
    if (entry !== null)
    {
        // update properties
        var updated = updateNode(model.node, entry, null, function(propDef) {return putValidator(propDef, true);});
        if (updated === null)
        {
            break script;
        }
        if (updated)
        {
            model.node.save();
        }
    }
    
    // checkin
    if (model.checkin)
    {
        var comment = args[cmis.ARG_CHECKIN_COMMENT];
        var major = args[cmis.ARG_MAJOR];
        major = (major === null || major == "true") ? true : false;
        model.node = cmis.checkIn(model.node, comment, major);
    }
}
