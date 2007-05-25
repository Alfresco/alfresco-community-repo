<#assign isImage=node.isDocument && (node.mimetype = "image/gif" || node.mimetype = "image/jpeg" || node.mimetype = "image/png")>
<table width="100%" cellpadding="2" cellspacing="0" border="0">
   <tr>
      <td>
         <div class="spacePreview">
      	   <#if node.isDocument && !isImage>
      	      <#assign c=cropContent(node.properties.content, 512)>
      	      <#if c?length != 0>
                  ${c?html?replace('$', '<br>', 'rm')}<#if (c?length >= 512)>...</#if>
      	      </#if>
            <#elseif isImage>
	            <center><a href="${url.context}${node.url}" target="new"><img src="${url.context}${node.url}" height=140 border=0></a></center>
	         <#elseif node.isContainer>
	            <#list node.children?sort_by('name') as c>
	               <#--if (c_index >= 25)><div>...</div><#break></#if>-->
	               <#if c.isDocument || c.isContainer>
	               <div style="padding:2px"><a class="childSpaceLink" onclick="event.cancelBubble=true;" href="${url.context}${c.url}" target="new"><img class="spaceImageIcon" src="${url.context}${c.icon16}" border="0">${c.name}</a></div>
	               </#if>
	            </#list>
      	   </#if>
         </div>
      </td>
      <td width="24"></td>
      <td width="300">
         <table width="100%" cellpadding="0" cellspacing="0">
            <#if node.isDocument>
            <tr>
               <td class="spaceAction docActionCheckout">Checkout</td>
               <td class="spaceAction docActionEditDetails">Edit Details</td>
            </tr>
            <tr>
               <td class="spaceAction docActionUpdate">Update</td>
               <td class="spaceAction docActionViewContent">View Content</td>
            </tr>
            <tr>
               <td class="spaceAction docActionDelete">Delete</td>
               <td class="spaceAction docActionMoreActions">More Actions...</td>
            </tr>
            <#else>
            <tr>
               <td class="spaceAction spaceActionEditDetails">Edit Details</td>
               <td class="spaceAction spaceActionDelete">Delete</td>
            </tr>
            <tr>
               <td class="spaceAction spaceActionMoreActions">More Actions...</td>
               <td></td>
            </tr>
            </#if>
         </table>
      </td>
   </tr>
</table>
