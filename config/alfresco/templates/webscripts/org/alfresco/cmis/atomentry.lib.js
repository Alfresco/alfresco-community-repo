
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
    var typeId = (object !== null) ? object.objectTypeId.nativeValue : null;

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
    var baseType = type.typeId.baseTypeId.id;
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
    var objectType = type.typeId.id;
    if (objectType != "document" && objectType != "folder")
    {
        if (!node.specializeType(type.typeId.QName))
        {
            status.code = 400;
            status.message = "Cannot create object of type " + typeId;
            status.redirect = true;
            return null;
        }
    }
    
    // update node properties (excluding object type & name)
    var exclude = [ "ObjectTypeId", "Name" ];
    var updated = updateNode(node, entry, exclude, function(propDef) {return patchValidator(propDef, true);});

    // only return node if updated successfully
    return (updated == null) ? null : node;
}


//
// Update Alfresco Node with Atom Entry
//
// @param node  Alfresco node to update
// @param entry  Atom entry to update from
// @param exclude  property names to exclude
// @param validator  function callback for validating property update
// @return  true => node has been updated (or null, in case of error)
//
function updateNode(node, entry, exclude, validator)
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
    var props = (object == null) ? null : object.properties;
    var vals = new Object();

    // calculate list of properties to update
    // TODO: consider array form of properties.names
    var updateProps = (props == null) ? new Array() : props.names.toArray().filter(function(element, index, array) {return true;});
    updateProps.push("Name");   // mapped to entry.title
    var exclude = (exclude == null) ? new Array() : exclude;
    exclude.push("BaseType");   // TODO: CMIS Issue where BaseType is not a property
    updateProps = updateProps.filter(includeProperty, exclude);
    
    // build values to update
    if (updateProps.length > 0)
    {
        var typeDef = cmis.queryType(node);
        var propDefs = typeDef.propertyDefinitions;
        for each (propName in updateProps)
        {
            // is this a valid property?
            var propDef = propDefs[propName];
            if (propDef == null)
            {
                status.code = 400;
                status.message = "Property " + propName + " is not a known property for type " + typeDef.typeId;
                status.redirect = true;
                return null;
            }

            // validate property update
            var valid = validator(propDef);
            if (valid == null)
            {
                // error, abort update
                return null;
            }
            if (valid == false)
            {
                // ignore property
                continue;
            }

            // extract value
            var val = null;
            var prop = (props == null) ? null : props.find(propName);
            if (prop != null && !prop.isNull())
            {
                if (prop.isMultiValued())
                {
                    if (propDef.updatability === Packages.org.alfresco.cmis.CMISCardinalityEnum.MULTI_VALUED)
                    {
                        status.code = 500;
                        status.message = "Property " + propName + " is single valued."
                        status.redirect = true;
                        return null;
                    }
                    val = prop.nativeValues;
                }
                else
                {
                    val = prop.nativeValue;
                }
            }
            
            // NOTE: special case name: entry.title overrides cmis:name
            if (propName === "Name")
            {
                val = entry.title;
            }
            
            vals[propDef.propertyAccessor.mappedProperty.toString()] = val;
        }
    }

    // NOTE: special case cm_description property (this is defined on an aspect, so not part of
    //       formal CMIS type model
    if (entry.summary != null) vals["cm:description"] = entry.summary;
    
    // update node values
    for (val in vals)
    {
        node.properties[val] = vals[val];
        updated = true;
    }

    // handle content
    if (entry.content != null && entry.contentSrc == null)
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


// callback for validating property update for patch
// return null => update not allowed, abort update
//        true => update allowed
//        false => update not allowed, ignore property
function patchValidator(propDef, pwc)
{
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
    var mappedProperty = propDef.propertyAccessor.mappedProperty;
    if (mappedProperty == null)
    {
        status.code = 500;
        status.message = "Internal error: Property " + propName + " does not map to a write-able Alfresco property";
        status.redirect = true;
        return null;
    }
    return true;
}

//callback for validating property update for put
//return null => update not allowed, abort update
//     true => update allowed
//     false => update not allowed, ignore property
function putValidator(propDef, pwc)
{
    // is the property write-able?
    if (propDef.updatability === Packages.org.alfresco.cmis.CMISUpdatabilityEnum.READ_ONLY)
    {
        return false;
    }
    if (!pwc && propDef.updatability === Packages.org.alfresco.cmis.CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT)
    {
        return false;
    }
    var mappedProperty = propDef.propertyAccessor.mappedProperty;
    if (mappedProperty == null)
    {
        return false;
    }
    return true;
}

// callback function for determining if property name should be excluded
// note: this refers to array of property names to exclude
function includeProperty(element, index, array)
{
    for each (exclude in this)
    {
        if (element == exclude)
        {
            return false;
        }
    }
    return true;
}
