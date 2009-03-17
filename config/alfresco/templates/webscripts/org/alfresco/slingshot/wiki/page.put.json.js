<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/wiki/lib/wiki.lib.js">

/**
 * Update specified wiki page.
 *
 * @method PUT
 * @param uri {string} /slingshot/wiki/page/{siteid}/{pageTitle}
 */
function getTemplateParams()
{
   // Grab the URI parameters
   var siteId = "" + url.templateArgs.siteId;
   var pageTitle = "" + url.templateArgs.pageTitle;

   if (siteId === null || siteId.length === 0)
   {
      return null;
   }

   if (pageTitle === null || pageTitle.length === 0)
   {
      return null;
   }

   return (
   {
      "siteId": siteId,
      "pageTitle": pageTitle
   });
}

function update()
{
   var params = getTemplateArgs(["siteId", "pageTitle"]);
   if (params === null)
   {
      return jsonError("No parameters supplied");
   }

   // Get the site
   var site = siteService.getSite(params.siteId);
   if (site === null)
   {
      return jsonError("Could not find site: " + siteId);
   }

   var wiki = getWikiContainer(site);
   if (wiki === null)
   {
      return jsonError("Could not locate wiki container");
   }
   
   var page = wiki.childByNamePath(params.pageTitle), activityType;
   // Create the page if it doesn't exist
   if (page === null)
   {
      page = createWikiPage(params.pageTitle, wiki,
      {
         content: json.get("pagecontent"),
         versionable: true
      });
   
      activityType = "org.alfresco.wiki.page-created";
   }
   else 
   {
      // Check the version of the page being submitted against the version now in the repo, or a forced save
      if (pageVersionMatchesSubmitted(page) || json.has("forceSave"))
      {
         // Create a new revision of the page
         var workingCopy = page.checkout();
         workingCopy.content = json.get("pagecontent");
         workingCopy.checkin();
         page.save();

         activityType = "org.alfresco.wiki.page-edited";
      }
      else
      {
         status.setCode(status.STATUS_CONFLICT, "Repository version is newer.");
         return;
      }
   }
   
   var data =
   {
      title: params.pageTitle.replace(/_/g, " "),
      page: json.get("page") + "?title=" + params.pageTitle
   }
   // Log activity   
   activities.postActivity(activityType, params.siteId, "wiki", jsonUtils.toJSONString(data));
   
   if (!json.isNull("tags"))
   {
      var tags = Array(json.get("tags"));
      if (tags) 
      {
         // This is so unnecessary!
         // A much cleaner approach would be to just pass in the tags as a space separated
         // string and call the (native) method split
         var tags = [];
         var tmp = json.get("tags");
         for (var x = 0, xx = tmp.length(); x < xx; x++)
         {
            tags.push(tmp.get(x));
         }
         page.tags = tags;
      }
      else
      {
         page.tags = []; // reset
      }
      page.save();
   }
   
   // NOTE: for now we return the raw page content and do the transformation
   // of any wiki markup on the client. This is because the edit view needs to display
   // the raw content (for editing) whereas the page view needs to display the rendered content.
   return (
   {
      page: page,
      tags: page.tags
   });
}

/**
 * Checks whether the current repository version is newer than a submitted version number.
 * Returns:
 *    false if currentVersion is older than repoVersion
 *    true  otherwise
 */
function pageVersionMatchesSubmitted(page)
{
   var currentVersion = "0",
      repoVersion = "0";
   
   if (json.has("currentVersion"))
   {
      currentVersion = json.get("currentVersion");
   }
   
   if (page.hasAspect("cm:versionable"))
   {
      repoVersion = getLatestVersion(page.versionHistory);
   }
   else
   {
      page.addAspect("cm:versionable");
      page.save();
      return 0;
   }
   
   return (sortByLabel(
   {
      label: repoVersion
   },
   {
      label: currentVersion
   }) != -1);
}

function sortByLabel(version1, version2)
{
   if ((version1.label.indexOf(".") == -1) || (version2.label.indexOf(".") == -1))
   {
      return -1;
   }
   
   var major1 = new Number(version1.label.substring(0, version1.label.indexOf(".")));
   var major2 = new Number(version2.label.substring(0, version2.label.indexOf(".")));
   if (major1 - 0 == major2 - 0)
   {
        var minor1 = new Number(version1.label.substring(version1.label.indexOf(".")+1));
        var minor2 = new Number(version2.label.substring(version2.label.indexOf(".")+1));
        return (minor1 < minor2) ? 1 : (minor1 > minor2) ? -1 : 0;
   }
   else
   {
       return (major1 < major2) ? 1 : -1;
   }
}

function getLatestVersion(versionHistory)
{
   versionHistory.sort(sortByLabel);
   return versionHistory[0].label;
}

model.result = update();