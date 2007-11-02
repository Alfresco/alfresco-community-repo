<script type="text/javascript" src="${url.context}/scripts/ajax/presence.js"></script>

<div class="presenceTitle">Invited Users Presence Status</div>
<div class="presenceKey">
	<img src="${url.context}/images/icons/presence_online.gif" width="16" height="16" alt="online" title="User is online">=online<br>
	<img src="${url.context}/images/icons/presence_offline.gif" width="16" height="16" alt="offline" title="User is offline">=offline<br>
	<img src="${url.context}/images/icons/presence_unknown.gif" width="16" height="16" alt="unknown" title="User status is unknown">=unknown
</div>
<div id="presenceContainer">
<#list presenceResults as pr>
	<div class="presenceUser">
		<div class="presenceStatus" rel="${pr[1]}" title=""></div>
		<div class="presenceUsername">${pr[0]}</div>
	</div>
</#list>
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

.presenceKey
{
	float: right;
}

.presenceUser
{
	float: left;
	padding: 2px;
}

#presenceContainer
{
	clear: left;
}

.presenceStatus
{
	background-image: url(${url.context}/images/icons/ajax_anim.gif);
	height: 16px;
	width: 16px;
	float: left;
	margin: 0px 4px;
}
.presenceStatus.online
{
	background-image: url(${url.context}/images/icons/presence_online.gif) !important;
}
.presenceStatus.offline
{
	background-image: url(${url.context}/images/icons/presence_offline.gif) !important;
}
.presenceStatus.unknown
{
	background-image: url(${url.context}/images/icons/presence_unknown.gif) !important;
}

.presenceUsername
{
	float: left;
	margin: 4px 0px 0px;
}
</style>
