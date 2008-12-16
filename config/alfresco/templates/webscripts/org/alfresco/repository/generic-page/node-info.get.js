
function main()
{
    var path = (url.templateArgs.path != undefined) ? url.templateArgs.path : "";
    var gp = getGenericPageNode();
    var nodeObj = gp.childByNamePath(path);
    if (nodeObj === null)
    {
        status.setCode(status.STATUS_NOT_FOUND, "No node found for the given path: \"" + path + "\" in container " + containerId + " of site " + siteId);
        return null;
        
    }else
    {
        getNodeInfo(nodeObj);
    }
}

function getNodeInfo(nodeObj)
{
    var nodeinfo  = {};

    nodeinfo.properties =  getProperties(nodeObj);
    nodeinfo.aspects = getAspects(nodeObj);

    model.nodeinfo = jsonUtils.toJSONString(nodeinfo);

    var data = getData(nodeObj);
    model.data = jsonUtils.toJSONString(data);
}

function getAspects(nodeObj){
    var  items = [];
    var asps = nodeObj.aspectsSet.toArray();
    for(var i = 0; i < asps.length; i++)
    {
        var ai = {};
        ai.name = asps[i];
        items.push(ai);        
    }

    return items;
}

function getData(nodeObj)
{
    var data= {};
    data.type = nodeObj.type || "";
    data.name = nodeObj.name || "";
    data.reference = nodeObj.nodeRef || "";
    data.url = nodeObj.url || "";
    return data; 
}

function getProperties(nodeObj)
{
    var prs = [];

    for (var pn in  nodeObj.properties)
    {
        var pi = {};
        pi.name = pn;
        pi.value = (nodeObj.properties[pn].toString)?nodeObj.properties[pn].toString():nodeObj.properties[pn];
        //pi.propertyType = "type";
        prs.push(pi);
    }
    return prs;
}

function getGenericPageNode()
{
    var luceneQuery = " +PATH:\"/app:company_home/cm:generic-page\"";
    var gp = search.luceneSearch(luceneQuery, null, true);
    gp  = gp[0];

    if(!gp)
    {
        var luceneQuery = " +PATH:\"/app:company_home\"";
        var ch = search.luceneSearch(luceneQuery, null, true);
        gp = ch[0].createNode("generic-page","cm:folder");
    }
    return gp||null;
}


main();