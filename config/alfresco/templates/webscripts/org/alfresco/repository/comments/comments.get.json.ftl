<#import "comment.lib.ftl" as commentLib/>
<#import "../generic-paged-results.lib.ftl" as gen/>
<@gen.pagedResults data=data ; item>
	<@commentLib.commentJSON item=item />
</@gen.pagedResults>
