/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.core;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.assertion.IModelsCollectionAssertion;
import org.alfresco.rest.core.assertion.ModelsCollectionAssertion;
import org.alfresco.rest.exception.EmptyRestModelCollectionException;
import org.alfresco.rest.model.RestPaginationModel;
import org.alfresco.rest.model.RestSiteModelsCollection;

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
        return new ModelsCollectionAssertion<>(this);
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
