//
// Alfresco OpenSearch library
// Gavin Cornwell 09-01-2007
//
// NOTE: This script relies on common.js so therefore needs to be loaded
//       prior to this one on the containing HTML page.

var _OS_NS_PREFIX = "opensearch";
var _OS_NS_URI = "http://a9.com/-/spec/opensearch/1.1/";

var _searchTermFieldId = null;
var _pageSizeFieldId = null;

var _resultsDivId = "os-results";
var _optionsDivId = "os-options";
var _resultSetPanelId = "-osresults-panel";
var _resultSetListId = "-osresults-list";
var _resultSetPositionId = "-osresults-position";
var _resultSetPagingId = "-osresults-paging";

var _engineEnabledId = "-engine-enabled";
var _engines = [];
var _enginesById = [];

/**
 * Define an object to hold the definition of an OpenSearch engine
 */
function OpenSearchEngine(id, label, url)
{
   this.id = id;
   this.label = label;
   this.url = url;
}

/**
 * Sets the field id of the search term input control
 */
function setSearchTermFieldId(id)
{
   _searchTermFieldId = id;
}

/**
 * Sets the field id of the page size input control
 */
function setPageSizeFieldId(id)
{
   _pageSizeFieldId = id;
}

/**
 * Registers an OpenSearch engine to be called when performing queries
 */
function registerOpenSearchEngine(id, label, url)
{
   var se = new OpenSearchEngine(id, label, url);
   _engines[_engines.length] = se;
   _enginesById[id] = se;
}


/**
 * Handles the key press event, if ENTER is pressed execute the query
 */
function handleKeyPress(e)
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
      executeQuery();
      return false;
   }
   
   return true;
}

/**
 * Toggles the visibility of the options panel
 */
function toggleOptions(icon)
{
   var currentState = icon.className;
   var optionsDiv = document.getElementById(_optionsDivId);
   
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
}

/**
 * Executes the query against all the registered and selected opensearch engines
 */
function executeQuery()
{
   // gather the required parameters
   var term = document.getElementById(_searchTermFieldId).value;
   var count = document.getElementById(_pageSizeFieldId).value;
   
   // default the count if its invalid
   if (count.length == 0 || isNaN(count))
   {
      count = 5;
   }
   
   // issue the queries if there is enough search criteria
   if (term != null && term.length > 1)
   {
      // issue the search request for each enabled engine
      for (var e = 0; e < _engines.length; e++)
      {
         // get the checkbox for the current engine
         var ose = _engines[e];
         var engCheckbox = document.getElementById(ose.id + _engineEnabledId);
         if (engCheckbox != null && engCheckbox.checked)
         {
            issueSearchRequest(ose, term, count);
         }
      }
   }
}

/**
 * Issues an Ajax request for the given OpenSearchEngine
 * using the given search term and page size.
 */
function issueSearchRequest(ose, term, pageSize)
{
   // generate the search url
   var searchUrl = generateSearchUrl(ose.url, term, pageSize);
   
   // issue the request
   if (searchUrl != null)
   {
      YAHOO.util.Connect.asyncRequest("GET", searchUrl, 
         { 
            success: processSearchResults,
            failure: handleSearchError,
            argument: [ose.id]
         }, 
         null);
   }
   else
   {
      handleErrorYahoo("Failed to generate url for search engine '" + ose.label + 
            "'.\n\nThis is probably caused by missing required parameters, check the template url for the search engine.");
   }
}

/**
 * Generates a concrete url for the given template url and parameters.
 *
 * All parameters (inside { and }) have to be replaced. We only need to populate
 * the 'searchTerms' and 'count' parameters, all optional ones will use the 
 * empty string. If there is a mandatory parameter present (other than searchTerms
 * and count) null will be returned.
 */
function generateSearchUrl(templateUrl, term, count)
{
   var searchUrl = null;
   
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
}

/**
 * Processes the XML search results
 */
