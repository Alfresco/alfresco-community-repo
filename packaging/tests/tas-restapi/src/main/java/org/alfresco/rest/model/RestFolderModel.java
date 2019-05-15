package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles single Folder JSON responses
 * Example:
      "createdAt": "2016-10-03T08:15:08.722+0000",
      "createdBy": "admin",
      "modifiedAt": "2016-10-03T08:15:08.722+0000",
      "name": "Folder-EJJXzPAauf",
      "guid": "fd84a762-7fea-4ed8-b9cd-7622ab966ac4",
      "modifiedBy": "admin",
      "id": "fd84a762-7fea-4ed8-b9cd-7622ab966ac4"
 */

public class RestFolderModel extends TestModel implements IRestModel<RestFolderModel>
{
    @JsonProperty(value = "entry")
    RestFolderModel model;

    @Override
    public RestFolderModel onModel()
    {
        return model;
    }

    @JsonProperty(required = true)
    protected String createdAt;

    @JsonProperty(required = true)
    protected String createdBy;

    @JsonProperty(required = true)
    protected String modifiedAt;

    @JsonProperty(required = true)
    protected String name;

    @JsonProperty(required = true)
    protected String guid;

    @JsonProperty(required = true)
    protected String modifiedBy;

    @JsonProperty(required = true)
    protected String id;

    @JsonProperty(required = false)
    RestPathModel path;

    private boolean isFolder;
    private boolean isFile;
    private RestByUserModel createdByUser;
    private RestByUserModel modifiedByUser;
    private String parentId;

    public RestFolderModel()
    {
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    public String getModifiedBy()
    {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy)
    {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public String getGuid()
    {
        return guid;
    }

    public void setGuid(String guid)
    {
        this.guid = guid;
    }

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

    public boolean getIsFolder()
    {
        return isFolder;
    }

    public void setIsFolder(boolean isFolder)
    {
        this.isFolder = isFolder;
    }

    public boolean getIsFile()
    {
        return isFile;
    }

    public void setIsFile(boolean isFile)
    {
        this.isFile = isFile;
    }

    public RestByUserModel getCreatedByUser()
    {
        return createdByUser;
    }

    public void setCreatedByUser(RestByUserModel createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public RestByUserModel getModifiedByUser()
    {
        return modifiedByUser;
    }

    public void setModifiedByUser(RestByUserModel modifiedByUser)
    {
        this.modifiedByUser = modifiedByUser;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public RestPathModel getPath()
    {
        return path;
    }

    public void setPath(RestPathModel path)
    {
        this.path = path;
    }

    @Override
    public ModelAssertion<RestFolderModel> and()
    {
        return assertThat();
    }

    @Override
    public ModelAssertion<RestFolderModel> assertThat()
    {
        return new ModelAssertion<RestFolderModel>(this);
    }
}