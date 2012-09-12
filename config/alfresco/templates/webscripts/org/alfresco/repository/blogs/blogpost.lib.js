<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

const ASPECT_SYNDICATION = "cm:syndication";
const PROP_PUBLISHED = "cm:published";
const PROP_UPDATED = "cm:updated";

function setOrUpdateReleasedAndUpdatedDates(node)
{
   // make sure the syndication aspect has been added
   if (!node.hasAspect(ASPECT_SYNDICATION))
   {
      node.addAspect(ASPECT_SYNDICATION, []);
   }
   
   // (re-)enable permission inheritance which got disable for draft posts
   // only set if was previously draft - as only the owner/admin can do this
   if (!node.inheritsPermissions())
   {
      node.setInheritsPermissions(true);
   }
   
   // check whether the published date has been set
   if (!node.properties[PROP_PUBLISHED])
   {
      // set the published date
      node.properties[PROP_PUBLISHED] = new Date();
      node.save();
   }
   else
   {
      // set/update the updated date
      node.properties[PROP_UPDATED] = new Date();
      node.save();
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
   
   // is the post published
   var isPublished = (node.properties[PROP_PUBLISHED] !== null);
   if (isPublished)
   {
      data.releasedDate = node.properties[PROP_PUBLISHED];
   }
   
   // draft
   data.isDraft = ! isPublished;
   
   // set the isUpdated flag
   var isUpdated = (node.properties[PROP_UPDATED] !== null);
   data.isUpdated = isUpdated;
   if (isUpdated)
   {
      data.updatedDate = node.properties[PROP_UPDATED];
   }
   
   // fetch standard created/modified dates
   data.createdDate = node.properties["cm:created"];
   data.modifiedDate = node.properties["cm:modified"];
   
   // does the external post require an update?
   if (isPublished && (node.properties["blg:lastUpdate"] !== null))
   {
      // we either use the release or updated date
      var modifiedDate = data.releasedDate;
      if (isUpdated)
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
   if (node.tags !== null)
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
   if (node === null || !node.hasPermission("ReadProperties"))
   {
      return false;
   }
   else if (node.hasAspect("blg:blogDetails"))
   {
      return true;
   }
   else
   {
      return hasExternalBlogConfiguration(node.parent);
   }
}