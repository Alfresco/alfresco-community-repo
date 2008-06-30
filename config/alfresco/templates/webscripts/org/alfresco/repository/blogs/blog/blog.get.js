<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">

function main()
{
	// get requested node
	var node = getRequestNode();
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}

	model.item = node;
	
	// process additional parameters
}

main();
