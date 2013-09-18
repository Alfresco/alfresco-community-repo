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
const DEFAULT_PAGE_SIZE = 50;
const SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";
const DISCUSSION_QNAMEPATH = "/fm:discussion";
const COMMENT_QNAMEPATH = DISCUSSION_QNAMEPATH + "/cm:Comments";

/**
 * Returns site information data structure.
 * { shortName: siteId, title: title }
 * 
 * Caches the data to avoid repeatedly querying the repository.
 */
var siteDataCache = {};
function getSiteData(siteId)
{
   if (typeof siteDataCache[siteId] === "object")
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
var personDataCache = {};
function getPersonDisplayName(userId)
{
   if (typeof personDataCache[userId] === "object")
   {
      return personDataCache[userId];
   }
   
   var displayName = people.getPersonFullName(userId);
   if (displayName == null)
   {
      displayName = "";
   }
   personDataCache[userId] = displayName;
   return displayName;
}

/**
 * Cache to not display twice the same element (e.g. if two comments of the
 * same blog post match the search criteria
 */
var processedCache;
function checkProcessedCache(key)
{
   var found = processedCache.hasOwnProperty(key);
   if (!found)
   {
      processedCache[key] = true;
   }
   else if (found && logger.isLoggingEnabled())
      logger.log("...already processed item with key: " + key);
   return found;
}

/**
 * Returns an item outside of a site in the main repository.
 */
function getRepositoryItem(folderPath, node, populate)
{
   // check whether we already processed this document
   if (checkProcessedCache("" + node.nodeRef.toString()))
   {
      return null;
   }
   
   // check whether this is a valid folder or a file
   var item = t = null;
   if (node.qnamePath.indexOf(COMMENT_QNAMEPATH) == -1 &&
       !(node.qnamePath.match(DISCUSSION_QNAMEPATH+"$") == DISCUSSION_QNAMEPATH))
   {
      if (node.isContainer || node.isDocument)
      {
         if (!populate) return {};
         item =
         {
            nodeRef: node.nodeRef.toString(),
            tags: ((t = node.tags) !== null) ? t : [],
            name: node.name,
            displayName: node.name,
            title: node.properties["cm:title"],
            description: node.properties["cm:description"],
            modifiedOn: node.properties["cm:modified"],
            modifiedByUser: node.properties["cm:modifier"],
            createdOn: node.properties["cm:created"],
            createdByUser: node.properties["cm:creator"],
            mimetype: node.mimetype,
            path: folderPath.join("/")
         };
         item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
         item.createdBy = getPersonDisplayName(item.createdByUser);
      }
      if (node.isContainer)
      {
         item.type = "folder";
         item.size = -1;
      }
      else if (node.isDocument)
      {
         item.type = "document";
         item.size = node.size;
      }
   }
   
   return item;
}

/**
 * Returns an item of the document library component.
 */
function getDocumentItem(siteId, containerId, pathParts, node, populate)
{
   // PENDING: how to handle comments? the document should
   //          be returned instead
   
   // check whether we already processed this document
   if (checkProcessedCache("" + node.nodeRef.toString()))
   {
      return null;
   }
   
   // check whether this is a valid folder or a file
   var item = t = null;
   if (node.qnamePath.indexOf(COMMENT_QNAMEPATH) == -1 &&
       !(node.qnamePath.match(DISCUSSION_QNAMEPATH+"$") == DISCUSSION_QNAMEPATH))
   {
      if (node.isContainer || node.isDocument)
      {
         if (!populate) return {};
         item =
         {
            site: getSiteData(siteId),
            container: containerId,
            nodeRef: node.nodeRef.toString(),
            tags: ((t = node.tags) !== null) ? t : [],
            name: node.name,
            displayName: node.name,
            title: node.properties["cm:title"],
            description: node.properties["cm:description"],
            modifiedOn: node.properties["cm:modified"],
            modifiedByUser: node.properties["cm:modifier"],
            createdOn: node.properties["cm:created"],
            createdByUser: node.properties["cm:creator"],
            mimetype: node.mimetype,
            path: pathParts.join("/")
         };
         item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
         item.createdBy = getPersonDisplayName(item.createdByUser);
      }
      if (node.isContainer)
      {
         item.type = "folder";
         item.size = -1;
      }
      else if (node.isDocument)
      {
         item.type = "document";
         item.size = node.size;
      }
   }
   
   return item;
}

function getBlogPostItem(siteId, containerId, pathParts, node, populate)
{
   /**
    * Investigate the rest of the path. the first item is the blog post, ignore everything that follows
    * are replies or folders
    */
   var site = siteService.getSite(siteId);
   if (site === null)
   {
      return null;
   }
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
   if (checkProcessedCache("" + child.nodeRef.toString()))
   {
      return null;
   }
   
   // child is our blog post
   if (!populate) return {};
   var item, t = null;
   item =
   {
      site: getSiteData(siteId),
      container: containerId,
      nodeRef: child.nodeRef.toString(),
      type: "blogpost",
      tags: ((t = child.tags) !== null) ? t : [],
      name: child.name,
      modifiedOn: child.properties["cm:modified"],
      modifiedByUser: child.properties["cm:modifier"],
      createdOn: node.properties["cm:created"],
      createdByUser: node.properties["cm:creator"],
      size: child.size,
      displayName: child.properties["cm:title"]
   };
   item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
   item.createdBy = getPersonDisplayName(item.createdByUser);
   
   return item;
}

function getForumPostItem(siteId, containerId, pathParts, node, populate)
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
   if (checkProcessedCache("" + topicNode.nodeRef.toString()))
   {
      return null;
   }
   
   // find the first post, which contains the post title
   // PENDING: error prone
   var postNode = topicNode.childAssocs["cm:contains"][0];
   
   // child is our forum post
   if (!populate) return {};
   var item = t = null;
   item =
   {
      site: getSiteData(siteId),
      container: containerId,
      nodeRef: topicNode.nodeRef.toString(),
      type: "forumpost",
      tags: ((t = topicNode.tags) !== null) ? t : [],
      name: topicNode.name,
      description: topicNode.properties["cm:description"],
      modifiedOn: topicNode.properties["cm:modified"],
      modifiedByUser: topicNode.properties["cm:modifier"],
      createdOn: node.properties["cm:created"],
      createdByUser: node.properties["cm:creator"],
      size: topicNode.size,
      displayName: postNode.properties["cm:title"]
   };
   item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
   item.createdBy = getPersonDisplayName(item.createdByUser);

   return item;
}

