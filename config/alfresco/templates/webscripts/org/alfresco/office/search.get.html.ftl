<#import "office.inc.ftl" as office />
<#--
   Template specific
-->
<#assign path=args.p!"">
<#if args.n??><#assign node=args.n><#else><#assign node=companyhome></#if>
<#assign extn=args.e!"doc"><#assign extnx=extn+"x">
<#assign nav=args.n!"">
<#if (args.searchagain??)><#assign searchText=args.searchagain><#else><#assign searchText=""></#if>
<#if (args.maxresults??)><#assign maxResults=args.maxresults><#else><#assign maxResults="5"></#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<#assign searchCommand="OfficeSearch.runSearch('${url.serviceContext}/office/searchResults', '${defaultQuery?js_string}')" >
<#--
   /Template specific
-->

<@office.header "search" defaultQuery>
   <script type="text/javascript" src="${url.context}/scripts/office/search.js"></script>
</@>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.search")}</div></div>
</div>

<div class="containerSearchTerms">
   <div id="nonStatusText">
      <div class="searchBox">
         <span class="searchParam">
            ${message("office.message.search_for")}
            <input type="text" id="searchText" value="${searchText}" maxlength="512" />
         </span>
         <span>
            <a id="simpleSearchButton" class="searchButton" href="#" onclick="${searchCommand}">${message("office.button.search")}</a>
         </span>
         <span class="searchParam">
            ${message("office.message.results_max.before")}
            <select id="maxResults" name="maxResults" onchange="${searchCommand}">
               <option <#if maxResults="5">selected="selected" </#if>value="5">5</option>
               <option <#if maxResults="10">selected="selected" </#if>value="10">10</option>
               <option <#if maxResults="15">selected="selected" </#if>value="15">15</option>
               <option <#if maxResults="20">selected="selected" </#if>value="20">20</option>
               <option <#if maxResults="50">selected="selected" </#if>value="50">50</option>
            </select>&nbsp;${message("office.message.results_max.after")}
         </span>
      </div>
   </div>
   
   <div id="statusText"></div>
   
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header"><span id="itemsFound">&nbsp;</span></div></div>
</div>

<div id="resultsList" class="containerSearchResults">
   <div id="searchResultsList"></div>
</div>

<#if (args.searchagain??)>
<script type="text/javascript">
   window.addEvent('domready', function(){${searchCommand}});
</script>
</#if>

<div style="position: absolute; top: 0px; left: 0px; z-index: 100; display: none">
   <iframe id="if_externalComponenetMethodCall" name="if_externalComponenetMethodCall" src="" style="visibility: hidden;" width="0" height="0"></iframe>
</div>

</body>
</html>
