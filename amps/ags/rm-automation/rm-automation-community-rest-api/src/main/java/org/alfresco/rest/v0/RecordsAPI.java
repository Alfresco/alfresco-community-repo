/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.v0;

import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.AssertJUnit.assertTrue;

import java.text.MessageFormat;
import java.util.Map;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.core.v0.BaseAPI;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Methods to make API requests using v0 API on records
 *
 * @author Oana Nechiforescu
 * @since 2.5
 */
@Component
public class RecordsAPI extends BaseAPI
{
    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsAPI.class);

    private static final String CREATE_NON_ELECTRONIC_RECORD_API = "{0}type/rma:nonElectronicDocument/formprocessor";

    /**
     * Declare documents as records
     *
     * @param user         the user declaring the document as record
     * @param password     the user's password
     * @param siteID       the site id in which the document exists
     * @param documentName the document name
     * @return The HTTP Response.
     */
    public HttpResponse declareDocumentAsRecord(String user, String password, String siteID, String documentName)
    {
        String docNodeRef = getNodeRefSpacesStore() + contentService.getNodeRef(user, password, siteID, documentName);

        JSONObject requestParams = new JSONObject();
        requestParams.put("actionedUponNode", docNodeRef);
        requestParams.put("actionDefinitionName", "create-record");

        return doPostJsonRequest(user, password, SC_OK, requestParams, ACTIONS_API);
    }

    /**
     * Completes the record given as parameter
     *
     * @param user       the user declaring the document as record
     * @param password   the user's password
     * @param recordName the record name
     * @return The HTTP Response.
     */
    public HttpResponse completeRecord(String user, String password, String recordName)
    {
        String recNodeRef = getNodeRefSpacesStore() + contentService.getNodeRef(user, password, RM_SITE_ID, recordName);

        JSONObject requestParams = new JSONObject();
        requestParams.put("name", "declareRecord");
        requestParams.put("nodeRef", recNodeRef);

        return doPostJsonRequest(user, password, SC_OK, requestParams, RM_ACTIONS_API);
    }

    /**
     * Reject the record given as parameter
     *
     * @param user       the user declaring the document as record
     * @param password   the user's password
     * @param recordName the record name
     * @param reason     reject reason
     * @return The HTTP Response.
     * @throws AssertionError If the POST call is not successful.
     */
    public HttpResponse rejectRecord(String user, String password, String recordName, String reason)
    {
        return rejectRecord(user, password, SC_OK, recordName, reason);
    }

    /**
     * Reject the record given as parameter
     *
     * @param user the user declaring the document as record
     * @param password the user's password
     * @param expectedStatusCode The expected return status code.
     * @param recordName the record name
     * @param reason reject reason
     * @return The HTTP Response.
     * @throws AssertionError If the expectedStatusCode was not returned.
     */
    public HttpResponse rejectRecord(String user, String password, int expectedStatusCode, String recordName, String reason)
    {
        String recNodeRef = getNodeRefSpacesStore() + contentService.getNodeRef(user, password, RM_SITE_ID, recordName);

        JSONObject requestParams = new JSONObject();
        requestParams.put("name", "reject");
        requestParams.put("nodeRef", recNodeRef);
        requestParams.put("params",new JSONObject()
                    .put("reason",reason));

        return doPostJsonRequest(user, password, expectedStatusCode, requestParams, RM_ACTIONS_API);
    }

    /**
     * Declare document version as record
     *
     * @param user         the user declaring the document version as record
     * @param password     the user's password
     * @param siteID       the site id in which the document exists
     * @param documentName the document name
     * @return The HTTP Response.
     */
    public HttpResponse declareDocumentVersionAsRecord(String user, String password, String siteID, String documentName)
    {
        String docNodeRef = getNodeRefSpacesStore() + contentService.getNodeRef(user, password, siteID, documentName);

        JSONObject requestParams = new JSONObject();
        requestParams.put("actionedUponNode", docNodeRef);
        requestParams.put("actionDefinitionName", "declare-as-version-record");

        return doPostJsonRequest(user, password, SC_OK, requestParams, ACTIONS_API);
    }

    /**
     * Creates a non-electronic record
     * <ul>
     * <li>eg. of usage for Unfiled records  with folder : createNonElectronicRecord(getAdminName(), getAdminPassword(), properties, UNFILED_RECORDS_BREADCRUMB, "unfiled records folder");
     * <li>eg. of usage for creating record directly in Unfiled Records : createNonElectronicRecord(getAdminName(), getAdminPassword(), properties, UNFILED_RECORDS_BREADCRUMB, "");
     * </ul>
     *
     * @param username     the username
     * @param password     the password
     * @param properties   a map of record properties and their values
     * @param categoryName the category that contains the record, in the case in which the container would be Unfiled records use UNFILED_RECORDS_BREADCRUMB as value
     * @param folderName   the folder inside which the record exists, in the case in which the folder name is "", the record will be created directly in the specified container
     *                     this case is useful when trying to create a record directly in Unfiled Records
     * @return The HTTP Response (or null if the request was not needed).
     */
    public <K extends Enum<?>> HttpResponse createNonElectronicRecord(String username, String password, Map<K, String> properties, String categoryName, String folderName)
    {
        String recordName = properties.get(RMProperty.NAME);
        if (getRecord(username, password, folderName, recordName) != null)
        {
            return null;
        }
        String recordPath = "/" + categoryName;
        if (!folderName.equals(""))
        {
            recordPath = recordPath + "/" + folderName;
        }
        // if the record already exists don't try to create it again
        CmisObject record = getObjectByPath(username, password, getFilePlanPath() + recordPath + "/" + recordName);

        if (record != null)
        {
            return null;
        }
        // non-electronic properties
        String recordTitle = getPropertyValue(properties, RMProperty.TITLE);
        String description = getPropertyValue(properties, RMProperty.DESCRIPTION);
        String physicalSize = getPropertyValue(properties, RMProperty.PHYSICAL_SIZE);
        String numberOfCopies = getPropertyValue(properties, RMProperty.NUMBER_OF_COPIES);
        String shelf = getPropertyValue(properties, RMProperty.SHELF);
        String storage = getPropertyValue(properties, RMProperty.STORAGE_LOCATION);
        String box = getPropertyValue(properties, RMProperty.BOX);
        String file = getPropertyValue(properties, RMProperty.FILE);

        // retrieve the container nodeRef
        String parentNodeRef = getItemNodeRef(username, password, recordPath);

        JSONObject requestParams = new JSONObject();
        requestParams.put("alf_destination", getNodeRefSpacesStore() + parentNodeRef);
        requestParams.put("prop_cm_name", recordName);
        requestParams.put("prop_cm_title", recordTitle);
        requestParams.put("prop_cm_description", description);
        requestParams.put("prop_rma_physicalSize", physicalSize);
        requestParams.put("prop_rma_numberOfCopies", numberOfCopies);
        requestParams.put("prop_rma_storageLocation", storage);
        requestParams.put("prop_rma_shelf", shelf);
        requestParams.put("prop_rma_box", box);
        requestParams.put("prop_rma_file", file);

        return doPostJsonRequest(username, password, SC_OK, requestParams, CREATE_NON_ELECTRONIC_RECORD_API);
    }

    /**
     * Uploads an electronic record
     * <p>
     * eg. of usage for creating record directly in Unfiled Records : uploadElectronicRecord(getAdminName(), getAdminPassword(), recordPropertiesStringMap, UNFILED_RECORDS_BREADCRUMB, DocumentType.HTML)
     * @param username   the username
     * @param password   the password
     * @param properties a map of record properties and their values
     * @param folderName the folder inside which the record will be created, it needs to have a unique name, as this method doesn't check other containers than the folder name
     * @throws AssertionError if the upload was unsuccessful.
     */
    public void uploadElectronicRecord(String username, String password, Map<RMProperty, String> properties, String folderName, DocumentType documentType)
    {
        String recordName = getPropertyValue(properties, RMProperty.NAME);
        String recordContent = getPropertyValue(properties, RMProperty.CONTENT);
        boolean success = (getRecord(username, password, folderName, recordName) != null) || (contentService.createDocumentInFolder(username, password, RM_SITE_ID, folderName, documentType, recordName, recordContent) != null);
        assertTrue("Failed to upload electronic record to " + folderName, success);
    }

    /**
     * Delete a record from the given path
     * <ul>
     * <li>eg. of usage in the case in which the record is inside a folder in Unfiled Records : deleteRecord(getAdminName(), getAdminPassword(), "f1 (2016-1472716888713)", UNFILED_RECORDS_BREADCRUMB, "unfiled records folder");
     * <li>eg. of usage in the case in which the record is created directly in Unfiled Records : deleteRecord(getAdminName(), getAdminPassword(), "f1 (2016-1472716888713)", UNFILED_RECORDS_BREADCRUMB, "");
     * </ul>
     * @param username     user's username
     * @param password     its password
     * @param recordName   the record name
     * @param categoryName the name of the category in which the folder is, in case of unfiled record, this will have UNFILED_RECORDS_BREADCRUMB as container
     * @param folderName   folder name, in case in which trying to delete a record in Unfiled records directly, this will be ""
     * @throws AssertionError If the record could not be deleted.
     */
    public void deleteRecord(String username, String password, String recordName, String categoryName, String folderName)
    {
        String recordPath = "/" + categoryName;
        if (!folderName.equals(""))
        {
            recordPath = recordPath + "/" + folderName;
        }
        deleteItem(username, password, recordPath + "/" + recordName);
    }

    /**
     * Retrieves the record object in case it exists
     *
     * @param username   the user's username
     * @param password   its password
     * @param folderName the folder in which the record is supposed to exist
     * @param recordName the String with which the record name starts
     * @return the record object in case it exists, null otherwise
     */
    private CmisObject getRecord(String username, String password, String folderName, String recordName)
    {
        for (CmisObject record : contentService.getFolderObject(contentService.getCMISSession(username, password), RM_SITE_ID, folderName).getChildren())
        {
            if (record.getName().startsWith(recordName))
            {
                return record;
            }
        }
        return null;
    }

    /**
     * Retrieves record full name for given partial name
     *
     * @param username          the user's username
     * @param password          its password
     * @param folderName        the folder in which the record is supposed to exist
     * @param recordPartialName the String with which the record name starts
     * @return the record name in case it exists, empty String otherwise
     */
    public String getRecordFullName(String username, String password, String folderName, String recordPartialName)
    {
        CmisObject record = getRecord(username, password, folderName, recordPartialName);
        if (record != null)
        {
            return record.getName();
        }
        return "";
    }


    /**
     * Share a document
     *
     * @param user     the user sharing the file
     * @param password the user's password
     * @param nodeId   the node id of the file
     * @return {@link Pair}. on success will be true and the shareId.
     * on failure will be false and the response status code.
     */
    public Pair<Boolean, String> shareDocument(String user, String password, String nodeId) throws JSONException
    {
        JSONObject response = doPostRequest(user, password, null,
                MessageFormat.format(SHARE_ACTION_API, "{0}", nodeId));
        try
        {
            if (response.has("sharedId"))
            {
                return Pair.of(true, response.getString("sharedId"));
            }
        } catch (JSONException e)
        {
            LOGGER.info("Unable to extract response parameter", e);
        }
        return Pair.of(false, String.valueOf(response.getJSONObject("status").getInt("code")));
    }

    /**
     * Hide in place record
     *
     * @param user         the user
     * @param password     the user's password
     * @param nodeId     the in place record node id
     * @return The HTTP Response.
     */
    public HttpResponse hideRecord(String user, String password, String nodeId)
    {
        String docNodeRef = getNodeRefSpacesStore() + nodeId;

        JSONObject requestParams = new JSONObject();
        requestParams.put("actionedUponNode", docNodeRef);
        requestParams.put("actionDefinitionName", "hide-record");

        return doPostJsonRequest(user, password, SC_OK, requestParams, ACTIONS_API);
    }

    /**
     * Retrieves the record's nodeRef
     *
     * @param username   the user's username
     * @param password   its password
     * @param recordName the record full name
     * @param recordPath the String with which the record name starts
     * @return the record nodeRef in case it exists, empty string otherwise
     */
    public String getRecordNodeRef(String username, String password, String recordName, String recordPath)
    {
        return getNodeRefSpacesStore() + getItemNodeRef(username, password, recordPath + "/" + recordName);
    }
}
