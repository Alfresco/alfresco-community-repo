package org.alfresco.rest.model;

import java.util.List;

import org.alfresco.utility.model.TestModel;

/**
 * Created by Meenal Bhave
 * "elements": [
 * {
 *      "aspectNames": ["cm:titled", "cm:auditable", "app:uifacets"],
 *      "id": "7f0c47ae-d334-4b66-a86b-1a60d2518ad1",
 *      "name": "Folder-oawzdncUXFLgnFe",
 *      "nodeType": "cm:folder"
 * }
 * ]
 */

public class RestElementModel extends TestModel
{
    private String name;
    private String id;
    private List<String> aspectNames;
    private String nodeType;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

}
