<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/repository/forms/pickerresults.lib.js">

function main()
{
   var argsFilterType = args['filterType'],
      argsSelectableType = args['selectableType'],
      argsSearchTerm = args['searchTerm'],
      argsMaxResults = args['size'],
      argsXPath = args['xpath'],
      pathElements = url.service.split("/"),
      parent = null,
      rootNode = companyhome,
      results = [],
      categoryResults = null,
      resultObj = null,
      lastPathElement = null;
   
   if (logger.isLoggingEnabled())
   {
      logger.log("children type = " + url.templateArgs.type);
      logger.log("argsSelectableType = " + argsSelectableType);
      logger.log("argsFilterType = " + argsFilterType);
      logger.log("argsSearchTerm = " + argsSearchTerm);
      logger.log("argsMaxResults = " + argsMaxResults);
      logger.log("argsXPath = " + argsXPath);
   }
         
   try
   {
      // construct the NodeRef from the URL
      var nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id;
      
      // determine if we need to resolve the parent NodeRef
      
      if (argsXPath != null)
      {
         // resolve the provided XPath to a NodeRef
         var nodes = search.xpathSearch(argsXPath);
         if (nodes.length > 0)
         {
            nodeRef = String(nodes[0].nodeRef);
         }
      }
      
      // if the last path element is 'doclib' or 'siblings' find parent node
      if (pathElements.length > 0)
      {
         lastPathElement = pathElements[pathElements.length-1];
         
         if (logger.isLoggingEnabled())
            logger.log("lastPathElement = " + lastPathElement);
         
         if (lastPathElement == "siblings")
         {
            // the provided nodeRef is the node we want the siblings of so get it's parent
            var node = search.findNode(nodeRef);
            if (node !== null)
            {
               nodeRef = node.parent.nodeRef;
            }
            else
            {
               // if the provided node was not found default to companyhome
               nodeRef = "alfresco://company/home";
            }
         }
         else if (lastPathElement == "doclib")
         {
            // we want to find the document library for the nodeRef provided
            nodeRef = findDoclib(nodeRef);
         }
      }

      if (url.templateArgs.type == "node")
      {
         // nodeRef input
         if (nodeRef == "alfresco://company/home")
         {
            parent = companyhome;
         }
         else if (nodeRef == "alfresco://user/home")
         {
            parent = userhome;
         }
         else if (nodeRef == "alfresco://sites/home")
         {
            parent = companyhome.childrenByXPath("st:sites")[0];
         }
         else
         {
            parent = search.findNode(nodeRef);
            if (parent === null)
            {
               status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
               return null;
            }
         }

         var query = "+PARENT:\"" + parent.nodeRef + "\"";
         if (argsFilterType != null)
         {
            // map short name to long name
            var types =
            {
              'rma:dispositionSchedule': '{http://www.alfresco.org/model/recordsmanagement/1.0}dispositionSchedule',
              'rma:dispositionActionDefinition': '{http://www.alfresco.org/model/recordsmanagement/1.0}dispositionActionDefinition',
              'rma:dispositionAction': '{http://www.alfresco.org/model/recordsmanagement/1.0}dispositionAction',
              'rma:hold':'{http://www.alfresco.org/model/recordsmanagement/1.0}hold',
              'rma:transfer':'{http://www.alfresco.org/model/recordsmanagement/1.0}transfer',
              'cm:thumbnail': '{http://www.alfresco.org/model/content/1.0}thumbnail'
            };

            var filterTypes = argsFilterType.split(',');
            for (var i=0,len=filterTypes.length; i<len; i++)
            {
               var identifier = filterTypes[i];
               if (types[identifier])
               {
                  query += " -TYPE:\"" + types[identifier] + "\"";
               }
            }
         }

         // make sure we don't return system folders
         query += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}systemfolder\"";

         if (logger.isLoggingEnabled())
            logger.log("query = " + query);

         var searchResults = search.luceneSearch(query, "@{http://www.alfresco.org/model/content/1.0}name", true);

         // Ensure folders and folderlinks appear at the top of the list
         var containerResults = new Array(),
            contentResults = new Array();

         for each (var result in searchResults)
         {
            if (result.isContainer || result.type == "{http://www.alfresco.org/model/application/1.0}folderlink")
            {
               // wrap result and determine if it is selectable in the UI
               resultObj = 
               { 
                  item: result
               };
               resultObj.selectable = isItemSelectable(result, argsSelectableType);
               
               containerResults.push(resultObj);
            }
            else
            {
               // wrap result and determine if it is selectable in the UI
               resultObj = 
               { 
                  item: result
               };
               resultObj.selectable = isItemSelectable(result, argsSelectableType);
               
               contentResults.push(resultObj);
            }
         }
         results = containerResults.concat(contentResults);
      }
      else if (url.templateArgs.type == "category")
      {
         var catAspect = (args["aspect"] != null) ? args["aspect"] : "cm:generalclassifiable";

         // TODO: Better way of finding this
         var rootCategories = classification.getRootCategories(catAspect);
         if (rootCategories != null && rootCategories.length > 0)
         {
            rootNode = rootCategories[0].parent;
            if (nodeRef == "alfresco://category/root")
            {
               parent = rootNode;
               categoryResults = classification.getRootCategories(catAspect);
            }
            else
            {
               parent = search.findNode(nodeRef);
               categoryResults = parent.children;
            }
            
            categoryResults.sort(sortByName);
            
            // make each result an object and indicate it is selectable in the UI
            for each (var result in categoryResults)
            {
               results.push(
               { 
                  item: result, 
                  selectable: true 
               });
            }
         }
      }
      else if (url.templateArgs.type == "authority")
      {
         // default to max of 100 results
         var maxResults = 100;
         if (argsMaxResults != null)
         {
            // force the argsMaxResults var to be treated as a number
            maxResults = argsMaxResults + 0;
         }
         
         if (argsSelectableType == "cm:person")
         {
            findUsers(argsSearchTerm, maxResults, results);
         }
         else if (argsSelectableType == "cm:authorityContainer")
         {
            findGroups(argsSearchTerm, maxResults, results);
         }
         else
         {
            // combine groups and users
            findGroups(argsSearchTerm, maxResults, results);
            findUsers(argsSearchTerm, maxResults, results);
         }
      }
      
      if (logger.isLoggingEnabled())
         logger.log("Found " + results.length + " results");
   }
   catch (e)
   {
      var msg = e.message;
      
      if (logger.isLoggingEnabled())
         logger.log(msg);
      
      status.setCode(500, msg);
      
      return;
   }

   model.parent = parent;
   model.rootNode = rootNode;
   model.results = results;
}

