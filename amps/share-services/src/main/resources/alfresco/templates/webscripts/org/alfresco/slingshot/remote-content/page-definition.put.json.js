function main() {
   
   if (url.templateArgs.pagename != null)
   {
      var alfQuery = 'TYPE:"{http://www.alfresco.org/model/surf/1.0}amdpage"' +
                     ' AND PATH:"/app:company_home/app:dictionary//*"' +
                     ' AND @cm\:name:"' + url.templateArgs.pagename + '"';;
      var queryDef = {
         query: alfQuery,
         language: "fts-alfresco",
         page: {maxItems: 50},
         templates: []
      };
      
      var pages = [],
          targetPage,
          nodes = search.query(queryDef);
      if (nodes.length > 0)
      {
         targetPage = nodes[0];
         var def = json.get("json");
         if (def == null || def == "")
         {
            status.code = 500;
            model.errorMessage = "page.update.error.noDefProvided"
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
            model.errorMessage = "page.update.error.invalidJson";
            model.errorMessageArg = def;
            return false;
         }
         
         targetPage.addAspect("cm:versionable");
         var workingCopy = targetPage.checkout();
         var pageDefinitionJSON = def;
         workingCopy.content = pageDefinitionJSON;
         targetPage = workingCopy.checkin();
         return true;
      }
      
   }
   else
   {
      // Not provided with anything to update
      model.errorMessage = "page.update.error.doesNotExist";
      status.code = 500;
      return false;
   }
   
   
   // Shouldn't get to here - there should be a return at every code path...
   model.errorMessage = "page.update.error.unexpected";
   status.code = 500;
   return false;
}

model.success = main();
