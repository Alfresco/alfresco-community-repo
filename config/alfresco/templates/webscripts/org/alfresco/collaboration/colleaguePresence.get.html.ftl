<script type="text/javascript" src="${url.context}/scripts/ajax/project_presence.js"></script>

<div class="collabHeader">
   <span>
      <img id="refreshColleagues" src="${url.context}/images/icons/reset.gif" align="top" alt="Refresh">
      Colleagues Online
   </span>
</div>
<div class="colleagueList" rel="${colleaguePresence.colleagues?size}">
   <div id="colleaguesOnline">
   </div>
   <div id="colleaguesNotOnline">
<#list colleaguePresence.colleagues?keys as key>
   <#assign c = colleaguePresence.colleagues[key]>
      <div class="colleagueRow"<#if c.nodeRef = colleaguePresence.self.nodeRef>rel="self"</#if>>
         <div class="colleagueAvatar">
   <#if (c.assocs["cm:avatar"]?exists)>
      <#assign avatarURL = c.assocs["cm:avatar"][0].url>
   <#else>
      <#assign avatarURL = "images/icons/default_avatar.png">
   </#if>
            <img src="${url.context}/${avatarURL}" height="48" width="48" alt="avatar">
            <div class="colleaguePresence" rel="${c.properties["presenceProvider"]!""}|${c.properties["presenceUsername"]!""}">
            </div>
         </div>
         <div class="colleagueDetails">
            <div class="colleagueName">${c.properties["firstName"]!""} ${c.properties["lastName"]!""}</div>
            <div class="colleagueDetail">${c.properties["jobtitle"]!""}</div>
            <div class="colleagueDetail">${c.properties["location"]!""}</div>
         </div>
      </div>
</#list>
   </div>
</div>
<div class="collabFooter">
   <span>&nbsp;</span>
</div>

<style>
/* Colleague Status */
#refreshColleagues {
   cursor: pointer;
}

.colleagueList {
   border-left: 1px solid #CACFD3;
   border-right: 1px solid #CACFD3;
   height: 297px;
   overflow-x: hidden;
   overflow-y: scroll;
}

#colleaguesOnline {
   float: left;
}
#colleaguesNotOnline {
   float: left;
}

.colleagueRow {
   clear: both;
   float: left;
   min-height: 56px;
   padding: 8px 8px 0px;
   width: 208px;
}

.colleagueAvatar {
   float: left;
   position: relative;
   height: 48px;
   width: 48px;
   padding-right: 8px;
}

.colleaguePresence {
	background-image: url(${url.context}/images/icons/ajax_anim.gif);
   position: absolute;
   left: 32px;
   top: 32px;
   height: 16px;
   width: 16px;
}

.colleaguePresence.skype-online
{
	background-image: url(${url.context}/images/icons/presence_skype_online.png) !important;
}
.colleaguePresence.skype-offline
{
	background-image: url(${url.context}/images/icons/presence_skype_offline.png) !important;
}
.colleaguePresence.yahoo-online
{
	background-image: url(${url.context}/images/icons/presence_yahoo_online.png) !important;
}
.colleaguePresence.yahoo-offline
{
	background-image: url(${url.context}/images/icons/presence_yahoo_offline.png) !important;
}
.colleaguePresence.unknown, .colleaguePresence.skype-unknown, .colleaguePresence.yahoo-unknown
{
	background-image: url(${url.context}/images/icons/presence_status_unknown.png) !important;
}
.colleaguePresence.none
{
	background-image: url(${url.context}/images/icons/presence_status_none.png) !important;
}

.colleagueDetails {
}

.colleagueName {
   font-weight: bold;
}

.colleagueDetail {
}
</style>
