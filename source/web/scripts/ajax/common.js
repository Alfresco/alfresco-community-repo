//
// Alfresco JavaScript support library
// Gavin Cornwell 14-07-2006
//


function getIEEngine()
{
   var engine = null;

   if (document.documentMode) // IE8
      engine = document.documentMode;
   else // IE 5-7
   {
      engine = 5; // Assume quirks mode unless proven otherwise
      if (document.compatMode)
      {
         if (document.compatMode == "CSS1Compat")
            engine = 7; // standards mode
      }
   }
   return engine;

}

function getIEVersion()
{
   var rv = -1;
   var ua = window.navigator.userAgent;
   var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
   if (re.exec(ua) != null) rv = parseFloat(RegExp.$1);
   return rv;
}

// Global Alfresco namespace object
if (typeof Alfresco == "undefined") 
{
   var Alfresco = {};
}

var _checkContextAgainstPath = false;
var _alfContextPath = null;


/**
 * window.onload function for r:page tag
 */
function onloadFunc(webdavUrl, cifsPath)
{
   if (webdavUrl != "")
   {
      openDoc(webdavUrl);
   }
   if (cifsPath != "")
   {
      window.open(cifsPath, "_blank");
   }
}

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
   
   alert("An error occurred:\n" + msg);
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
   if (e.status == 401)
   {
      document.location = window.location.protocol + "//" + window.location.host + getContextPath();
   }
   else
   {
      // TODO: Show a nicer error page, an alert will do for now!
      alert(e.status + " : " + e.statusText);
   }
}

/**
 * Determines whether the setContextPath method below should
 * check the provided context path against the URLs path
 */
function setCheckContextAgainstPath(checkContextAgainstPath)
{
   _checkContextAgainstPath = checkContextAgainstPath;
}

/**
 * Sets the context path to use, useful for portals where 
 * the URL can be different from the app's context path.
 */