function getCalendarItem(siteId, containerId, pathParts, node, populate)
{
   // only process nodes of the correct type
   if (node.type != "{http://www.alfresco.org/model/calendar}calendarEvent")
   {
      return null;
   }
   
   // make sure we haven't already added the event
   if (checkProcessedCache("" + node.nodeRef.toString()))
   {
      return null;
   }
   
   if (!populate) return {};
   var item, t = null;
   item =
   {
      site: getSiteData(siteId),
      container: containerId,
      nodeRef: node.nodeRef.toString(),
      type: "calendarevent",
      tags: ((t = node.tags) !== null) ? t : [],
      name: node.name,
      description: node.properties["ia:descriptionEvent"],
      modifiedOn: node.properties["cm:modified"],
      modifiedByUser: node.properties["cm:modifier"],
      createdOn: node.properties["cm:created"],
      createdByUser: node.properties["cm:creator"],
      size: -1,
      displayName: node.properties["ia:whatEvent"]
   };
   item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
   item.createdBy = getPersonDisplayName(item.createdByUser);
      
   return item;
}

function getWikiItem(siteId, containerId, pathParts, node, populate)
{
   // only process documents
   if (!node.isDocument)
   {
      return null;
   }
   
   // make sure we haven't already added the page
   if (checkProcessedCache("" + node.nodeRef.toString()))
   {
      return null;
   }
   
   if (!populate) return {};
   var item, t = null;
   item =
   {
      site: getSiteData(siteId),
      container: containerId,
      nodeRef: node.nodeRef.toString(),
      type: "wikipage",
      tags: ((t = node.tags) !== null) ? t : [],
      name: node.name,
      description: node.properties["cm:description"],
      modifiedOn: node.properties["cm:modified"],
      modifiedByUser: node.properties["cm:modifier"],
      createdOn: node.properties["cm:created"],
      createdByUser: node.properties["cm:creator"],
      size: node.size,
      displayName: ("" + node.name).replace(/_/g, " ")
   };
   item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
   item.createdBy = getPersonDisplayName(item.createdByUser);
   
   return item;
}

