/**
 * Search Component
 * 
 * Takes the following object as Input:
 *    params
 *    {
 *       siteId: the site identifier to search into, null for all sites
 *       containerId: the component the search in, null for all components in the site
 *       term: search terms
 *       tag: search tag
 *       query: advanced search query json
 *       sort: sort parameter
 *       maxResults: maximum results to return
 *    };
 * 
 * Outputs:
 *  items - Array of objects containing the search results
 */
const DEFAULT_MAX_RESULTS = 250;
const SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";
const QUERY_TEMPLATES = [
   {field: "keywords", template: "%(cm:name cm:title cm:description ia:whatEvent ia:descriptionEvent lnk:title lnk:description TEXT)"}];

/**
 * Returns site information data structure.
 * { shortName: siteId, title: title }
 * 
 * Caches the data to avoid repeatedly querying the repository.
 */
var siteDataCache = [];
function getSiteData(siteId)
{
   if (siteDataCache[siteId] !== undefined)
   {
      return siteDataCache[siteId];
   }
   var site = siteService.getSite(siteId);
   var data =
   {
      shortName : siteId,
      title : (site !== null ? site.title : "unknown")
   };
   siteDataCache[siteId] = data;
   return data;
}

/**
 * Returns person display name string as returned to the user.
 * 
 * Caches the person full name to avoid repeatedly querying the repository.
 */
var personDataCache = [];
function getPersonDisplayName(userId)
{
   if (personDataCache[userId] != undefined)
   {
      return personDataCache[userId];
   }
   
   var displayName = "";
   var person = people.getPerson(userId);
   if (person != null)
   {
      displayName = person.properties.firstName + " " + person.properties.lastName;
   }
   personDataCache[userId] = displayName;
   return displayName;
}

/**
 * Cache to not display twice the same element (e.g. if two comments of the
 * same blog post match the search criteria
 */
var processedCache = {};
function addToProcessed(category, key)
{
   var cat = processedCache[category];
   if (cat === undefined)
   {
      processedCache[category] = [];
      cat = processedCache[category];
   }
   cat.push(key);
}
function checkProcessed(category, key)
{
   var cat = processedCache[category];
   if (cat !== undefined)
   {
      for (var x in cat)
      {
         if (cat[x] == key)
         {
            return true;
         }
      }
   }
   return false;
}

/**
 * Returns the name path for a space
 */
function getSpaceNamePath(siteId, containerId, space)
{
   // first find the container to which we are relative to
   var site = siteService.getSite(siteId);
   var container = site.getContainer(containerId);
   var folders = [];
   while (! space.nodeRef.equals(container.nodeRef))
   {
      folders.push(space.name);
      space = space.parent;
   }
   var path = "";
   for (var x = folders.length - 1; x >= 0; x--)
   {
      path += "/" + folders[x];
   }
   return path;
}

/**
 * Returns an item of the document library component.
 */
function getDocumentItem(siteId, containerId, restOfPath, node)
{
   // PENDING: how to handle comments? the document should
   //          be returned instead
   
   // check whether we already processed this document
   var cat = siteId + containerId, refkey = "" + node.nodeRef.toString();
   if (checkProcessed(cat, refkey))
   {
      return null;
   }
   addToProcessed(cat, refkey);
   
   // check whether this is a valid folder or a file
   var item = null;
   if (node.isContainer || node.isDocument)
   {
      item =
      {
         site: getSiteData(siteId),
         container: containerId,
         nodeRef: node.nodeRef.toString(),
         tags: (node.tags !== null) ? node.tags : [],
         name: node.name,
         displayName: node.name,
         description: node.properties["cm:description"],
         modifiedOn: node.properties["cm:modified"],
         modifiedByUser: node.properties["cm:modifier"],
         createdOn: node.properties["cm:created"],
         createdByUser: node.properties["cm:creator"]         
      };
      item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
      item.createdBy = getPersonDisplayName(item.createdByUser);
   }
   if (node.isContainer)
   {
      item.type = "folder";
      item.size = -1;
      item.browseUrl = "documentlibrary?path=" + encodeURIComponent(getSpaceNamePath(siteId, containerId, node));
   }
   else if (node.isDocument)
   {
      item.type = "document";
      item.size = node.size;
      item.browseUrl = "document-details?nodeRef=" + node.nodeRef.toString();
   }
   
   return item;
}

