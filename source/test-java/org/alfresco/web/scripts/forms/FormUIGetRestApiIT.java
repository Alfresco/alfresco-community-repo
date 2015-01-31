/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.web.scripts.forms;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.web.config.forms.FormConfigElement;
import org.alfresco.web.config.forms.FormField;
import org.alfresco.web.config.forms.FormsConfigElement;
import org.alfresco.web.config.forms.NodeTypeEvaluator;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigSource;
import org.springframework.extensions.config.source.ClassPathConfigSource;
import org.springframework.extensions.config.xml.XMLConfigService;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.extensions.webscripts.connector.ResponseStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONTokener;
import org.alfresco.repo.web.scripts.servlet.LocalTestRunAsAuthenticatorFactory;

public class FormUIGetRestApiIT extends TestCase
{
    private static final Log log = LogFactory.getLog(FormUIGetRestApiIT.class);

    private final static String[] CONFIG_LOCATIONS = new String[] { "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
            "classpath:alfresco/web-scripts-application-context-webframework-test.xml" };

    private static ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);
    
    private static TestWebScriptServer server = (TestWebScriptServer) ctx.getBean("webscripts.web.framework.test");

    protected NodeService nodeService;
    protected FileFolderService fileFolderService;
    protected Repository repositoryHelper;
    protected NodeRef containerNodeRef;
    protected TransactionService transactionService;

    private NodeRef folderWithoutAspect = null;
    private NodeRef folderWithAspect = null;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.fileFolderService = (FileFolderService) ctx.getBean("FileFolderService");
        this.repositoryHelper = (Repository) ctx.getBean("repositoryHelper");
        this.nodeService = (NodeService) ctx.getBean("NodeService");
        this.transactionService = (TransactionService) ctx.getBean("transactionService");

        server.setServletAuthenticatorFactory(new LocalTestRunAsAuthenticatorFactory());
        
        NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
        String guid = GUID.generate();

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        folderWithoutAspect = fileFolderService.create(companyHomeNodeRef, "folder_" + guid, ContentModel.TYPE_FOLDER).getNodeRef();
        assertNotNull("Doesn't create folder", folderWithoutAspect);

        folderWithAspect = fileFolderService.create(companyHomeNodeRef, "folder_aspect_" + guid, ContentModel.TYPE_FOLDER).getNodeRef();
        assertNotNull("Doesn't create folder", folderWithoutAspect);
        // add 'dublincore' aspect
        Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>(1);
        aspectProps.put(ContentModel.PROP_SUBJECT, "Test subject");
        nodeService.addAspect(folderWithAspect, ContentModel.ASPECT_DUBLINCORE, aspectProps);
    }

    @Override
    public void tearDown()
    {
        if (folderWithoutAspect != null && fileFolderService.exists(folderWithoutAspect))
        {
            fileFolderService.delete(folderWithoutAspect);
        }

        if (folderWithAspect != null && fileFolderService.exists(folderWithAspect))
        {
            fileFolderService.delete(folderWithAspect);
        }
    }

    public void testMNT11660() throws Exception
    {
        FormUIGet formUIGet = (FormUIGetExtend) ctx.getBean("webscript.org.alfresco.test.components.form.form.get");
        assertNotNull("'FormUIGetExtend' bean for test is null.", formUIGet);

        ConfigSource configSource = new ClassPathConfigSource("test-config-custom-forms.xml");
        XMLConfigService svc = new XMLConfigService(configSource);
        svc.initConfig();

        formUIGet.setConfigService(svc);

        GetRequest requestWithAspect = new GetRequest("/test/components/form?htmlid=template_default-formContainer&itemKind=node&itemId=" + folderWithAspect.toString()
                + "&formId=null&mode=view");
        Response rspFormWithAspect = server.submitRequest(requestWithAspect.getMethod(), requestWithAspect.getFullUri(), requestWithAspect.getHeaders(),
                requestWithAspect.getBody(), requestWithAspect.getEncoding(), requestWithAspect.getType());

        assertEquals("The status of response is " + rspFormWithAspect.getStatus(), 200, rspFormWithAspect.getStatus());

        String contentWithAspect = rspFormWithAspect.getContentAsString();
        log.info("Response form for node with dublincore aspect status is " + rspFormWithAspect.getStatus() + " content is " + contentWithAspect);
        assertNotNull("Response content for 'contentWithAspect' is null", contentWithAspect);
        assertTrue("Return the following content: " + contentWithAspect, contentWithAspect.contains("My Set"));

        GetRequest requestWithoutAspect = new GetRequest("/test/components/form?htmlid=template_default-formContainer&itemKind=node&itemId=" + folderWithoutAspect.toString()
                + "&formId=null&mode=view");
        Response rspFormWithoutAspect = server.submitRequest(requestWithoutAspect.getMethod(), requestWithoutAspect.getFullUri(), requestWithoutAspect.getHeaders(),
                requestWithoutAspect.getBody(), requestWithoutAspect.getEncoding(), requestWithoutAspect.getType());

        assertEquals("The status of response is " + rspFormWithoutAspect.getStatus(), 200, rspFormWithoutAspect.getStatus());

        String contentWithoutAspect = rspFormWithoutAspect.getContentAsString();
        log.info("Response form for node without aspect status is " + rspFormWithoutAspect.getStatus() + " content is " + contentWithoutAspect);
        assertNotNull("Response content for 'contentWithoutAspect' is null", contentWithoutAspect);
        assertFalse("Return the following content: " + contentWithoutAspect, contentWithoutAspect.contains("My Set"));
    }

    private static class FormUIGetExtend extends FormUIGet
    {   
        @Override
        protected FormConfigElement getFormConfig(String itemId, String formId)
        {
            FormConfigElement formConfig = null;

            Config configResult = this.configService.getConfig(itemId);
            FormsConfigElement formsConfig = (FormsConfigElement) configResult.getConfigElement(CONFIG_FORMS);

            assertNotNull("The ConfigElement object doesn't exist", formsConfig);

            if (formsConfig != null)
            {
                // Extract the form we are looking for
                if (formsConfig != null)
                {
                    // try and retrieve the specified form
                    if (formId != null && formId.length() > 0)
                    {
                        formConfig = formsConfig.getForm(formId);
                    }

                    // fall back to the default form
                    if (formConfig == null)
                    {
                        formConfig = formsConfig.getDefaultForm();
                    }
                }
            }
            assertNotNull("The ConfigElement object doesn't exist", formConfig);

            return formConfig;
        }

        private String getStringFromInputStream(InputStream is)
        {

            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try
            {

                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null)
                {
                    sb.append(line);
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (br != null)
                {
                    try
                    {
                        br.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            assertTrue("StringBuilder has 0 length", sb.length() > 0);
            
            return sb.toString();

        }

        @Override
        protected org.springframework.extensions.webscripts.connector.Response retrieveFormDefinition(String itemKind, String itemId, List<String> visibleFields,
                FormConfigElement formConfig)
        {
            org.springframework.extensions.webscripts.connector.Response response = null;
            try
            {
                assertEquals("Parameter 'itemKind' isn't 'node' value", "node", itemKind);
                assertFalse("itemId is empty", itemId != null && itemId.isEmpty());
                assertTrue("Visible fields are empty", visibleFields.size() > 0);
                assertEquals("Form config 'name' field isn't 'form' value", "form", formConfig.getName());
                
                ByteArrayInputStream bais = generateFormDefPostBody(itemKind, itemId, visibleFields, formConfig);
                assertTrue("Can't read bytes from ByteArrayInputStream ", bais.available() > 0);

                String json = getStringFromInputStream(bais);

                log.info("Request 'formdefinitions' json is: " + json);
                
                PostRequest request = new PostRequest("/api/formdefinitions", json, "application/json");

                org.springframework.extensions.webscripts.TestWebScriptServer.Response responseTest = server.submitRequest(request.getMethod(), request.getFullUri(),
                        request.getHeaders(), request.getBody(), request.getEncoding(), request.getType());

                if (responseTest.getStatus() == 200)
                {
                    JSONObject jsonParsedObject = new JSONObject(new JSONTokener(responseTest.getContentAsString()));
                    assertNotNull("JSON from responseTest is null", jsonParsedObject);
                    
                    Object dataObj = jsonParsedObject.get("data");
                    assertEquals(JSONObject.class, dataObj.getClass());
                    JSONObject rootDataObject = (JSONObject)dataObj;
                    
                    String item = (String)rootDataObject.get("item");
                    String submissionUrl = (String)rootDataObject.get("submissionUrl");
                    String type = (String)rootDataObject.get("type");
                    JSONObject definitionObject = (JSONObject)rootDataObject.get("definition");
                    JSONObject formDataObject = (JSONObject)rootDataObject.get("formData");
                    
                    assertNotNull("Item is null ", item);
                    assertNotNull("Submission url is null ", submissionUrl);
                    assertNotNull("Type is null ", type);
                    assertNotNull("Definition is null ", definitionObject);
                    assertNotNull("Form data is null ", formDataObject);
                    
                    log.info("Response form 'formdefinitions' json 'data' is: " + dataObj);
                    
                    ResponseStatus status = new ResponseStatus();
                    status.setCode(responseTest.getStatus());
                    assertFalse("Response content is empty", responseTest.getContentAsString().isEmpty());
                    response = new org.springframework.extensions.webscripts.connector.Response(responseTest.getContentAsString(), status);
                    assertNotNull("Response data is null.", response.getText());
                }
                else
                {
                    assertEquals("Response /api/formdefinitions is not 200 status", 200, responseTest.getStatus());
                }

            }
            catch (Exception e)
            {
                log.error("Response form 'formdefinitions' exception : " + e.getMessage());
            }

            return response;
        }

        @Override
        protected Field generateFieldModel(ModelContext context, String fieldName, FormField fieldConfig)
        {
            Field field = null;

            try
            {
                // make sure the field is not ambiguous
                if (isFieldAmbiguous(context, fieldName))
                {
                    field = generateTransientFieldModel(fieldName, "/org/alfresco/components/form/controls/ambiguous.ftl");
                }
                else
                {
                    JSONObject fieldDefinition = discoverFieldDefinition(context, fieldName);

                    if (fieldDefinition != null)
                    {
                        // create the initial field model
                        field = new Field();

                        // populate the model with the appropriate data
                        processFieldIdentification(context, field, fieldDefinition, fieldConfig);
                        processFieldState(context, field, fieldDefinition, fieldConfig);
                        processFieldText(context, field, fieldDefinition, fieldConfig);
                        processFieldData(context, field, fieldDefinition, fieldConfig);
                        processFieldContent(context, field, fieldDefinition, fieldConfig);
                    }
                    else
                    {
                        // the field does not have a definition but may be a 'transient' field
                        field = generateTransientFieldModel(context, fieldName, fieldDefinition, fieldConfig);
                    }
                }
            }
            catch (JSONException je)
            {
                field = null;
                log.error("Generate field model exception: " + je.getMessage());
            }

            log.info("Generated field model " + fieldName + " is null");

            return field;
        }

}

    public static class NodeTypeEvaluatorExtend extends NodeTypeEvaluator
    {
        protected String callMetadataService(String nodeString) throws ConnectorServiceException
        {
            GetRequest request = new GetRequest("/api/metadata?nodeRef=" + nodeString + "&shortQNames=true");
            Response response = null;
            String jsonResponse = null;
            try
            {
                response = server.submitRequest(request.getMethod(), request.getFullUri(), request.getHeaders(), request.getBody(), request.getEncoding(), request.getType());

                if (response != null)
                {
                    jsonResponse = response.getContentAsString();
                }
            }
            catch (UnsupportedEncodingException e)
            {
                throw new AlfrescoRuntimeException(e.getMessage());
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException(e.getMessage());
            }

            assertNotNull("Response /api/metadata is null", jsonResponse);

            log.info("Response /api/metadata json is: " + jsonResponse);

            return jsonResponse;
        }
    }
}
