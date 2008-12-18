// todo
// check if previous version was minor or major if no version was submitted
// autognerate comment if none was submitted

function main()
{

   var nodeRef = json.get("nodeRef");
   var version = json.get("version");
   var majorVersion = json.get("majorVersion") == "true";
   var description = json.get("description");

   // allow for content to be loaded from id
   if (nodeRef != null && version != null)
   {

      var workingCopy = search.findNode(nodeRef);

      var versions = null;
      if (workingCopy != null)
      {
         if (workingCopy.isLocked)
         {
            // We cannot revert a locked document
            status.code = 404;
            status.message = "error.nodeLocked";
            status.redirect = true;
            return;
         }

         versions = [];
         var versionHistory = workingCopy.versionHistory;
         if (versionHistory != null)
         {
            for (i = 0; i < versionHistory.length; i++)
            {
               var v = versionHistory[i];
               if (v.label.equals(version))
               {      
                  if (!workingCopy.hasAspect("cm:workingcopy"))
                  {
                     // Ensure the original file is versionable - may have been uploaded via different route
                     if (!workingCopy.hasAspect("cm:versionable"))
                     {
                        // We cannot revert a non versionable document
                        status.code = 404;
                        status.message = "error.nodeNotVersionable";
                        status.redirect = true;
                        return;
                     }

                     // It's not a working copy, do a check out to get the actual working copy
                     workingCopy = workingCopy.checkout();
                  }

                  // Update the working copy content
                  workingCopy.properties.content.write(v.node.properties.content);
                  workingCopy.properties.content.mimetype = v.node.properties.content.mimetype;
                  workingCopy.properties.content.encoding = v.node.properties.content.encoding;

                  // check it in again, with supplied version history note
                  workingCopy = workingCopy.checkin(description, majorVersion);

                  model.document = workingCopy;
                  return;
               }
            }
         }

         // Could not find the version
         status.code = 404;
         status.message = "error.versionNotFound";
         status.redirect = true;
         return;
      }
      else
      {
         // Could not find a document for the nodeRef
         status.code = 404;
         status.message = "error.nodeNotFound";
         status.redirect = true;
         return;
      }
   }
}

main();

