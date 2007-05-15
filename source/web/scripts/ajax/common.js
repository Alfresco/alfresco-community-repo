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
       * Returns the x/y position of the element in page coordinates.
       * Takes all parent scrollable containers into account.
       */
      getPageXY: function(el)
      {
         var parentNode = null;
         var pos = new Object();
         
         if (el.getBoundingClientRect) // IE
         { 
            var box = el.getBoundingClientRect();
            var doc = document;
            
            var scrollTop = Math.max(doc.documentElement.scrollTop, doc.body.scrollTop);
            var scrollLeft = Math.max(doc.documentElement.scrollLeft, doc.body.scrollLeft);
            
            pos.x = box.left + scrollLeft;
            pos.y = box.top + scrollTop;
            
            return pos;
         }
         else
         {
            // firefox, opera
            pos.x = el.offsetLeft;
            pos.y = el.offsetTop;
            parentNode = el.offsetParent;
            if (parentNode != el)
            {
               while (parentNode)
               {
                   pos.x += parentNode.offsetLeft;
                   pos.y += parentNode.offsetTop;
                   parentNode = parentNode.offsetParent;
               }
            }
         }
         
         if (el.parentNode)
         {
            parentNode = el.parentNode;
         }
         else
         {
            parentNode = null;
         }
         
         while (parentNode && parentNode.tagName.toUpperCase() != 'BODY' && parentNode.tagName.toUpperCase() != 'HTML')
         {
            // account for any scrolled ancestors
            if ($(parentNode).getStyle('display') != 'inline')
            {
               pos.x -= parentNode.scrollLeft;
               pos.y -= parentNode.scrollTop;
            }
            
            if (parentNode.parentNode)
            {
               parentNode = parentNode.parentNode; 
            }
            else
            {
               parentNode = null;
            }
         }
         
         return pos;
      },
      
      /**
       * Returns the height of the document.
       */
      getDocumentHeight: function()
      {
         var scrollHeight = (document.compatMode != 'CSS1Compat') ? document.body.scrollHeight : document.documentElement.scrollHeight;
         return Math.max(scrollHeight, this.getViewportHeight());
      },
      
      /**
       * Returns the width of the document.
       */
      getDocumentWidth: function()
      {
         var scrollWidth = (document.compatMode != 'CSS1Compat') ? document.body.scrollWidth : document.documentElement.scrollWidth;
         return Math.max(scrollWidth, this.getViewportWidth());
      },
      
      /**
       * Returns the current height of the viewport.
       */
      getViewportHeight: function()
      {
          return (document.compatMode == 'CSS1Compat') ?
                 document.documentElement.clientHeight : // Standards
                 document.body.clientHeight; // Quirks
      },
      
      /**
       * Returns the current width of the viewport.
       */
      getViewportWidth: function()
      {
          return (document.compatMode == 'CSS1Compat') ?
                 document.documentElement.clientWidth : // Standards
                 document.body.clientWidth; // Quirks
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
         // extend element with useful mootools prototypes
         el = $(el);
         
         // get the position of the element we are aligning against
         var pos = this.getPageXY(destEl);
         
         // calculate display position for the element
         var region = el.getCoordinates();
         
         var elHeight = region.bottom - region.top;
         var elWidth = region.right - region.left;
         if (maxwidth != undefined && maxwidth != null)
         {
            if (elWidth > maxwidth) elWidth = maxwidth;
         }
         var docWidth = this.getDocumentWidth();
         if (pos.x + 20 + elWidth < docWidth)
         {
            el.style.left = (pos.x + 20) + "px";
         }
         else
         {
            // Shifting X coord left - overflow
            el.style.left = (pos.x + 20 - ((pos.x + elWidth) - docWidth)) + "px";
         }
         if (pos.y + 12 + elHeight < this.getDocumentHeight())
         {
            el.style.top = (pos.y + 12) + "px";
         }
         else
         {
            // Shifting Y coord up - overflow
            el.style.top = (pos.y - elHeight + 4) + "px";
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

if (!String.prototype.startsWith)
{
  String.prototype.startsWith = function(s)
  {
    return this.indexOf(s) == 0;
  }
}

if (!Array.prototype.indexOf)
{
  Array.prototype.indexOf = function(o)
  {
    for (var i = 0; i < this.length; i++)
    {
      if (this[i] == o)
      {
        return i;
      }
    }
    return -1;
  }
}

if (!Array.prototype.peek)
{
  Array.prototype.peek = function(o)
  {
    return this[this.length - 1];
  }
}
