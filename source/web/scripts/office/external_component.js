/**
 * External component static wrapper that composes and 'calls' external component methods
 */
var ExternalComponent =
{
   extender: null,
   
   init: function(params, extenderType)
   {
      if (typeof extenderType == "undefined")
      {
         // MSOffice mode check
         extenderType = (typeof top.window.external != "undefined" && typeof top.window.external.saveToAlfresco != "undefined") ? "msoffice" : "openoffice";
      }
      
      switch (extenderType.toLowerCase())
      {
         case "msoffice":
            this.extender = new MSOffice(params);
            break;
            
         case "openoffice":
            this.extender = new OpenOffice(params);
            break;
         
         default:
            alert('ExtenderType "' + extenderType + '" is not supported.');
            return;
      }
   },

   openDocument: function()
   {
      return this.extender.openDocument.apply(this.extender, arguments);
   },
   
   docHasExtension: function()
   {
      return this.extender.docHasExtension.apply(this.extender, arguments);
   },
   
   saveToAlfresco: function()
   {
      return this.extender.saveToAlfresco.apply(this.extender, arguments);
   },
   
   saveToAlfrescoAs: function()
   {
      return this.extender.saveToAlfrescoAs.apply(this.extender, arguments);
   },
   
   compareDocument: function()
   {
      return this.extender.compareDocument.apply(this.extender, arguments);
   },
   
   insertDocument: function()
   {
      return this.extender.insertDocument.apply(this.extender, arguments);
   }
}


/**
 * External component wrapper for Microsoft Internet Explorer and MSOffice add-in
 */
var MSOffice = new Class(
{
   params: {},
   
   initialize: function(params)
   {
      $extend(this.params, params);
   },

   openDocument: function(relativePath)
   {
      window.external.openDocument(relativePath);
   },
   
   docHasExtension: function(fnTrue, fnFalse)
   {
      if (window.external.docHasExtension())
      {
         fnTrue.apply(arguments.callee);
      }
      else
      {
         fnFalse.apply(arguments.callee);
      }
   },
   
   saveToAlfresco: function(relativePath)
   {
      return window.external.saveToAlfresco(relativePath);
   },
   
   saveToAlfrescoAs: function(relativePath, filename)
   {
      return window.external.saveToAlfrescoAs(relativePath, filename);
   },
   
   compareDocument: function(url)
   {
      return window.external.compareDocument(url);
   },
   
   insertDocument: function(relativePath, nodeRef)
   {
      return window.external.insertDocument(relativePath, nodeRef);
   }
});


/**
 * External component wrapper for OpenOffice.org add-in
 */
var OpenOffice = new Class(
{
   debugMode: false,
   queryResults: [],
   params: {},

   initialize: function(params)
   {
      $extend(this.params, params);
   },
   
   // Open document with given relativePath
   openDocument: function(relativePath) 
   {
      with (this)
      {
         logDebug('openDocument', 'relativePath=' + relativePath);
         doExternalCall('openDocument', relativePath);
      }
   },

   // call external hoster object method
   docHasExtension: function(functionIfTrue, functionIfFalse)
   {
      with (this)
      {
         logDebug('docHasExtension', 'functionIfTrue, functionIfFalse');
         queryResults["docHasExtension"] = null;
         checkBooleanResult(10, 'docHasExtension', functionIfTrue, functionIfFalse).delay(100, this);
         doExternalCall('docHasExtension', '').delay(1000, this);
      }
   },

   // call external hoster object method
   saveToAlfresco: function(currentPath)
   {
      with (this)
      {
         logDebug('saveToAlfresco', 'currentPath=' + currentPath);
         doExternalCall('saveToAlfresco', currentPath);
      }
   },
  
   // call external hoster object method
   saveToAlfrescoAs: function(currentPath, filename)
   {
      with (this)
      {
         logDebug('saveToAlfrescoAs', 'currentPath=' + currentPath + ", filename=" + filename);
         doExternalCallEx('saveToAlfrescoAs', currentPath, filename);
      }
   },
  
   compareDocument: function(currentPath)
   {
      with (this)
      {
         logDebug('compareDocument', 'currentPath=' + currentPath);
         doExternalCall('compareDocument', currentPath);
      }
   },
  
   // Insert a document into the currently open one
   insertDocument: function(relativePath)
   {
      with (this)
      {
         logDebug('insertDocument', 'relativePath=' + relativePath);
         doExternalCall('insertDocument', relativePath);
      }
   },

  
   /**
    * Implementation-specific functions
    */
   
   // Set external method call result
   setResult: function(methodName, success) 
   {
      with (this)
      {
         logDebug('setResult', 'method=' + methodName + ', success=' + success);
         queryResults[methodName] = success;
      }
   },
  
   // used by timer for checking boolean result of external method call   
   checkBooleanResult: function(maxCount, methodName, functionIfTrue, functionIfFalse)
   {
      with (this)
      {
         var result = queryResults[methodName];
         logDebug('checkBooleanResult', 'waiting: maxCount=' + maxCount + ", methodName=" + methodName + ", ...");
         if (result != null)
         {
            if (result)
            {
               functionIfTrue();
            }
            else
            {
               functionIfFalse();
            }
            return;
         }
         if (maxCount <= 0)
         {
            logDebug('checkBooleanResult', 'waiting timeout: maxCount=' + maxCount + ", methodName=" + methodName + ", ...");
            return;
         }
         checkBooleanResult(maxCount-1, methodName, functionIfTrue, functionIfFalse).delay(1000, this);
      }
   },
  
   // compose URL for purpose to call external object method 
   doExternalCall: function(methodName, path)
   {
      with (this)
      {
         var newUrl = params.folderPath + "callexternal?extcall=&action=" + methodName 
            + "&path=" + encodeURIComponent(path) + "&ts=" + new Date().getTime() 
            + (params.ticket != "" ? "&ticket=" + params.ticket : "");
         logDebug('doExternalCall', 'url=' + newUrl);
         $("if_externalComponenetMethodCall").src = newUrl;
      }
   },

   // compose URL for purpose to call external object method 
   doExternalCallEx: function(methodName, path, filename)
   {
      with (this)
      {
         var newUrl = params.folderPath + "callexternal?extcall=&action=" + methodName 
            + "&path=" + encodeURIComponent(path) + "&filename=" + encodeURIComponent(filename) + "&ts=" + new Date().getTime() 
            + (params.ticket != "" ? "&ticket=" + params.ticket : "");
         logDebug('doExternalCallEx', 'url=' + newUrl);
         $("if_externalComponenetMethodCall").src = newUrl;
      }
   },

   // logger method
   logDebug: function(methodName, message) 
   {
      with (this)
      {
         if (debugMode.enabled && debugMode.methods[methodName])
         {
            alert("[DEBUG][ExternalComponent::" + methodName + "] " + message);
         }
      }
   }
});
