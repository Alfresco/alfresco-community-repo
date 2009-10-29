<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/constants.lib.js">

script:
{
    // relationship type
    var relType = url.templateArgs.rel_type;
    model.relTypeDef = cmis.queryType(relType);
    if (model.relTypeDef === null)
    {
        status.setCode(400, "Relationship type " + relType + " unknown");
        break script;
    }
    if (model.relTypeDef.baseType.typeId != RELATIONSHIP_TYPE_ID)
    {
        status.setCode(400, "Type + " + relType + " is not a relationship type");
        break script;
    }

    // source and target
    var source = [url.templateArgs.store_type, url.templateArgs.store_id, url.templateArgs.id];
    var target = [url.templateArgs.target_store_type, url.templateArgs.target_store_id, url.templateArgs.target_id];

    // locate association
    var assoc = cmis.findRelationship(model.relTypeDef, source, target);
    if (assoc === null)
    {
        status.setCode(404, "Assoc " + source.join("/") + "/" + relType + "/" + target.join("/") + " not found");
        break script;
    }

    // TODO: check permission
//    if (!assoc.source.hasPermission("DeleteAssociations"))
//    {
//        status.setCode(403, "Permission to delete is denied");
//        break script;
//    }

    // delete
    assoc.source.removeAssociation(assoc.target, assoc.type);
    
    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
