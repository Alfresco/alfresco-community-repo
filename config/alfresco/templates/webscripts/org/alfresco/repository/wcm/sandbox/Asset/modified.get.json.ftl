<#import "asset.lib.ftl" as assetLib/>
{
  data:	[ 
    	  <#list assets as asset>
    	    <@assetLib.assetJSON asset=asset depth=0 />   
    	      <#if asset_has_next>,</#if>	 	   
          </#list>
	]
}


