function main()
{
    // Extract args
    var type = args['type'];
    var filter = args['filter'];
    var maxResults = args['maxResults'];
    
    if (type === null)
    {
        type = "cm:content";
    }
    
    if (maxResults === null)
    {
        maxResults = 100;
    }
   
    if (logger.isLoggingEnabled())
    {
        logger.log("type = " + type);
        logger.log("filter = " + filter);
        logger.log("maxResults = " + maxResults);
    }

    var results = [];
    
    // execute a lucene query using the type parameter
    var query = "+TYPE:\"" + type + "\"";
    if (filter !== null)
    {
        query += " AND +@\\{http\\://www.alfresco.org/model/content/1.0\\}name:\"*" + filter + "*\"";
    }
    
    results = search.luceneSearch(query);
    
    if (logger.isLoggingEnabled())
    {
        logger.log("result count = " + results.length);
    }
    
    if (results.length > maxResults)
    {
        results = results.slice(0, maxResults);
        if (logger.isLoggingEnabled())
        {
            logger.log("Restricted results size to " + maxResults);
        }
    }
    
    model.results = results;
}

main();
