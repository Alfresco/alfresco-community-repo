package org.alfresco.rest.model;

import java.util.List;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated from 'Alfresco Core REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/alfresco/versions/1}
 * 
 * @author Meenal Bhave
 */
public class RestSharedLinksModel extends TestModel implements IRestModel<RestSharedLinksModel>
{

    public RestSharedLinksModel()
    {
    }

    public RestSharedLinksModel(String fileGuid)
    {
        super();
        this.nodeId = fileGuid;
    }

    @JsonProperty(value = "entry")
    RestSharedLinksModel model;

    @Override
    public RestSharedLinksModel onModel()
    {
        return model;
    }

    @Override
    public ModelAssertion<RestSharedLinksModel> assertThat()
    {
        return new ModelAssertion<RestSharedLinksModel>(this);
    }

    @Override
    public ModelAssertion<RestSharedLinksModel> and()
    {
        return assertThat();
    }

    @JsonProperty(required = true)
    private String modifiedAt;

    @JsonProperty(required = true)
    private RestByUserModel modifiedByUser;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String id;

    @JsonProperty(required = true)
    private String nodeId;

    @JsonProperty(required = true)
    private RestByUserModel sharedByUser;

    private RestContentModel content;

    @JsonProperty(required = false)
    RestPathModel path;

    @JsonProperty(required = false)
    private String expiresAt;

    @JsonProperty(required = false)
    private List<String> allowableOperations;

    public List<String> getAllowableOperations()
    {
        return allowableOperations;
    }

    public void setAllowableOperations(List<String> allowableOperations)
    {
        this.allowableOperations = allowableOperations;
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getNodeType()
    {
        return this.nodeId;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeId = nodeType;
    }

    public String getModifiedAt()
    {
        return this.modifiedAt;
    }

    public void setModifiedAt(String modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }

    public RestByUserModel getModifiedByUser()
    {
        return modifiedByUser;
    }

    public void setModifiedByUser(RestByUserModel modifiedByUser)
    {
        this.modifiedByUser = modifiedByUser;
    }

    public RestByUserModel getSharedByUser()
    {
        return sharedByUser;
    }

    public void setSharedByUser(RestByUserModel SharedByUser)
    {
        this.sharedByUser = SharedByUser;
    }

    public RestContentModel getContent()
    {
        return content;
    }

    public void setContent(RestContentModel content)
    {
        this.content = content;
    }

    public RestPathModel getPath()
    {
        return path;
    }

    public void setPath(RestPathModel path)
    {
        this.path = path;
    }
    
    public String getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt)
    {
        this.expiresAt = expiresAt;
    }

}
