/**
 * Live Search Component
 * 
 * Takes the following object as Input:
 *    params
 *    {
 *       type: search mode type - one of "documents|sites|people"
 *       term: search terms
 *       maxResults: maximum results to return
 *    };
 * 
 * Outputs:
 *  items - Array of objects containing the search results
 */

const DEFAULT_MAX_RESULTS = 5;
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
 * Return the fts-alfresco query template to use.
 * The default searches name, title, descripton, calendar, link, full text and tag fields.
 * It is configurable via the .config.xml attached to this webscript.
 */
function getQueryTemplate()
{
   var t =
      [{
         field: "keywords",
         template: "%(cm:name cm:title cm:description TEXT TAG)"
      }],
      qt = new XML(config.script)["default-query-template"];
   if (qt != null && qt.length() != 0)
   {
      t[0].template = qt.toString();
   }
   return t;
}

/**
 * Process and return a document item node
 */
function getDocumentItem(container, node)
{
   // check whether this is a valid folder or a file
   var item = null;
   if (node.qnamePath.indexOf(COMMENT_QNAMEPATH) == -1 &&
       !(node.qnamePath.match(DISCUSSION_QNAMEPATH+"$") == DISCUSSION_QNAMEPATH))
   {
      if (node.isDocument)
      {
         item =
         {
            nodeRef: node.nodeRef.toString(),
            name: node.name,
            title: node.properties["cm:title"],
            description: node.properties["cm:description"],
            modifiedOn: node.properties["cm:modified"],
            modifiedBy: node.properties["cm:modifier"],
            createdOn: node.properties["cm:created"],
            createdBy: node.properties["cm:creator"],
            mimetype: node.mimetype,
            size: node.size
         };
         if (container.siteId !== null)
         {
            item.site = getSiteData(container.siteId);
            item.container = container.containerId;
         }
         if (node.hasAspect("{http://www.alfresco.org/model/content/1.0}thumbnailModification"))
         {
            var dates = node.properties["lastThumbnailModification"];
            for (var i=0; i<dates.length; i++)
            {
               if (dates[i].indexOf("doclib") !== -1)
               {
                  item.lastThumbnailModification = dates[i];
                  break;
               }
            }
         }
      }
   }
   
   return item;
}

/**
 * Splits the qname path to a node.
 * 
 * Returns container meta object containing the following properties:
 *    siteId
 *    containerId
 */
function splitQNamePath(node)
{
   var path = node.qnamePath,
       container = {
         siteId: null,
         containerId: null
       };
   
   // TODO: should we do this processing here? until it is clicked...
   if (path.match("^"+SITES_SPACE_QNAME_PATH) == SITES_SPACE_QNAME_PATH)
   {
      var tmp = path.substring(SITES_SPACE_QNAME_PATH.length),
          pos = tmp.indexOf('/');
      if (pos >= 1)
      {
         var siteQName = Packages.org.alfresco.util.ISO9075.decode(tmp.split("/")[0]);
             siteId = siteQName.substring(siteQName.indexOf(":") + 1);
         tmp = tmp.substring(pos + 1);
         pos = tmp.indexOf('/');
         if (pos >= 1)
         {
            // strip container id from the path
            var containerId = tmp.substring(0, pos);
            containerId = containerId.substring(containerId.indexOf(":") + 1);
            
            container.siteId = siteId;
            container.containerId = containerId;
         }
      }
   }
   
   return container;
}

/**
 * Dispatch a live search to the appropriate search method for the requested result type.
 */
function liveSearch(params)
{
   switch (params.type)
   {
      case "documents":
         return getDocResults(params);
         break;
      case "sites":
         return getSiteResults(params);
         break;
      case "people":
         return getPeopleResults(params);
         break;
   }
}

/**
 * Return Document Search results with the given search terms.
 * 
 * "AND" is the default operator unless configured otherwise, OR, AND and NOT are also supported -
 * as is any other valid fts-alfresco elements such as "quoted terms" and (bracket terms) and also
 * propname:propvalue syntax.
 * 
 * @param params  Object containing search parameters - see API description above
 */
function getDocResults(params)
{
   // ensure a TYPE is specified
   var ftsQuery = params.term + ' AND +TYPE:"cm:content"';
   
   // root node - generally used for overridden Repository root in Share
   if (params.rootNode !== null)
   {
      ftsQuery = 'PATH:"' + rootNode.qnamePath + '//*" AND (' + ftsQuery + ')';
   }
   ftsQuery = '(' + ftsQuery + ') AND -TYPE:"cm:thumbnail" AND -TYPE:"cm:failedThumbnail" AND -TYPE:"cm:rating" AND NOT ASPECT:"sys:hidden"';
   
   if (logger.isLoggingEnabled())
      logger.log("LiveQuery:\r\n" + ftsQuery);
   
   // get default fts operator from the config
   //
   // TODO: common search lib - for both live and standard e.g. to get values like this...
   //
   var operator = "AND";
   var cf = new XML(config.script)["default-operator"];
   if (cf != null && cf.length != 0)
   {
      operator = cf.toString();
   }
   
   // perform fts-alfresco language query
   var queryDef = {
      query: ftsQuery,
      language: "fts-alfresco",
      templates: getQueryTemplate(),
      defaultField: "keywords",
      defaultOperator: operator,
      onerror: "no-results",
      page: {
         maxItems: params.maxResults + 1,
         skipCount: params.startIndex
      }
   };
   var nodes = search.query(queryDef),
       results = [];
   
   if (logger.isLoggingEnabled())
      logger.log("Processing resultset of length: " + nodes.length);
   
   for (var i=0, item; i<nodes.length && i<params.maxResults; i++)
   {
      // For each node we extract the site/container qname path and then
      // let the per-container helper function decide what to do.
      try
      {
         item = getDocumentItem(splitQNamePath(nodes[i]), nodes[i]);
         if (item !== null)
         {
            results.push(item);
         }
      }
      catch (e)
      {
         if (logger.isWarnLoggingEnabled() == true)
         {
            logger.warn("live-search.lib.js: Skipping node due to exception when processing query result: " + e);
            logger.warn("..." + nodes[i].nodeRef);
         }
      }
   }
   
   return buildResults(results, params, (nodes.length > params.maxResults));
}

/**
 * Return Site Search results with the given search terms.
 * 
 * @param params  Object containing search parameters - see API description above
 */
function getSiteResults(params)
{
   // Get the list of sites - ensure we use the faster fts based search code path
   var t = params.term;
   if (t[0] !== '*') t = "*" + t;
   var sites = siteService.getSites(t, null, params.maxResults);
   return buildResults(sites, params);
}

/**
 * Return People Search results with the given search terms.
 * 
 * @param params  Object containing search parameters - see API description above
 */
function getPeopleResults(params)
{
   // Get the list of people
   var persons = people.getPeople(params.term, params.maxResults);
   return buildResults(persons, params);
}

function buildResults(data, params, more)
{
   return {
      totalRecords: data.length,
      startIndex: params.startIndex,
      hasMoreRecords: (more === true),
      items: data
   };
}
