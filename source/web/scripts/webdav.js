//
// Helper functions for launching WebDAV documents for editing
// Gavin Cornwell 30-11-2005
//

function openDoc(url)
{
   var showDoc = true;
   var agent = navigator.userAgent.toLowerCase();
      
   // work out the context path from the pathname (this means we dont't
   // have to rely on the context path being passed in an anyway)   
   var contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 1));
   var fullUrl = window.location.protocol + "//" + window.location.host + contextPath + url;
   var lowerUrl = url.toLowerCase();
   
   // if the link represents an Office document and we are in IE try and
   // open the file directly to get WebDAV editing capabilities
   if (agent.indexOf("msie") != -1)
   {
      if (lowerUrl.indexOf(".doc") != -1 || lowerUrl.indexOf(".docx") != -1 ||
          lowerUrl.indexOf(".xls") != -1 || lowerUrl.indexOf(".xlsx") != -1 ||
          lowerUrl.indexOf(".ppt") != -1 || lowerUrl.indexOf(".pptx") != -1 ||
          lowerUrl.indexOf(".dot") != -1 || lowerUrl.indexOf(".dotx") != -1)
      {
         try
         {
            var wordDoc = new ActiveXObject("SharePoint.OpenDocuments.1");
            if (wordDoc)
            {
               showDoc = false;
               wordDoc.EditDocument(fullUrl);
            }
         }
         catch(e)
         {
            showDoc = true;
         }
      }
   }
   
   if (showDoc == true)
   {
      window.open(fullUrl, "_blank");
   }
}