function isItemSelectable(node, selectableType)
{
   var selectable = true;
   
   if (selectableType !== null && selectableType !== "")
   {
      selectable = node.isSubType(selectableType);
      
      if (!selectable)
      {
         // the selectableType could also be an aspect,
         // if the node has that aspect it is selectable
         selectable = node.hasAspect(selectableType);
      }
   }
   
   return selectable;
}

/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.properties.name.toLowerCase() > a.properties.name.toLowerCase() ? -1 : 1);
}

function findUsers(searchTerm, maxResults, results)
{
   // construct query string
   var query = '+TYPE:"cm:person"';
   
   if (searchTerm != null && searchTerm.length > 0)
   {
      searchTerm = searchTerm.replace(/\"/g, "");
         
      query += ' AND (@cm\\:firstName:"*' + searchTerm + '*" @cm\\:lastName:"*' + searchTerm + 
         '*" @cm\\:userName:' + searchTerm + '* )';
   }
   
   if (logger.isLoggingEnabled())
      logger.log("user query = " + query);
   
   // do the query
   var searchResults = search.query(
   {
      query: query,
      page:
      {
         maxItems: maxResults
      },
      sort:
      [
      {
         column: "cm:lastName",
         ascending: true
      },
      {
         column: "cm:firstName",
         ascending: true
      }
      ]
   });
   
   // create person objet for each result
   for each(var node in searchResults)
   {
      // add to results
      results.push(
      {
         item: createPersonResult(node),
         selectable: true 
      });
   }
}

function findGroups(searchTerm, maxResults, results)
{
   var searchTermPattern = "*";
   
   if (searchTerm != null && searchTerm.length > 0)
   {
      searchTermPattern = searchTermPattern + searchTerm;
   }
   
   if (logger.isLoggingEnabled())
      logger.log("Finding groups matching pattern: " + searchTermPattern);
   
   var searchResults = groups.searchGroupsInZone(searchTermPattern, "APP.DEFAULT");
   for each(var node in searchResults)
   {
      // find the actual node that represents the group
      var query = '+TYPE:"cm:authorityContainer" AND @cm\\:authorityName:' + node.fullName;
      
      if (logger.isLoggingEnabled())
         logger.log("group query = " + query);
      
      var searchResults = search.query(
      {
         query: query
      });

      if (searchResults.length > 0)
      {
         // add to results
         results.push(
         {
            item: createGroupResult(searchResults[0]),
            selectable: true 
         });
      }
   }
   
   // sort the groups by name alphabetically
   if (results.length > 0)
   {
      results.sort(function(a, b)
      {
         return (a.item.properties.name < b.item.properties.name) ? -1 : (a.item.properties.name > b.item.properties.name) ? 1 : 0;
      });
   }
}

/**
 * Returns the nodeRef of the document library of the site the
 * given nodeRef is located within. If the nodeRef provided does
 * not live within a site "alfresco://company/home" is returned.
 * 
 * @param nodeRef The node to find the document library for
 * @return The nodeRef of the doclib or "alfresco://company/home" if the node
 *         is not located within a site
 */
function findDoclib(nodeRef)
{
   var resultNodeRef = "alfresco://company/home";
   
   // find the given node
   var node = search.findNode(nodeRef);
   if (node !== null)
   {
      // get the name of the site
      var siteName = node.siteShortName;
      
      if (logger.isLoggingEnabled())
         logger.log("siteName = " + siteName);
      
      // if the node is in a site find the document library node using an XPath search
      if (siteName !== null)
      {
         var nodes = search.xpathSearch("/app:company_home/st:sites/cm:" + search.ISO9075Encode(siteName) + "/cm:documentLibrary");
         if (nodes.length > 0)
         {
            // there should only be 1 result, get the first one
            resultNodeRef = String(nodes[0].nodeRef);
         }
      }
   }
   
   return resultNodeRef;
}

main();