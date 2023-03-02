/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TagModel;
import org.testng.Assert;

/**
 * Handles single Tag Entry JSON response
 * "entry":
 * {
 * "tag":"addedtag-c7444-1474370805346"
 * "id":"f45c4d06-f4df-42d7-a118-29121557d284"
 * }
 * 
 * @author Corina Nechifor
 */
public class RestTagModel extends TagModel implements IRestModel<RestTagModel>
{
    @JsonProperty(value = "entry")
    RestTagModel model;

    protected Integer count;

    public RestTagModel onModel()
    {
        return model;
    }

    public RestTagModel assertResponseIsNotEmpty()
    {
        STEP(String.format("REST API: Assert get tags response is not empty"));
        Assert.assertFalse(getId().isEmpty(), "Get tags response is empty.");

        return this;
    }

    public Integer getCount()
    {
        return count;
    }

    public void setCount(Integer count)
    {
        this.count = count;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RestTagModel tagModel = (RestTagModel) o;
        return Objects.equals(id, tagModel.id) && Objects.equals(tag, tagModel.tag) && Objects.equals(count, tagModel.count);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, tag, count);
    }

    @Override
    public String toString()
    {
        return "RestTagModel{" + "id='" + id + ", tag='" + tag + '\'' + ", count=" + count + '\'' + '}';
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String id;
        private String tag;
        private Integer count;

        public Builder id(String id)
        {
            this.id = id;
            return this;
        }

        public Builder tag(String tag)
        {
            this.tag = tag;
            return this;
        }

        public Builder count(Integer count)
        {
            this.count = count;
            return this;
        }

        public RestTagModel create()
        {
            final RestTagModel tag = new RestTagModel();
            tag.setId(id);
            tag.setTag(this.tag);
            tag.setCount(count);
            return tag;
        }
    }
}    