function getLinkItem(siteId, containerId, pathParts, node, populate)
{
   // only process documents
   if (!node.isDocument)
   {
      return null;
   }
   
   // make sure we haven't already added this link
   if (checkProcessedCache("" + node.nodeRef.toString()))
   {
      return null;
   }
   
   var item = t = null;
   if (node.qnamePath.indexOf(COMMENT_QNAMEPATH) == -1 &&
       !(node.qnamePath.match(DISCUSSION_QNAMEPATH+"$") == DISCUSSION_QNAMEPATH))
   {
      if (!populate) return {};
      item =
      {
         site: getSiteData(siteId),
         container: containerId,
         nodeRef: node.nodeRef.toString(),
         type: "link",
         tags: ((t = node.tags) !== null) ? t : [],
         name: node.name,
         description: node.properties["cm:description"],
         modifiedOn: node.properties["cm:modified"],
         modifiedByUser: node.properties["cm:modifier"],
         createdOn: node.properties["cm:created"],
         createdByUser: node.properties["cm:creator"],
         size: -1,
         displayName: node.properties["lnk:title"]
      };
      item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
      item.createdBy = getPersonDisplayName(item.createdByUser);
   }
   
   return item;
}

function getDataItem(siteId, containerId, pathParts, node, populate)
{
   // make sure we haven't already added this item
   if (checkProcessedCache("" + node.nodeRef.toString()))
   {
      return null;
   }
   
   var item = null;
   
   // data item can be either ba containing dl:dataList or any dl:dataListItem subtype
   if (node.type == "{http://www.alfresco.org/model/datalist/1.0}dataList")
   {
      if (!populate) return {};
      // found a data list
      item =
      {
         site: getSiteData(siteId),
         container: containerId,
         nodeRef: node.nodeRef.toString(),
         type: "datalist",
         tags: [],
         name: node.name,
         description: node.properties["cm:description"],
         modifiedOn: node.properties["cm:modified"],
         modifiedByUser: node.properties["cm:modifier"],
         createdOn: node.properties["cm:created"],
         createdByUser: node.properties["cm:creator"],
         size: -1,
         displayName: node.properties["cm:title"]
      };
      item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
      item.createdBy = getPersonDisplayName(item.createdByUser);
   }
   else if (node.isSubType("{http://www.alfresco.org/model/datalist/1.0}dataListItem"))
   {
      if (!populate) return {};
      // found a data list item
      item =
      {
         site: getSiteData(siteId),
         container: containerId,
         nodeRef: node.nodeRef.toString(),
         type: "datalistitem",
         tags: [],
         name: node.parent.name,    // used to generate link to parent datalist - not ideal
         modifiedOn: node.properties["cm:modified"],
         modifiedByUser: node.properties["cm:modifier"],
         createdOn: node.properties["cm:created"],
         createdByUser: node.properties["cm:creator"],
         size: -1,
         displayName: node.name     // unfortunately does not have a common display name property
      };
      item.modifiedBy = getPersonDisplayName(item.modifiedByUser);
      item.createdBy = getPersonDisplayName(item.createdByUser);
   }
   
   return item;
}

/**
 * Delegates the extraction to the correct extraction function
 * depending on containerId.
 */