function getBlogPostItem(siteId, containerId, restOfPath, node)
{
   /**
    * Investigate the rest of the path. the first item is the blog post, ignore everything that follows
    * are replies or folders
    */
   var site = siteService.getSite(siteId);
   var container = site.getContainer(containerId);
   
   /**
    * Find the direct child of the container
    * Note: this only works for post which are direct children of the blog container
    */
   var child = node;
   var parent = child.parent;
   while ((parent !== null) && (!parent.nodeRef.equals(container.nodeRef)))
   {
      child = parent;
      parent = parent.parent;
   }
   
   // check whether we found the container
   if (parent === null)
   {
      return null;
   }
   
   // check whether we already added this blog post
   var cat = siteId + containerId, refkey = "" + child.nodeRef.toString();
   if (checkProcessed(cat, refkey))
   {
      return null;
   }
   addToProcessed(cat, refkey);
   
   // child is our blog post
   item =
   {
      site: getSiteData(siteId),
      container: containerId,
      nodeRef: child.nodeRef.toString(),
      type: "blogpost",
      tags: (child.tags !== null) ? child.tags : [],
      name: child.name,
      modifiedOn: child.properties["cm:modified"],
      modifiedByUser: child.properties["cm:modifier"],
      createdOn: child.properties["cm:created"],
      createdByUser: child.properties["cm:creator"],
      size: child.size,
      displayName: child.properties["cm:title"],
      browseUrl: "blog-postview?container=" + containerId + "&postId=" + child.name
   };
   item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
   item.createdBy = getPersonDisplayName(item.createdByUser);  
   
   return item;
}

function getForumPostItem(siteId, containerId, restOfPath, node)
{
   // try to find the first fm:topic node, that's what we return as search result
   var topicNode = node;
   while ((topicNode !== null) && (topicNode.type != "{http://www.alfresco.org/model/forum/1.0}topic"))
   {
      topicNode = topicNode.parent;
   }
   if (topicNode === null)
   {
      return null;
   }
   
   // make sure we haven't already added the post
   var cat = siteId + containerId, refkey = "" + topicNode.nodeRef.toString();
   if (checkProcessed(cat, refkey))
   {
      return null;
   }
   addToProcessed(cat, refkey);
   
   // find the first post, which contains the post title
   // PENDING: error prone
   var postNode = topicNode.childAssocs["cm:contains"][0];
   
   // child is our forum post
   var item =
   {
      site: getSiteData(siteId),
      container: containerId,
      nodeRef: topicNode.nodeRef.toString(),
      type: "forumpost",
      tags: (topicNode.tags != null) ? topicNode.tags : [],
      name: topicNode.name,
      description: topicNode.properties["cm:description"],
      modifiedOn: topicNode.properties["cm:modified"],
      modifiedByUser: topicNode.properties["cm:modifier"],
      createdOn: topicNode.properties["cm:created"],
      createdByUser: topicNode.properties["cm:creator"],
      size: topicNode.size,
      displayName: postNode.properties["cm:title"],
      browseUrl: "discussions-topicview?container=" + containerId + "&topicId=" + topicNode.name
   };
   item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
   item.createdBy = getPersonDisplayName(item.createdByUser);

   return item;
}

function getCalendarItem(siteId, containerId, restOfPath, node)
{
   // only process nodes of the correct type
   if (node.type != "{http://www.alfresco.org/model/calendar}calendarEvent")
   {
      return null;
   }
   
   // make sure we haven't already added the post
   var cat = siteId + containerId, refkey = "" + node.nodeRef.toString();
   if (checkProcessed(cat, refkey))
   {
      return null;
   }
   addToProcessed(cat, refkey);
   
   var item =
   {
      site: getSiteData(siteId),
      container: containerId,
      nodeRef: node.nodeRef.toString(),
      type: "calendarevent",
      tags: (node.tags != null) ? node.tags : [],
      name: node.name,
      description: node.properties["ia:descriptionEvent"],
      modifiedOn: node.properties["cm:modified"],
      modifiedByUser: node.properties["cm:modifier"],
      createdOn: node.properties["cm:created"],
      createdByUser: node.properties["cm:creator"],
      size: -1,
      displayName: node.properties["ia:whatEvent"],
      browseUrl: containerId // this is "calendar"
   };
   item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
   item.createdBy = getPersonDisplayName(item.createdByUser);
      
   return item;
}

