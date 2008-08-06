<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/wiki/lib/wiki.lib.js">

var siteId = url.templateArgs.siteId;
var filter = args.filter;

model.pageList = getWikiPages(siteId, filter);

function getWikiPages(siteId)
{
   if (siteId === null || siteId.length === 0)
   {
	   status.setCode(status.STATUS_BAD_REQUEST, "Site not found: '" + siteId + "'");
   	return;
   }
   
   var site = siteService.getSite(siteId);
   if (site === null)
   {
	   status.setCode(status.STATUS_BAD_REQUEST, "Site not found: '" + siteId + "'");
	   return;
   }
   
   var wiki = getWikiContainer(site);
   if (wiki === null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Wiki container not found");
	   return;
   }
   
   var query = "+PATH:\"" + wiki.qnamePath + "//*\" ";
   
   if (filter && filter != "all")
   {
      query += getFilterQuery(filter);
   }
   
   var wikiPages = search.luceneSearch(query);
   
   var pages = [];   
   var page, createdBy, modifiedBy;
   
   for each (page in wikiPages)
   {
      createdBy = people.getPerson(page.properties["cm:creator"]);
      modifiedBy = people.getPerson(page.properties["cm:modifier"]);
      pages.push(
      {
         "page": page,
         "tags": page.tags,
         "modified": page.properties.modified,
         "createdBy": createdBy,
         "modifiedBy": modifiedBy
      });
   }
   
   return ( 
   {
      "pages": pages
   });
}

function getFilterQuery(filter)
{
   var filterQuery = "";
   
   switch (String(filter))
   {
      case "all":
         // Nothing to do
         break;
      case "recentlyModified":
         var usingModified = true;
         // fall through...
      case "recentlyAdded":
         // Which query: created, or modified?
         var dateField = "modified";
         if (typeof usingModified === "undefined")
         {
            dateField = "created";
         }
         
         // Default to 7 days - can be overridden using "days" argument
         var dayCount = 7;
         var argDays = args["days"];
         if ((argDays != null) && !isNaN(argDays))
         {
            dayCount = argDays;
         }
         var date = new Date();
         var toQuery = date.getFullYear() + "\\-" + (date.getMonth() + 1) + "\\-" + date.getDate();
         date.setDate(date.getDate() - dayCount);
         var fromQuery = date.getFullYear() + "\\-" + (date.getMonth() + 1) + "\\-" + date.getDate();

         filterQuery += "+@cm\\:" + dateField + ":[" + fromQuery + "T00\\:00\\:00 TO " + toQuery + "T23\\:59\\:59] ";
         filterQuery += "-ASPECT:\"{http://www.alfresco.org/model/content/1.0}workingcopy\"";
         break;
         
      case "myPages":
         filterQuery += "+@cm\\:creator:" + person.properties.userName;
         break;   
   }
   
   return filterQuery;
}