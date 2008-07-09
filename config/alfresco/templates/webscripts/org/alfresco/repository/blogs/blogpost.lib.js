<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

function setOrUpdateReleasedAndUpdatedDates(node)
{
   // check whether we already got the date tracking aspect,
   // in this case we got an update
   if (node.hasAspect("blg:releaseDetails"))
   {
      // just update the modified date
      node.properties["blg:updated"] = new Date();
      node.save();
   }
   else
   {
      // attach the released/update date tracking aspect
      var props = new Array();
      props["blg:released"] = new Date();
      props["blg:updated"] = new Date();	  
      node.addAspect("blg:releaseDetails", props);
      
      // re-enable permission inheritance
      node.setInheritsPermissions(true);
   }
}

function setTags(node, tags)
{
    // convert tags, which is probably not of type array
    var t = new Array();
    for (var a=0; a < tags.length(); a++)
    {
        t.push(tags.get(a));
    }
    tags = t;
   
    logger.log("set tags: " + tags);
    
    // get current tags
    var oldTags = node.tags;
    if (oldTags == undefined)
    {
        oldTags = [];
    }

    // remove the tags that should be removed
    for (var x=0; x < oldTags.length; x++)
    {
        var toRemove = true;
        for (var y=0; y < tags.length; y++)
        {
            if (oldTags[x] == tags[y])
            {
                toRemove = false;
                break;
            }
        }
        if (toRemove)
        {
            logger.log("removing tag " + tags[x]);
            oldTags.removeTag(oldTags[x]);
        }
    }
    
    // add new 
    for (var x=0; x < tags.length; x++)
    {
        var toAdd = true;
        for (var y=0; y < oldTags.length; y++)
        {
            if (tags[x] == oldTags[y])
            {
                toAdd = false;
                break;
            }
        }
        if (toAdd)
        {
            logger.log("adding tag " + tags[x]);
            node.addTag(tags[x]);
        }
        else
        {
            logger.log("tag " + tags[x] + " already added.");
        }
    }
    logger.log("finished setting tags");
}

/**
 * Returns the data of a blog post.
 */
function getBlogPostData(node)
{
   var data = {};
   data.node = node;
   data.commentCount = getCommentsCount(node);
   
   // draft
   data.isDraft = (! node.hasAspect("blg:releaseDetails")) ||
                  (node.properties["blg:released"] == undefined);
   
   // use the released date if it exists
   var createdDate = node.properties["cm:created"];
   if (node.hasAspect("blg:releaseDetails") && node.properties["blg:released"] != undefined)
   {
       createdDate = node.properties["blg:released"];
   }
   data.createdDate = createdDate;
   var modifiedDate = node.properties["cm:modified"];
   if (node.hasAspect("blg:releaseDetails") && node.properties["blg:updated"] != undefined)
   {
       modifiedDate = node.properties["blg:updated"];
   }
   data.modifiedDate = modifiedDate;
   
   // set the isUpdated flag
   data.isUpdated = (modifiedDate - createdDate) > 5000;
   
   // does the external post require an update?
   if ((node.properties["blg:lastUpdate"] != undefined))
   {
      if ((modifiedDate - node.properties["blg:lastUpdate"]) > 5000)
      {
         data.outOfDate = true;
      }
      else
      {
         data.outOfDate = false;
      }
   }
   else
   {
      data.outOfDate = false;
   }
   
   // tags
   if (node.tags != undefined)
   {
       data.tags = node.tags;
   }
   else
   {
       data.tags = [];
   }
   
   return data;
}
