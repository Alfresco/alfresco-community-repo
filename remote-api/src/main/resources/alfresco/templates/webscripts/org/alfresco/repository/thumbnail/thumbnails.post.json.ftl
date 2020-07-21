<#import "thumbnail.lib.ftl" as thumbnailLib/>

<#if node?exists>
   <@thumbnailLib.thumbnailJSON node=node thumbnailName=thumbnailName/>
</#if>