// check that search term has been provided
if (args.store == undefined || args.store.length == 0 ||
    args.path == undefined || args.path.length == 0 ||
    args.view == undefined || args.view.length == 0)
{
   status.code = 400;
   status.message = "Mandatory arguments not set - please complete all form fields.";
   status.redirect = true;
}
else
{
   // lookup the root on the store
   var storeRootNode = avm.lookupStoreRoot(args.store);
   if (storeRootNode != null)
   {
      var path = storeRootNode.path + args.path;
      var node = avm.lookupNode(path);
      if (node != null)
      {
         // add the custom view aspect
         node.addAspect("cm:webscriptable");
         node.properties["cm:webscript"] = "/wcs" + args.view;
         node.save();
         model.success = true;
      }
   }
}