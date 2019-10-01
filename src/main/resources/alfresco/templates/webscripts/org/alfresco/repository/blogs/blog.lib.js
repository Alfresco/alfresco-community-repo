
/** Name of the blog details aspect. */
const BLOG_DETAILS_ASPECT = "blg:blogDetails";

/**
 * Fetches the blog properties from the json object and adds them to an array
 * using the correct property names as indexes.
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
 * Returns the data object of a blog node.
 */
function getBlogData(node)
{
   return node;
}
