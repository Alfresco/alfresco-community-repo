<#assign isImage=node.isDocument && (node.mimetype = "image/gif" || node.mimetype = "image/jpeg" || node.mimetype = "image/png")>
<#assign isVideo=node.isDocument && node.mimetype?starts_with("video/")>

<table width="100%" cellpadding="2" cellspacing="0" border="0">
   <tr>
      <td>
         <div class="docMetapreview">
      	   <#if node.isDocument && !isImage && !isVideo>
      	      <#assign c=cropContent(node.properties.content, 512)>
      	      <#if c?length != 0>
                  ${c?html?replace('$', '<br>', 'rm')}<#if (c?length >= 512)>...</#if>
      	      </#if>
            <#elseif isImage>
	            <a href="${url.context}${node.url}" target="new"><img src="${url.context}${node.url}" width=120 border=0></a>
	         <#else>
	            TBD - Video?
      	   </#if>
         </div>
      </td>
      <td width="24"></td>
      <td width="300">
         <table width="100%" cellpadding="0" cellspacing="0">
            <tr class="">
               <td class="docAction docActionCheckout">Checkout</td>
               <td class="docAction docActionEditDetails">Edit Details</td>
            </tr>
            <tr class="">
               <td class="docAction docActionUpdate">Update</td>
               <td class="docAction docActionViewContent">View Content</td>
            </tr>
            <tr class="">
               <td class="docAction docActionDelete">Delete</td>
               <td class="docAction docActionMoreActions">More Actions...</td>
            </tr>
         </table>
      </td>
   </tr>
</table>
