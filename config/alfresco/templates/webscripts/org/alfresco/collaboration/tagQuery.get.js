function tagQuery()
{
   var rootNode = args.n,
      maxResults = args.m,
      sortOrder = args.s,
      tags = [],
      countMin = Number.MAX_VALUE,
      countMax = 0;
   
   /* rootNode input */
   var node = null;
   if ((rootNode !== null) && (rootNode !== ""))
   {
      node = resolveVirtualNodeRef(rootNode) || resolveXPath(rootNode) || search.findNode(rootNode);
   }
   if (node === null)
   {
      node = companyhome;
   }

   /* maxResults input */
   if ((maxResults === null) || (maxResults === ""))
   {
      maxResults = -1;
   }

   /* sortOrder input */
   var validSortOrders =
   {
      "name": true,
      "count": true
   };
   if (!(sortOrder in validSortOrders))
   {
      sortOrder = "name";
   }
   
   /* Query for tagged node(s) */
   var query = "";
   if (node !== companyhome)
   {
      query = "PATH:\"" + node.qnamePath;
      if (node.isContainer)
      {
         query += "//*";
      }
      query += "\" AND ";
   }
   query += "ASPECT:\"{http://www.alfresco.org/model/content/1.0}taggable\"";
   
   var taggedNodes = search.luceneSearch(query);

   if (taggedNodes.length === 0)
   {
      countMin = 0;
   }
   else
   {   
      /* Build a hashtable of tags and tag count */
      var tagHash = {},
         count, taggedNode, tag, key;
      
      for each (taggedNode in taggedNodes)
      {
         try
         {
            for each (tag in taggedNode.properties["cm:taggable"])
            {
               if (tag !== null)
               {
                  count = tagHash[tag.name];
                  tagHash[tag.name] = count ? count+1 : 1;
               }
            }
         }
         catch (e)
         {
            continue;
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
      for each (tag in tags)
      {
         countMin = Math.min(countMin, tag.count);
         countMax = Math.max(countMax, tag.count);
      }
   
      if (sortOrder == "name")
      {
         /* Sort the results by tag name (ascending) */
         tags.sort();
      }
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

/**
 * Resolve "virtual" nodeRefs into nodes
 *
 * @method resolveVirtualNodeRef
 * @param virtualNodeRef {string} nodeRef
 * @return {ScriptNode|null} Node corresponding to supplied virtual nodeRef. Returns null if supplied nodeRef isn't a "virtual" type
 */
function resolveVirtualNodeRef(nodeRef)
{
   var node = null;
   if (nodeRef == "alfresco://company/home")
   {
      node = companyhome;
   }
   else if (nodeRef == "alfresco://user/home")
   {
      node = userhome;
   }
   else if (nodeRef == "alfresco://sites/home")
   {
      node = companyhome.childrenByXPath("st:sites")[0];
   }
   else if (nodeRef == "alfresco://shared")
   {
      node = companyhome.childrenByXPath("app:shared")[0];
   }
   return node;
}

/**
 * Resolve xpath location
 *
 * @method resolveXPath
 * @param xpath {string} xpath expression
 * @return {ScriptNode|null} First node corresponding to supplied xpath expression. Results null if xpath doesn't resolve.
 */
function resolveXPath(xpath)
{
   var node = null;
   try
   {
      node = companyhome.childrenByXPath(xpath)[0];
   }
   catch(e)
   {
      return null;
   }
   return node;
}

model.tagQuery = tagQuery();