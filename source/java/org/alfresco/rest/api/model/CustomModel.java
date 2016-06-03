
package org.alfresco.rest.api.model;

import java.util.List;

import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModel implements Comparable<CustomModel>
{
    public static enum ModelStatus
    {
        ACTIVE, DRAFT
    }

    private String name;
    private String author;
    private String description;
    private ModelStatus status;
    private String namespaceUri;
    private String namespacePrefix;
    private List<CustomType> types;
    private List<CustomAspect> aspects;
    private List<CustomModelConstraint> constraints;

    public CustomModel()
    {
    }

    public CustomModel(CustomModelDefinition modelDefinition)
    {
        this(modelDefinition, null, null, null);
    }

    public CustomModel(CustomModelDefinition modelDefinition, List<CustomType> types, List<CustomAspect> aspects, List<CustomModelConstraint> constraints)
    {
        this.name = modelDefinition.getName().getLocalName();
        this.author = modelDefinition.getAuthor();
        this.description = modelDefinition.getDescription();
        this.status = modelDefinition.isActive() ? ModelStatus.ACTIVE : ModelStatus.DRAFT;
        // we don't need to check for NoSuchElementException, as we don't allow
        // the model to be saved without a valid namespace
        NamespaceDefinition nsd = modelDefinition.getNamespaces().iterator().next();
        this.namespaceUri = nsd.getUri();
        this.namespacePrefix = nsd.getPrefix();
        this.types = types;
        this.aspects = aspects;
        this.constraints = constraints;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public ModelStatus getStatus()
    {
        return this.status;
    }

    public void setStatus(ModelStatus status)
    {
        this.status = status;
    }

    public String getNamespaceUri()
    {
        return this.namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri)
    {
        this.namespaceUri = namespaceUri;
    }

    public String getNamespacePrefix()
    {
        return this.namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix)
    {
        this.namespacePrefix = namespacePrefix;
    }

    public List<CustomType> getTypes()
    {
        return this.types;
    }

    public void setTypes(List<CustomType> types)
    {
        this.types = types;
    }

    public List<CustomAspect> getAspects()
    {
        return this.aspects;
    }

    public void setAspects(List<CustomAspect> aspects)
    {
        this.aspects = aspects;
    }

    public List<CustomModelConstraint> getConstraints()
    {
        return this.constraints;
    }

    public void setConstraints(List<CustomModelConstraint> constraints)
    {
        this.constraints = constraints;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof CustomModel))
        {
            return false;
        }
        CustomModel other = (CustomModel) obj;
        if (this.name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!this.name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CustomModel customModel)
    {
        return this.name.compareTo(customModel.getName());
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(512);
        builder.append("CustomModel [name=").append(this.name).append(", author=").append(this.author)
                    .append(", description=").append(this.description).append(", status=").append(this.status)
                    .append(", namespaceUri=").append(this.namespaceUri).append(", namespacePrefix=")
                    .append(this.namespacePrefix).append(']');
        return builder.toString();
    }
}
