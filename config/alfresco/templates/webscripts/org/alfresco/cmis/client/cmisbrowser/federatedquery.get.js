script:
{
    var connections = cmis.getUserConnections();

    model.connectionCount = connections.size();
    model.defaultStatement = "select cmis:objectId, cmis:name, cmis:objectTypeId\nfrom cmis:document";
    model.formattedStatement = args.statement;
    model.maxItems = (args.maxItems == null) ? 5 : parseInt(args.maxItems);

    // execute query
    if (model.formattedStatement != null)
    {
        var statement = model.formattedStatement.replace(/\n/g, " ").replace(/\r/g, "");
        var results = new Array();

        for (var i = 0; i < connections.size(); i++)
        {
        	if(!connections.get(i).supportsQuery()) {
        		continue;
        	}
        	
        	var oc = connections.get(i).session.createOperationContext();
        	oc.setIncludeAllowableActions(false);
        	
            var skipCount = (args[connections.get(i).id + "_skipCount"] == null) ? 0 : parseInt(args[connections.get(i).id + "_skipCount"]);
            skipCount = (skipCount < 0) ? 0 : skipCount;
            var iterable = connections.get(i).session.query(statement, false, oc).skipTo(skipCount).getPage(model.maxItems);
            
            var result = new Object();
            result.conn = connections.get(i);
            result.rows = iterable.iterator();
            result.skipCount = skipCount;
            result.hasMoreItems = iterable.hasMoreItems;
            result.pageNumItems = iterable.pageNumItems;
            results.push(result);
        }
        
        model.results = results;
    }
}
