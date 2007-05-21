<div class="summaryPopupPanel" style="background-color:#F8FCFD">
	<table cellpadding='3' cellspacing='0'>
	   <tr>
	      <td colspan='2' class='mainSubTitle'>
	         <table cellspacing='0' cellpadding='0' width='100%' style='cursor:move' id='dragable'>
	            <tr>
	               <td class='mainSubTitle'>${node.name}</td>
	               <#if node.isDocument>
	                  <#assign navurl="/navigate/showDocDetails/">
	               <#else>
	                  <#assign navurl="/navigate/showSpaceDetails/">
	               </#if>
	               <#assign navurl=navurl + node.nodeRef.storeRef.protocol + '/' + node.nodeRef.storeRef.identifier + '/' + node.nodeRef.id>
	               <td width=24><center><a href='${url.context}${navurl}' target="new"><img src='${url.context}/images/icons/View_details.gif' style='cursor:pointer' width=12 height=16 border=0 title="Details" alt="Details"></a></center></td>
	               <td width=14 align=right><img src='${url.context}/images/icons/close_panel.gif' onclick="AlfNodeInfoMgr.close('${node.nodeRef}');" style='cursor:pointer' width=14 height=14 border=0 title="Close" alt="Close"></td>
	            </tr>
	         </table>
	      </td>
	   </tr>
	   
	   <tr>
	      <td valign="middle" align="center">
	         <#assign isImage=node.isDocument && (node.mimetype = "image/gif" || node.mimetype = "image/jpeg" || node.mimetype = "image/png")>
	         <#if isImage>
	            <a href="${url.context}${node.url}" target="new"><img src="${url.context}${node.url}" width=120 border=0></a>
	         <#else>
	            <table cellspacing="0" cellpadding="0" border="0">
	               <tr>
	                  <td>
	                     <div style="border: 1px solid #cccccc; padding:4px">
	                        <a href="${url.context}${node.url}" target="new"><img src="${url.context}${node.icon32}" width="32" height="32" border="0"></a>
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
	               <tr><td>&nbsp;Title:</td><td>${node.properties.title?html}</td></tr>
	            </#if>
	            <#if node.properties.description?exists>
	               <tr><td>&nbsp;Description:</td><td>${node.properties.description?html}</td></tr>
	            </#if>
	            <tr><td>&nbsp;Created:</td><td>${node.properties.created?datetime}</td></tr>
	            <tr><td>&nbsp;Creator:</td><td>${node.properties.creator}</td></tr>
	            <tr><td>&nbsp;Modified:</td><td>${node.properties.modified?datetime}</td></tr>
	            <tr><td>&nbsp;Modifier:</td><td>${node.properties.modifier}</td></tr>
	            <#if node.properties.owner?exists>
	               <tr><td>&nbsp;Owner:</td><td>${node.properties.owner}</td></tr>
	            </#if>
	            <#if node.properties.author?exists>
	               <tr><td>&nbsp;Author:</td><td>${node.properties.author}</td></tr>
	            </#if>
	            <#if node.isDocument>
	               <tr><td>&nbsp;Size:</td><td>${(node.size / 1000)?string("0.##")} KB</td></tr>
	            </#if>
	         </table>
	      </td>
	   </tr>
	   
	   <#if node.isDocument && !isImage>
	      <#assign c=cropContent(node.properties.content, 512)>
	      <#if c?length != 0>
	         <tr>
	            <td colspan='2'>Preview:</td>
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
