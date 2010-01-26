<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/evaluator.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

const THUMBNAIL_NAME = "doclib",
   PREF_DOCUMENT_FAVOURITES = "org.alfresco.share.documents.favourites";
   PREF_FOLDER_FAVOURITES = "org.alfresco.share.folders.favourites";

var PeopleCache = {},
   SiteCache = {};

/**
 * Gets / caches a person object
 * @method getPerson
 * @param username {string} User name
 */
function getPerson(username)
{
   if (typeof PeopleCache[username] == "undefined")
   {
      var person = people.getPerson(username);
      if (person == null && username == "System")
      {
         person =
         {
            properties:
            {
               userName: "System",
               firstName: "System",
               lastName: "User"
            }
         }
      }
      PeopleCache[username] =
      {
         userName: person.properties.userName,
         firstName: person.properties.firstName,
         lastName: person.properties.lastName,
         displayName: (person.properties.firstName + " " + person.properties.lastName).replace(/^\s+|\s+$/g, "")
      };
   }
   return PeopleCache[username];
}

/**
 * Gets / caches a site object
 * @method getSite
 * @param siteId {string} Site ID
 */
function getSite(siteId)
{
   if (typeof SiteCache[siteId] == "undefined")
   {
      SiteCache[siteId] = siteService.getSite(siteId);
   }
   return SiteCache[siteId];
}

/**
 * Main entry point: Create collection of documents and folders in the given space
 * @method main
 */
