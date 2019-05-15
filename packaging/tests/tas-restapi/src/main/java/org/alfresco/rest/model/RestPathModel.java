package org.alfresco.rest.model;

import java.util.List;

import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Meenal Bhave
 * "path": {
 *      "name": "/Folder-oawzdncUXFLgnFe",
 *      "isComplete": false,
 *      "elements": [
 *      {
 *              "aspectNames": ["cm:titled", "cm:auditable", "app:uifacets"],
 *              "id": "7f0c47ae-d334-4b66-a86b-1a60d2518ad1",
 *              "name": "Folder-oawzdncUXFLgnFe",
 *              "nodeType": "cm:folder"
 *      }
 *      ]
 * }
 */
public class RestPathModel extends TestModel
{
    private String name;
    @JsonProperty(value = "isComplete")
    private boolean isComplete;
    private List<RestElementModel> elements;

    public List<RestElementModel> getElements()
    {
        return elements;
    }

    public void setElements(List<RestElementModel> elements)
    {
        this.elements = elements;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isComplete()
    {
        return isComplete;
    }

    public void setComplete(boolean isComplete)
    {
        this.isComplete = isComplete;
    }
}