function processSearchResults(ajaxResponse)
{
   try
   {
      // render the results from the Ajax response
      var engineId = ajaxResponse.argument[0];
      var feed = ajaxResponse.responseXML.documentElement;
      
      // if the name of the feed element is "rss", get the channel child element
      if (feed.tagName == "rss")
      {
         feed = getElementByTagName(feed, "channel");
      }
      
      var resultsDiv = renderSearchResults(engineId, feed);

      // create the div to hold the results and add the results
      var resultsPanel = document.getElementById(_resultsDivId);
      if (resultsPanel != null)
      {
         // first remove any existing results
         while (resultsPanel.firstChild) 
         {
            resultsPanel.removeChild(resultsPanel.firstChild);
         };
         
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
      handleError(e);
   }
}

/**
 * Renders the results for the given feed element.
 */
function renderSearchResults(engineId, feed)
{
   // look up the label from the osengine registry
   var engineLabel = _enginesById[engineId].label;
   
   // create the div to hold the results and the header bar
   var sb = [];
   sb[sb.length] = "<div id='";
   sb[sb.length] = engineId;
   sb[sb.length] = _resultSetPanelId;
   sb[sb.length] = "' class='osResults'>";
   sb[sb.length] = "<div class='osEngineTitle'><table cellpadding='0' cellspacing='0' width='100%'>";
   sb[sb.length] = "<tr><td class='osEngineTitleText'>";
   sb[sb.length] = engineLabel;
   sb[sb.length] = "</td><td id='";
   sb[sb.length] = engineId;
   sb[sb.length] = _resultSetPositionId;
   sb[sb.length] = "' class='osResultsPosition'>";
   sb[sb.length] = generatePostionHTML(feed);
   sb[sb.length] = "</td></tr></table></div>";
   
   // create the actual results to display, start with the containing div
   sb[sb.length] = "<div id='";
   sb[sb.length] = engineId;
   sb[sb.length] = _resultSetListId;
   sb[sb.length] = "'>";
   sb[sb.length] = generateResultsListHTML(feed); 
   sb[sb.length] = "</div>";
   
   // create the paging controls
   sb[sb.length] = "<div id='";
   sb[sb.length] = engineId;
   sb[sb.length] = _resultSetPagingId;
   sb[sb.length] = "' class='osResultsPaging'>";
   sb[sb.length] = generatePagingHTML(engineId, feed);
   sb[sb.length] = "</div>";

   // close the containing div
   sb[sb.length] = "</div>";
   
   // create a div element to hold the results
   var d = document.createElement("div");
   d.innerHTML = sb.join("");
   
   // return the div
   return d;
}

/**
 * Shows another page of the current search results for the
 * given engineId
 */
function showPage(engineId, url)
{
   // execute the query and process the results
   YAHOO.util.Connect.asyncRequest("GET", url, 
      { 
         success: processShowPageResults,
         failure: handleSearchError,
         argument: [engineId]
      }, 
      null);
}

/**
 * Processes the search results and updates the postion, result list
 * and paging controls.
 */
function processShowPageResults(ajaxResponse)
{
   try
   {
      // render the results from the Ajax response
      var engineId = ajaxResponse.argument[0];
      var feed = ajaxResponse.responseXML.documentElement;
      
      // if the name of the feed element is "rss", get the channel child element
      if (feed.tagName == "rss")
      {
         feed = getElementByTagName(feed, "channel");
      }
      
      // find the position div and update the count
      var positionDiv = document.getElementById(engineId + _resultSetPositionId);
      if (positionDiv != null)
      {
         positionDiv.innerHTML = generatePostionHTML(feed);
      }
   
      // append the results list to the results list div
      var resultsListDiv = document.getElementById(engineId + _resultSetListId);
      if (resultsListDiv != null)
      {
         resultsListDiv.innerHTML = generateResultsListHTML(feed);
      }
   
      // update the paging div with new urls
      var pagingDiv = document.getElementById(engineId + _resultSetPagingId);
      if (pagingDiv != null)
      {
         pagingDiv.innerHTML = generatePagingHTML(engineId, feed);
      }
   }
   catch (e)
   {
      handleError(e);
   }
}

/**
 * Generates the HTML required to display the current position i.e. "x - y of z".
 */
function generatePostionHTML(feed)
{
   var totalResults = 0;
   var pageSize = 5;
   var startIndex = 0;
   
   // extract position information from results
   var elTotalResults = getElementByTagNameNS(feed, _OS_NS_URI, _OS_NS_PREFIX, "totalResults");
   if (elTotalResults != null)
   {
      totalResults = getElementText(elTotalResults);
   }
   
   // if there are no results just return an empty string
   if (totalResults == 0)
   {
      return "";
   }
   
   var elStartIndex = getElementByTagNameNS(feed, _OS_NS_URI, _OS_NS_PREFIX, "startIndex");
   if (elStartIndex != null)
   {
      startIndex = getElementText(elStartIndex);
   }
   
   var elItemsPerPage = getElementByTagNameNS(feed, _OS_NS_URI, _OS_NS_PREFIX, "itemsPerPage");
   if (elItemsPerPage != null)
   {
      pageSize = getElementText(elItemsPerPage);
   }
   
   // calculate the number of pages the results span
   /*var noPages = Math.floor(totalResults / pageSize);
   var remainder = totalResults % pageSize;
   if (remainder != 0)
   {
      noPages++;
   }*/
   
   // calculate the endIndex for this set of results
   var endIndex = (Number(startIndex) + Number(pageSize)) - 1;
   if (endIndex > totalResults)
   {
      endIndex = totalResults;
   }
   
   // make sure the startIndex is correct
   if (totalResults == 0)
   {
      startIndex = 0;
   }
   
   var sb = [];
   sb[sb.length] = startIndex;
   sb[sb.length] = "&nbsp;-&nbsp;";
   sb[sb.length] = endIndex;
   sb[sb.length] = "&nbsp;of&nbsp;";
   sb[sb.length] = totalResults;
   
   return sb.join("");    
}

/**
 * Generates the HTML to display the search results from the 
 * given feed.
 */
function generateResultsListHTML(feed)
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
      return "<div class='osResultNoMatch'>No results</div>";
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
         var title = getElementTextByTagName(elResult, "title");
         var icon = getElementTextByTagName(elResult, "icon");
         var summary = null;
         if (isAtom)
         {
            summary = getElementTextByTagName(elResult, "summary");
         }
         else
         {
            summary = getElementTextByTagName(elResult, "description");
         }
         
         // get the link href
         var link = null;
         var elLink = getElementByTagName(elResult, "link");
         if (elLink != null)
         {
            if (isAtom)
            {
               link = elLink.getAttribute("href");
            }
            else
            {
               link = getElementText(elLink);
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
               sb[sb.length] = "<a target='_new' href='";
               sb[sb.length] = link;
               sb[sb.length] = "'>";
            }
            sb[sb.length] = title;
            if (link != null)
            {
               sb[sb.length] = "</a>";
            }
         }
         sb[sb.length] = "</div><div class='osResultSummary'>";
         if (summary != null)
         {
            sb[sb.length] = summary;
         }
         sb[sb.length] = "</div></td></tr>";
      }
      
      // close the table
      sb[sb.length] = "</table>";
      
      return sb.join("");
   }
}

