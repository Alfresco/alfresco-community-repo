<#assign isImage=node.isDocument && (node.mimetype = "image/gif" || node.mimetype = "image/jpeg" || node.mimetype = "image/png")>
<table width="690" cellpadding="2" cellspacing="0" border="0" onclick="event.cancelBubble=true;">
   <tr>
      <td>
         <div class="spacePreview">
      	   <#if node.isDocument && !isImage>
      	      <#assign c=cropContent(node.properties.content, 2048)>
      	      <#if c?length != 0>
                  ${c?html?replace('$', '<br>', 'rm')}<#if (c?length >= 2048)>...</#if>
               <#else>
                  Sorry, no preview currently available for this document.
      	      </#if>
            <#elseif isImage>
	            <center><a href="${url.context}${node.url}" target="new"><img src="${url.context}${node.url}?${node.size}" height=140 border=0></a></center>
	         <#elseif node.isContainer>
	            <#list node.children?sort_by('name') as c>
	               <#--if (c_index >= 25)><div>...</div><#break></#if>-->
	               <#if c.isDocument || c.isContainer>
	               <div style="padding:2px"><a class="childSpaceLink" href="${url.context}${c.url}" target="new"><img class="spaceImageIcon" src="${url.context}${c.icon16}" border="0">${c.name}</a></div>
	               </#if>
	            </#list>
      	   </#if>
         </div>
      </td>
      <td width="24"></td>
      <td width="300">
         <table width="100%" cellpadding="0" cellspacing="0">
<#assign navurl='/navigate/showDocDetails/' + node.nodeRef.storeRef.protocol + '/' + node.nodeRef.storeRef.identifier + '/' + node.nodeRef.id>
<#if node.isDocument>
            <tr>
<#if node.isLocked >
               <td class="spaceAction docActionCheckout docActionLocked">(Locked)</td>
<#elseif hasAspect(node, "cm:workingcopy") == 1>
               <td class="spaceAction docActionCheckin" <#if node.hasPermission("CheckIn")>onclick='event.cancelBubble=true;MySpaces.checkinItem("${node.name}", "${node.nodeRef}");'</#if>>Check In</td>
<#else>
               <td class="spaceAction docActionCheckout" <#if node.hasPermission("CheckOut")>onclick='event.cancelBubble=true;MySpaces.checkoutItem("${node.name}", "${node.nodeRef}");'</#if>>Check Out</td>
</#if>
<#if node.isLocked >
               <td class="spaceAction docActionEditDetails docActionLocked">Edit Details</td>
<#else>
               <td class="spaceAction docActionEditDetails" onclick="window.open('${url.context}/command/ui/editcontentprops?container=plain&amp;noderef=${node.nodeRef}', '_blank');">Edit Details</td>
</#if>
            </tr>
            <tr>
<#if node.isLocked >
               <td class="spaceAction docActionUpdate docActionLocked">Update</td>
<#else>
               <td class="spaceAction docActionUpdate" onclick="event.cancelBubble=true;MySpaces.updateItem(this, '${node.nodeRef}');">Update</td>
</#if>
               <td class="spaceAction docActionViewContent" onclick="window.open('${url.context}${node.downloadUrl}', '_blank');">View Content</td>
            </tr>
            <tr>
               <td class="spaceAction docActionDelete" <#if node.hasPermission("Delete")>onclick='event.cancelBubble=true;MySpaces.deleteItem("${node.name}", "${node.nodeRef}");'</#if>>Delete</td>
               <td class="spaceAction docActionMoreActions" onclick="window.open('${url.context}${navurl}', '_blank');">More Actions...</td>
            </tr>
<#else>
            <tr>
               <td class="spaceAction docActionEditDetails" onclick="window.open('${url.context}/command/ui/editcontentprops?container=plain&amp;noderef=${node.nodeRef}', '_blank');">Edit Details</td>
               <td class="spaceAction spaceActionDelete" <#if node.hasPermission("Delete")>onclick='event.cancelBubble=true;MySpaces.deleteItem("${node.name}", "${node.nodeRef}");'</#if>>Delete</td>
            </tr>
            <tr>
               <td class="spaceAction docActionMoreActions" onclick="window.open('${url.context}${navurl}', '_blank');">More Actions...</td>
               <td></td>
            </tr>
</#if>
         </table>
      </td>
   </tr>
</table>
