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
