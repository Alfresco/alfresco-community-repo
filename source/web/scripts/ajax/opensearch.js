//
// Alfresco OpenSearch client library
// Gavin Cornwell 09-01-2007
//
// NOTE: This script relies on common.js so therefore needs to be loaded
//       prior to this one on the containing HTML page.

var _OS_NS_PREFIX = "opensearch";
var _OS_NS_URI = "http://a9.com/-/spec/opensearch/1.1/";
var _RESULTS_DIV_ID_SUFFIX = "-os-results";
var _OPTIONS_DIV_ID_SUFFIX = "-os-options";
var _RESULTSET_PANEL_DIV_ID_SUFFIX = "-osresults-panel";
var _RESULTSET_LIST_DIV_ID_SUFFIX = "-osresults-list";
var _RESULTSET_PAGING_DIV_ID_SUFFIX = "-osresults-paging";
var _ENGINE_ENABLED_FIELD_ID = "-engine-enabled";
var _SEARCH_TERM_FIELD_ID = "-search-term";
var _PAGE_SIZE_FIELD_ID = "-page-size";

/**
 * Constructor for an object to hold the definition of an OpenSearch engine
 */
Alfresco.OpenSearchEngine = function(id, label, url)
{
   this.id = id;
   this.label = label;
   this.url = url;
}

/**
 * Constructor for an OpenSearchClient object
 */
Alfresco.OpenSearchClient = function(id)
{
   this.id = id;
   this.engines = [];
   this.enginesById = [];
   this.searchInProgress = false;
}

