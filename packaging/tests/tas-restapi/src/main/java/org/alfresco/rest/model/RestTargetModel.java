package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestTargetModel extends TestModel implements IRestModel<RestTargetModel>
{
    @Override
    public RestTargetModel onModel() 
    {      
      return model;
    }
  
    @JsonProperty(value = "target")
    RestTargetModel model;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RestSiteModel site;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RestFolderModel folder;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RestFileModel file;
    
    public RestTargetModel()
    {
    }
    
    public RestTargetModel(RestSiteModel site)
    {
        super();
        this.site = site;
    }

    public RestTargetModel(RestSiteModel site, RestFolderModel folder, RestFileModel file)
    {
        super();
        this.site = site;
        this.folder = folder;
        this.file = file;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public RestSiteModel getSite()
    {
        return site;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setSite(RestSiteModel site)
    {
        this.site = site;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public RestFolderModel getFolder()
    {
        return folder;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setFolder(RestFolderModel folder)
    {
        this.folder = folder;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public RestFileModel getFile()
    {
        return file;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setFile(RestFileModel file)
    {
        this.file = file;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestTargetModel> assertThat() 
    {
      return new ModelAssertion<RestTargetModel>(this);
    }
    
    @Override
    public ModelAssertion<RestTargetModel> and() 
    {
      return assertThat();
    }
}    