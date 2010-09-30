<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">

/**
 * Fetches all posts of the given blog
 */
function getDraftBlogPostList()
{
   var q = " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"" +
           " +PATH:\"/app:company_home/st:sites/*/cm:blog/*\"" +
           " -ISNOTNULL:\"{http://www.alfresco.org/model/content/1.0}published\"" +
           " +@cm\\:creator:" + person.properties.userName;
   
   nodes = search.luceneSearch(q, 'cm:modified', false, 3);
   
   return processResults(nodes, 3);
}

function getWikiPages()
{
   var q = " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"" +
           " +PATH:\"/app:company_home/st:sites/*/cm:wiki/*\"" +
           " +@cm\\:modifier:" + person.properties.userName;

   nodes = search.luceneSearch(q, 'cm:modified', false, 3);

   return processResults(nodes, 3);
}

function getDiscussions()
{
   var q = " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"" +
           " +PATH:\"/app:company_home/st:sites/*/cm:discussions//*\"" +
           " +@cm\\:creator:" + person.properties.userName;

   nodes = search.luceneSearch(q, 'cm:modified', false, 3);

   return processResults(nodes, 3);         
}

model.data = {};

model.data.blogPosts = getDraftBlogPostList();
model.data.wikiPages = getWikiPages();
model.data.discussions = getDiscussions();