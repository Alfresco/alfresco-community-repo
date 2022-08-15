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
package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

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
}
