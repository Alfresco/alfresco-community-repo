<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">

/**
 * Fetches all posts of the given blog
 */
function getDraftBlogPostList()
{
   var q = " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"" +
           " +PATH:\"/app:company_home/st:sites/*//*\" " +
           " -ISNOTNULL:\"{http://www.alfresco.org/model/content/1.0}published\" " +
           " +@cm\\:creator:" + person.properties.userName + 
           " -TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\" " + 
           " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\" "+
           " -TYPE:\"{http://www.alfresco.org/model/linksmodel/1.0}link\" "+
           " -TYPE:\"{http://www.alfresco.org/model/calendar}calendarEvent\" " +
           " -ASPECT:\"cm:versionable\" " +
           " -ASPECT:\"cm:lockable\"";
   q += " +(@\\{http\\://www.alfresco.org/model/content/1.0\\}content.mimetype:application/octet-stream OR";
   q += "  @\\{http\\://www.alfresco.org/model/content/1.0\\}content.mimetype:text/html)"

   // // get the data
   nodes = search.luceneSearch(q,"cm:modified",false);
   // return nodes.length;
   return processResults(nodes, 3);
}

function getWikiPages()
{
   var q = " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" " +
           " +PATH:\"/app:company_home/st:sites/*//*\" " +
           " +@cm\\:modifier:admin " + 
           " +ASPECT:\"cm:titled\" " +
           " -ASPECT:\"cm:lockable\" " +
           " +ASPECT:\"cm:versionable\"";

   nodes = search.luceneSearch(q,'cm:modified',false);

   return processResults(nodes,3);
}

function getDiscussions()
{
   var q = " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\" " +
           " +PATH:\"/app:company_home/st:sites/*//*\" " +
           " +@cm\\:creator:" + person.properties.userName +
           " +ASPECT:\"cm:syndication\"";

   nodes = search.luceneSearch(q,'cm:modified',false);

   return processResults(nodes,3);         
}
model.data = {};

model.data.blogPosts = getDraftBlogPostList();
model.data.wikiPages = getWikiPages();
model.data.discussions = getDiscussions();