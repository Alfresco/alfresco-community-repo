
package org.alfresco.repo.virtual.template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A virtual folder definition. This class holds the (virtual entry)
 * structure of a virtual folder, created according to the template that is
 * applied.<br>
 * 
 * @author Bogdan Horje
 */
public class VirtualFolderDefinition
{

    private String name;

    private String description;

    private FilingRule filingRule;

    private VirtualQuery query;

    private List<VirtualFolderDefinition> children = new LinkedList<VirtualFolderDefinition>();

    private Map<String, VirtualFolderDefinition> childrenByName = new HashMap<String, VirtualFolderDefinition>();

    private Map<String, VirtualFolderDefinition> childrenById = new HashMap<String, VirtualFolderDefinition>();

    private String id;

    private Map<String, String> properties = new HashMap<>();

    public VirtualFolderDefinition()
    {
        this("");
    }

    public VirtualFolderDefinition(String name)
    {
        super();
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        if (this.id == null)
        {
            this.id = name;
        }
    }

    public void setId(String id)
    {
        this.id = id;
        if (this.name == null)
        {
            this.name = id;
        }
    }

    public String getId()
    {
        return this.id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public FilingRule getFilingRule()
    {
        return filingRule;
    }

    public void setFilingRule(FilingRule filingRule)
    {
        this.filingRule = filingRule;
    }

    public VirtualQuery getQuery()
    {
        return query;
    }

    public void setQuery(VirtualQuery query)
    {
        this.query = query;
    }

    public VirtualFolderDefinition findChildByName(String name)
    {
        return childrenByName.get(name);
    }

    public List<VirtualFolderDefinition> getChildren()
    {
        return children;
    }

    public void addChild(VirtualFolderDefinition child)
    {
        this.children.add(child);
        this.childrenByName.put(child.getName(),
                                child);
        this.childrenById.put(child.getId(),
                              child);
    }

    public VirtualFolderDefinition findChildById(String childId)
    {
        return childrenById.get(childId);
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    public Map<String, String> getProperties()
    {
        return this.properties;
    }
}
