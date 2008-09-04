script:
{
    // query type
    var typeId = url.templateArgs.typeId;
    model.typedef = cmis.queryType(typeId);
    if (model.typedef === null)
    {
        status.code = 404;
        status.message = "Type " + typeId + " not found";
        status.redirect = true;
        break script;   
    }

    // handle inherited properties
    var includeInheritedProperties = cmis.findArg(args.includeInheritedProperties, headers["CMIS-includeInheritedProperties"]);
    model.includeInheritedProperties = includeInheritedProperties == "false" ? false : true;
}
