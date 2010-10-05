<#import "office.inc.ftl" as office />
<#--
   Template specific
-->
<#assign path=args.p!"">
<#assign extn=args.e!"doc"><#assign extnx=extn+"x">
<#assign nav=args.n!"">
<#if args.tag??><#assign tag=args.tag><#else><#assign tag=""></#if>
<#-- resolve the path (from Company Home) into a node -->
<#if companyhome.childByNamePath[path]??>
   <#assign d=companyhome.childByNamePath[path]>
<#else>
   <#assign d=companyhome>
</#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<#--
   /Template specific
-->

<@office.header "document_tags" defaultQuery>
   <script type="text/javascript" src="${url.context}/scripts/office/tags.js"></script>
</@>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.tag_cloud")}</div></div>
</div>

<div class="containerMedium">
   <div id="nonStatusText">
      <div id="tagCloud"></div>
   </div>
   <div id="statusText"></div>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div id="taggedHeader" class="header">${message("office.header.tagged")}</div></div>
</div>

<div id="taggedContainer">
   <div id="itemsFound" class="taggedFound"></div>
   <div id="taggedList" class="containerBig"></div>
</div>

<div style="position: absolute; top: 0px; left: 0px; z-index: 100; display: none">
   <iframe id="if_externalComponenetMethodCall" name="if_externalComponenetMethodCall" src="" style="visibility: hidden;" width="0" height="0"></iframe>
</div>

<#if (args.tag??)>
<script type="text/javascript">
   window.addEvent("domready", function()
   {
      OfficeTags.preselectTag("${args.tag}")
   });
</script>
</#if>
</body>
</html>
