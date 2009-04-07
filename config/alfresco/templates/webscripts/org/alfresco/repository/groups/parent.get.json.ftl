<#-- get parents -->

<#import "authority.lib.ftl" as authorityLib/>
{
	"data": [
    	<#list parents as thegroup>	
    		<@authorityLib.authorityJSON authority=thegroup />	 	   
	   	<#if thegroup_has_next>,</#if>
    	</#list>
  	]
}