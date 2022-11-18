<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/repository/forms/pickerresults.lib.js">

function main()
{
   var argsFilterType = args['filterType'],
      argsSelectableType = args['selectableType'],
      argsSearchTerm = args['searchTerm'],
      argsMaxResults = args['size'],
      argsXPath = args['xpath'],
      argsRootNode = args['rootNode'],
      argsSelectableMimeType = args['selectableMimeType'],
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
      logger.log("argsSelectableMimeType = " + argsSelectableMimeType);
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

      // default to max of 100 results
      var maxResults = 100;
      if (argsMaxResults != null)
      {
         // force the argsMaxResults var to be treated as a number
         maxResults = parseInt(argsMaxResults, 10) || maxResults;
      }
      
      //consider argsSearchTerm null if empty string
      argsSearchTerm = (argsSearchTerm != null && argsSearchTerm.length == 0) ? null : argsSearchTerm ;

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
         parent = resolveNode(nodeRef);
         if (parent === null)
         {
            status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
            return null;
         }
         
         if (argsRootNode != null)
         {
            rootNode = resolveNode(argsRootNode) || companyhome;
         }

         var ignoreTypes = null;
         if (argsFilterType != null)
         {
            if (logger.isLoggingEnabled())
               logger.log("ignoring types = " + argsFilterType);
            
            ignoreTypes = argsFilterType.split(',');
         }

         // retrieve the children of this node
         var childNodes = parent.childFileFolders(true, true, ignoreTypes, -1, maxResults, 0, "cm:name", true, null).getPage();

         // Ensure folders and folderlinks appear at the top of the list
         var containerResults = new Array(),
            contentResults = new Array();

         for each (var result in childNodes)
         {
            if (result.isContainer || result.type == "{http://www.alfresco.org/model/application/1.0}folderlink")
            {
               // wrap result and determine if it is selectable in the UI
               resultObj = 
               { 
                  item: result
               };
               if(argsSelectableMimeType)
               {
                  resultObj.selectable = isItemSelectableByMimeType(result, argsSelectableType, argsSelectableMimeType);
               }
               else
               {
                  resultObj.selectable = isItemSelectable(result, argsSelectableType);
               }
               containerResults.push(resultObj);
            }
            else
            {
               var qnamePaths = result.qnamePath.split("/"), container;
               if ((qnamePaths.length > 4) && (qnamePaths[2] == "st:sites"))
               {
                  container = qnamePaths[4].substr(3);
               }
               // wrap result and determine if it is selectable in the UI
               resultObj = 
               { 
                  item: result
               };
               if(argsSelectableMimeType)
               {
                  resultObj.selectable = isItemSelectableByMimeType(result, argsSelectableType, argsSelectableMimeType);
               }
               else
               {
                  resultObj.selectable = isItemSelectable(result, argsSelectableType);
               }
               resultObj.container = container;
               contentResults.push(resultObj);
            }
         }
         results = containerResults.concat(contentResults);
      }
      else if (url.templateArgs.type == "category")
      {
         var catAspect = (args["aspect"] != null) ? args["aspect"] : "cm:generalclassifiable";
         
         if (nodeRef == "workspace://SpacesStore/tag:tag-root")
         {
            categoryResults = classification.getRootCategories(catAspect, argsSearchTerm, maxResults, 0);
            parent = resolveNode(nodeRef);
         }
         else
         {
            var rootCategories = classification.getRootCategories(catAspect);
            if (rootCategories != null && rootCategories.length > 0)
            {
               if (argsRootNode)
               {
                  rootNode = resolveNode(argsRootNode);
                  if (rootNode == null)
                  {
                     rootNode = rootCategories[0].parent;
                  }
               }
               else
               {
                  rootNode = rootCategories[0].parent;
               }
               
               if (nodeRef == "alfresco://category/root")
               {
                  parent = rootNode;
                  categoryResults = classification.getRootCategories(catAspect, argsSearchTerm, maxResults, 0);
               }
               else
               {
                  parent = resolveNode(nodeRef);
                  categoryResults = parent.children;
                  if (argsSearchTerm != null)
                  {
                     var filteredResults = [];
                     for each (result in categoryResults)
                     {
                        if (result.properties.name.indexOf(argsSearchTerm) == 0)
                        {
                           filteredResults.push(result);
                        }
                     }
                     categoryResults = filteredResults.slice(0);
                  }
                  categoryResults.sort(sortByName);
                  categoryResults = categoryResults.slice(0, maxResults);
               }
            }
         }
         
         // make each result an object and indicate it is selectable in the UI
         if (categoryResults)
         {
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

/**
 * Identifies if the node can be selected. The node can be selected if it is of specified type and has specified mime type.
 * 
 * @param node
 * @param selectableType
 * @param argsSelectableMimeType
 * @returns <code>true<code> if the node can be selected, or false otherwise.
 */
function isItemSelectableByMimeType(node, selectableType, argsSelectableMimeType)
{
   var selectable = isItemSelectable(node, selectableType);
   if(selectable && argsSelectableMimeType != null && argsSelectableMimeType != "")
   {
      selectable = node.properties.content.mimetype.equals(argsSelectableMimeType);
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
   var personRefs = people.getPeople(searchTerm+ " [hint:useCQ]", maxResults, "lastName", true);
   
   // create person object for each result
   for each(var personRef in personRefs)
   {
      if (logger.isLoggingEnabled())
         logger.log("found personRefs = " + personRefs);
      
      //filter out  the disabled users
      var daname = (search.findNode(personRef)).properties.userName;
 
      if(people.isAccountEnabled(daname)){
          results.push(
          {
              item: createPersonResult(search.findNode(personRef)),
              selectable: true
          });
      }
      else{
          results.push(
	      {
	          item: createPersonResult(search.findNode(personRef)),
	          selectable: false
	      });   
          if (logger.isLoggingEnabled())
              logger.log("User not added to results not enabled" + daname);
      }
   }
}

function findGroups(searchTerm, maxResults, results)
{
   if (logger.isLoggingEnabled())
      logger.log("Finding groups matching pattern: " + searchTerm);
   
   var paging = utils.createPaging(maxResults, 0);
   var searchResults = groups.getGroupsInZone(searchTerm, "APP.DEFAULT", paging, "displayName");
   for each(var group in searchResults)
   {
      if (logger.isLoggingEnabled())
         logger.log("found group = " + group.fullName);
         
      // add to results
      results.push(
      {
         item: createGroupResult(group.groupNode),
         selectable: true 
      });
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

/**
 * Resolve "virtual" nodeRefs, nodeRefs and xpath expressions into nodes
 *
 * @method resolveNode
 * @param reference {string} "virtual" nodeRef, nodeRef or xpath expressions
 * @return {ScriptNode|null} Node corresponding to supplied expression. Returns null if node cannot be resolved.
 */
function resolveNode(reference)
{
   return utils.resolveNodeReference(reference);
}

main();