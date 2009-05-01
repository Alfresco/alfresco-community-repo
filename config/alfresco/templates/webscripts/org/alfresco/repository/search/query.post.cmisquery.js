script:
{
    // process query statement
    // <?xml version="1.0"?>
    // <query xmlns="http://docs.oasis-open.org/ns/cmis/core/200901">
    //    <statement>SELECT name FROM DOCUMENT_OBJECT_TYPE</statement>
    //    <searchAllVersions>false</searchAllVersions>
    //    <pageSize>0</pageSize>
    //    <skipCount>0</skipCount>
    //    <returnAllowableActions>false</returnAllowableActions>
    // </query>
    
    // TODO: XML parsing need to be moved to Java

    function ltrim(str){
        return str.replace(/^\s+/, '');
    }

    default xml namespace = 'http://docs.oasis-open.org/ns/cmis/core/200901';
    
    // regex to match an XML declaration
    var xmlDeclaration = /^<\?xml version[^>]+?>/; 
    
    // remove xml declaration and leading whitespace
    query = ltrim(query.replace(xmlDeclaration, ''));

    // need to move the XML declaration if it exists
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
    
    // TODO: CMIS-124
    // include allowable actions
    var includeAllowableActions = cmisQuery.returnAllowableActions.toString();
    model.includeAllowableActions = (includeAllowableActions == null || includeAllowableActions == "true" ? true : false);
    
    // process paging
    var skipCount = parseInt(cmisQuery.skipCount);
    var pageSize = parseInt(cmisQuery.pageSize);
    var page = paging.createPageOrWindow(null, null, isNaN(skipCount) ? null : skipCount, isNaN(pageSize) ? null : pageSize);
    
    // perform query
    var paged = cmis.query(model.statement, page);
    model.resultset = paged.result;
    model.cursor = paged.cursor;
}
