//
// Alfresco AJAX support library
// Gavin Cornwell 14-07-2006
//

/**
 * Default handler for errors
 */
function handleErrorDojo(type, errObj)
{
   // remove the dojo prefix from the message
   var errorStart = "XMLHttpTransport Error: 500 ";
   var msg = errObj.message;
   
   if (msg.indexOf(errorStart) != -1)
   {
      msg = msg.substring(errorStart.length);
   }
   
   // TODO: Show a nicer error page, an alert will do for now!
   alert(msg);
}