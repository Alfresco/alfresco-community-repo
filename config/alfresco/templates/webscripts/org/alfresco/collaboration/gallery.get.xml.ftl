
<gallery>
	<name>${gallery.name}</name>			
		<#list gallery.children as image>	
			<#if image.isDocument>			
				<image>
					<url>${absurl(url.context)}${image.url}</url>	
					<#if image.properties["cm:title"]?exists>					
						<title>${image.properties["cm:title"]}</title>
					<#else>
						<title/>
					</#if>
					<#if image.properties["cm:description"]?exists>					
						<description>${image.properties["cm:description"]}</description>
					<#else>
						<description/>
					</#if>
				</image>
			</#if>
		</#list>								
</gallery>	
