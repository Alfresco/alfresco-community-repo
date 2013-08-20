function main() {
   
   // This query should find the Share Resources folder 
   // NOTE: There might be a better way to get this node (this was written by a UI Developer!)
   var alfQuery = 'PATH:"/app:company_home/app:dictionary/cm:ShareResources/cm:Pages"';
      
   var queryDef = {
      query: alfQuery,
      language: "fts-alfresco",
      page: {maxItems: 50},
      templates: []
   };
   
   var shareResources,
       nodes = search.query(queryDef);
   if (nodes.length > 0)
   {
      shareResources = nodes[0];
      
      // Get the page name and JSON definition from the request parameters...
      var valid = true;
      var name = json.get("name");
      var def = json.get("json");
      if (name == null || name == "")
      {
         status.code = 500;
         model.errorMessage = "page.create.error.noNameProvided";
         return false;
      }

      // Check to see if the page name is already in use...
      alfQuery = 'TYPE:"{http://www.alfresco.org/model/surf/1.0}amdpage"' +
                 ' AND PATH:"/app:company_home/app:dictionary//*"' +
                 ' AND @cm\:name:"' + name + '"';
      queryDef.query = alfQuery;
      var existingPages = search.query(queryDef);
      if (existingPages.length == 1)
      {
         status.code = 500;
         model.errorMessage = "page.create.error.nameAlreadyUsed";
         model.errorMessageArg = name;
         return false;
      }

      if (def == null || def == "")
      {
         status.code = 500;
         model.errorMessage = "page.create.error.noDefProvided"
         model.errorMessageArg = def;
         return false;
      }
   
      try
      {
         pageDetails = jsonUtils.toObject(def);
      }
      catch(e)
      {
         status.code = 500;
         model.errorMessage = "page.create.error.invalidJson";
         model.errorMessageArg = def;
         return false;
      }
      
      // Get the page name and it's content...
      var pageDefinitionName = name;
      var pageDefinitionJSON = def; 
      var doc = shareResources.createNode(pageDefinitionName, "surf:amdpage");
      if (doc == null)
      {
         status.code = 500;
         model.errorMessage = "page.create.error.couldNotCreate";
         return false;
      }
      else
      {
         doc.content = pageDefinitionJSON;
         doc.mimetype = "application/json";
         model.nodeRef = doc.nodeRef.toString();
         return true;
      }
   }
   else
   {
      // The Data Dictionary location for pages hasn't been set up...
      status.code = 500;
      model.errorMessage = "page.create.error.noTargetLocation";
      return false;
   }
   
   // Shouldn't get to here - there should be a return at every code path...
   model.errorMessage = "page.create.error.unexpected";
   status.code = 500;
   return false;
}

model.success = main();
