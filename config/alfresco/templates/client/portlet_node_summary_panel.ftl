<#assign isImage=node.isDocument && (node.mimetype = "image/gif" || node.mimetype = "image/jpeg" || node.mimetype = "image/png")>
<#assign isVideo=node.isDocument && node.mimetype?starts_with("video/")>
<div class="summaryPopupPanel" style="background-color:#F8FCFD">
	<table cellpadding='3' cellspacing='0'>
	   <tr>
	      <td colspan='2' class='mainSubTitle'>
	         <table cellspacing='0' cellpadding='0' width='100%' style='cursor:move' id='dragable'>
	            <tr>
	               <td class='mainSubTitle'>${node.name?html}</td>
	               <#if node.isDocument>
	                  <#assign navurl="/navigate/showDocDetails/">
	               <#else>
	                  <#assign navurl="/navigate/showSpaceDetails/">
	               </#if>
	               <#assign navurl=navurl + node.nodeRef.storeRef.protocol + '/' + node.nodeRef.storeRef.identifier + '/' + node.nodeRef.id>
	               <td width=24><center><a href="${url.context}${navurl}?close=true" target="new"><img src='${url.context}/images/icons/View_details.gif' style='cursor:pointer' width=12 height=16 border=0 title='${msg("details")}' alt='${msg("details")}'></a></center></td>
	               <td width=14 align=right><img src='${url.context}/images/icons/close_panel.gif' onclick="<#if isVideo>if (document.all) document.getElementById('${node.id}_player').controls.stop();</#if>AlfNodeInfoMgr.close('${node.nodeRef}');" style='cursor:pointer' width=14 height=14 border=0 title='${msg("close")}' alt='${msg("close")}'></td>
	            </tr>
	         </table>
	      </td>
	   </tr>
	   
	   <tr>
	      <td valign="middle" align="center">
	         <#if isImage>
	            <a href="${url.context}${node.url}?close=true" target="new"><img src="${url.context}${node.url}" width=120 border=0></a>
	         <#elseif isVideo>
	            <object width="320" height="240" border="0" id="${node.id}_player" classid="CLSID:6BF52A52-394A-11d3-B153-00C04F79FAA6">
	               <param name="URL" value="${url.context}${node.url}" />
	               <param name="AutoStart" value="true" />
	               <param name="AutoSize" value="true" />
	               <embed type="application/x-mplayer2" pluginspage="http://microsoft.com/windows/mediaplayer/en/download/" src="${url.context}${node.url}?ticket=${session.ticket}" showcontrols="1" showdisplay="0" showstatusbar="0" autosize="1" autoplay="1" autoStart="1" height="240" width="320"></embed>
	            </object>
	         <#else>
	            <table cellspacing="0" cellpadding="0" border="0">
	               <tr>
	                  <td>
	                     <div style="border: 1px solid #cccccc; padding:4px">
	                        <a href="${url.context}${node.url}?close=true" target="new"><img src="${url.context}${node.icon32}" width="32" height="32" border="0"></a>
	                     </div>
	                  </td>
	                  <td><img src="${url.context}/images/parts/rightSideShadow42.gif" width="6" height="42"></td>
	               </tr>
	               <tr><td colspan="2" style="font-size: 5px;"><img src="${url.context}/images/parts/bottomShadow42.gif" width="48" height="5"></td></tr>
	            </table>
	         </#if>
	      </td>
	      
	      <td valign='top'>
	         <table cellpadding='2' cellspacing='0'>
	            <#if node.properties.title?exists>
	               <tr><td>&nbsp;${msg("title")}:</td><td>${node.properties.title?html}</td></tr>
	            </#if>
	            <#if node.properties.description?exists>
	               <tr><td>&nbsp;${msg("description")}:</td><td>${node.properties.description?html}</td></tr>
	            </#if>
	            <tr><td>&nbsp;${msg("created")}:</td><td>${node.properties.created?string(msg("date_time_pattern"))}</td></tr>
	            <tr><td>&nbsp;${msg("creator")}:</td><td>${node.properties.creator}</td></tr>
	            <tr><td>&nbsp;${msg("modified")}:</td><td>${node.properties.modified?string(msg("date_time_pattern"))}</td></tr>
	            <tr><td>&nbsp;${msg("modifier")}:</td><td>${node.properties.modifier}</td></tr>
	            <#if node.properties.owner?exists>
	               <tr><td>&nbsp;${msg("owner")}:</td><td>${node.properties.owner}</td></tr>
	            </#if>
	            <#if node.properties.author?exists>
	               <tr><td>&nbsp;${msg("author")}:</td><td>${node.properties.author?html}</td></tr>
	            </#if>
	            <#if node.isDocument>
	               <tr><td>&nbsp;${msg("size")}:</td><td>${(node.size / 1000)?string("0.##")} ${msg("kilobyte")}</td></tr>
	            </#if>
	         </table>
	      </td>
	   </tr>
	   
	   <#if node.isDocument && !isImage>
	      <#assign c=cropContent(node.properties.content, 512)>
	      <#if c?length != 0>
	         <tr>
	            <td colspan='2'>${msg("snapshot_preview")}:</td>
	         </tr>
	         <tr>
	            <td colspan='2'>
	               ${c?html?replace('$', '<br>', 'rm')}<#if (c?length >= 512)>...</#if>
	            </td>
	         </tr>
	      </#if>
	   </#if>
	</table>
</div>
