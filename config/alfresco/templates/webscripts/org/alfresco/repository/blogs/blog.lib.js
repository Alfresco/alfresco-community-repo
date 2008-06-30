
const BLOG_DETAILS_ASPECT = "blg:blogDetails";

/**
 * Fetches the blog properties from the json object and adds them to the array.
 */
function getBlogPropertiesArray()
{
    var arr = new Array();
	if (json.has("blogType"))
	{
		arr["blg:blogImplementation"] = json.get("blogType");
	}
	if (json.has("blogId"))
	{
		arr["blg:id"] = json.get("blogId");
	}
	if (json.has("blogName"))
	{
		arr["blg:name"] = json.get("blogName");
	}
	if (json.has("blogDescription"))
	{
		arr["blg:description"] = json.get("blogDescription");
	}
	if (json.has("blogUrl"))
	{
		arr["blg:url"] = json.get("blogUrl");
	}
	if (json.has("username"))
	{
		arr["blg:userName"] = json.get("username");
	}
	if (json.has("password"))
	{
		arr["blg:password"] = json.get("password");
	}
	return arr;
}

/**
 * Returns the data of a blog post.
 */
function getBlogData(node)
{
   return node;
}

/**
 * Returns an array containing all topics found in the passed array.
 * Filters out non-fm:topic nodes.
 */
function getBlogListData(nodes, index, count)
{
   var items = new Array();
   var i;
   var added = 0;
   for (i = index; i < nodes.length && added < count; i++)
   {
      items.push(getBlogData(nodes[i]));
	  added++;
   }
   
   return ({
      "total" : nodes.length,
	  "pageSize" : count,
	  "startIndex" : index,
	  "itemCount" : items.length,
      "items": items
   });
}

/**
 * Returns a list of topics, as returned by the lucene query
 */
function getBlogsListByLuceneQuery(node, luceneQuery, sortAttribute, ascending, index, count)
{
   var nodes = null;
   if (sortAttribute != null)
   {
      nodes = search.luceneSearch(node.nodeRef.storeRef.toString(), luceneQuery, sortAttribute, ascending);
   }
   else
   {
      nodes = search.luceneSearch(luceneQuery);
   }
   return getBlogListData(nodes, index, count);
}
