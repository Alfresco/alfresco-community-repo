
package org.alfresco.rest.api.model;

import java.util.List;

import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class CustomType extends AbstractClassModel
{

    public CustomType()
    {
    }

    public CustomType(TypeDefinition typeDefinition, MessageLookup messageLookup, List<CustomModelProperty> properties)
    {
        this.name = typeDefinition.getName().getLocalName();
        this.prefixedName = typeDefinition.getName().toPrefixString();
        this.title = typeDefinition.getTitle(messageLookup);
        this.description = typeDefinition.getDescription(messageLookup);
        this.parentName = getParentNameAsString(typeDefinition.getParentName());
        this.properties = setList(properties);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(512);
        builder.append("CustomType [name=").append(this.name)
                    .append(", prefixedName=").append(this.prefixedName)
                    .append(", title=").append(this.title)
                    .append(", description=").append(this.description)
                    .append(", parentName=").append(parentName)
                    .append(", properties=").append(properties)
                    .append(']');
        return builder.toString();
    }
}
