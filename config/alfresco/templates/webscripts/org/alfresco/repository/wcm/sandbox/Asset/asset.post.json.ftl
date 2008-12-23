<#import "asset.lib.ftl" as assetLib/>
{
  data:	[ 
    	  <#list assets as asset>
    	    <@assetLib.assetJSON asset=asset />   
    	      <#if asset_has_next>,</#if>	 	   
          </#list>
	]
}