function getItem(siteId, containerId, pathParts, node, populate)
{
   var item = null;
   if (siteId == null)
   {
      item = getRepositoryItem(pathParts, node, populate);
   }
   else
   {
      switch ("" + containerId.toLowerCase())
      {
         case "documentlibrary":
            item = getDocumentItem(siteId, containerId, pathParts, node, populate);
            break;
         case "blog":
            item = getBlogPostItem(siteId, containerId, pathParts, node, populate);
            break;
         case "discussions":
            item = getForumPostItem(siteId, containerId, pathParts, node, populate);
            break;
         case "calendar":
            item = getCalendarItem(siteId, containerId, pathParts, node, populate);
            break;
         case "wiki":
            item = getWikiItem(siteId, containerId, pathParts, node, populate);
            break;
         case "links":
            item = getLinkItem(siteId, containerId, pathParts, node, populate);
            break;
         case "datalists":
            item = getDataItem(siteId, containerId, pathParts, node, populate);
            break;
      }
   }
   return item;
}

/**
 * Splits the qname path to a node.
 * 
 * Returns an array with:
 * [0] = site
 * [1] = container or null if the node does not match
 * [2] = remaining part of the cm:name based path to the object - as an array
 */
function splitQNamePath(node, rootNodeDisplayPath, rootNodeQNamePath)
{
   var path = node.qnamePath,
       displayPath = utils.displayPath(node).split("/"),
       parts = null;
   
   // restructure the display path of the node if we have an overriden root node
   if (rootNodeDisplayPath != null && path.indexOf(rootNodeQNamePath) === 0)
   {
      var nodeDisplayPath = utils.displayPath(node).split("/");
      nodeDisplayPath = nodeDisplayPath.splice(rootNodeDisplayPath.length);
      for (var i = 0; i < rootNodeQNamePath.split("/").length-1; i++)
      {
         nodeDisplayPath.unshift(null);
      }
      displayPath = nodeDisplayPath;
   }
   
   if (path.match("^"+SITES_SPACE_QNAME_PATH) == SITES_SPACE_QNAME_PATH)
   {
      var tmp = path.substring(SITES_SPACE_QNAME_PATH.length),
          pos = tmp.indexOf('/');
      if (pos >= 1)
      {
         // site id is the cm:name for the site - we cannot use the encoded QName version
         var siteId = displayPath[3];
         tmp = tmp.substring(pos + 1);
         pos = tmp.indexOf('/');
         if (pos >= 1)
         {
            // strip container id from the path
            var containerId = tmp.substring(0, pos);
            containerId = containerId.substring(containerId.indexOf(":") + 1);
            
            parts = [ siteId, containerId, displayPath.slice(5, displayPath.length) ];
         }
      }
   }
   
   return (parts !== null ? parts : [ null, null, displayPath ]);
}

/**
 * Processes the search results. Filters out unnecessary nodes
 * 
 * @return the final search results object
 */
function processResults(nodes, maxPageResults, startIndex, rootNode)
{
   // empty cache state
   processedCache = {};
   var results = [],
      added = processed = failed = 0,
      parts,
      item,
      rootNodeDisplayPath = rootNode ? utils.displayPath(rootNode).split("/") : null,
      rootNodeQNamePath = rootNode ? rootNode.qnamePath : null;
   
   if (logger.isLoggingEnabled())
      logger.log("Processing resultset of length: " + nodes.length);
   
   startIndex = startIndex ? startIndex : 0;
   for (var i = 0, j = nodes.length; i < j; i++)
   {
      // For each node we extract the site/container qname path and then
      // let the per-container helper function decide what to do.
      try
      {
         // We only want to populate node return structures if we are going to add the items to the results,
         // so we skip (process, but don't populate or add to results) until we have reached the startIndex
         // then we populate and add items up to the maxPageResults - after that we still need to process
         // (but don't populate or add to results) each item to correctly calculate the totalRecordsUpper.
         var populate = (processed >= startIndex && added < maxPageResults);
         parts = splitQNamePath(nodes[i], rootNodeDisplayPath, rootNodeQNamePath);
         item = getItem(parts[0], parts[1], parts[2], nodes[i], populate);
         if (item !== null)
         {
            processed++;
            if (populate)
            {
               results.push(item);
               added++;
            }
         }
         else
         {
            failed++;
         }
      }
      catch (e)
      {
         // THOR-833
         if (logger.isWarnLoggingEnabled() == true)
         {
            logger.warn("search.lib.js: Skipping node due to exception when processing query result: " + e);
            logger.warn("..." + nodes[i].nodeRef);
         }
         
         failed++;
      }
   }
   
   if (logger.isLoggingEnabled())
      logger.log("Filtered resultset to length: " + results.length + ". Discarded item count: " + failed);
   
   return (
   {
      paging:
      {
         totalRecords: results.length,
         totalRecordsUpper: nodes.length - failed,
         startIndex: startIndex
      },
      items: results
   });
}