Alfresco.OpenSearchClient.prototype = 
{
   id: null,
   
   engines: null,
   
   enginesById: null,
   
   searchInProgress: false,
   
   msgNoResults: null,
   
   msgOf: null,
   
   msgFailedGenerateUrl: null,
   
   msgFailedSearch: null,
   
   msgFirstPage: null,
   
   msgPreviousPage: null,
   
   msgNextPage: null,
   
   msgLastPage: null,

   msgInvalidTermLength: null,
   
   minTermLength: 0,

   /**
    * Registers an OpenSearch engine to be called when performing queries
    */
   registerOpenSearchEngine: function(id, label, url)
   {
      var se = new Alfresco.OpenSearchEngine(id, label, url);
      this.engines[this.engines.length] = se;
      this.enginesById[id] = se;
   },

   /**
    * Registers an OpenSearch engine to be called when performing queries.
    * Extra parameter is the minimum search term length. 
    */
   registerOpenSearchEngine: function(id, label, url, minLengh)
   {
      var se = new Alfresco.OpenSearchEngine(id, label, url);
      this.engines[this.engines.length] = se;
      this.enginesById[id] = se;
      this.minTermLength = minLengh;
   },

   /**
    * Sets the 'No Results' message
    */
   setMsgNoResults: function(msg)
   {
      this.msgNoResults = msg;
   },
   
   /**
    * Sets the 'of' message
    */
   setMsgOf: function(msg)
   {
      this.msgOf = msg;
   },
   
   /**
    * Sets the 'Failed to generate url' message
    */
   setMsgFailedGenerateUrl: function(msg)
   {
      this.msgFailedGenerateUrl = msg;
   },
   
   /**
    * Sets the 'Failed to retrieve search results' message
    */
   setMsgFailedSearch: function(msg)
   {
      this.msgFailedSearch = msg;
   },
   
   /**
    * Sets the 'First Page' message
    */
   setMsgFirstPage: function(msg)
   {
      this.msgFirstPage = msg;
   },
   
   /**
    * Sets the 'Previous Page' message
    */
   setMsgPreviousPage: function(msg)
   {
      this.msgPreviousPage = msg;
   },
   
   /**
    * Sets the 'Next Page' message
    */
   setMsgNextPage: function(msg)
   {
      this.msgNextPage = msg;
   },
   
   /**
    * Sets the 'Last Page' message
    */
   setMsgLastPage: function(msg)
   {
      this.msgLastPage = msg;
   },
   
   /**
    * Sets the invalid term length message
    */
   setMsgInvalidTermLength: function(msg)
   {
      this.msgInvalidTermLength = msg;
   },

   /**
    * Handles the key press event, if ENTER is pressed execute the query
    */
   handleKeyPress: function(e)
   {
      var keycode;
      
      // get the keycode
      if (window.event) 
      {
         keycode = window.event.keyCode;
      }
      else if (e)
      {
         keycode = e.which;
      }
      
      // if ENTER was pressed execute the query
      if (keycode == 13)
      {  
         this.executeQuery();
         return false;
      }
      
      return true;
   },

   /**
    * Toggles the visibility of the options panel
    */
   toggleOptions: function(icon)
   {
      var currentState = icon.className;
      var optionsDiv = document.getElementById(this.id + _OPTIONS_DIV_ID_SUFFIX);
      
      if (currentState == "collapsed")
      {
         icon.src = getContextPath() + "/images/icons/expanded.gif";
         icon.className = "expanded";
         
         // show the div holding the options
         if (optionsDiv != null)
         {
            optionsDiv.style.display = "block";
         }
      }
      else
      {
         icon.src = getContextPath() + "/images/icons/collapsed.gif";
         icon.className = "collapsed";
         
         // hide the div holding the options
         if (optionsDiv != null)
         {
            optionsDiv.style.display = "none";
         }
      }
   },

   /**
    * Executes the query against all the registered and selected opensearch engines
    */
   executeQuery: function()
   {
      // gather the required parameters
      var term = document.getElementById(this.id + _SEARCH_TERM_FIELD_ID).value;
      var count = document.getElementById(this.id + _PAGE_SIZE_FIELD_ID).value;
      
      // ADB-134 fix (Error message about not enough search criteria)
      if (term.length < this.minTermLength)
      {
         var errorMsg = this.msgInvalidTermLength.replace("{0}", this.minTermLength);
         handleCaughtError(errorMsg);
         return;
      } 
      
      // default the count if its invalid
      if (count.length == 0 || isNaN(count) || count < 1)
      {
         count = 5;
         document.getElementById(this.id + _PAGE_SIZE_FIELD_ID).value = count;
      }
      
      // issue the queries if there is enough search criteria (& ADB-133 fix: parametrized minTermLength)
      if (this.searchInProgress == false && term != null && term.length >= this.minTermLength)
      {
         // remove previous results (if necessary)
         var resultsPanel = document.getElementById(this.id + _RESULTS_DIV_ID_SUFFIX);
         if (resultsPanel != null)
         {
            while (resultsPanel.firstChild) 
            {
               resultsPanel.removeChild(resultsPanel.firstChild);
            };
         }
         
         // issue the search request for each enabled engine
         for (var e = 0; e < this.engines.length; e++)
         {
            // get the checkbox for the current engine
            var ose = this.engines[e];
            var engCheckbox = document.getElementById(this.id + "-" + ose.id + _ENGINE_ENABLED_FIELD_ID);
            if (engCheckbox != null && engCheckbox.checked)
            {
               // we found at least one engine - show that we are executing a search
               this.searchInProgress = true;
               this.issueSearchRequest(ose, term, count);
            }
         }
      }
   },
   
   /**
    * Issues an Ajax request for the given OpenSearchEngine
    * using the given search term and page size.
    */
   issueSearchRequest: function(ose, term, pageSize)
   {
      // generate the search url
      var searchUrl = this.calculateSearchUrl(ose.url, term, pageSize);
      
      // issue the request
      if (searchUrl != null)
      {
         YAHOO.util.Connect.asyncRequest("GET", searchUrl, 
            { 
               success: Alfresco.OpenSearchEngine.processSearchResults,
               failure: Alfresco.OpenSearchEngine.handleSearchError,
               argument: [ose.id, this]
            }, 
            null);
      }
      else
      {
         // replace the token with the engine label
         var errorMsg = this.msgFailedGenerateUrl.replace("{0}", ose.label);
         handleCaughtError(errorMsg);
      }
   },
   
   /**
    * Shows another page of the current search results for the
    * given engineId
    */
   showPage: function(engineId, url)
   {
      // execute the query and process the results
      YAHOO.util.Connect.asyncRequest("GET", url, 
         { 
            success: Alfresco.OpenSearchEngine.processShowPageResults,
            failure: Alfresco.OpenSearchEngine.handleSearchError,
            argument: [engineId, this]
         }, 
         null);
   },

   /**
    * Generates a concrete url for the given template url and parameters.
    *
    * All parameters (inside { and }) have to be replaced. We only need to populate
    * the 'searchTerms' and 'count' parameters, all optional ones will use the 
    * empty string. If there is a mandatory parameter present (other than searchTerms
    * and count) null will be returned.
    */
   calculateSearchUrl: function(templateUrl, term, count)
   {
      var searchUrl = null;
      
      term = encodeURIComponent(term);
      
      // define regex pattern to look for params
      var pattern = /\{+\w*\}+|\{+\w*\?\}+|\{+\w*:\w*\}+|\{+\w*:\w*\?\}+/g;
   
      var params = templateUrl.match(pattern);
      if (params != null && params.length > 0)
      {
         searchUrl = templateUrl;
         
         // go through the parameters and replace the searchTerms and count
         // parameters with the given values and then replace all optional
         // parameters with an empty string.
         for (var p = 0; p < params.length; p++)
         {
            var param = params[p];
            
            if (param == "{searchTerms}")
            {
               searchUrl = searchUrl.replace(param, term);
            }
            else if (param == "{count}" || param == "{count?}")
            {
               searchUrl = searchUrl.replace(param, count);
            }
            else if (param.indexOf("?") != -1)
            {
               // replace the optional parameter with ""
               searchUrl = searchUrl.replace(param, "");
            }
            else
            {
               // an unknown manadatory parameter return
               searchUrl = null;
               break;
            }
         }
      }
      
      return searchUrl;
   },

   /**
    * Renders the results for the given feed element.
    */
   renderSearchResults: function(engineId, feed)
   {
      // look up the label from the osengine registry
      var engineLabel = this.enginesById[engineId].label;
      
      // create the div to hold the results and the header bar
      var sb = [];
      sb[sb.length] = "<div id='";
      sb[sb.length] = this.id;
      sb[sb.length] = "-";
      sb[sb.length] = engineId;
      sb[sb.length] = _RESULTSET_PANEL_DIV_ID_SUFFIX;
      sb[sb.length] = "' class='osResults'>";
      sb[sb.length] = "<div class='osEngineTitle'>";
      sb[sb.length] = engineLabel;
      sb[sb.length] = "</div>";
      
      // create the actual results to display, start with the containing div
      sb[sb.length] = "<div id='";
      sb[sb.length] = this.id;
      sb[sb.length] = "-";
      sb[sb.length] = engineId;
      sb[sb.length] = _RESULTSET_LIST_DIV_ID_SUFFIX;
      sb[sb.length] = "'>";
      sb[sb.length] = this.generateResultsListHTML(feed); 
      sb[sb.length] = "</div>";
      
      // create the paging controls
      sb[sb.length] = "<div id='";
      sb[sb.length] = this.id;
      sb[sb.length] = "-";
      sb[sb.length] = engineId;
      sb[sb.length] = _RESULTSET_PAGING_DIV_ID_SUFFIX;
      sb[sb.length] = "' class='osResultsPaging'>";
      sb[sb.length] = this.generatePagingHTML(engineId, feed);
      sb[sb.length] = "</div>";
   
      // close the containing div
      sb[sb.length] = "</div>";
      
      // create a div element to hold the results
      var d = document.createElement("div");
      d.innerHTML = sb.join("");
      
      // return the div
      return d;
   },

   /**
    * Generates the HTML to display the search results from the 
    * given feed.
    */
   generateResultsListHTML: function(feed)
   {
      var isAtom = true;
      
      // if the name of the feed element is "channel" this is an RSS feed
      if (feed.tagName == "channel")
      {
         isAtom = false;
      }
         
      var results = null;
      if (isAtom)
      {
         results = feed.getElementsByTagName("entry");
      }
      else
      {
         results = feed.getElementsByTagName("item");
      }
      
      if (results == null || results.length == 0)
      {
         return "<div class='osResultNoMatch'>" + this.msgNoResults + "</div>";
      }
      else
      {
         var sb = [];
         sb[sb.length] = "<table cellpadding='0' cellspacing='0'>";
      
         for (var x = 0; x < results.length; x++)
         {
            // get the current entry
            var elResult = results[x];
            
            // get the title, icon and summary
            var title = Alfresco.Dom.getElementTextByTagName(elResult, "title");
            var icon = Alfresco.Dom.getElementTextByTagName(elResult, "icon");
            var summary = null;
            if (isAtom)
            {
               summary = Alfresco.Dom.getElementTextByTagName(elResult, "summary");
            }
            else
            {
               summary = Alfresco.Dom.getElementTextByTagName(elResult, "description");
            }
            
            // get the link href
            var link = null;
            var elLink = Alfresco.Dom.getElementByTagName(elResult, "link");
            if (elLink != null)
            {
               if (isAtom)
               {
                  link = elLink.getAttribute("href");
               }
               else
               {
                  link = Alfresco.Dom.getElementText(elLink);
               }
            }
            
            // generate the row to represent the result
            sb[sb.length] = "<tr><td valign='top'><div class='osResultIcon'>";
            if (icon != null)
            {
               sb[sb.length] = "<img src='";
               sb[sb.length] = icon;
               sb[sb.length] = "' />";
            }
            sb[sb.length] = "</div></td><td><div class='osResultTitle'>";
            if (title != null)
            {
               if (link != null)
               {
                  sb[sb.length] = "<a target='new' href='";
                  sb[sb.length] = link;
                  sb[sb.length] = "'>";
               }
               sb[sb.length] = Alfresco.Dom.encodeHTML(title);
               if (link != null)
               {
                  sb[sb.length] = "</a>";
               }
               var noderef = Alfresco.Dom.getElementTextByTagNameNS(elResult, "http://www.alfresco.org/opensearch/1.0/", "alf", "noderef");
               if (noderef != null)
               {
                  sb[sb.length] = "<span onclick=\"AlfNodeInfoMgr.toggle('" + noderef + "',this);\">";
                  sb[sb.length] = "<img src='" + getContextPath() + "/images/icons/popup.gif' class='popupImage' width='16' height='16' />";
                  sb[sb.length] = "</span>";
               }
            }
            sb[sb.length] = "</div>";
            if (summary != null)
            {
               sb[sb.length] = "<div class='osResultSummary'>";
               sb[sb.length] = Alfresco.Dom.encodeHTML(summary);
               sb[sb.length] = "</div>";
            }
            sb[sb.length] = "</td></tr>";
         }
         
         // close the table
         sb[sb.length] = "</table>";
         
         return sb.join("");
      }
   },

   /**
    * Generates the HTML to display the paging information i.e. the first, next, previous 
    * and last buttons and the position info i.e. "x - y of z".
    */
   generatePagingHTML: function(engineId, feed)
   {
      var totalResults = 0;
      var pageSize = 5;
      var startIndex = 0;
      
      // check there are results
      var elTotalResults = Alfresco.Dom.getElementByTagNameNS(feed, _OS_NS_URI, _OS_NS_PREFIX, "totalResults");
      if (elTotalResults != null)
      {
         totalResults = Alfresco.Dom.getElementText(elTotalResults);
      }
      
      // if there are no results return an empty string
      if (totalResults == 0)
      {
         return "";
      }
      
      var elStartIndex = Alfresco.Dom.getElementByTagNameNS(feed, _OS_NS_URI, _OS_NS_PREFIX, "startIndex");
      if (elStartIndex != null)
      {
         startIndex = Alfresco.Dom.getElementText(elStartIndex);
      }
      
      var elItemsPerPage = Alfresco.Dom.getElementByTagNameNS(feed, _OS_NS_URI, _OS_NS_PREFIX, "itemsPerPage");
      if (elItemsPerPage != null)
      {
         pageSize = Alfresco.Dom.getElementText(elItemsPerPage);
      }
      
      // calculate the number of pages the results span
      /*var noPages = Math.floor(totalResults / pageSize);
      var remainder = totalResults % pageSize;
      if (remainder != 0)
      {
         noPages++;
      }*/
      
      var endIndex = (Number(startIndex) + Number(pageSize)) - 1;
      if (endIndex > totalResults)
      {
         endIndex = totalResults;
      }
      
      // extract the navigation urls
      var firstUrl = null;
      var nextUrl = null;
      var previousUrl = null;
      var lastUrl = null;
      
      var links = feed.getElementsByTagName("link");
      if (links != null && links.length > 0)
      {
         for (var x = 0; x < links.length; x++)
         {
            var elNavLink = links[x];
            var linkType = elNavLink.getAttribute("rel");
            if (linkType == "first")
            {
               firstUrl = elNavLink.getAttribute("href");
            }
            else if (linkType == "next")
            {
               nextUrl = elNavLink.getAttribute("href");
            }
            else if (linkType == "previous")
            {
               previousUrl = elNavLink.getAttribute("href");
            }
            else if (linkType == "last")
            {
               lastUrl = elNavLink.getAttribute("href");
            }
         }
      }
   
      var sb = [];
   
      if (firstUrl != null)
      {
         sb[sb.length] = "<a href='#' onclick='";
         sb[sb.length] = this.id;
         sb[sb.length] = ".showPage(&quot;";
         sb[sb.length] = engineId;
         sb[sb.length] = "&quot;, &quot;";
         sb[sb.length] = firstUrl;
         sb[sb.length] = "&quot;);'><img src='";
         sb[sb.length] = getContextPath();
         sb[sb.length] = "/images/icons/FirstPage.gif' title='";
         sb[sb.length] = this.msgFirstPage;
         sb[sb.length] = "' border='0' /></a>";
      }
      else
      {
         sb[sb.length] = "<img src='";
         sb[sb.length] = getContextPath();
         sb[sb.length] = "/images/icons/FirstPage_unavailable.gif' />";
      }
      
      sb[sb.length] = "&nbsp;";
      
      if (previousUrl != null)
      {
         sb[sb.length] = "<a href='#' onclick='";
         sb[sb.length] = this.id;
         sb[sb.length] = ".showPage(&quot;";
         sb[sb.length] = engineId;
         sb[sb.length] = "&quot;, &quot;";
         sb[sb.length] = previousUrl;
         sb[sb.length] = "&quot;);'><img src='";
         sb[sb.length] = getContextPath();
         sb[sb.length] = "/images/icons/PreviousPage.gif' title='";
         sb[sb.length] = this.msgPreviousPage;
         sb[sb.length] = "' border='0' /></a>";
      }
      else
      {
         sb[sb.length] = "<img src='";
         sb[sb.length] = getContextPath();
         sb[sb.length] = "/images/icons/PreviousPage_unavailable.gif' />";
      }
      
      sb[sb.length] = "&nbsp;&nbsp;";
      sb[sb.length] = startIndex;
      sb[sb.length] = "&nbsp;-&nbsp;";
      sb[sb.length] = endIndex;
      sb[sb.length] = "&nbsp;";
      sb[sb.length] = this.msgOf;
      sb[sb.length] = "&nbsp;";
      sb[sb.length] = totalResults;
      sb[sb.length] = "&nbsp;&nbsp;";
      
      if (nextUrl != null)
      {
         sb[sb.length] = "<a href='#' onclick='";
         sb[sb.length] = this.id;
         sb[sb.length] = ".showPage(&quot;";
         sb[sb.length] = engineId;
         sb[sb.length] = "&quot;, &quot;";
         sb[sb.length] = nextUrl;
         sb[sb.length] = "&quot;);'><img src='";
         sb[sb.length] = getContextPath();
         sb[sb.length] = "/images/icons/NextPage.gif' title='";
         sb[sb.length] = this.msgNextPage;
         sb[sb.length] = "' border='0' /></a>";
      }
      else
      {
         sb[sb.length] = "<img src='";
         sb[sb.length] = getContextPath();
         sb[sb.length] = "/images/icons/NextPage_unavailable.gif' />";
      }
      
      sb[sb.length] = "&nbsp;";
      
      if (lastUrl != null)
      {
         sb[sb.length] = "<a href='#' onclick='";
         sb[sb.length] = this.id;
         sb[sb.length] = ".showPage(&quot;";
         sb[sb.length] = engineId;
         sb[sb.length] = "&quot;, &quot;";
         sb[sb.length] = lastUrl;
         sb[sb.length] = "&quot;);'><img src='";
         sb[sb.length] = getContextPath();
         sb[sb.length] = "/images/icons/LastPage.gif' title='";
         sb[sb.length] = this.msgLastPage;
         sb[sb.length] = "' border='0' /></a>";
      }
      else
      {
         sb[sb.length] = "<img src='";
         sb[sb.length] = getContextPath();
         sb[sb.length] = "/images/icons/LastPage_unavailable.gif' />";
      }
      
      return sb.join("");
   }
}

