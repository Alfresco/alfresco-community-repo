script:
{
    model.conn = cmis.getConnection(url.templateArgs["conn"]);
    model.folder = model.conn.session.getObject(args.id);
    model.folderParent = model.folder.folderParent;
    
    var oc = model.conn.session.createOperationContext();
    oc.setFilterString("cmis:name,cmis:objectId,cmis:baseType,cmis:lastModifiedBy,cmis:lastModificationDate,cmis:contentStreamLength");
    oc.setMaxItemsPerPage(10000);
    oc.setIncludeAcls(false);
    oc.setIncludeAllowableActions(false);
    oc.setIncludePolicies(false);
    oc.setIncludePathSegments(false);
    model.folderChildren = model.folder.getChildren(oc).iterator();
}
