//
// Alfresco JavaScript support library
// Gavin Cornwell 14-07-2006
//

// Global Alfresco namespace object
if (typeof Alfresco == "undefined") 
{
   var Alfresco = {};
}

var _alfContextPath = null;

/**
 * Error handler for errors caught in a catch block
 */
function handleCaughtError(err)
{
   var msg = null;
      
   if (err.message)
   {
      msg = err.message;
   }
   else
   {
      msg = err;
   }
   
   alert("An error occurred: " + msg);
}
   
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
function handleErrorYahoo(e)
{
	// TODO: Show a nicer error page, an alert will do for now!
   alert(e.status + " : " + e.statusText);
}

/**
 * Sets the context path to use, useful for portals where 
 * the URL can be different from the app's context path.
 */
function setContextPath(contextPath)
{
   _alfContextPath = contextPath;
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
 * Alfresco Utility libraries
 */
(function()
{
   /**
    * DOM library
    */
   Alfresco.Dom = {
      
      /**
       * Returns a single child element with the given tag
       * name from the given parent. If more than one tag
       * exists the first one is returned, if none exist null
       * is returned.
       */
      getElementByTagName: function(elParent, tagName)
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
      },
      
      /**
       * Returns a single child element with the given tag
       * name and namespace from the given parent. 
       * If more than one tag exists the first one is returned, 
       * if none exist null is returned.
       */
      getElementByTagNameNS: function(elParent, nsUri, nsPrefix, tagName)
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
      },
      
      /**
       * Returns the text of the given DOM element object
       */
      getElementText: function(el)
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
      },
      
      /**
       * Returns the text content of a single child element 
       * with the given tag name from the given parent. 
       * If more than one tag exists the text of the first one 
       * is returned, if none exist null is returned.
       */
      getElementTextByTagName: function(elParent, tagName)
      {
         var txt = null;
         
         var el = this.getElementByTagName(elParent, tagName);
         if (el != null)
         {
            txt = this.getElementText(el);
         }
      
         return txt;   
      },
      
      /**
       * Returns the text a single child element with the given tag
       * name and namespace from the given parent. 
       * If more than one tag exists the text of the first one is returned, 
       * if none exist null is returned.
       */
      getElementTextByTagNameNS: function(elParent, nsUri, nsPrefix, tagName)
      {
         var txt = null;
         
         var el = this.getElementByTagNameNS(elParent, nsUri, nsPrefix, tagName);
         if (el != null)
         {
            txt = this.getElementText(el);
         }
      
         return txt;
      },
   
      /**
       * Aligns an element against the specified element. Automatically adjusts the element above or to
       * the left of the destination if the element would cause a scrollbar to appear.
       * 
       * @param el         Element to align
       * @param destEl     Destination element to align against
       * @param maxwidth   Maximum width of the element (assumed max-width CSS applied)
       */
      smartAlignElement: function (el, destEl, maxwidth)
      {
         // get the position of the element we are aligning against
         var pos = YAHOO.util.Dom.getXY(destEl);
         
         // calculate display position for the element
         var region = YAHOO.util.Dom.getRegion(el);
         //log("DIV popup size: Width:" + (region.right-region.left) + ", Height:" + (region.bottom-region.top));
         var elHeight = region.bottom - region.top;
         var elWidth = region.right - region.left;
         //log("elWidth:" + elWidth + " maxwidth:" + maxwidth);
         if (maxwidth != undefined && maxwidth != null)
         {
            if (elWidth > maxwidth) elWidth = maxwidth;
         }
         var docWidth = YAHOO.util.Dom.getDocumentWidth();
         if (pos[0] + elWidth < docWidth)
         {
            el.style.left = pos[0];
         }
         else
         {
            el.style.left = pos[0] - ((pos[0] + elWidth) - docWidth);
         }
         //log(" Element Y:" + pos[1] + " doc height:" + YAHOO.util.Dom.getDocumentHeight());
         if (pos[1] + 16 + elHeight < YAHOO.util.Dom.getDocumentHeight())
         {
            el.style.top = pos[1] + 12;
         }
         else
         {
            //log(" ***Changing position - will overflow");
            el.style.top = pos[1] - elHeight - 4;
         }
      }
   };
})();

/**
 * Logs a message to a debug log window.
 * 
 * Example taken from http://ajaxcookbook.org/javascript-debug-log
 */
function log(message) 
{
   if (!log.window_ || log.window_.closed) 
   {
      var win = window.open("", null, "width=600,height=400," +
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