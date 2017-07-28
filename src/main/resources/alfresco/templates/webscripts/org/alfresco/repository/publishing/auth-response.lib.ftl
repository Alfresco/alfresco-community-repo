<#-- Renders the HTML response to a completed Auth Request -->
<#macro htmlPage>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">   
      <meta name="Generator" content="Alfresco Repository">
      <title>Alfresco &raquo; Authorisation</title>
      <style type="text/css">
         body {margin:3em;font-family:arial,helvetica,clean,sans-serif;}
         div.header {background:#56A3D9;}
         h1 {color: white;font-size: 1.3em;padding:5px 6px 3px;}
      </style>
   </head>
   <body>
      <img src="http://www.alfresco.com/images/alfresco-logo.png" alt="Alfresco" />
      <div class="header"><h1>Publishing Channel Authorisation</h1></div>
      <p id="status">Completing your authorisation...</p>
      <script>
         // The Auth token is received from the publisher as a hash on the URL of this call
         // This needs passing back to the originating Alfresco instance.
         // One of the easiest ways to achieve this is if we still have a handle to the
         // window that opened the page:
         var statusEl = document.getElementById("status");
         try
         {
            if (window.opener !== null)
            {
               // We have a handle on the window:
               window.opener.location.hash = "complete";
               statusEl.innerHTML = "Your authorisation has been completed. You may now close this window";
            } else
            {
               // No window opener - we can't submit the token back.
               statusEl.innerHTML = "Your authorisation could not be completed. Please return to the Admin Console and try again."
            }
            self.close();
         }
         catch(error)
         {
            statusEl.innerHTML = "Your authorisation has been completed. You may now close this window. You will need to refresh the channel list to see your changes";
         }
      </script>
   </body>
</html>
</#macro>