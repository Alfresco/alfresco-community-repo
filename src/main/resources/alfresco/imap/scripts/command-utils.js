/**
* Create e-mail
* contentEML (string) content message
*/
function createEmail(messageTXT, messageHTML, subject)
{
   var command = document.properties["cm:title"];
   var userName = person.properties["cm:userName"];
   
   var imapRoot = imap.getImapHomeRef(userName);
   var inboxFolder = imapRoot.childByNamePath("INBOX");
   
   if (inboxFolder == null)
   {
      logger.log("Command Processor: INBOX folder doesn't exist.");
      return;
   }

   var response = inboxFolder.createNode("response" + Date.now() + ".eml", "cm:content");
   response.properties["imap:messageFrom"] = "command@alfresco.com";
   response.properties["imap:messageSubject"] = subject;
   response.properties["imap:messageTo"] = document.properties["cm:originator"];
   response.properties["imap:messageCc"] = "";
   response.addAspect("imap:imapContent", null);

   response.content = createRFC822Message("command@alfresco.com", document.properties["cm:originator"], subject, messageTXT, messageHTML);
   response.save();
   }

function createRFC822Message(from, to, subject, textPart, htmlPart)
{
	var id = new Number(Date.now()).toString(16);
    var boundary = "----------" + id;
    var date = new Date().toGMTString();    
    var messageHeaders = "MIME-Version: 1.0\r\n" +
                         "Date: " + date + "\r\n" +
                         "From: " + from + "\r\n" +
                         "To: " + to + "\r\n" +
                         "Subject: " + subject + "\r\n" +
                         "Message-ID: " + id + "\r\n" +
                         "X-Priority: 3 (Normal)\r\n" +
                         "Content-Type: multipart/alternative; boundary=\"" + boundary + "\"\r\n\r\n";
    var messageBody = "";
    messageBody += messageHeaders;
    messageBody += "--" + boundary + "\r\n";
    messageBody += "Content-Type: text/plain; charset=utf-8\r\n";
    //TODO Content-Transfer-Encoding
    messageBody += "\r\n";
    messageBody += textPart + "\r\n\r\n";
    messageBody += "--" + boundary + "\r\n";
    messageBody += "Content-Type: text/html; charset=utf-8\r\n";
    //TODO Content-Transfer-Encoding
    messageBody += "\r\n";
    messageBody += htmlPart + "\r\n\r\n";
    messageBody += "--" + boundary + "--\r\n\r\n";
    return messageBody;
}