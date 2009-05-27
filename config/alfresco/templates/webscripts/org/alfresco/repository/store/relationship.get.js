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
    if (model.relTypeDef.baseType.typeId != "relationship")
    {
        status.setCode(400, "Type + " + relType + " is not a relationship type");
        break script;
    }

    // source and target
    var source = [url.templateArgs.store_type, url.templateArgs.store_id, url.templateArgs.id];
    var target = [url.templateArgs.target_store_type, url.templateArgs.target_store_id, url.templateArgs.target_id];
    
    // locate association
    model.assoc = cmis.findRelationship(model.relTypeDef, source, target);
    if (model.assoc === null)
    {
        status.setCode(404, "Assoc " + source.join("/") + "/" + relType + "/" + target.join("/") + " not found");
        break script;
    }
}
