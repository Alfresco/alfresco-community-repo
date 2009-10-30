<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/constants.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/modify.lib.js">

script:
{
    // ensure atom entry is posted
    if (entry === null)
    {
        status.code = 400;
        status.message = "Expected atom entry";
        status.redirect = true;
        break script;
    }
    
    // locate node
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    model.node = object.node;

    // update properties
    var updated = updateNode(model.node, entry, null, function(propDef) {return patchValidator(propDef, false);});
    if (updated)
    {
        model.node.save();
    }
}
