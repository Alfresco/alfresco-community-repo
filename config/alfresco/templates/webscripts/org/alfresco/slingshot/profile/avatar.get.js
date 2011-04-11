/**
 * User Profile - User avatar GET method
 * 
 * Returns a user avatar image in the format specified by the thumbnailname, or the "avatar" preset if omitted.
 * 
 * @method GET
 */

function getPlaceholder(thumbnailName)
{
   // Try and get the place holder resource for a png avatar.
   var phPath = thumbnailService.getMimeAwarePlaceHolderResourcePath(thumbnailName, "images/png");
   if (phPath == null)
   {
      // 404 since no thumbnail was found
      status.setCode(status.STATUS_NOT_FOUND, "Thumbnail was not found and no place holder resource set for '" + thumbnailName + "'");
      return;
   }
   
   return phPath;
}

function main()
{
   var userName = url.templateArgs.username,
      thumbnailName = url.templateArgs.thumbnailname || "avatar",
      person = people.getPerson(userName);
   
   if (person == null)
   {
      // Stream the placeholder image
      model.contentPath = getPlaceholder(thumbnailName);
      return;
   }
   
   // Retrieve the avatar NodeRef for this person, if there is one.
   var avatarAssoc = person.assocs["cm:avatar"];
   
   if (avatarAssoc != null)
   {
      var avatarNode = avatarAssoc[0];
      if (avatarNode != null)
      {
         // Get the thumbnail
         var thumbnail = avatarNode.getThumbnail(thumbnailName);
         if (thumbnail == null || thumbnail.size == 0)
         {
            // Remove broken thumbnail
            if (thumbnail != null)
            {
               thumbnail.remove();
            }
            
            // Force the creation of the thumbnail
            thumbnail = avatarNode.createThumbnail(thumbnailName, false);
            if (thumbnail != null)
            {
               model.contentNode = thumbnail;
               return;
            }
         }
         else
         {
            // Place the details of the thumbnail into the model, this will be used to stream the content to the client
            model.contentNode = thumbnail;
            return;
         } 
      }
   }
   
   // Stream the placeholder image
   model.contentPath = getPlaceholder(thumbnailName);
}

main();