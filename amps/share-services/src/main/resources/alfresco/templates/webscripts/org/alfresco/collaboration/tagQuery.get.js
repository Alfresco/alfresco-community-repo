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
      node = utils.resolveNodeReference(rootNode);
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
   //MNT-2118 Share inconsistencies when displaying locked files with tags
   query += " -ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";

   // MNT-20091 check to prevent cm:taggable with NULL
   query += " AND ISNOTNULL:\"{http://www.alfresco.org/model/content/1.0}taggable\"";
   
   if (search.searchSubsystem.startsWith("solr"))
   {
      // MNT-11511: use facet search
      var queryDef = {
         query: query,
         language: "lucene",
         page: {
            // query minimum rows because all usefull info will come with facets 
            maxItems: 1,
            skipCount: 0
         },
         fieldFacets: [ "TAG" ]
      };
      var rs = search.queryResultSet(queryDef);
      var tagFacets = rs.meta.facets.TAG;
      
      for(var i=0; i < tagFacets.size(); i++)
      {
         var tagFacet = tagFacets.get(i);
         tag =
         {
            name: tagFacet.facetValue,
            count: tagFacet.hits,
            toString: function()
            {
               return this.name;
            }
         };
         tags.push(tag);
      }
   }
   else
   {
      var taggedNodes = search.luceneSearch(query);
      
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
   }

   if (tags.length === 0)
   {
      countMin = 0;
   }
   else
   {
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

model.tagQuery = tagQuery();
