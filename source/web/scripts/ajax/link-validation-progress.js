Alfresco.LinkValidationMonitor = function() 
{
   this.url = getContextPath() + '/ajax/invoke/LinkValidationProgressBean.getStatus';
   this.failedMsg = '';
   this.successMsg = '';
}

Alfresco.LinkValidationMonitor.prototype = 
{
   url: null,
   failedMsg: null,
   successMsg: null,
   retrieveLinkValidationStatus: function() 
   {
      YAHOO.util.Connect.asyncRequest('GET', this.url,
         {
            success: this.processResults,
            failure: this.handleError,
            scope: this
         },
         null);
   },
   processResults: function(ajaxResponse) 
   {
      var xml = ajaxResponse.responseXML.documentElement;
      var finished = xml.getAttribute('finished');
      var fileCount = xml.getAttribute('file-count');
      var linkCount = xml.getAttribute('link-count');
      
      var fileCountElem = document.getElementById('file-count');
      if (fileCountElem != null) 
      {
         fileCountElem.innerHTML = fileCount;
      }
      var linkCountElem = document.getElementById('link-count');
      if (linkCountElem != null) 
      {
         linkCountElem.innerHTML = linkCount;
      }

      if (finished == 'true') 
      {
         var linkOnclick = document.getElementById('validation-callback-link').onclick;
         linkOnclick();
      } 
      else 
      {
         setTimeout('Alfresco.linkMonitor.retrieveLinkValidationStatus()', 2000);
      }
   },
   handleError: function(ajaxResponse) 
   {
      handleErrorYahoo(ajaxResponse.status + ' ' + ajaxResponse.statusText);
   }
}

Alfresco.initLinkValidationMonitor = function() 
{
   Alfresco.linkMonitor = new Alfresco.LinkValidationMonitor();
   Alfresco.linkMonitor.retrieveLinkValidationStatus();
}

Alfresco.linkMonitor = null;
window.onload = Alfresco.initLinkValidationMonitor;







