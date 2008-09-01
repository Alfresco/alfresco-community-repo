<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

const ASPECT_RELEASED = "blg:released";
const PROP_RELEASED = "blg:released";
const ASPECT_UPDATED = "cm:contentupdated";
const PROP_UPDATED = "cm:contentupdatedate";

function setOrUpdateReleasedAndUpdatedDates(node)
{
   // check whether we already got the date tracking aspect,
   // in this case we got an update
   if (node.hasAspect(ASPECT_RELEASED))
   {
      if (node.hasAspect(ASPECT_UPDATED))
      {
         // just update the modified date
         node.properties[PROP_UPDATED] = new Date();
         node.save();
      }
      else
      {
         // add the updated aspect
         var props = new Array();
         props[PROP_UPDATED] = new Date();
         node.addAspect(ASPECT_UPDATED, props);
      }
   }
   else
   {
      // attach the released aspect
      var props = new Array();
      var now = new Date();
      props[PROP_RELEASED] = now;
      node.addAspect(ASPECT_RELEASED, props);
      
      // re-enable permission inheritance which got disable for the draft
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
   data.author = people.getPerson(node.properties["cm:creator"]);
   data.commentCount = getCommentsCount(node);
   
   // draft
   data.isDraft = ! node.hasAspect(ASPECT_RELEASED);
   
   // set the isUpdated flag
   data.isUpdated = node.hasAspect(ASPECT_UPDATED);

   // fetch all available dates
   data.createdDate = node.properties["cm:created"];
   data.modifiedDate = node.properties["cm:modified"];
   
   if (node.hasAspect(ASPECT_RELEASED))
   {
      data.releasedDate = node.properties[PROP_RELEASED];
   }
   if (node.hasAspect(ASPECT_UPDATED))
   {
      data.updatedDate = node.properties[PROP_UPDATED];
   }
   
   // does the external post require an update?
   if (node.hasAspect(ASPECT_RELEASED) && (node.properties["blg:lastUpdate"] != undefined))
   {
      // we either use the release or updated date
      var modifiedDate = data.releaseDate;
      if (data.updatedDate != undefined)
      {
         modifiedDate = data.updatedDate;
      }
       
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

/**
 * Checks whether a blog configuration is available
 * This should at some point also check whether the configuration is enabled.
 * 
 * @param node the node that should be checked. Will check all parents if
 *        the node itself doesn't contain a configuration.
 * @return {boolean} whether a configuration could be found.
 */
function hasExternalBlogConfiguration(node)
{
   if (node == null)
   {
      return false;
   }
   else if (node.hasAspect("blg:blogDetails"))
   {
      return true;
   }
   else
   {
      return hasExternalBlogConfiguration(node.parent)
   }
}