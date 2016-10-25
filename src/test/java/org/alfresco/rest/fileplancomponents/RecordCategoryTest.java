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
package org.alfresco.rest.fileplancomponents;

import static java.util.UUID.randomUUID;

import static org.alfresco.com.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.com.FilePlanComponentFields.NAME;
import static org.alfresco.com.FilePlanComponentFields.NODE_TYPE;
import static org.alfresco.com.FilePlanComponentFields.PROPERTIES;
import static org.alfresco.com.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.com.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.gson.JsonObject;

import org.alfresco.rest.BaseRestTest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.FilePlanComponent;
import org.alfresco.rest.model.FilePlanComponentProperties;
import org.alfresco.rest.requests.FilePlanComponentApi;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * FIXME: Document me :)
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 1.0
 */
public class RecordCategoryTest extends BaseRestTest
{
    @Autowired
    private FilePlanComponentApi filePlanComponentApi;

    @Autowired
    private DataUser dataUser;

    @Test
    (
        description = "Create category as authorised user"
    )
    public void createCategoryAsAuthorisedUser() throws Exception
    {
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        String categoryName = "Category name " + randomUUID().toString().substring(0, 8);
        String categoryTitle = "Category title " + randomUUID().toString().substring(0, 8);

        // Build the record category properties
        JsonObject recordCategoryProperties = buildObject().
                add(NAME, categoryName).
                add(NODE_TYPE, RECORD_CATEGORY_TYPE.toString()).
                addObject(PROPERTIES).
                    add(PROPERTIES_TITLE, categoryTitle).
                    end().
                getJson();

        // Create the record category
        FilePlanComponent filePlanComponent = filePlanComponentApi.createFilePlanComponent(recordCategoryProperties, FILE_PLAN_ALIAS.toString());

        // Verify the status code
        restWrapper.assertStatusCodeIs(CREATED);

        // Verify the returned file plan component
        assertTrue(filePlanComponent.isIsCategory());
        assertEquals(filePlanComponent.getName(), categoryName);
        assertEquals(filePlanComponent.getNodeType(), RECORD_CATEGORY_TYPE.toString());

        // Verify the returned file plan component properties
        FilePlanComponentProperties filePlanComponentProperties = filePlanComponent.getProperties();
        assertEquals(filePlanComponentProperties.getTitle(), categoryTitle);
    }

    @Test
    (
        description = "Rename category as authorised user"
    )
    public void renameCategoryAsAuthorisedUser() throws Exception
    {
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Create record category first

        String categoryName = "Category name " + randomUUID().toString().substring(0, 8);
        String categoryTitle = "Category title " + randomUUID().toString().substring(0, 8);

        // Build the record category properties
        JsonObject recordCategoryProperties = buildObject().
                add(NAME, categoryName).
                add(NODE_TYPE, RECORD_CATEGORY_TYPE.toString()).
                addObject(PROPERTIES).
                    add(PROPERTIES_TITLE, categoryTitle).
                    end().
                getJson();

        // Create the record category
        FilePlanComponent filePlanComponent = filePlanComponentApi.createFilePlanComponent(recordCategoryProperties, FILE_PLAN_ALIAS.toString());


        String newCategoryName = "Rename " + categoryName;

        // Build the properties which will be updated
        JsonObject updateRecordCategoryProperties = buildObject().
                add(NAME, newCategoryName).
                getJson();

        // Update the record category
        FilePlanComponent renamedFilePlanComponent = filePlanComponentApi.updateFilePlanComponent(updateRecordCategoryProperties, filePlanComponent.getId());

        // Verify the status code
        restWrapper.assertStatusCodeIs(OK);

        // Verify the returned file plan component
        assertEquals(renamedFilePlanComponent.getName(), newCategoryName);
    }

    @Test
    (
        description = "Rename category as authorised user"
    )
    public void deleteCategoryAsAuthorisedUser() throws Exception
    {
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Create record category first

        String categoryName = "Category name " + randomUUID().toString().substring(0, 8);
        String categoryTitle = "Category title " + randomUUID().toString().substring(0, 8);

        // Build the record category properties
        JsonObject recordCategoryProperties = buildObject().
                add(NAME, categoryName).
                add(NODE_TYPE, RECORD_CATEGORY_TYPE.toString()).
                addObject(PROPERTIES).
                    add(PROPERTIES_TITLE, categoryTitle).
                    end().
                getJson();

        // Create the record category
        FilePlanComponent filePlanComponent = filePlanComponentApi.createFilePlanComponent(recordCategoryProperties, FILE_PLAN_ALIAS.toString());

        // Delete the record category
        filePlanComponentApi.deleteFilePlanComponent(filePlanComponent.getId());

        // Verify the status code
        restWrapper.assertStatusCodeIs(NO_CONTENT);
    }
}
