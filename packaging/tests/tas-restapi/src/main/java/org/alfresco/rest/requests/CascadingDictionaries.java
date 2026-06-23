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
 * Declares all Rest API under the /cascading-dictionaries path
 */
public class CascadingDictionaries extends ModelRequest<CascadingDictionaries>
{
    private static final String ENDPOINT = "cascading-dictionaries";

    public CascadingDictionaries(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * Lists all registered cascading dictionaries. {@code GET /cascading-dictionaries}
     *
     * @return paged collection of {@link RestCDModel}
     * @throws JsonToModelConversionException
     *             if the response cannot be deserialized
     */
    public RestCDModelsCollection getCascadingDictionaries() throws JsonToModelConversionException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, ENDPOINT + "?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestCDModelsCollection.class, request);
    }

    /**
     * Retrieves a single cascading dictionary by its aspect prefixed name. {@code GET /cascading-dictionaries/{aspectId}}
     *
     * @param aspectId
     *            the prefixed aspect name (e.g. {@code "custom:myAspect"})
     * @return the {@link RestCDModel} for the given aspect
     * @throws JsonToModelConversionException
     *             if the response cannot be deserialized
     */
    public RestCDModel getCascadingDictionary(String aspectId) throws JsonToModelConversionException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, ENDPOINT + "/{aspectId}?{parameters}", aspectId, restWrapper.getParameters());
        return restWrapper.processModel(RestCDModel.class, request);
    }

    /**
     * Retrieves a specific version of a cascading dictionary. {@code GET /cascading-dictionaries/{aspectId}/versions/{version}}
     *
     * @param aspectId
     *            the prefixed aspect name (e.g. {@code "cdict:account"})
     * @param version
     *            the version string (e.g. {@code "1.0"})
     * @return the {@link RestCDModel} for the given aspect and version
     * @throws JsonToModelConversionException
     *             if the response cannot be deserialized
     */
    public RestCDModel getCascadingDictionaryVersion(String aspectId, String version) throws JsonToModelConversionException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, ENDPOINT + "/{aspectId}/versions/{version}?{parameters}", aspectId, version, restWrapper.getParameters());
        return restWrapper.processModel(RestCDModel.class, request);
    }

    /**
     * Creates a new cascading dictionary definition. {@code POST /cascading-dictionaries}
     *
     * @param name
     *            human-readable name for the dictionary (e.g. {@code "Department"})
     * @param aspect
     *            the prefixed aspect name that must already exist in the content model (e.g. {@code "cdict:department"})
     * @param keyProperty
     *            the prefixed property name used as the lookup key (e.g. {@code "cdict:departmentId"})
     * @param versionProperty
     *            the prefixed property name holding the dictionary version (e.g. {@code "cdict:departmentDictVersion"})
     * @return the created {@link RestCDModel}
     * @throws JsonToModelConversionException
     *             if the response cannot be deserialized
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI") // four distinct semantic parameters — a wrapper object adds no clarity here
    public RestCDModel createCascadingDictionary(String name, String aspect, String keyProperty, String versionProperty)
            throws JsonToModelConversionException
    {
        String body = String.format(
                "{\"name\":\"%s\",\"aspect\":\"%s\",\"keyProperty\":\"%s\",\"versionProperty\":\"%s\"}",
                name, aspect, keyProperty, versionProperty);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, body, ENDPOINT);
        return restWrapper.processModel(RestCDModel.class, request);
    }

    /**
     * Adds content (data + level definitions) to a cascading dictionary, creating a new version. Use {@code usingParams("majorVersion=true")} before calling to produce version {@code 1.0}; the default produces a minor version (e.g. {@code 0.1}). {@code POST /cascading-dictionaries/{aspectId}/content}
     *
     * @param aspectId
     *            the prefixed aspect name (e.g. {@code "cdict:department"})
     * @param contentJson
     *            the JSON body with {@code definition} (levels) and {@code data} (entries)
     * @return the {@link RestCDModel} for the resulting dictionary version
     * @throws JsonToModelConversionException
     *             if the response cannot be deserialized
     */
    public RestCDModel addCascadingDictionaryContent(String aspectId, String contentJson)
            throws JsonToModelConversionException
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, contentJson,
                ENDPOINT + "/{aspectId}/content?{parameters}", aspectId, restWrapper.getParameters());
        return restWrapper.processModel(RestCDModel.class, request);
    }

    /**
     * Updates the header (name) of an existing cascading dictionary. {@code PUT /cascading-dictionaries/{aspectId}}
     *
     * @param aspectId
     *            the prefixed aspect name (e.g. {@code "cdict:department"})
     * @param newName
     *            the new human-readable name for the dictionary
     * @return the updated {@link RestCDModel}
     * @throws JsonToModelConversionException
     *             if the response cannot be deserialized
     */
    public RestCDModel updateCascadingDictionary(String aspectId, String newName)
            throws JsonToModelConversionException
    {
        String body = String.format("{\"name\":\"%s\"}", newName);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, body, ENDPOINT + "/{aspectId}", aspectId);
        return restWrapper.processModel(RestCDModel.class, request);
    }

    /**
     * Deletes a cascading dictionary definition by its aspect prefixed name. {@code DELETE /cascading-dictionaries/{aspectId}}
     *
     * @param aspectId
     *            the prefixed aspect name (e.g. {@code "cdict:department"})
     */
    public void deleteCascadingDictionary(String aspectId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, ENDPOINT + "/{aspectId}", aspectId);
        restWrapper.processEmptyModel(request);
    }
}
