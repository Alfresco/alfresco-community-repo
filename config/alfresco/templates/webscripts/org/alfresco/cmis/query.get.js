script:
{
    // process query statement
    // <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    // <cmis:query xmlns:cmis="http://docs.oasis-open.org/ns/cmis/core/200908/">
    //   <cmis:statement>SELECT * FROM Document</cmis:statement>
    //   <cmis:searchAllVersions>true</cmis:searchAllVersions>
    //   <cmis:maxItems>50</cmis:maxItems>
    //   <cmis:skipCount>0</cmis:skipCount>
    // </cmis:query>
    
    
    // extract query statement
    model.statement = args.q;
    if (model.statement == null || model.statement.length == 0)
    {
        status.setCode(status.STATUS_BAD_REQUEST, "Query statement must be provided");
        break script;
    }
    
    // process search all versions (NOTE: not supported)
    var searchAllVersions = args.searchAllVersions;
    if (searchAllVersions != null && searchAllVersions === "true")
    {
        status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Search all versions not supported");
        break script;
    }
    
    // include allowable actions
    var includeAllowableActions = args[cmisserver.ARG_INCLUDE_ALLOWABLE_ACTIONS];
    model.includeAllowableActions = (includeAllowableActions == "true" ? true : false);

    // include relationships
    model.includeRelationships = args[cmisserver.ARG_INCLUDE_RELATIONSHIPS];
    if (model.includeRelationships == null || model.includeRelationships.length == 0)
    {
        model.includeRelationships = "none";
    }
    
    // perform query
    var page = paging.createPageOrWindow(args);
    var paged = cmisserver.query(model.statement, page);
    model.resultset = paged.result;
    model.cursor = paged.cursor;
    
    // TODO: check includeFlags are valid for query (with multiple types referenced in selectors)
}