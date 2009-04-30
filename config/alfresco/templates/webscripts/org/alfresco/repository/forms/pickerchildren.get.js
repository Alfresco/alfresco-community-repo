function main()
{
   var argsFilterType = args['filterType'],
      parent = null,
      rootNode = companyhome,
      results = [];

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
            query += " +TYPE:\"" + argsFilterType + "\"";
         }
         
         var searchResults = search.luceneSearch(query, "@{http://www.alfresco.org/model/content/1.0}name", true);

         // Ensure folders and folderlinks appear at the top of the list
         var containerResults = new Array();
         var contentResults = new Array();
         for each(var result in searchResults)
         {
            if (result.isContainer || result.type == "{http://www.alfresco.org/model/application/1.0}folderlink")
            {
               containerResults.push(result);
            }
            else
            {
               contentResults.push(result);
            }
         }
         results = containerResults.concat(contentResults);
      }
      else if (url.templateArgs.type == "category")
      {
         var catAspect = (args["aspect"] != null) ? args["aspect"] : "cm:generalclassifiable";
         var nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id;
         // TODO: Better way of finding this
         rootNode = classification.getRootCategories(catAspect)[0].parent;
         if (nodeRef == "alfresco://category/root")
         {
            parent = rootNode;
            results = classification.getRootCategories(catAspect);
         }
         else
         {
            parent = search.findNode(nodeRef);
            results = parent.children;
         }
      }
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

main();