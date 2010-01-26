function main()
{
   var argsFilterType = args['filterType'],
      argsSelectableType = args['selectableType']
      parent = null,
      rootNode = companyhome,
      results = [],
      categoryResults = null,
      resultObj = null;
   
   if (logger.isLoggingEnabled())
   {
      logger.log("children type = " + url.templateArgs.type);
      logger.log("argsSelectableType = " + argsSelectableType);
      logger.log("argsFilterType = " + argsFilterType);
   }
         
   try
   {
      if (url.templateArgs.type == "node")
      {
         // nodeRef input
         var nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id;
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
            //map short name to long name
            var types = {
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
         var catAspect = (args["aspect"] != null) ? args["aspect"] : "cm:generalclassifiable",
            nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id;

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
   }
   
   return selectable;
}

/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.properties.name.toLowerCase() > a.properties.name.toLowerCase() ? -1 : 1);
}

main();