/*********************************/
/** Handlers for AJAX callbacks **/
/*********************************/

/**
 * Processes the XML search results
 */
Alfresco.OpenSearchEngine.processSearchResults = function(ajaxResponse)
{
   try
   {
      // render the results from the Ajax response
      var engineId = ajaxResponse.argument[0];
      var clientInstance = ajaxResponse.argument[1];
      var feed = ajaxResponse.responseXML.documentElement;

      // reset the search in progress flag, we do this on the 
      // first set of results as this is enough time to stop
      // the double press of the enter key
      clientInstance.searchInProgress = false;
      
      // if the name of the feed element is "rss", get the channel child element
      if (feed.tagName == "rss")
      {
         feed = Alfresco.Dom.getElementByTagName(feed, "channel");
      }
      
      var resultsDiv = clientInstance.renderSearchResults(engineId, feed);

      // create the div to hold the results and add the results
      var resultsPanel = document.getElementById(clientInstance.id + _RESULTS_DIV_ID_SUFFIX);
      if (resultsPanel != null)
      {
         // add the new results
         resultsPanel.appendChild(resultsDiv);
      }
      else
      {
         alert("Failed to final results panel, unable to render search results!");
         return;
      }
   }
   catch (e)
   {
      handleCaughtError(e);
   }
}


