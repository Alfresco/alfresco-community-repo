<#-- get parents -->

<#import "authority.lib.ftl" as authorityLib/>
<#import "../generic-paged-results.lib.ftl" as genericPaging />
{
	"data": [
    	<#list parents as thegroup>	
    		<@authorityLib.authorityJSON authority=thegroup />	 	   
	   	<#if thegroup_has_next>,</#if>
    	</#list>
  	]

   <@genericPaging.pagingJSON />
}