function getWikiItem(siteId, containerId, restOfPath, node)
{
   // only process documents
   if (!node.isDocument)
   {
      return null;
   }
   
   // make sure we haven't already added the page
   var cat = siteId + containerId, refkey = "" + node.nodeRef.toString();
   if (checkProcessed(cat, refkey))
   {
      return null;
   }
   addToProcessed(cat, refkey);
   
   var item =
   {
      site: getSiteData(siteId),
      container: containerId,
      nodeRef: node.nodeRef.toString(),
      type: "wikipage",
      tags: (node.tags != null) ? node.tags : [],
      name: node.name,
      description: node.properties["cm:description"],
      modifiedOn: node.properties["cm:modified"],
      modifiedByUser: node.properties["cm:modifier"],
      createdOn: node.properties["cm:created"],
      createdByUser: node.properties["cm:creator"],
      size: node.size,
      displayName: ("" + node.properties["cm:name"]).replace(/_/g, " "),
      browseUrl: "wiki-page?title=" + node.properties["cm:name"]
   };
   item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
   item.createdBy = getPersonDisplayName(item.createdByUser);
      
   return item;
}

function getLinkItem(siteId, containerId, restOfPath, node)
{
   // only process documents
   if (!node.isDocument)
   {
      return null;
   }
   
   // make sure we haven't already added this link
   var cat = siteId + containerId, refkey = "" + node.nodeRef.toString();
   if (checkProcessed(cat, refkey))
   {
      return null;
   }
   addToProcessed(cat, refkey);
   
   var item =
   {
      site: getSiteData(siteId),
      container: containerId,
      nodeRef: node.nodeRef.toString(),
      type: "link",
      tags: (node.tags !== null) ? node.tags : [],
      name: node.name,
      description: node.properties["cm:description"],
      modifiedOn: node.properties["cm:modified"],
      modifiedByUser: node.properties["cm:modifier"],
      createdOn: node.properties["cm:created"],
      createdByUser: node.properties["cm:creator"],
      size: -1,
      displayName: node.properties["lnk:title"],
      browseUrl: "links-view?linkId=" + node.properties["cm:name"]
   };
   item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
   item.createdBy = getPersonDisplayName(item.createdByUser);   
   return item;
}

/**
 * Delegates the extraction to the correct extraction function
 * depending site/container id.
 */
function getItem(siteId, containerId, restOfPath, node)
{
   switch ("" + containerId)
   {
      case "documentLibrary":
         return getDocumentItem(siteId, containerId, restOfPath, node);
         break;
      case "blog":
         return getBlogPostItem(siteId, containerId, restOfPath, node);
         break;
      case "discussions":
         return getForumPostItem(siteId, containerId, restOfPath, node);
         break;
      case "calendar":
         return getCalendarItem(siteId, containerId, restOfPath, node);
         break;
      case "wiki":
         return getWikiItem(siteId, containerId, restOfPath, node);
         break;
      case "links":
         return getLinkItem(siteId, containerId, restOfPath, node);
         break;
   }
   return null;
}

/**
 * Returns an array with [0] = site and [1] = container or null if the node does not match
 */
function splitQNamePath(node)
{
   var path = node.qnamePath;
   var displayPath = node.displayPath.split("/");
   
   if (path.match("^"+SITES_SPACE_QNAME_PATH) != SITES_SPACE_QNAME_PATH)
   {
      return null;
   }
   
   var tmp = path.substring(SITES_SPACE_QNAME_PATH.length);
   var pos = tmp.indexOf('/');
   if (pos < 1)
   {
      return null;
   }
   
   // site id is the cm:name for the site - we cannot use the encoded QName version
   var siteId = displayPath[3];
   tmp = tmp.substring(pos + 1);
   pos = tmp.indexOf('/');
   if (pos < 1)
   {
      return null;
   }
   
   var containerId = tmp.substring(0, pos);
   containerId = containerId.substring(containerId.indexOf(":") + 1);
   var restOfPath = tmp.substring(pos + 1);
   
   return [ siteId, containerId, restOfPath ];
}

/**
 * Processes the search results. Filters out unnecessary nodes
 * 
 * @return the final search results object
 */
function processResults(nodes, maxResults)
{    
   var results = [],
      added = 0,
      parts,
      item,
      i, j;
   
   for (i = 0, j = nodes.length; i < j && added < maxResults; i++)
   {
      /**
       * For each node we extract the site/container qname path and then
       * let the per-container helper function decide what to do.
       */
      parts = splitQNamePath(nodes[i]);
      if (parts !== null)
      {
         item = getItem(parts[0], parts[1], parts[2], nodes[i]);
         if (item !== null)
         {
            results.push(item);
            added++;
         }
      }
   }
   
   return (
   {
      items: results
   });
}

/**
 * Return Search results with the given search terms.
 * 
 * "or" is the default operator, AND and NOT are also supported - as is any other valid fts-alfresco
 * elements such as "quoted terms" and (bracket terms) and also propname:propvalue syntax.
 * 
 * @param params  Object containing search parameters - see API description above
 */
