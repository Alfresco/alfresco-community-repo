// locate folder by path
// NOTE: only supports path beneath company home, not from root
var folder = roothome.childByNamePath(url.extension);
if (folder == undefined || !folder.isContainer)
{
      status.code = 404;
   status.message = "Folder " + url.extension + " not found.";
   status.redirect = true;
 }
 model.folder = folder;