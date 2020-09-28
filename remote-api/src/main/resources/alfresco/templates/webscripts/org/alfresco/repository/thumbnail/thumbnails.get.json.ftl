<#import "thumbnail.lib.ftl" as thumbnailLib/>

[
	<#list thumbnails as thumbnail>
		<@thumbnailLib.thumbnailJSON node=node thumbnailName=thumbnail.properties.thumbnailName/>
		<#if thumbnail_has_next>,</#if>
	</#list>
]