function getSearchResults(params)
{
   var nodes,
      ftsQuery = "",
      term = params.term,
      tag = params.tag,
      formData = params.query;
   
   // Simple keyword search and tag specific search
   if (term !== null && term.length !== 0)
   {
      ftsQuery = "(" + term + ") PATH:\"/cm:taggable/cm:" + search.ISO9075Encode(term) + "/member\" ";
   }
   else if (tag !== null && tag.length !== 0)
   {
      ftsQuery = "PATH:\"/cm:taggable/cm:" + search.ISO9075Encode(tag) + "/member\" ";
   }
   
   // Advanced search form data search.
   // Supplied as json in the standard Alfresco Forms data structure:
   //    prop_<name>:value|assoc_<name>:value
   //    name = namespace_propertyname|pseudopropertyname
   //    value = string value - comma separated for multi-value, no escaping yet!
   // - underscore represents colon character in name
   // - pseudo property is one of any cm:content url property: mimetype|encoding|size
   // - always string values - interogate DD for type data
   if (formData !== null && formData.length !== 0)
   {
      var formJson = jsonUtils.toObject(formData);
      
      // extract form data and generate search query
      var first = true;
      for (var p in formJson)
      {
         // retrieve value and check there is someting to search for
         // currently all values are returned as strings
         var propValue = formJson[p];
         if (propValue.length !== 0)
         {
            if (p.indexOf("prop_") === 0)
            {
               // found a property - is it namespace_propertyname or pseudo property format?
               var propName = p.substr(5);
               if (propName.indexOf("_") !== -1)
               {
                  // property name - convert to DD property name format
                  propName = propName.replace("_", ":");
                  // TODO: adv search in jsf uses quotes for boolean, number etc... dates!
                  ftsQuery += (first ? '(' : ' AND ') + propName + ':"' + propValue + '"';
               }
               else
               {
                  // pseudo cm:content property - e.g. mimetype, size or encoding
                  ftsQuery += (first ? '(' : ' AND ') + 'cm:content.' + propName + ':' + propValue;
               }
               first = false;
            }
         }
      }
      
      // extract data type for this search
      ftsQuery = 'TYPE:"' + formJson.datatype + '" AND (' + ftsQuery + (!first ? '))' : ')');
   }
   
   if (ftsQuery.length !== 0)
   {
      // we processed the search terms, so suffix the PATH query
      var path = SITES_SPACE_QNAME_PATH;
      if (params.siteId !== null && params.siteId.length > 0)
      {
         path += "cm:" + search.ISO9075Encode(params.siteId) + "/";
      }
      else
      {
         path += "*/";
      }
      if (params.containerId !== null && params.containerId.length > 0)
      {
         path += "cm:" + search.ISO9075Encode(params.containerId) + "/";
      }
      else
      {
         path += "*/";
      }
   	
      ftsQuery  = 'PATH:"' + path + '/*" AND (' + ftsQuery + ') ';
      ftsQuery += 'AND -TYPE:"cm:thumbnail"';
      
      // sort field - expecting field to in one of the following formats:
      //  - short QName form such as: cm:name
      //  - pseudo cm:content field starting with "." such as: .size
      //  - any other directly supported search field such as: TYPE
      var sortColumns = [];
      var sort = params.sort;
      if (sort != null && sort.length != 0)
      {
         var asc = true;
         var separator = sort.indexOf("|");
         if (separator != -1)
         {
            sort = sort.substring(0, separator);
            asc = (sort.substring(separator + 1) == "true");
         }
         var column;
         if (sort.charAt(0) == '.')
         {
            // handle pseudo cm:content fields
            column = "@{http://www.alfresco.org/model/content/1.0}content" + sort;
         }
         else if (sort.indexOf(":") != -1)
         {
            // handle attribute field sort
            column = "@" + utils.longQName(sort);
         }
         else
         {
            // other sort types e.g. TYPE
            column = sort;
         }
         sortColumns.push(
         {
            column: column,
            ascending: asc
         });
      }
      
      // perform fts-alfresco language query
      var queryDef = {
         query: ftsQuery,
         language: "fts-alfresco",
         page: {maxItems: params.maxResults},
         templates: QUERY_TEMPLATES,
         defaultField: "keywords",
         onerror: "no-results",
         sort: sortColumns 
      };
      nodes = search.query(queryDef);
   }
   else
   {
      // failed to process the search string - empty list returned
      nodes = [];
   }
   
   return processResults(nodes, params.maxResults);
}