function main()
{
   var filter = args.filter,
      items = [],
      assets;
   
   // Is our thumbnail type registered?
   var haveThumbnails = thumbnailService.isThumbnailNameRegistered(THUMBNAIL_NAME);

   // Use helper function to get the arguments
   var parsedArgs = ParseArgs.getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }

   // "node" Type implies single nodeRef requested
   if (!filter && parsedArgs.type === "node")
   {
      filter = "node";
   }

   // Get the user's favourite docs from our slightly eccentric Preferences Service
   var prefs = preferenceService.getPreferences(person.properties.userName, PREF_DOCUMENT_FAVOURITES),
      favourites = {},
      strFavs, f, ff;
   try
   {
      /**
       * Fasten seatbelts...
       * An "eval" could be used here, but the Rhino debugger will complain if throws an exception, which gets old very quickly.
       * e.g. var strFavs = eval('try{(prefs.' + PREF_DOCUMENT_FAVOURITES + ')}catch(e){}');
       */
      if (prefs && prefs.org && prefs.org.alfresco && prefs.org.alfresco.share && prefs.org.alfresco.share.documents)
      {
         strFavs = prefs.org.alfresco.share.documents.favourites;
         if (typeof strFavs == "string")
         {
            arrFavs = strFavs.split(",");
            for (f = 0, ff = arrFavs.length; f < ff; f++)
            {
               favourites[arrFavs[f]] = true;
            }
         }
      }
      // Same thing but for folders
      prefs = preferenceService.getPreferences(person.properties.userName, PREF_FOLDER_FAVOURITES);
      if (prefs && prefs.org && prefs.org.alfresco && prefs.org.alfresco.share && prefs.org.alfresco.share.folders)
      {
         strFavs = prefs.org.alfresco.share.folders.favourites;
         if (typeof strFavs == "string")
         {
            arrFavs = strFavs.split(",");
            for (f = 0, ff = arrFavs.length; f < ff; f++)
            {
               favourites[arrFavs[f]] = true;
            }
         }
      }
   }
   catch (e)
   {
   }

   // Try to find a filter query based on the passed-in arguments
   var allAssets,
      filterParams = Filters.getFilterParams(filter, parsedArgs,
      {
         favourites: favourites
      }),
      query = filterParams.query;

   // Query the assets - passing in sort and result limit parameters
   if (query !== "")
   {
      allAssets = search.query(
      {
         query: query,
         language: filterParams.language,
         page:
         {
            maxItems: (filterParams.limitResults ? parseInt(filterParams.limitResults, 10) : 0)
         },
         sort: filterParams.sort,
         templates: filterParams.templates,
         namespace: (filterParams.namespace ? filterParams.namespace : null)
      });
   }

   // Ensure folders and folderlinks appear at the top of the list
   var folderAssets = [],
      documentAssets = [];
   
   for each (asset in allAssets)
   {
      try
      {
         if (asset.isContainer || asset.typeShort == "app:folderlink")
         {
            folderAssets.push(asset);
         }
         else
         {
            documentAssets.push(asset);
         }
      }
      catch (e)
      {
         // Possibly an old indexed node - ignore it
      }
   }
   
   var folderAssetsCount = folderAssets.length,
      documentAssetsCount = documentAssets.length;
   
   if (parsedArgs.type === "documents")
   {
      assets = documentAssets;
   }
   else
   {
      assets = folderAssets.concat(documentAssets);
   }
   
   // Make a note of totalRecords before trimming the assets array
   var totalRecords = assets.length;

   // Pagination
   var pageSize = args.size || assets.length,
      pagePos = args.pos || "1",
      startIndex = (pagePos - 1) * pageSize;
   
   assets = assets.slice(startIndex, pagePos * pageSize);

   var thumbnail, assetEvaluator, defaultLocation, location, qnamePaths, displayPaths, site, item,
      libraryRoot = parsedArgs.libraryRoot;

   // Location if we're in a site
   defaultLocation =
   {
      site: parsedArgs.location.site,
      siteTitle: parsedArgs.location.siteTitle,
      container: parsedArgs.location.container,
      path: parsedArgs.location.path,
      file: null
   };

   // Evaluate parent container
   var parent = Evaluator.run(parsedArgs.parentNode);
   
   // User permissions and role
   var user =
   {
      permissions: parent.actionPermissions
   };
   if (defaultLocation.site !== null)
   {
      user.role = parsedArgs.location.siteNode.getMembersRole(person.properties.userName);
   }

   // Loop through and evaluate each asset in this result set
   for each (asset in assets)
   {
      // Get evaluated properties.
      item = Evaluator.run(asset);
      // Note: Only access item.asset after this point, as a link may have been resolved.

      item.isFavourite = (favourites[item.asset.nodeRef] === true);

      // Does this collection of assets have potentially differering paths?
      if (filterParams.variablePath || item.isLink)
      {
         locationAsset = (item.isLink && item.type == "document") ? item.linkAsset : item.asset;

         qnamePaths = locationAsset.qnamePath.split("/");
         displayPaths = locationAsset.displayPath.split("/");

         if (libraryRoot !== null)
         {
            // Generate the path from the supplied library root
            location =
            {
               site: null,
               siteTitle: null,
               container: null,
               path: "/" + displayPaths.slice(libraryRoot.displayPath.split("/").length + 1, displayPaths.length).join("/"),
               file: locationAsset.name
            };
         }
         else if ((qnamePaths.length > 5) && (qnamePaths[2] == "st:sites"))
         {
            // This asset belongs to a site
            location =
            {
               site: displayPaths[3],
               container: displayPaths[4],
               path: "/" + displayPaths.slice(5, displayPaths.length).join("/"),
               file: locationAsset.name
            };
            
            site = getSite(location.site);
            if (site != null)
            {
               location.siteTitle = site.title;
            }
         }
         else
         {
            location =
            {
               site: null,
               siteTitle: null,
               container: null,
               path: "/" + displayPaths.slice(2, displayPaths.length).join("/"),
               file: locationAsset.name
            };
         }
      }
      else
      {
         location =
         {
            site: defaultLocation.site,
            siteTitle: defaultLocation.siteTitle,
            container: defaultLocation.container,
            path: defaultLocation.path,
            file: asset.name
         };
      }
      
      // Resolved location
      item.location = location;
      
      // Make sure we have a thumbnail.
      if (haveThumbnails)
      {
         thumbnail = item.asset.getThumbnail(THUMBNAIL_NAME);
         if (thumbnail === null)
         {
            // No thumbnail, so queue creation
            item.asset.createThumbnail(THUMBNAIL_NAME, true);
         }
      }
      
      items.push(item);
   }

   var parentMeta = filterParams.variablePath ? null :
   {
      nodeRef: String(parsedArgs.parentNode.nodeRef),
      type: parent.typeShort
   };

   return (
   {
      luceneQuery: query,
      onlineEditing: utils.moduleInstalled("org.alfresco.module.vti"),
      itemCount:
      {
         folders: folderAssetsCount,
         documents: documentAssetsCount
      },
      paging:
      {
         startIndex: startIndex,
         totalRecords: totalRecords
      },
      user: user,
      items: items,
      parent: parentMeta
   });
}

/**
 * Document List Component: doclist
 */
model.doclist = main();