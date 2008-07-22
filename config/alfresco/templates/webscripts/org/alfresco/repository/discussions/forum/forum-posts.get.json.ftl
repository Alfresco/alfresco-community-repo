<#import "../post.lib.ftl" as postLib/>
<#import "../../generic-paged-results.lib.ftl" as gen/>
<@gen.pagedResults data=data ; item>
	<@postLib.postJSON postData=item />
</@gen.pagedResults>