function setContextPath(contextPath)
{
   if (_checkContextAgainstPath)
   {
      if (window.location.pathname.indexOf(contextPath) == 0 ) 
      { 
         _alfContextPath = contextPath; 
      } 
      else 
      { 
         _alfContextPath = ""; 
      }
   }
   else
   {
      _alfContextPath = contextPath;
   }
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

if (typeof document.ELEMENT_NODE == "undefined")
{
  // define dom constants for IE compatability
  document.ELEMENT_NODE = 1;
  document.ATTRIBUTE_NODE = 2;
  document.TEXT_NODE = 3;
  document.CDATA_SECTION_NODE = 4;
  document.ENTITY_REFERENCE_NODE = 5;
  document.ENTITY_NODE = 6;
  document.PROCESSING_INSTRUCTION_NODE = 7;
  document.COMMENT_NODE = 8;
  document.DOCUMENT_NODE = 9;
  document.DOCUMENT_TYPE_NODE = 10;
  document.DOCUMENT_FRAGMENT_NODE = 11;
  document.NOTATION_NODE = 12;
}

/**
 * UIDataPager functions
 */
function validateAndSubmit(e, pageInputId, formClientId, hiddenFieldName)
{
   var keycode;
   if (window.event) keycode = window.event.keyCode;
   else if (e) keycode = e.which;
   if (keycode == 13)
   {
      var inputControl = $(pageInputId);
      var dialogForm = $('dialog');
      if (dialogForm)
      {
         dialogForm.removeProperty('onsubmit');
      }
      var val = parseInt(inputControl.value);
      if (val == 'NaN' || document.forms[formClientId][hiddenFieldName] == undefined)
      {
         inputControl.value = 1;
         //console.log("validateAndSubmit: reverting to 1");
         return false;
      }
      else
      {
         val = (val-1)>=0 ? val-1 : 0; 
         document.forms[formClientId][hiddenFieldName].value = val;
         document.forms[formClientId].submit();
         //console.log("validateAndSubmit: submitting value: " + val);
         return false;
      }
   }
   //console.log("validateAndSubmit: passthrough...");
   return true;
}

function onlyDigitsIE6(e, pageInputId, formClientId, hiddenFieldName)
{
   var keycode;
   if (window.event) keycode = window.event.keyCode;
   else if (e) keycode = e.which;
   var keychar = String.fromCharCode(keycode);
   var numcheck = /\d/;
   if (keycode == 13)
   {
      var inputControl = $(pageInputId);
      var val = parseInt(inputControl.value);
      if (val == 'NaN' || document.forms[formClientId][hiddenFieldName] == undefined)
      {
         inputControl.value = 1;
         return false;
      }
      else
      {
         val = (val-1)>=0 ? val-1 : 0; 
         document.forms[formClientId][hiddenFieldName].value = val;
         document.forms[formClientId].submit();
         return false;
      }
   }
   var result = (keycode==13 || keycode==8 || keycode==37 || keycode==39 || keycode==46 || (keycode>=96 && keycode<=105) || numcheck.test(keychar));
   //console.log("onlyDigits: " + result);
   return result;
}

function onlyDigits(e)
{
   var keycode;
   if (window.event) keycode = window.event.keyCode;
   else if (e) keycode = e.which;
   var keychar = String.fromCharCode(keycode);
   var numcheck = /\d/;
   var dialogForm = $('dialog');
   if (dialogForm && keycode == 13)
   { 
      dialogForm.setProperty('onsubmit', 'return false;');
   }
   var result = (keycode==13 || keycode==8 || keycode==37 || keycode==39 || keycode==46 || (keycode>=96 && keycode<=105) || numcheck.test(keychar));
   //console.log("onlyDigits: " + result);
   return result;
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
         var shiftedLeft = false;
         if (pos.x + 20 + elWidth < docWidth)
         {
            el.style.left = (pos.x + 20) + "px";
         }
         else
         {
            // Shifting X coord left - overflow
            el.style.left = (pos.x + 20 - ((pos.x + elWidth) - docWidth)) + "px";
            shiftedLeft = true;
         }
         if (pos.y + 12 + elHeight < this.getDocumentHeight())
         {
            el.style.top = (pos.y + 12) + "px";
         }
         else
         {
            // Shifting Y coord up - overflow
            if (shiftedLeft == true)
            {
               el.style.top = (pos.y - elHeight + 4) + "px";
            }
            else
            {
               // we have room to shift vertically without overwriting the pop-up icon
               var ypos = (pos.y - elHeight + 4);
               if (ypos < 0) ypos = 0;
               el.style.top = ypos + "px";
            }
         }
      },
      
      encodeHTML: function(text)
      {
         if (text === null || typeof text == "undefined")
         {
            return "";
         }
         
         text = "" + text;
         return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
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
   if (window.console)
   {
     console.log(message);
   }
   else
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
}

/**
 * Throws an error if the specified condition is not met.
 */
function assert(condition, message)
{
  if (!condition)
  {
    log(message);
    throw new Error("Assertion failed: " + message);
  }
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

// this is an exact copy of ../upload_helper.js - needs refactoring
var _fileUploads = [];

function handleUploadHelper(fileInputElement,
                            uploadId,
                            callback,
                            contextPath,
                            actionUrl,
                            params)
{
   var id = fileInputElement.getAttribute("name");
   var d = fileInputElement.ownerDocument;
   var iframe = d.createElement("iframe");
   iframe.style.display = "none";
   iframe.name = id + "upload_frame";
   iframe.id = iframe.name;
   document.body.appendChild(iframe);
   
   // makes it possible to target the frame properly in IE.
   window.frames[iframe.name].name = iframe.name;
   
   _fileUploads[uploadId] = { path: fileInputElement.value, callback: callback };
   
   var form = d.createElement("form");
   d.body.appendChild(form);
   form.id = id + "_upload_form";
   form.name = form.id;
   form.style.display = "none";
   form.method = "post";
   form.encoding = "multipart/form-data";
   form.enctype = "multipart/form-data";
   form.target = iframe.name;
   actionUrl = actionUrl || "/uploadFileServlet";
   form.action = contextPath + actionUrl;
   form.appendChild(fileInputElement);
   
   var id = document.createElement("input");
   id.type = "hidden";
   form.appendChild(id);
   id.name = "upload-id";
   id.value = uploadId;
   
   if (params != undefined && params != null)
   {
      for (var i in params)
      {
         var p = document.createElement("input");
         p.type = "hidden";
         form.appendChild(p);
         id.name = i;
         id.value = params[i];
      }
   }
   
   var rp = document.createElement("input");
   rp.type = "hidden";
   form.appendChild(rp);
   rp.name = "return-page";
   rp.value = "javascript:window.parent.uploadCompleteHelper('" + uploadId + 
              "',{error: '${UPLOAD_ERROR}'})";
   
   form.submit();
}

function uploadCompleteHelper(id, args)
{
   var upload = _fileUploads[id];
   upload.callback(id, 
                   upload.path, 
                   upload.path.replace(/.*[\/\\]([^\/\\]+)/, "$1"),
                   args.error != "${UPLOAD_ERROR}" ? args.error : null);
}

var openWindowCallbackFn = null;

function openWindowCallback(url, callback)
{
   // Store the callback function for later
   openWindowCallbackFn = callback;
   // Register our "well known" callback function
   window.alfrescoCallback = openWindowOnCallback;
   // Use a named window so that only one dialog is active at a time
   window.open(url, 'alfrescoDialog', 'width=1024,height=768,scrollbars=yes');
}
   
function openWindowOnCallback(fromTimeout)
{
   if (typeof(fromTimeout)=='undefined')
   {
      window.setTimeout("openWindowOnCallback(true)", 10);
   }
   else
   {
      // Clear out the global callback function
      window.alfrescoCallback = null;
      // Try the callback function
      try
      {
         openWindowCallbackFn();
      }
      catch (e)
      {
      }
      openWindowCallbackFn = null;
   }
}