/**
 * Processes the search results and updates the postion, result list
 * and paging controls.
 */
Alfresco.OpenSearchEngine.processShowPageResults = function(ajaxResponse)
{
   try
   {
      // render the results from the Ajax response
      var engineId = ajaxResponse.argument[0];
      var clientInstance = ajaxResponse.argument[1];
      var feed = ajaxResponse.responseXML.documentElement;
      
      // if the name of the feed element is "rss", get the channel child element
      if (feed.tagName == "rss")
      {
         feed = Alfresco.Dom.getElementByTagName(feed, "channel");
      }
   
      // append the results list to the results list div
      var resultsListDiv = document.getElementById(clientInstance.id + "-" + 
            engineId + _RESULTSET_LIST_DIV_ID_SUFFIX);
      if (resultsListDiv != null)
      {
         resultsListDiv.innerHTML = clientInstance.generateResultsListHTML(feed);
      }
   
      // update the paging div with new urls and position info
      var pagingDiv = document.getElementById(clientInstance.id + "-" + 
            engineId + _RESULTSET_PAGING_DIV_ID_SUFFIX);
      if (pagingDiv != null)
      {
         pagingDiv.innerHTML = clientInstance.generatePagingHTML(engineId, feed);
      }
   }
   catch (e)
   {
      handleCaughtError(e);
   }
}

/**
 * Error handler for Ajax call to search engine
 */
Alfresco.OpenSearchEngine.handleSearchError = function(ajaxResponse)
{
   var engineId = ajaxResponse.argument[0];
   var clientInstance = ajaxResponse.argument[1];
   var engineLabel = clientInstance.enginesById[engineId].label;
   
   var errorMsg = clientInstance.msgFailedSearch.replace("{0}", engineLabel);
   handleCaughtError(errorMsg + ": " + ajaxResponse.status + " " + ajaxResponse.statusText);

   // reset the search in progress flag
   clientInstance.searchInProgress = false;
}
