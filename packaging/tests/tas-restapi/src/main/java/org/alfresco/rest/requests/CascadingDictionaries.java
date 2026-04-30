/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import org.springframework.http.HttpMethod;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestCDModel;
import org.alfresco.rest.model.RestCDModelsCollection;

/**
 * REST request wrapper for the {@code /cascading-dictionaries} Core API endpoints.
 *
 * <p>Obtain an instance via:
 * <pre>
 *   restClient.authenticateUser(adminUser).withCoreAPI().usingCascadingDictionaries()
 * </pre>
 */
public class CascadingDictionaries extends ModelRequest<CascadingDictionaries>
{
    private static final String ENDPOINT = "cascading-dictionaries";

    public CascadingDictionaries(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * Lists all registered cascading dictionaries.
     * {@code GET /cascading-dictionaries}
     *
     * @return paged collection of {@link RestCDModel}
     * @throws JsonToModelConversionException if the response cannot be deserialized
     */
    public RestCDModelsCollection getCascadingDictionaries() throws JsonToModelConversionException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, ENDPOINT + "?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestCDModelsCollection.class, request);
    }

    /**
     * Retrieves a single cascading dictionary by its aspect prefixed name.
     * {@code GET /cascading-dictionaries/{aspectId}}
     *
     * @param aspectId the prefixed aspect name (e.g. {@code "custom:myAspect"})
     * @return the {@link RestCDModel} for the given aspect
     * @throws JsonToModelConversionException if the response cannot be deserialized
     */
    public RestCDModel getCascadingDictionary(String aspectId) throws JsonToModelConversionException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, ENDPOINT + "/{aspectId}?{parameters}", aspectId, restWrapper.getParameters());
        return restWrapper.processModel(RestCDModel.class, request);
    }

    /**
     * Retrieves a specific version of a cascading dictionary.
     * {@code GET /cascading-dictionaries/{aspectId}/versions/{version}}
     *
     * @param aspectId the prefixed aspect name (e.g. {@code "cdict:account"})
     * @param version  the version string (e.g. {@code "1.0"})
     * @return the {@link RestCDModel} for the given aspect and version
     * @throws JsonToModelConversionException if the response cannot be deserialized
     */
    public RestCDModel getCascadingDictionaryVersion(String aspectId, String version) throws JsonToModelConversionException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, ENDPOINT + "/{aspectId}/versions/{version}?{parameters}", aspectId, version, restWrapper.getParameters());
        return restWrapper.processModel(RestCDModel.class, request);
    }
}
