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
package org.alfresco.rest.requests;

import static org.alfresco.rest.core.JsonBodyGenerator.arrayToJson;

import java.util.List;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.model.RestCategoryModelsCollection;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.springframework.http.HttpMethod;

public class Categories extends ModelRequest<Categories>
{
    private RestCategoryModel category;

    public Categories(RestWrapper restWrapper, RestCategoryModel category)
    {
        super(restWrapper);
        this.category = category;
    }

    /**
     * Retrieves a category with ID using GET call on using GET call on "/tags/{tagId}"
     *
     * @return RestCategoryModel
     */
    public RestCategoryModel getCategory()
    {
        RestRequest request = RestRequest
                .simpleRequest(HttpMethod.GET, "categories/{categoryId}?{parameters}", category.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestCategoryModel.class, request);
    }

    /**
     * Create several categories in one request.
     *
     * @param restCategoryModels The list of categories to create.
     * @return The list of created categories with additional data populated by the repository.
     */
    public RestCategoryModelsCollection createCategoriesList(List<RestCategoryModel> restCategoryModels) {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, arrayToJson(restCategoryModels), "categories/{categoryId}/subcategories", category.getId());
        return restWrapper.processModels(RestCategoryModelsCollection.class, request);
    }

    /**
     * Create single category.
     *
     * @param restCategoryModel The categories to create.
     * @return Created category with additional data populated by the repository.
     */
    public RestCategoryModel createSingleCategory(RestCategoryModel restCategoryModel) {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, restCategoryModel.toJson(), "categories/{categoryId}/subcategories", category.getId());
        return restWrapper.processModel(RestCategoryModel.class, request);
    }

}
