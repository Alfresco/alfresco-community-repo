//
// Alfresco AJAX support library
// Gavin Cornwell 14-07-2006
//

/**
 * Makes an AJAX request to the server using POST. A text/html response
 * is presumed.
 *
 * @param context The name of the application, normally "/alfresco"
 * @param command The AJAX command to call, either 'invoke', 'get' or 'set'
 * @param expression The managed bean expression
 * @param callbackHandler The function to callback when the request completes
 */
function ajaxPostRequest(context, command, expression, callbackHandler)
{
   makeAjaxRequest(context, command, expression, null, callbackHandler, 
                   "post", "text/html");
}

/**
 * Makes an AJAX request to the server using POST.
 *
 * @param context The name of the application, normally "/alfresco"
 * @param command The AJAX command to call, either 'invoke', 'get' or 'set'
 * @param expression The managed bean expression
 * @param parameters Set of parameters to pass with the request
 * @param callbackHandler The function to callback when the request completes
 * @param method The HTTP method to use for the request either "get" or "post"
 * @param contentType The mimetype to expect from the server
 */
function makeAjaxRequest(context, command, expression, parameters, 
                         callbackHandler, method, contentType)
{
   // use dojo to do the actual work
   dojo.io.bind({
      method: method,
      url: context + "/ajax/" + command + "/" + expression,
      content: parameters,
      load: callbackHandler,
      error: handleErrorDojo,
      mimetype: contentType
   });
}

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