
package org.alfresco.rest.api.model;

import java.util.List;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class CustomAspect extends AbstractClassModel
{

    public CustomAspect()
    {
    }

    public CustomAspect(AspectDefinition aspectDefinition, MessageLookup messageLookup, List<CustomModelProperty> properties)
    {
        this.name = aspectDefinition.getName().getLocalName();
        this.prefixedName = aspectDefinition.getName().toPrefixString();
        this.title = aspectDefinition.getTitle(messageLookup);
        this.description = aspectDefinition.getDescription(messageLookup);
        this.parentName = getParentNameAsString(aspectDefinition.getParentName());
        this.properties = setList(properties);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(512);
        builder.append("CustomAspect [name=").append(this.name)
                    .append(", prefixedName=").append(this.prefixedName)
                    .append(", title=").append(this.title)
                    .append(", description=").append(this.description)
                    .append(", parentName=").append(parentName)
                    .append(", properties=").append(properties)
                    .append(']');
        return builder.toString();
    }
}
