// Generate a PDF transform of the current object

if (document.isDocument)
{
   var runAction = args['action'];
   var result = "Action failed.";

   if (runAction == "makepdf")
   {
      var trans = document.transformDocument("application/pdf");
      result = "Action completed.";
   }
   else if (runAction == "delete")
   {
      var rc = document.remove();
      result = "Action completed.";
   }
   else if (runAction == "checkout")
   {
      var wc = null;
      wc = document.checkout();
      result = "Action completed.";
   }
   else if (runAction == "checkin")
   {
      var wc = document.checkin();
      result = "Action completed.";
   }
   else if (runAction == "makeversion")
   {
      var wc = document.addAspect("cm:versionable");
      result = "Action completed.";
   }
   else
   {
       result = "Unknown action.";
   }
   
   result;
}