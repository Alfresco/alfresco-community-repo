script:
{
    // process query statement
    // <?xml version="1.0"?>
    // <query xmlns="http://www.cmis.org/CMIS/2008/05">
    //    <statement>object_id1</statement >
    //    <searchAllVersions>false</searchAllVersions>
    //    <pageSize>0</pageSize>
    //    <skipCount>0</skipCount>
    //    <returnAllowableActions>false</returnAllowableActions>
    // </query>
    
    default xml namespace = 'http://www.cmis.org/2008/05';
    
    var cmisQuery = new XML(query);
    
    // extract query statement
    model.statement = cmisQuery.statement.toString();
    if (model.statement == null || model.statement.length == 0)
    {
        status.setCode(status.STATUS_BAD_REQUEST, "Query statement must be provided");
        break script;
    }
    
    // process search all versions (NOTE: not supported)
    var searchAllVersions = cmisQuery.searchAllVersions;
    if (searchAllVersions != null && searchAllVersions === "true")
    {
        status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Search all versions not supported");
        break script;
    }
    
    // TODO: process allowableActions
    
    // process paging
    var skipCount = parseInt(cmisQuery.skipCount);
    var pageSize = parseInt(cmisQuery.pageSize);
    var page = paging.createPageOrWindow(null, null, isNaN(skipCount) ? null : skipCount, isNaN(pageSize) ? null : pageSize);
    
    // perform query
    var paged = cmis.query(model.statement, page);
    model.resultset = paged.result;
    model.cursor = paged.cursor;
}
