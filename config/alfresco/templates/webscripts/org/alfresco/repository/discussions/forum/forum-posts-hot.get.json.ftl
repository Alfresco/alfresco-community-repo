<#import "../topicpost.lib.ftl" as topicpostLib/>
<#import "../../generic-paged-results.lib.ftl" as gen/>
<@gen.pagedResults data=data ; item>
	<@topicpostLib.topicpostJSON item=item />
</@gen.pagedResults>
