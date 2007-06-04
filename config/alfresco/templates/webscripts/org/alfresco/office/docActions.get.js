// Client has requested certain actions on the current document

/* Inputs */
var docId = args["d"],
   runAction = args["a"];

/* Outputs */
var resultString = "Action failed.",
   resultCode = false;

var doc = search.findNode("workspace://SpacesStore/" + docId);

if (doc != null && doc.isDocument)
{
   try
   {
      if (runAction == "makepdf")
      {
         resultString = "Could not convert document";
         var nodeTrans = doc.transformDocument("application/pdf");
         if (nodeTrans != null)
         {
            resultString = "Document converted";
            resultCode = true;
         }
      }
      else if (runAction == "delete")
      {
         resultString = "Could not delete document";
         if (doc.remove())
         {
            resultString = "Document deleted";
            resultCode = true;
         }
      }
      else if (runAction == "checkout")
      {
         var workingCopy = doc.checkout();
         if (workingCopy != null)
         {
            resultString = "Document checked out";
            resultCode = true;
         }
      }
      else if (runAction == "checkin")
      {
         var originalDoc = doc.checkin();
         if (originalDoc != null)
         {
            resultString = "Document checked in";
            resultCode = true;
         }
      }
      else if (runAction == "makeversion")
      {
         resultString = "Could not version document";
         if (doc.addAspect("cm:versionable"))
         {
            resultString = "Document versioned";
            resultCode = true;
         }
      }
      else if (runAction == "test")
      {
         resultString = "Test complete.";
         resultCode = true;
      }
      else
      {
          resultString = "Unknown action.";
      }
   }
   catch(e)
   {
      resultString = "Action failed due to exception";
   }
}
model.resultString = resultString;
model.resultCode = resultCode;