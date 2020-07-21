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

         // Ensure the original file is versionable - may have been uploaded via different route
         if (!workingCopy.hasAspect("cm:versionable"))
         {
            // We cannot revert a non versionable document
        	status.code = 404;
            status.message = "error.nodeNotVersionable";
            status.redirect = true;
            return;
         }

         // Revert the node
    	 workingCopy = workingCopy.revert(description, majorVersion, version);
         if (workingCopy == null)
         {
             // Could not find the version
        	 status.code = 404;
             status.message = "error.versionNotFound";
             status.redirect = true;
             return;
         }
         
    	 model.document = workingCopy;
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

