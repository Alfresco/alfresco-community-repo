function main()
{
    // get requested node
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }
    
   var pNumber = parseInt(args.page);
   var pSize = parseInt(args.pageSize);
   if ((pNumber == undefined) || (pSize == undefined))
   {
      model.error = "Parameters missing!";
      return;
   }

    model.data = getContentObjects((pNumber - 1) * pSize,pSize,getProperties);

}

/**
 * Returns a data object that can be passed to the paged results template
 * for rendering.
 *
 * @param nodes: The complete result set nodes
 * @param index: the start index from which results should be returned
 * @param count: the number of elements that should be returned
 * @param extractDataFn: The function that extracts the data to be returned
 *                       for each node in the final data set.
 *                       The functions signature is name(index, node).
 *
 * Returns an array containing all topics found in the passed array.
 * Filters out non-fm:topic nodes.
 */
function getPagedResultsData(nodes, index, count, extractDataFn)
{
   var items = [];
   var i;
   var added = 0;
   for (i = index; i < nodes.length && added < count; i++)
   {
      items.push(extractDataFn(nodes[i]));
	  added++;
   }

   return (
   {
      "total" : nodes.length,
	  "pageSize" : count,
	  "startIndex" : index,
	  "itemCount" : items.length,
      "items": jsonUtils.toJSONString(items)
   });
}

function getProperties(node)
{

    var itm = {};
    if (node.type == "{http://www.alfresco.org/model/content/1.0}content")
    {
        itm.type = node.type || "";
        itm.name = node.name || "";
        itm.reference = node.nodeRef||"";

        return itm;

    }
}


function getContentObjects(index, count, extractDataFn)
{
    var nodes = [];
    var gp = getGenericPageNode();
    var luceneQuery = " +PATH:\"" + gp.qnamePath + "/*\"" +
                      " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
                      //" +PATH:\"/app:company_home/cm:generic-page/*\"";

    nodes = search.luceneSearch(luceneQuery, null, true);

    return getPagedResultsData(nodes, index, count, extractDataFn);
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