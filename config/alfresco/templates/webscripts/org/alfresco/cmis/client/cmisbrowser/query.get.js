script:
{
    model.conn = cmis.getConnection(url.templateArgs["conn"]);

    model.defaultStatement = "select cmis:objectId, cmis:name, cmis:objectTypeId\nfrom cmis:document";
    model.formattedStatement = args.statement;
    model.skipCount = (args.skipCount == null || args.skipCount < 0) ? 0 : parseInt(args.skipCount);
    model.maxItems = (args.maxItems == null) ? 10 : parseInt(args.maxItems);

    // execute query
    if (model.formattedStatement != null)
    {
        var statement = model.formattedStatement.replace(/\n/g, " ").replace(/\r/g, "");
        var iterable = model.conn.session.query(statement, false).skipTo(model.skipCount).getPage(model.maxItems);
        model.rows = iterable.iterator();
        model.hasMoreItems = iterable.hasMoreItems;
        model.pageNumItems = iterable.pageNumItems;
    }
}