/**
 * Helper to escape the QName string so it is valid inside an fts-alfresco query.
 * The language supports the SQL92 identifier standard.
 * 
 * @param qname   The QName string to escape
 * @return escaped string
 */
function escapeQName(qname)
{
   var separator = qname.indexOf(':'),
       namespace = qname.substring(0, separator),
       localname = qname.substring(separator + 1);

   return escapeString(namespace) + ':' + escapeString(localname);
}

function escapeString(value)
{
   var result = "";

   for (var i=0,c; i<value.length; i++)
   {
      c = value.charAt(i);
      if (i == 0)
      {
         if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'))
         {
            result += '\\';
         }
      }
      else
      {
         if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$' || c == '#'))
         {
            result += '\\';
         }
      }
      result += c;
   }
   return result;
}
 
/**
 * Helper method used to determine whether the property value is multi-valued.
 *
 * @param propValue the property value to test
 * @param modePropValue the logical operand that should be used for multi-value property
 * @return true if it is multi-valued, false otherwise
 */
function isMultiValueProperty(propValue, modePropValue)
{
   return modePropValue != null && propValue.indexOf(",") !== -1;
}

/**
 * Helper method used to construct lucene query fragment for a multi-valued property.
 *
 * @param propName property name
 * @param propValue property value (comma separated)
 * @param operand logical operand that should be used
 * @param pseudo is it a pseudo property
 * @return lucene query with multi-valued property
 */
function processMultiValue(propName, propValue, operand, pseudo)
{
   var multiValue = propValue.split(","),
       formQuery = "";
   for (var i = 0; i < multiValue.length; i++)
   {
      if (i > 0)
      {
         formQuery += ' ' + operand + ' ';
      }
      
      if (pseudo)
      {
         formQuery += '(cm:content.' + propName + ':"' + multiValue[i] + '")';
      }
      else
      {
         formQuery += '(' + escapeQName(propName) + ':"' + multiValue[i] + '")';
      }
   }
   
   return formQuery;
}

/**
 * Resolve a root node reference to use as the Repository root for a search.
 * 
 * NOTE: see ParseArgs.resolveNode()
 * 
 * @method resolveRootNode
 * @param reference {string} "virtual" nodeRef, nodeRef or xpath expressions
 * @return {ScriptNode|null} Node corresponding to supplied expression. Returns null if node cannot be resolved.
 */
