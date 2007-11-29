<#assign maxRecentDocs = 5>
<script type="text/javascript" src="${url.context}/scripts/ajax/presence.js"></script>

<div class="presenceTitle">Colleague Status</div>
<div id="presenceKeyContainer">
	<div class="presenceKeyTitle">Presence Status Key:</div>
	<div class="presenceKey">
		<img src="${url.context}/images/icons/presence_skype_online.png" width="16" height="16" alt="Skype online" title="Skype online">=Skype online<br>
		<img src="${url.context}/images/icons/presence_yahoo_online.png" width="16" height="16" alt="Yahoo online" title="Yahoo online">=Yahoo online<br>
		<img src="${url.context}/images/icons/presence_status_none.png" width="16" height="16" alt="Not configured" title="Not configured">=not configured
	</div>
	<div class="presenceKey">
		<img src="${url.context}/images/icons/presence_skype_offline.png" width="16" height="16" alt="Skype offline" title="Skype offline">=offline<br>
		<img src="${url.context}/images/icons/presence_yahoo_offline.png" width="16" height="16" alt="Yahoo offline" title="Yahoo offline">=offline<br>
		<img src="${url.context}/images/icons/presence_status_unknown.png" width="16" height="16" alt="Unknown" title="Status possibly blocked due to privacy settings">=unknown
	</div>
</div>

<div id="presenceContainer">
<#list presenceResults as pr>
	<div class="presenceUser">
		<div class="presenceStatus" rel="${pr[2]}" title=""></div>
		<div class="presenceUsername">${pr[1]}</div>
	</div>
</#list>
</div>

<div id="recentDocsContainer">
	<div class="recentDocsTitle">Recently created or modified documents<#if (maxRecentDocs != -1)><span class="recentDocsTitleExtra">(showing ${maxRecentDocs} max.)</span></#if></div>
	<div id="recentDocsTable">
		<table cellspacing="1" cellpadding="2" border="0">
		   <tr>
	   	   <td></td>
		      <td><b>Name</b></td>
	      	<td><b>Created Date</b></td>
	      	<td><b>Modified Date</b></td>
	   	</tr>
	<#assign rowNum=0>
	<#list space.childrenByXPath[".//*[subtypeOf('cm:content')]"]?sort_by(['properties','cm:modified'])?reverse as child>
	   <#if (dateCompare(child.properties["cm:modified"], date, 1000*60*60*24*7) == 1) || (dateCompare(child.properties["cm:created"], date, 1000*60*60*24*7) == 1)>
	   	<#assign rowNum = rowNum + 1>
	   	<#if ((maxRecentDocs != -1) && (rowNum > maxRecentDocs))>
	   		<#break>
	   	</#if>
	      <tr class="recentDoc ${(rowNum % 2 = 0)?string("even", "odd")}">
	         <td><a href="${url.context}${child.url}" target="new"><img src="${url.context}${child.icon16}" alt="*" border="0"></a></td>
	         <td>&nbsp;<a href="${url.context}${child.url}" target="new">${child.properties.name}</a>&nbsp;&nbsp;</td>
	         <td>&nbsp;&nbsp;${child.properties["cm:created"]?datetime}&nbsp;&nbsp;</td>
	         <td>&nbsp;&nbsp;${child.properties["cm:modified"]?datetime}&nbsp;&nbsp;</td>
	      </tr>
	   </#if>
	</#list>
		</table>
	</div>
</div>

<style>
.presenceTitle
{
	font-family: "Trebuchet MS", Verdana, Helvetica, sans-serif;
	font-size: medium;
	font-weight: bold;
	margin: -8px 0px 4px;
	float: left;
}

#presenceKeyContainer
{
	float: right;
	border: 1px dashed grey;
	padding: 4px;
}

.presenceKeyTitle
{
	padding: 0px 0px 4px 0px;
	font-weight: bold;
}

.presenceKey
{
	float: left;
	margin: 0px 2px 0px 4px;
}

.presenceUser
{
	float: left;
	padding: 2px;
	width: 150px;
}

#presenceContainer
{
	clear: left;
}

.presenceProvider, .presenceStatus
{
	background-image: url(${url.context}/images/icons/ajax_anim.gif);
	height: 16px;
	width: 16px;
	float: left;
	margin: 0px 4px;
}

.presenceStatus.skype-online
{
	background-image: url(${url.context}/images/icons/presence_skype_online.png) !important;
}
.presenceStatus.skype-offline
{
	background-image: url(${url.context}/images/icons/presence_skype_offline.png) !important;
}
.presenceStatus.yahoo-online
{
	background-image: url(${url.context}/images/icons/presence_yahoo_online.png) !important;
}
.presenceStatus.yahoo-offline
{
	background-image: url(${url.context}/images/icons/presence_yahoo_offline.png) !important;
}
.presenceStatus.unknown, .presenceStatus.skype-unknown, .presenceStatus.yahoo-unknown
{
	background-image: url(${url.context}/images/icons/presence_status_unknown.png) !important;
}
.presenceStatus.none
{
	background-image: url(${url.context}/images/icons/presence_status_none.png) !important;
}

.presenceUsername
{
	float: left;
	margin: 4px 0px 0px;
}

.recentDocsTitle
{
	font-family: "Trebuchet MS", Verdana, Helvetica, sans-serif;
	font-size: medium;
	font-weight: bold;
	margin: 0px 0px 4px;
	float: left;
}

.recentDocsTitleExtra
{
   font-size: small;
   font-style: italic;
   margin-left: 0.5em;
}

#recentDocsContainer
{
	float: left;
	clear: left;
	margin-top: 32px;
}

#recentDocsTable
{
	float: left;
	clear: both;
}

.recentDoc.even
{
	background-color: #ffffff;
}
.recentDoc.odd
{
	background-color: #eeeeee;
}
</style>
