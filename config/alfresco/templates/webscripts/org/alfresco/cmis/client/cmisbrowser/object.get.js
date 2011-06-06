script:
{
    model.conn = cmis.getConnection(url.templateArgs["conn"]);
    var operationContext = model.conn.session.createOperationContext();
    operationContext.renditionFilterString = "*";
    operationContext.includeRelationships = Packages.org.apache.chemistry.opencmis.commons.enums.IncludeRelationships.BOTH;
    model.object = model.conn.session.getObject(args.id, operationContext);
    model.isDoc = (model.object.baseType.id == "cmis:document");
    model.isFolder = (model.object.baseType.id == "cmis:folder");
    if (model.isDoc)
    {
        var parents = model.object.parents;
        if (parents.size() > 0)
        {
            model.parent = parents.get(0);
        }
    }
}