/**
 * Generates the HTML to display the paging links i.e. first, next, previous and last.
 */
function generatePagingHTML(engineId, feed)
{
   // check there are results
   var totalResults = 0;
   var elTotalResults = getElementByTagNameNS(feed, _OS_NS_URI, _OS_NS_PREFIX, "totalResults");
   if (elTotalResults != null)
   {
      totalResults = getElementText(elTotalResults);
   }
   
   // if there are no results return an empty string
   if (totalResults == 0)
   {
      return "";
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
      sb[sb.length] = "<a href='#' onclick='showPage(&quot;";
      sb[sb.length] = engineId;
      sb[sb.length] = "&quot;, &quot;";
      sb[sb.length] = firstUrl;
      sb[sb.length] = "&quot;);'>first</a> | ";
   }
   if (previousUrl != null)
   {
      sb[sb.length] = "<a href='#' onclick='showPage(&quot;";
      sb[sb.length] = engineId;
      sb[sb.length] = "&quot;, &quot;";
      sb[sb.length] = previousUrl;
      sb[sb.length] = "&quot;);'>previous</a>";
      if (nextUrl != null)
      {
         sb[sb.length] = " | ";
      }
   }
   if (nextUrl != null)
   {
      sb[sb.length] = "<a href='#' onclick='showPage(&quot;";
      sb[sb.length] = engineId;
      sb[sb.length] = "&quot;, &quot;";
      sb[sb.length] = nextUrl;
      sb[sb.length] = "&quot;);'>next</a> | ";
   }
   if (lastUrl != null)
   {
      sb[sb.length] = "<a href='#' onclick='showPage(&quot;";
      sb[sb.length] = engineId;
      sb[sb.length] = "&quot;, &quot;";
      sb[sb.length] = lastUrl;
      sb[sb.length] = "&quot;);'>last</a>";
   }
   
   return sb.join("");
}

/**
 * Error handler for errors caught in a catch block
 */
function handleError(o)
{
   var msg = null;
      
   if (e.message)
   {
      msg = e.message;
   }
   else
   {
      msg = e;
   }
   
   alert("Error occurred processing search results: " + msg);
}

/**
 * Error handler for Ajax call to search engine
 */
function handleSearchError(o)
{
   // TODO: find out which search engine results could not be found!

   handleErrorYahoo("Error: Failed to retrieve search results");
}

/**
 * Error handler for Ajax call to initialise component
 */
function handleInitError(o)
{
   handleErrorYahoo("Failed to initialise OpenSearch component: " + 
         o.status + " " + o.statusText);
}
