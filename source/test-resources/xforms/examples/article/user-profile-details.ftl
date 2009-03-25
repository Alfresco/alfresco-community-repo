<#ftl ns_prefixes={"D":"http://www.alfresco.org/alfresco/article","xsi":"http://www.w3.org/2001/XMLSchema-instance"}>
<style>
	
	.user_profile {
		background: #fff;
		border: 1px solid #e2e1e1;
		position: relative;
		padding:10px;
	}
	
	.user_profile_picture {
	    display: block;
	    float: left;
		width: 80px;
		margin: 2px;
	}

	.user_profile_name {
	}

</style>

<div class="user_profile">
	<#if user_profile[0]??>
		<#if user_profile.picture[0]??>
			<img src="${user_profile.picture}" class="user_profile_picture"/>
		</#if>
		<div class="user_profile_name">
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
	</#if>
</div>

