model.tagQuery = tagQuery(args["n"], args["m"]);

function tagQuery(nodeRef, maxResults)
{
   var tags = new Array();
   var countMin = Number.MAX_VALUE,
      countMax = 0;
   
   /* nodeRef input */
   var node = null;
   if ((nodeRef != null) && (nodeRef != ""))
   {
      node = search.findNode(nodeRef);
   }
   if (node == null)
   {
      node = companyhome;
   }

   /* maxResults input */
   if ((maxResults == null) || (maxResults == ""))
   {
      maxResults = -1;
   }
   
   /* Query for tagged node(s) */
   var query = "PATH:\"" + node.qnamePath;
   if (node.isContainer)
   {
      query += "//*";
   }
   query += "\" AND ASPECT:\"{http://www.alfresco.org/model/content/1.0}taggable\"";
   
   var taggedNodes = search.luceneSearch(query);

   if (taggedNodes.length == 0)
   {
      countMin = 0;
   }
   else
   {   
      /* Build a hashtable of tags and tag count */
      var tagHash = {};
      var count;
      for each (taggedNode in taggedNodes)
      {
         for each(tag in taggedNode.properties["cm:taggable"])
         {
            if (tag != null)
            {
               count = tagHash[tag.name];
               tagHash[tag.name] = count ? count+1 : 1;
            }
         }
      }
      
      /* Convert the hashtable into an array of objects */
      for (key in tagHash)
      {
         tag =
         {
            name: key,
            count: tagHash[key],
            toString: function()
            {
               return this.name;
            }
         };
         tags.push(tag);
      }
   
      /* Sort the results by count (descending) */
      tags.sort(sortByCountDesc);
   
      /* Trim the results to maxResults if specified */
      if (maxResults > -1)
      {
         tags = tags.slice(0, maxResults);
      }
   
      /* Calculate the min and max tag count values */
      for each(tag in tags)
      {
         countMin = Math.min(countMin, tag.count);
         countMax = Math.max(countMax, tag.count);
      }
   
      /* Sort the results by tag name (ascending) */
      tags.sort();
   }
   
   var results =
   {
      "countMin": countMin,
      "countMax": countMax,
      "tags": tags
   };
   return results;
}

function sortByCountDesc(a, b)
{
   return (b.count - a.count);
}
