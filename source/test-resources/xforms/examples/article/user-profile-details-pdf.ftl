<#ftl ns_prefixes={"D":"http://www.alfresco.org/alfresco/article","xsi":"http://www.w3.org/2001/XMLSchema-instance","alf":"http://www.alfresco.org"}>

<table>
	<tr>
	<#if user_profile[0]??>
		<#if user_profile.picture[0]??>
			<td>
				<img src="${alf.avm_sandbox_url}${user_profile.picture}" width="100"/>
			</td>
		</#if>
		<td>
			<div>
				${user_profile.name.prefix}. ${user_profile.name.first} ${user_profile.name.last}
			</div>
			<div>
				${user_profile.bio}
			</div>
			<div>
				<#if user_profile.address[0]??>
				<b>Address: </b>
					<#list user_profile.address.street as street>
						${street}&nbsp;
					</#list>
					&nbsp;${user_profile.address.city}, ${user_profile.address.state} ${user_profile.address.zip}
				</#if>
			</div>
			<div>
				<#if user_profile.email[0]??>
					<b>Email: </b>${user_profile.email}
				</#if>
			</div>
		</td>
	</#if>
	</tr>
</table>

