package org.alfresco.opencmis.search;

import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.service.cmr.search.ResultSetSelector;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 * 
 */
public class CMISResultSetSelector implements ResultSetSelector
{
    private String name;

    private TypeDefinitionWrapper typeDefinition;

    public CMISResultSetSelector(String name, TypeDefinitionWrapper typeDefinition)
    {
        this.name = name;
        this.typeDefinition = typeDefinition;
    }

    public String getName()
    {
        return name;
    }

    public TypeDefinitionWrapper getTypeDefinition()
    {
        return typeDefinition;
    }

    public QName getType()
    {
        return typeDefinition.getAlfrescoName();
    }

}
