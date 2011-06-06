<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    // locate node
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    model.node = object.node;

    // TODO: XML parsing need to be moved to Java
    // remove xml declaration and leading whitespace
    var request = ('' + requestbody.content).replace(/^<\?xml\s+version\s*=\s*(["'])[^\1]+\1[^?]*\?>/, ""); // bug 336551

    default xml namespace = 'http://docs.oasis-open.org/ns/cmis/core/200908/';

    // Flatten out the request into principal ID / permission pairs
    var principalIds = new Array();
    var permissions = new Array();
    var i=0;
    for each (var ace in new XML(request).permission)
    {
        // Only pay attention to direct ACEs
        if (ace.direct.toString() == "true")
        {
            var principalId = ace.principal.principalId.toString();
            for each (var permission in ace.permission)
            {
                principalIds[i] = principalId;
                permissions[i] = permission.toString();
                i++;
            }
        }
    }
    cmisserver.applyACL(model.node, principalIds, permissions);
}