function resolveRootNode(reference)
{
   var node = null;
   try
   {
      if (reference == "alfresco://company/home")
      {
         node = "";
      }
      else if (reference == "alfresco://user/home")
      {
         node = userhome;
      }
      else if (reference == "alfresco://sites/home")
      {
         node = companyhome.childrenByXPath("st:sites")[0];
      }
      else if (reference == "alfresco://shared")
      {
         node = companyhome.childrenByXPath("app:shared")[0];
      }
      else if (reference.indexOf("://") > 0)
      {
         if (reference.indexOf(":") < reference.indexOf("://"))
         {
            var newRef = "/" + reference.replace("://", "/");
            var newRefNodes = search.xpathSearch(newRef);
            node = search.findNode(String(newRefNodes[0].nodeRef));
         }
         else
         {
            node = search.findNode(reference);
         }
      }
      else if (reference.substring(0, 1) == "/")
      {
         node = search.xpathSearch(reference)[0];
      }
      if (node === null)
      {
         logger.log("Unable to resolve specified root node reference: " + reference);
      }
   }
   catch (e)
   {
      node = null;
   }
   return node !== "" ? node : null;
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
      formData = params.query,
      rootNode = params.rootNode ? resolveRootNode(params.rootNode) : null;
   
   // Simple keyword search and tag specific search
   if (term !== null && term.length !== 0)
   {
      // TAG is now part of the default search macro
      ftsQuery = term + " ";
   }
   else if (tag !== null && tag.length !== 0)
   {
      // Just look for tag
      ftsQuery = "TAG:" + tag +" ";
   }
   
   // Advanced search form data search.
   // Supplied as json in the standard Alfresco Forms data structure:
   //    prop_<name>:value|assoc_<name>:value
   //    name = namespace_propertyname|pseudopropertyname
   //    value = string value - comma separated for multi-value, no escaping yet!
   // - underscore represents colon character in name
   // - pseudo property is one of any cm:content url property: mimetype|encoding|size
   // - always string values - interogate DD for type data
   // - an additional "-mode" suffixed parameter for a value is allowed to specify
   //   either an AND or OR join condition for multi-value property searches
   if (formData !== null && formData.length !== 0)
   {
      var formQuery = "",
          formJson = jsonUtils.toObject(formData);
      
      // extract form data and generate search query
      var first = true;
      var useSubCats = false;
      for (var p in formJson)
      {
         // retrieve value and check there is someting to search for
         // currently all values are returned as strings
         var propValue = formJson[p], modePropValue = formJson[p + "-mode"];
         if (propValue.length !== 0)
         {
            if (p.indexOf("prop_") === 0 && p.match("-mode$") != "-mode")
            {
               // found a property - is it namespace_propertyname or pseudo property format?
               var propName = p.substr(5);
               if (propName.indexOf("_") !== -1)
               {
                  // property name - convert to DD property name format
                  propName = propName.replace("_", ":");
                  
                  // special case for range packed properties
                  if (propName.match("-range$") == "-range")
                  {
                     // currently support text based ranges (usually numbers) or date ranges
                     // range value is packed with a | character separator
                     
                     // if neither value is specified then there is no need to add the term
                     if (propValue.length > 1)
                     {
                        var from, to, sepindex = propValue.indexOf("|");
                        if (propName.match("-date-range$") == "-date-range")
                        {
                           // date range found
                           propName = propName.substr(0, propName.length - "-date-range".length)
                           
                           // work out if "from" and/or "to" are specified - use MIN and MAX otherwise;
                           // we only want the "YYYY-MM-DD" part of the ISO date value - so crop the strings
                           from = (sepindex === 0 ? "MIN" : propValue.substr(0, 10));
                           to = (sepindex === propValue.length - 1 ? "MAX" : propValue.substr(sepindex + 1, 10));
                        }
                        else
                        {
                           // simple range found
                           propName = propName.substr(0, propName.length - "-range".length);
                           
                           // work out if "min" and/or "max" are specified - use MIN and MAX otherwise
                           from = (sepindex === 0 ? "MIN" : propValue.substr(0, sepindex));
                           to = (sepindex === propValue.length - 1 ? "MAX" : propValue.substr(sepindex + 1));
                        }
                        formQuery += (first ? '' : ' AND ') + escapeQName(propName) + ':"' + from + '".."' + to + '"';
                        first = false;
                     }
                  }
                  else if (propName.indexOf("cm:categories") != -1) 
                  {
                     // determines if the checkbox use sub categories was clicked
                     if (propName.indexOf("usesubcats") == -1)
                     {
                        if (formJson["prop_cm_categories_usesubcats"] == "true")
                        {
                           useSubCats = true;
                        }
                        
                        // build list of category terms to search for
                        var firstCat = true;
                        var catQuery = "";
                        var cats = propValue.split(',');
                        for (var i = 0; i < cats.length; i++) 
                        {
                           var cat = cats[i];
                           var catNode = search.findNode(cat);
                           if (catNode) 
                           {
                              catQuery += (firstCat ? '' : ' OR ') + "PATH:\"" + catNode.qnamePath + (useSubCats ? "//*\"" : "/member\"" );
                              firstCat = false;
                           }
                        }
                        
                        if (catQuery.length !== 0)
                        {
                           // surround category terms with brackets if appropriate
                           formQuery += (first ? '' : ' AND ') + "(" + catQuery + ")";
                           first = false;
                        }
                     }
                  }
                  else if (isMultiValueProperty(propValue, modePropValue))
                  {
                     formQuery += (first ? '(' : ' AND (');
                     formQuery += processMultiValue(propName, propValue, modePropValue, false);
                     formQuery += ')';
                     first = false;
                  }
                  else
                  {
                     if (propValue.charAt(0) === '"' && propValue.charAt(propValue.length-1) === '"')
                     {
                        formQuery += (first ? '' : ' AND ') + escapeQName(propName) + ':' + propValue;
                     }
                     else
                     {
                        formQuery += (first ? '' : ' AND ') + escapeQName(propName) + ':\\"' + propValue + '\\"';
                     }
                     first = false;
                  }
               }
               else
               {
                  if (isMultiValueProperty(propValue, modePropValue))
                  {
                     // multi-valued pseudo cm:content property - e.g. mimetype, size or encoding
                     formQuery += (first ? '(' : ' AND (');
                     formQuery += processMultiValue(propName, propValue, modePropValue, true);
                     formQuery += ')';
                  }
                  else
                  {
                     // single pseudo cm:content property - e.g. mimetype, size or encoding
                     formQuery += (first ? '' : ' AND ') + 'cm:content.' + propName + ':"' + propValue + '"';
                  }
                  first = false;
               }
            }
         }
      }
      
      if (formQuery.length !== 0 || ftsQuery.length !== 0)
      {
         // extract data type for this search - advanced search query is type specific
         ftsQuery = 'TYPE:"' + formJson.datatype + '"' +
                    (formQuery.length !== 0 ? ' AND (' + formQuery + ')' : '') +
                    (ftsQuery.length !== 0 ? ' AND (' + ftsQuery + ')' : '');
      }
   }
   
   if (ftsQuery.length !== 0)
   {
      // ensure a TYPE is specified - if no add one to remove system objects from result sets
      if (ftsQuery.indexOf("TYPE:\"") === -1 && ftsQuery.indexOf("TYPE:'") === -1)
      {
         ftsQuery += ' AND (+TYPE:"cm:content" +TYPE:"cm:folder")';
      }
      
      // we processed the search terms, so suffix the PATH query
      var path = null;
      if (!params.repo)
      {
         path = SITES_SPACE_QNAME_PATH;
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
      }
      
      // root node - generally used for overridden Repository root in Share
      if (params.repo && rootNode !== null)
      {
         ftsQuery = 'PATH:"' + rootNode.qnamePath + '//*" AND (' + ftsQuery + ')';
      }
      else if (path !== null)
      {
         ftsQuery = 'PATH:"' + path + '/*" AND (' + ftsQuery + ')';
      }
      ftsQuery = '(' + ftsQuery + ') AND -TYPE:"cm:thumbnail" AND -TYPE:"cm:failedThumbnail" AND -TYPE:"cm:rating"';
      ftsQuery = '(' + ftsQuery + ') AND NOT ASPECT:"sys:hidden"';

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
            asc = (sort.substring(separator + 1) == "true");
            sort = sort.substring(0, separator);
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
      
      if (logger.isLoggingEnabled())
         logger.log("Query:\r\n" + ftsQuery + "\r\nSortby: " + (sort != null ? sort : ""));
      
      // perform fts-alfresco language query
      var queryDef = {
         query: ftsQuery,
         language: "fts-alfresco",
         page: {maxItems: params.maxResults * 2},    // allow for space for filtering out results
         templates: getQueryTemplate(),
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
   
   return processResults(nodes, params.pageSize < params.maxResults ? params.pageSize : params.maxResults, params.startIndex, rootNode);
}

/**
 * Return the fts-alfresco query template to use.
 * The default searches name, title, descripton, calendar, link, full text and tag fields.
 * It is configurable via the .config.xml attached to this webscript.
 */
function getQueryTemplate()
{
   var t =
      [{
         field: "keywords",
         template: "%(cm:name cm:title cm:description ia:whatEvent ia:descriptionEvent lnk:title lnk:description TEXT TAG)"
      }],
      qt = new XML(config.script)["default-query-template"];
   if (qt != null && qt.length() != 0)
   {
      t[0].template = qt.toString();
   }
   return t;
}
