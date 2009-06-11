/**
* Create e-mail
* contentTextHtml (string) html content
* contentTextPlain (string) text content
*/
function createEmail(contentTextHtml, contentTextPlain, subject, templateUsed)
{
   var command = document.properties["cm:title"];
   var userName = person.properties["cm:userName"];
   
   var inboxFolder = companyhome.childByNamePath("IMAP Home/" + userName + "/INBOX");
   if (inboxFolder == null)
   {
      logger.log("Command Processor: INBOX folder does't exists.");
      return;
   }

   var nextMessageUID = inboxFolder.properties["imap:nextMessageUID"];
   inboxFolder.properties["imap:nextMessageUID"] = nextMessageUID + 1;
   inboxFolder.save();
   
   var response = inboxFolder.createNode("response" + Date.now(), "imap:imapContent");
   response.properties["imap:messageFrom"] = "command@alfresco.com";
   response.properties["imap:messageSubject"] = subject;
   response.properties["imap:messageTo"] = document.properties["cm:originator"];
   response.properties["imap:messageCc"] = "";
   response.properties["imap:messageUID"] = nextMessageUID;

   response.save();

   var textBody = response.createNode("Body.txt", "imap:imapBody");
   textBody.content = contentTextPlain;
   textBody.save();
   
   var htmlBody = response.createNode("Body.html", "imap:imapBody");
   if (templateUsed == true)
   {
      htmlBody.content = contentTextHtml;
   }
   else
   {
      htmlBody.content = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" +
        "<html><head>" +
        "<meta http-equiv=Content-Type content=\"text/html; charset=UTF-8\">" +
        "<style type=\"text/css\">" +
        "* {font-family:Verdana,Arial,sans-serif;font-size:11px;}" +
        ".links {border:1px dotted #555555;border-collapse:collapse;width:99%;}" +
        ".links td {border:1px dotted #555555;padding:5px;}" +
        "</style>" +
        "</head>" +
        "<body>" +
        "<div>" + contentTextHtml + "</div></body></html>";
   }
   htmlBody.save();

}