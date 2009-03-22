
//
// Create Alfresco Node from Atom Entry
//
// @param parent  parent to create node within
// @param entry  atom entry
// @param slug (optional)  node name
// @return  created node (or null, in case of error)
//
function createNode(parent, entry, slug)
{
    var object = entry.getExtension(atom.names.cmis_object);
    var typeId = (object !== null) ? object.objectTypeId.value : null;

    // locate type definition
    // TODO: check this against spec - default to Document, if not specified
    var type = cmis.queryType(typeId === null ? "document" : typeId);
    if (type === null)
    {
        status.code = 400;
        status.message = "CMIS object type " + typeId + " not understood";
        status.redirect = true;
        return null;
    }

    // construct node of folder or file
    var name = (slug !== null) ? slug : entry.title;
    var baseType = type.rootTypeId.typeId;
    if (baseType == "document")
    {
        node = parent.createFile(name);
        // TODO: versioningState argument (CheckedOut/CheckedInMinor/CheckedInMajor)
    }
    else if (baseType == "folder")
    {
        node = parent.createFolder(name);
    }
    else
    {
        status.code = 400;
        status.message = "Cannot create object of type " + typeId;
        status.redirect = true;
        return null;
    }

    // specialize to required custom type
    var objectType = type.objectTypeId.typeId;
    if (objectType != "document" && objectType != "folder")
    {
        if (!node.specializeType(type.objectTypeId.QName))
        {
            status.code = 400;
            status.message = "Cannot create object of type " + typeId;
            status.redirect = true;
            return null;
        }
    }
    
    // update node properties (excluding object type)
    // TODO: consider array form of properties.names
    var propNames = object.properties.names.toArray().filter( function(element, index, array) { return element != "ObjectTypeId"; } );
    var updated = updateNode(node, entry, propNames, true);

    // only return node if updated successfully
    return (updated === null) ? null : node;
}


//
// Update Alfresco Node with Atom Entry
//
// @param node  Alfresco node to update
// @param entry  Atom entry to update from
// @param propNames  properties to update
// @param pwc  true => node represents private working copy
// @return  true => node has been updated (or null, in case of error)
//
function updateNode(node, entry, propNames, pwc)
{
    // check update is allowed
    if (!node.hasPermission("WriteProperties") || !node.hasPermission("WriteContent"))
    {
        status.code = 403;
        status.message = "Permission to update is denied";
        status.redirect = true;
        return null;
    }    
    
    var updated = false;
    var object = entry.getExtension(atom.names.cmis_object);
    var props = (object === null) ? null : object.properties;
    var vals = new Object();

    // apply defaults
    if (pwc == null) pwc = false;
    if (propNames == null) propNames = (props !== null) ? props.names : null;
    
    // build values to update
    if (props !== null && propNames.length > 0)
    {
        var typeDef = cmis.queryType(node);
        var propDefs = typeDef.propertyDefinitions;
        for each (propName in propNames)
        {
            // is this a valid property?
            var propDef = propDefs[propName];
            if (propDef === null)
            {
                status.code = 400;
                status.message = "Property " + propName + " is not a known property for type " + typeDef.objectTypeId;
                status.redirect = true;
                return null;
            }

            // is the property write-able?
            if (propDef.updatability === Packages.org.alfresco.cmis.CMISUpdatabilityEnum.READ_ONLY)
            {
                status.code = 500;
                status.message = "Property " + propName + " cannot be updated. It is read only."
                status.redirect = true;
                return null;
            }
            if (!pwc && propDef.updatability === Packages.org.alfresco.cmis.CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT)
            {
                status.code = 500;
                status.message = "Property " + propName + " can only be updated on a private working copy.";
                status.redirect = true;
                return null;
            }
            
            // extract value
            var prop = props.find(propName);
            var val = null;
            if (!prop.isNull())
            {
                // TODO: handle multi-valued properties
                val = prop.value;
            }
            vals[propName] = val;
        }
    }
    
    // handle aspect specific properties
    // NOTE: atom entry values override cmis:values
    if (entry.title != null) vals["cm_name"] = entry.title;
    if (entry.summary != null) vals["cm_description"] = entry.summary;

    // update node values
    for (val in vals)
    {
        var propName = cmis.mapPropertyName(val);
        if (propName === null)
        {
            status.code = 500;
            status.message = "Internal error: Property " + val + " does not map to a write-able Alfresco property";
            status.redirect = true;
            return null;
        }
        node.properties[propName] = vals[val];
        updated = true;
    }

    // handle content
    if (entry.content != null)
    {
        if (!node.isDocument)
        {
            status.code = 400;
            status.message = "Cannot update content on folder " + node.displayPath;
            status.redirect = true;
            return null;
        }
        
        if (entry.contentType != null && entry.contentType == "MEDIA")
        {
            node.properties.content.write(entry.contentStream);
        }
        else
        {
            node.content = entry.content;
        }
        node.properties.content.encoding = "UTF-8";
        node.properties.content.mimetype = atom.toMimeType(entry);
        updated = true;
    }
    
    return updated;
}
