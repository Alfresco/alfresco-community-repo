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
package org.alfresco.rest.api.fileplancomponents;

import static io.restassured.RestAssured.authentication;
import static io.restassured.RestAssured.basePath;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.enableLoggingOfRequestAndResponseIfValidationFails;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;

import static java.util.UUID.randomUUID;

import static com.google.common.collect.ImmutableMap.builder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * FIXME: Document me :)
 *
 * @author Tuna Aksoy
 * @since 1.0
 */
public class FilePlanComponentsTest
{
    private static final String CONTENT_TYPE_KEY = "content-type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";

    /**
     * FIXME: Document me :)
     * FIXME: Create a base class and move this method, extract to properties to a file, etc. etc.
     *
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        baseURI = "http://localhost";
        port = 8090;
        basePath = "/alfresco/api/-default-/public/ig/versions/1";
        authentication = basic("admin", "admin");
        enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testfilePlanComponentsGet()
    {
        given().
        when().
            get("/fileplan-components/-filePlan-").
        then().
            statusCode(200).
            header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE).
            root("entry").
            body("isFile", is(false)).
            body("nodeType", equalTo("rma:filePlan")).
            body("aspectNames", hasItems("rma:recordComponentIdentifier", "rma:filePlanComponent", "rma:recordsManagementRoot")).
            body("properties.\"st:componentId\"", equalTo("documentLibrary"));

        /*
        given().
        expect().
            body("entry.isFile", is(false)).
            body("entry.nodeType", equalTo("rma:filePlan")).
            body("entry.aspectNames", hasItems("rma:recordComponentIdentifier", "rma:filePlanComponent", "rma:recordsManagementRoot")).
            body("entry.properties.\"st:componentId\"", equalTo("documentLibrary")).
            statusCode(200).
            header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE).
        when().
            get("/fileplan-components/-filePlan-");



        get("/fileplan-components/-filePlan-").
            then().
            assertThat().
            statusCode(200).
            header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE).
            body("entry.isFile", is(false)).
            body("entry.nodeType", equalTo("rma:filePlan")).
            body("entry.aspectNames", hasItems("rma:recordComponentIdentifier", "rma:filePlanComponent", "rma:recordsManagementRoot")).
            body("entry.properties.\"st:componentId\"", equalTo("documentLibrary"));



        given().
        when().
            get("/fileplan-components/-filePlan-").
        then().
            statusCode(200).
            header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE).
            body("entry.isFile", is(false)).
            body("entry.nodeType", equalTo("rma:filePlan")).
            body("entry.aspectNames", hasItems("rma:recordComponentIdentifier", "rma:filePlanComponent", "rma:recordsManagementRoot")).
            body("entry.properties.\"st:componentId\"", equalTo("documentLibrary"));
        */


        given().
            contentType("application/json").
            body(
                builder().
                put("name", randomUUID().toString()).
                put("nodeType", "rma:recordCategory").
                build()).
        when().
            post("/fileplan-components/-filePlan-/children").
        then().
            statusCode(201).
            header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);


        /*
        given().
            contentType("application/json").
            body("{\"name\":\"" + randomUUID().toString() + "\",\"nodeType\":\"rma:recordCategory\"}").
        when().
            post("/fileplan-components/-filePlan-/children").
        then().
            statusCode(201).
            header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);



        Map<String,String> category = new HashMap<>();
        category.put("name", randomUUID().toString());
        category.put("nodeType", "rma:recordCategory");

        given().
            contentType("application/json").
            body(category).
        when().
            post("/fileplan-components/-filePlan-/children").
        then().
            //log().ifValidationFails().
            statusCode(201).
            header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
        */
    }
}
