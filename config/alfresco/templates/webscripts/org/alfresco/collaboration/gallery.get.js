
var gallery = search.findNode(url.extension);
if (gallery == undefined || gallery.isContainer == false)
{
	status.code = 404;
   	status.message = "Gallery " + url.extension + " not found.";
   	status.redirect = true;
 }
 model.gallery = gallery;