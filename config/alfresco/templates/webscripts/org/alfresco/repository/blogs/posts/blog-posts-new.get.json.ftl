<#import "../blogpost.lib.ftl" as blogpostLib/>
<#import "../../generic-paged-results.lib.ftl" as gen/>
<@gen.pagedResults data=data ; item>
	<@blogpostLib.blogpostJSON item=item />
</@gen.pagedResults>
