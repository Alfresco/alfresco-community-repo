
package org.alfresco.rest.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * @author Jamal Kaabi-Mofrad
 */
public abstract class AbstractClassModel extends AbstractCommonDetails
{
    /* package */String parentName;
    /* package */List<CustomModelProperty> properties = Collections.emptyList();

    public String getParentName()
    {
        return this.parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    public List<CustomModelProperty> getProperties()
    {
        return this.properties;
    }

    public void setProperties(List<CustomModelProperty> properties)
    {
        this.properties = properties;
    }

    /* package */<T> List<T> setList(List<T> sourceList)
    {
        if (sourceList == null)
        {
            return Collections.<T> emptyList();
        }
        return new ArrayList<>(sourceList);
    }

    /* package */String getParentNameAsString(QName parentQName)
    {
        if (parentQName != null)
        {
            return parentQName.toPrefixString();
        }
        return null;
    }
}
