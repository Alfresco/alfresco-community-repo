<#-- get children -->

<#import "authority.lib.ftl" as authorityLib/>
{
	"data": [
	
	    <#list children as wibble>	
    		<@authorityLib.authorityJSON authority=wibble />	 	   
	   		<#if wibble_has_next>,</#if>
    	</#list>
    	
  	]
}
