package org.alfresco.rest.core;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.List;
import java.util.Random;

import org.alfresco.rest.core.assertion.IModelsCollectionAssertion;
import org.alfresco.rest.core.assertion.ModelsCollectionAssertion;
import org.alfresco.rest.exception.EmptyRestModelCollectionException;
import org.alfresco.rest.model.RestPaginationModel;
import org.alfresco.rest.model.RestSiteModelsCollection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Map multiple entries of JSON response to a class <T>
 * 
 * Example:
 * 
 *  "entries": [
      {
        "entry": {
          "visibility": "PUBLIC",
          "guid": "79e140e1-5039-4efa-acaf-c22b5ba7c947",
          "description": "Description1470255221170",
          "id": "0-C2291-1470255221170",
          "title": "0-C2291-1470255221170"
        }
      },
 * 
 * Having this JSON Entry, we can auto-map this to {@link RestSiteModelsCollection} class having a List of <SiteModel> based on this example
 * 
 * @author Paul Brodner
 */
public abstract class RestModels<Model, ModelCollection> implements IRestModelsCollection<Model>, IModelsCollectionAssertion<ModelCollection>
{
    @JsonProperty(value = "entries")
    private List<Model> modelEntries;

    private RestPaginationModel pagination;
    
    @Override
    public List<Model> getEntries()
    {
        return modelEntries;
    }
    
    /**
     * @return a random entry from entries list
     * @throws EmptyRestModelCollectionException
     */
    @Override
    public Model getOneRandomEntry() throws EmptyRestModelCollectionException
    {
        STEP("REST API: Get random one entry from response");
        Random random = new Random();
        List<Model> models = getEntries();
        if(models.isEmpty())
          throw new EmptyRestModelCollectionException(models);
        
        int index = random.nextInt(models.size());
        return models.get(index);
    }
    
    /**
     * Example
     * <code>
     *         siteMembershipRequests.getEntryByIndex(0)
                .assertThat().field("site.visibility").is(moderatedSite.getVisibility())
                .assertThat().field("site.description").is(moderatedSite.getDescription())
                .assertThat().field("site.id").is(moderatedSite.getId())
                .assertThat().field("site.title").is(moderatedSite.getTitle());
     * </code>
     * @param index
     * @return
     * @throws EmptyRestModelCollectionException
     */
    @SuppressWarnings("unchecked")
    public Model getEntryByIndex(int index) throws EmptyRestModelCollectionException{
        STEP("REST API: Get index entry from response");
        
        List<Model> models = getEntries();
        if(models.isEmpty())
          throw new EmptyRestModelCollectionException(models);
        
        if(models.size() > index){
            return (Model) ((IRestModel<?>)models.get(index)).onModel();
        }
        
       return null;
    }
    
    @Override    
    public ModelsCollectionAssertion<RestModels<Model, ModelCollection>> assertThat()
    {      
        return new ModelsCollectionAssertion<RestModels<Model, ModelCollection>>(this);
    }
    
    @Override    
    public ModelsCollectionAssertion<RestModels<Model, ModelCollection>> and()
    {
        return assertThat();
    }
    
    @SuppressWarnings("unchecked")
    @Override    
    public ModelCollection when()
    {
      return (ModelCollection)this;
    }    

    /**
     * @return boolean value if entry is empty
     */
    @Override
    public boolean isEmpty()
    {
        if (getEntries() != null)
            return getEntries().isEmpty();
        else
            return true;
    }
    
    @Override
    public RestPaginationModel getPagination()
    {
        return pagination;
    }

    public void setPagination(RestPaginationModel pagination)
    {
        this.pagination = pagination;
    } 
    
    
}