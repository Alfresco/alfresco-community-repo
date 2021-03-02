package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

public class RestClassModel extends TestModel
{
    public String  id;
    public String  author;
    public String  description;
    public String  namespaceUri;
    public String  namespacePrefix;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getNamespaceUri()
    {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri)
    {
        this.namespaceUri = namespaceUri;
    }

    public String getNamespacePrefix()
    {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix)
    {
        this.namespacePrefix = namespacePrefix;
    }
}