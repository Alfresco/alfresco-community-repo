<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/constants.lib.js">

script:
{
    // locate node
    var pathSegments = url.match.split("/");
    var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
    model.node = cmis.findNode(pathSegments[2], reference);
    if (model.node === null)
    {
        status.setCode(404, "Repository " + pathSegments[2] + " " + reference.join("/") + " not found");
        break script;
    }

    // property filter
    model.filter = args[cmis.ARG_FILTER];
    if (model.filter === null)
    {
        model.filter = "*";
    }

    // relationship type
    var relType = args[cmis.ARG_RELATIONSHIP_TYPE];
    if (relType != null)
    {
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
    }

    // include sub relationship types
    model.includeSubRelationshipTypes = args[cmis.ARG_INCLUDE_SUB_RELATIONSHIP_TYPES] == "true" ? true : false;

    // direction
    var direction = args[cmis.ARG_DIRECTION];
    if (direction !== null && !CMISRelationshipDirectionEnum.FACTORY.validLabel(direction))
    {
        status.setCode(400, "Direction " + direction + " unknown");
        break script;
    }
    model.direction = CMISRelationshipDirectionEnum.FACTORY.toEnum(direction);
    
    // include allowable actions
    model.includeAllowableActions = args[cmis.ARG_INCLUDE_ALLOWABLE_ACTIONS] == "true" ? true : false;
    
    // retrieve relationships
    var page = paging.createPageOrWindow(args);
    var paged = cmis.queryRelationships(model.node, model.relDefType, model.includeSubRelationshipTypes, model.direction, page);
    model.results = paged.results;
    model.cursor = paged.cursor;
}
