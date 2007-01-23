//
// Alfresco JavaScript support library
// Gavin Cornwell 14-07-2006
//

var _alfContextPath = null;

/**
 * Default handler for errors when using the dojo toolkit
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

/**
 * Default handler for errors when using the yahoo toolkit
 */
function handleErrorYahoo(msg)
{
	// TODO: Show a nicer error page, an alert will do for now!
   alert(msg);
}

/**
 * Calculates and returns the context path for the current page
 */
function getContextPath()
{
	if (_alfContextPath == null)
   {
      var path = window.location.pathname;
      var idx = path.indexOf("/", 1);
      if (idx != -1)
      {
         _alfContextPath = path.substring(0, idx);
      }
      else
      {
         _alfContextPath = "";
      }
   }

   return _alfContextPath;
}

/**
 * Returns a single child element with the given tag
 * name from the given parent. If more than one tag
 * exists the first one is returned, if none exist null
 * is returned.
 */
function getElementByTagName(elParent, tagName)
{
   var el = null;
   
   if (elParent != null && tagName != null)
   {
      var elems = elParent.getElementsByTagName(tagName);
      if (elems != null && elems.length > 0)
      {
         el = elems[0];
      }
   }
   
   return el;
}

/**
 * Returns a single child element with the given tag
 * name and namespace from the given parent. 
 * If more than one tag exists the first one is returned, 
 * if none exist null is returned.
 */
function getElementByTagNameNS(elParent, nsUri, nsPrefix, tagName)
{
   var el = null;
   
   if (elParent != null && tagName != null)
   {
      var elems = null;
      
      if (elParent.getElementsByTagNameNS)
      {
         elems = elParent.getElementsByTagNameNS(nsUri, tagName);
      }
      else
      {
         elems = elParent.getElementsByTagName(nsPrefix + ":" + tagName);
      }
      
      if (elems != null && elems.length > 0)
      {
         el = elems[0];
      }
   }
   
   return el;
}

/**
 * Returns the text of the given DOM element object
 */
function getElementText(el)
{
   var txt = null;
   
   if (el.text != undefined)
   {
      // get text using IE specific property
      txt = el.text;
   }
   else
   {
      // use the W3C textContent property
      txt = el.textContent;
   }
   
   return txt;
}

/**
 * Returns the text content of a single child element 
 * with the given tag name from the given parent. 
 * If more than one tag exists the text of the first one 
 * is returned, if none exist null is returned.
 */
function getElementTextByTagName(elParent, tagName)
{
   var txt = null;
   
   var el = getElementByTagName(elParent, tagName);
   if (el != null)
   {
      txt = getElementText(el);
   }

   return txt;   
}

/**
 * Returns the text a single child element with the given tag
 * name and namespace from the given parent. 
 * If more than one tag exists the text of the first one is returned, 
 * if none exist null is returned.
 */
function getElementTextByTagNameNS(elParent, nsUri, nsPrefix, tagName)
{
   var txt = null;
   
   var el = getElementByTagNameNS(elParent, nsUri, nsPrefix, tagName);
   if (el != null)
   {
      txt = getElementText(el);
   }

   return txt;   
}

/**
 * Logs a message to a debug log window.
 * 
 * Example taken from http://ajaxcookbook.org/javascript-debug-log
 */
function log(message) 
{
   if (!log.window_ || log.window_.closed) 
   {
      var win = window.open("", null, "width=400,height=200," +
                              "scrollbars=yes,resizable=yes,status=no," +
                              "location=no,menubar=no,toolbar=no");
      if (!win) return;
      var doc = win.document;
      doc.write("<html><head><title>Debug Log</title></head>" +
                "<body></body></html>");
      doc.close();
      log.window_ = win;
   }
   
   var logLine = log.window_.document.createElement("div");
   logLine.appendChild(log.window_.document.createTextNode(message));
   log.window_.document.body.appendChild(logLine);
}





