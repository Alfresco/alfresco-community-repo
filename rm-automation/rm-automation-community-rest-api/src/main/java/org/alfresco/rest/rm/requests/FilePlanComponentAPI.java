/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.requests;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.util.ParameterCheck.mandatoryString;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import com.google.gson.JsonObject;

import org.alfresco.rest.core.RestAPI;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentsCollection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * File plan component REST API Wrapper
 *
 * @author Tuna Aksoy
 * @author Kristijan Conkas
 * @since 2.6
 */
@Component
@Scope(value = "prototype")
public class FilePlanComponentAPI extends RestAPI<FilePlanComponentAPI>
{
    /**
     * Get a file plan component
     *
     * @param filePlanComponentId The id of the file plan component to get
     * @return The {@link FilePlanComponent} for the given file plan component id
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code fileplanComponentId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     * </ul>
     */
    public FilePlanComponent getFilePlanComponent(String filePlanComponentId) throws Exception
    {
        mandatoryString("filePlanComponentId", filePlanComponentId);

        return usingRestWrapper().processModel(FilePlanComponent.class, simpleRequest(
                GET,
                "fileplan-components/{fileplanComponentId}?{parameters}",
                filePlanComponentId, getParameters()
        ));
    }

    /**
     * List child components of a file plan component
     *
     * @param filePlanComponentId The id of the file plan component of which to get child components
     * @return The {@link FilePlanComponent} for the given file plan component id
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code fileplanComponentId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     * </ul>
     */
    public FilePlanComponentsCollection listChildComponents(String filePlanComponentId) throws Exception
    {
        mandatoryString("filePlanComponentId", filePlanComponentId);

        return usingRestWrapper().processModels(FilePlanComponentsCollection.class, simpleRequest(
                GET,
                "fileplan-components/{fileplanComponentId}/children",
                filePlanComponentId
        ));
    }

    /**
     * Creates a file plan component with the given properties under the parent node with the given id
     *
     * @param filePlanComponentProperties The properties of the file plan component to be created
     * @param parentId The id of the parent where the new file plan component should be created
     * @return The {@link FilePlanComponent} with the given properties
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code fileplanComponentId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to add children to {@code fileplanComponentId}</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     *  <li>new name clashes with an existing node in the current parent container</li>
     *  <li>model integrity exception, including node name with invalid characters</li>
     * </ul>
     */
    public FilePlanComponent createFilePlanComponent(JsonObject filePlanComponentProperties, String parentId) throws Exception
    {
        mandatoryObject("filePlanComponentProperties", filePlanComponentProperties);
        mandatoryString("parentId", parentId);

        return usingRestWrapper().processModel(FilePlanComponent.class, requestWithBody(
                POST,
                filePlanComponentProperties.toString(),
                "fileplan-components/{fileplanComponentId}/children",
                parentId
        ));
    }

    /**
     * Updates a file plan component
     *
     * @param filePlanComponentProperties The properties to be updated
     * @param filePlanComponentId The id of the file plan component which will be updated
     * @param returns The updated {@link FilePlanComponent}
     * @throws Exception for the following cases:
     * <ul>
     *  <li>the update request is invalid or {@code fileplanComponentId} is not a valid format or {@code filePlanComponentProperties} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to update {@code fileplanComponentId}</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     *  <li>the updated name clashes with an existing node in the current parent folder</li>
     *  <li>model integrity exception, including node name with invalid characters</li>
     * </ul>
     */
    public FilePlanComponent updateFilePlanComponent(JsonObject filePlanComponentProperties, String filePlanComponentId) throws Exception
    {
        mandatoryObject("filePlanComponentProperties", filePlanComponentProperties);
        mandatoryString("filePlanComponentId", filePlanComponentId);

        return usingRestWrapper().processModel(FilePlanComponent.class, requestWithBody(
                PUT,
                filePlanComponentProperties.toString(),
                "fileplan-components/{fileplanComponentId}",
                filePlanComponentId
        ));
    }

    /**
     * Delete file plan component
     *
     * @param filePlanComponentId The id of the file plan component to be deleted
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code fileplanComponentId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to delete {@code fileplanComponentId}</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     *  <li>{@code fileplanComponentId} is locked and cannot be deleted</li>
     * </ul>
     */
    public void deleteFilePlanComponent(String filePlanComponentId) throws Exception
    {
        mandatoryString("filePlanComponentId", filePlanComponentId);

        usingRestWrapper().processEmptyModel(simpleRequest(
                DELETE,
                "fileplan-components/{fileplanComponentId}",
                filePlanComponentId
        ));
    }
}
