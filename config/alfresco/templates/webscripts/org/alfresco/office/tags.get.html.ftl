<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if args.e?exists><#assign extn=args.e><#else><#assign extn="doc"></#if><#assign extnx=extn+"x">
<#if args.n?exists><#assign nav=args.n><#else><#assign nav=""></#if>
<#if args.tag?exists><#assign tag=args.tag><#else><#assign tag=""></#if>
<#-- resolve the path (from Company Home) into a node -->
<#if companyhome.childByNamePath[path]?exists>
   <#assign d=companyhome.childByNamePath[path]>
<#else>
   <#assign d=companyhome>
</#if>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
   <title>${message("office.title.document_tags")}</title>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/tags.js"></script>
</head>
<body>

<div class="tabBar">
   <ul>
      <li><a title="${message("office.title.my_alfresco")}" href="${url.serviceContext}/office/myAlfresco?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="${message("office.title.my_alfresco")}" /></span></a></li>
      <li><a title="${message("office.title.navigation")}" href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/navigator.gif" alt="${message("office.title.navigation")}" /></span></a></li>
      <li><a title="${message("office.title.search")}" href="${url.serviceContext}/office/search?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/search.gif" alt="${message("office.title.search")}" /></span></a></li>
      <li><a title="${message("office.title.document_details")}" href="${url.serviceContext}/office/documentDetails?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/document_details.gif" alt="${message("office.title.document_details")}" /></span></a></li>
      <li><a title="${message("office.title.my_tasks")}" href="${url.serviceContext}/office/myTasks?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="${message("office.title.my_tasks")}" /></span></a></li>
      <li id="current"><a title="${message("office.title.document_tags")}" href="${url.serviceContext}/office/tags?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/tag.gif" alt="${message("office.title.document_tags")}" /></span></a></li>
   </ul>
</div>

<div class="header">Tag Cloud</div>

<div class="containerMedium">
   <div id="nonStatusText">
      <div id="tagCloud"></div>
   </div>
   <div id="statusText"></div>
</div>

<div id="taggedHeader" class="header">Tagged Documents</div>

<div id="taggedContainer">
   <div id="itemsFound" class="taggedFound"></div>
   <div id="taggedList" class="containerBig"></div>
</div>

<script type="text/javascript">
   OfficeTags.setParams("${path?url}&amp;e=${extn}");
</script>
<#if (args.tag?exists)>
<script type="text/javascript">
   window.addEvent("domready", function(){OfficeTags.preselectTag("${args.tag}")});
</script>
</#if>
</body>
</html>
