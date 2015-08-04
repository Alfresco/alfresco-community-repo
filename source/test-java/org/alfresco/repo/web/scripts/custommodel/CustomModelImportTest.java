/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.custommodel;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.CustomModelServiceImpl;
import org.alfresco.repo.dictionary.M2Association;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.XMLUtil;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This class tests the custom model upload REST API.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelImportTest extends BaseWebScriptTest
{
    private static final String NON_ADMIN_USER = "nonAdminUserName";
    private static final String CUSTOM_MODEL_ADMIN = "customModelAdmin";
    private static final String RESOURCE_PREFIX = "custommodel/";
    private static final String UPLOAD_URL = "/api/cmm/upload";
    private static final int BUFFER_SIZE = 20 * 1024;

    private MutableAuthenticationService authenticationService;
    private AuthorityService authorityService;
    private PersonService personService;
    private RetryingTransactionHelper transactionHelper;
    private CustomModelService customModelService;
    private List<String> importedModels = new ArrayList<>();
    private List<File> tempFiles = new ArrayList<>();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        authenticationService = getServer().getApplicationContext().getBean("AuthenticationService", MutableAuthenticationService.class);
        authorityService = getServer().getApplicationContext().getBean("AuthorityService", AuthorityService.class);
        personService = getServer().getApplicationContext().getBean("PersonService", PersonService.class);
        transactionHelper = getServer().getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        customModelService = getServer().getApplicationContext().getBean("customModelService", CustomModelService.class);

        AuthenticationUtil.clearCurrentSecurityContext();

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                createUser(NON_ADMIN_USER);
                createUser(CUSTOM_MODEL_ADMIN);

                if (!authorityService.getContainingAuthorities(AuthorityType.GROUP, CUSTOM_MODEL_ADMIN, true).contains(
                            CustomModelServiceImpl.GROUP_ALFRESCO_MODEL_ADMINISTRATORS_AUTHORITY))
                {
                    authorityService.addAuthority(CustomModelServiceImpl.GROUP_ALFRESCO_MODEL_ADMINISTRATORS_AUTHORITY, CUSTOM_MODEL_ADMIN);
                }
                return null;
            }
        });
        AuthenticationUtil.setFullyAuthenticatedUser(CUSTOM_MODEL_ADMIN);
    }

    @Override
    public void tearDown() throws Exception
    {
        for (File file : tempFiles)
        {
            file.delete();
        }

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                for (String modelName : importedModels)
                {
                    customModelService.deleteCustomModel(modelName);
                }
                return null;
            }
        });

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        deleteUser(NON_ADMIN_USER);
                        deleteUser(CUSTOM_MODEL_ADMIN);
                        return null;
                    }
                });
                return null;
            }
        });

        AuthenticationUtil.clearCurrentSecurityContext();

        super.tearDown();
    }

    public void testValidUpload_ModelAndExtModule() throws Exception
    {
        File zipFile = getResourceFile("validModelAndExtModule.zip");
        PostRequest postRequest = buildMultipartPostRequest(zipFile);

        AuthenticationUtil.setFullyAuthenticatedUser(NON_ADMIN_USER);
        Response response = sendRequest(postRequest, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(CUSTOM_MODEL_ADMIN);
        response = sendRequest(postRequest, 200);

        JSONObject json = new JSONObject(new JSONTokener(response.getContentAsString()));

        String importedModelName = json.getString("modelName");
        importedModels.add(importedModelName);

        String extModule = json.getString("shareExtModule");
        Document document = XMLUtil.parse(extModule);
        NodeList nodes = document.getElementsByTagName("id");
        assertEquals(1, nodes.getLength());
        assertNotNull(nodes.item(0).getTextContent());
    }

    public void testValidUpload_ModelOnly() throws Exception
    {
        File zipFile = getResourceFile("validModel.zip");
        PostRequest postRequest = buildMultipartPostRequest(zipFile);

        AuthenticationUtil.setFullyAuthenticatedUser(NON_ADMIN_USER);
        Response response = sendRequest(postRequest, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(CUSTOM_MODEL_ADMIN);
        response = sendRequest(postRequest, 200);

        JSONObject json = new JSONObject(new JSONTokener(response.getContentAsString()));
        String importedModelName = json.getString("modelName");
        importedModels.add(importedModelName);

        assertFalse(json.has("shareExtModule"));

        // Import the same model again
        sendRequest(postRequest, 409); // name conflict
    }

    public void testValidUpload_ExtModuleOnly() throws Exception
    {
        File zipFile = getResourceFile("validExtModule.zip");
        PostRequest postRequest = buildMultipartPostRequest(zipFile);

        AuthenticationUtil.setFullyAuthenticatedUser(NON_ADMIN_USER);
        Response response = sendRequest(postRequest, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(CUSTOM_MODEL_ADMIN);
        response = sendRequest(postRequest, 200);

        JSONObject json = new JSONObject(new JSONTokener(response.getContentAsString()));
        assertFalse(json.has("modelName"));

        String extModule = json.getString("shareExtModule");
        Document document = XMLUtil.parse(extModule);
        NodeList nodes = document.getElementsByTagName("id");
        assertEquals(1, nodes.getLength());
        assertNotNull(nodes.item(0).getTextContent());
    }

    public void testNotZipFileUpload() throws Exception
    {
        File file = getResourceFile("validModel.zip");
        ZipFile zipFile = new ZipFile(file);
        ZipEntry zipEntry = zipFile.entries().nextElement();

        File unzippedModelFile = TempFileProvider.createTempFile(zipFile.getInputStream(zipEntry), "validModel", ".xml");
        tempFiles.add(unzippedModelFile);
        zipFile.close();

        PostRequest postRequest = buildMultipartPostRequest(unzippedModelFile);
        sendRequest(postRequest, 400); // CMM upload supports only zip file.
    }

    public void testInvalidZipUpload() throws Exception
    {
        String content = "<note>"
                        +   "<from>Jane</from>"
                        +   "<to>John</to>"
                        +   "<heading>Upload test</heading>"
                        +   "<body>This is an invalid model or a Share extension module</body>"
                        +"</note>";

        ZipEntryContext context = new ZipEntryContext("invalidFormat.xml", content.getBytes());
        File zipFile = createZip(context);

        PostRequest postRequest = buildMultipartPostRequest(zipFile);
        sendRequest(postRequest, 400); // Invalid. Neither a model nor a Share extension module file
    }

    public void testUploadModel_Invalid() throws Exception
    {
        long timestamp = System.currentTimeMillis();
        final String modelName = getClass().getSimpleName() + timestamp;
        final String prefix = "prefix" + timestamp;
        final String uri = "uriNamespace" + timestamp;

        M2Model model = M2Model.createModel(prefix + QName.NAMESPACE_PREFIX + modelName);
        model.setAuthor("Admin");
        model.setDescription("Desc");

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        model.toXML(xml);
        ZipEntryContext context = new ZipEntryContext(modelName + ".xml", xml.toByteArray());
        File zipFile = createZip(context);

        PostRequest postRequest = buildMultipartPostRequest(zipFile);
        sendRequest(postRequest, 409); // no namespace has been defined

        // Create two namespaces
        model.createNamespace(uri, prefix);
        model.createNamespace(uri + "anotherUri", prefix + "anotherPrefix");
        xml = new ByteArrayOutputStream();
        model.toXML(xml);
        context = new ZipEntryContext(modelName + ".xml", xml.toByteArray());
        zipFile = createZip(context);

        postRequest = buildMultipartPostRequest(zipFile);
        sendRequest(postRequest, 409); // custom model can only have one namespace
    }

    public void testUploadModel_UnsupportedModelElements() throws Exception
    {
        // Note: here we only test a couple of not-supported model elements to check for the correct status code.
        // This test should be removed when we implement the required support

        long timestamp = System.currentTimeMillis();
        final String modelName = getClass().getSimpleName() + timestamp;
        final String prefix = "prefix"+timestamp;
        final String uri = "uriNamespace"+timestamp;
        final String aspectName = prefix + QName.NAMESPACE_PREFIX + "testAspec";
        final String typeName = prefix + QName.NAMESPACE_PREFIX + "testType";
        final String associationName = prefix + QName.NAMESPACE_PREFIX + "testAssociation";

        M2Model model = M2Model.createModel(prefix + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(uri, prefix);
        model.setAuthor("John Doe");
        model.createAspect(aspectName);
        model.createImport(NamespaceService.CONTENT_MODEL_1_0_URI, NamespaceService.CONTENT_MODEL_PREFIX);

        M2Type type = model.createType(typeName);
        // Add 'association' not supported yet.
        M2Association association = type.createAssociation(associationName);
        association.setSourceMandatory(false);
        association.setSourceMany(false);
        association.setTargetMandatory(false);
        association.setTargetClassName("cm:content");

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        model.toXML(xml);
        ZipEntryContext context = new ZipEntryContext(modelName + ".xml", xml.toByteArray());
        File zipFile = createZip(context);

        PostRequest postRequest = buildMultipartPostRequest(zipFile);
        sendRequest(postRequest, 409); // <associations> element is not supported yet

        type.removeAssociation(associationName);
        // Add 'mandatory-aspect' not supported yet.
        type.addMandatoryAspect(aspectName);
        xml = new ByteArrayOutputStream();
        model.toXML(xml);
        context = new ZipEntryContext(modelName + ".xml", xml.toByteArray());
        zipFile = createZip(context);

        postRequest = buildMultipartPostRequest(zipFile);
        sendRequest(postRequest, 409); // <mandatory-aspects> element is not supported yet
    }

    public void testInvalidNumberOfZipEntries() throws Exception
    {
        long timestamp = System.currentTimeMillis();
        String modelName = getClass().getSimpleName() + timestamp;
        String prefix = "prefix" + timestamp;
        String uri = "uriNamespace" + timestamp;

        // Model one
        M2Model modelOne = M2Model.createModel(prefix + QName.NAMESPACE_PREFIX + modelName);
        modelOne.createNamespace(uri, prefix);
        modelOne.setDescription("Model 1");
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        modelOne.toXML(xml);
        ZipEntryContext contextOne = new ZipEntryContext(modelName + ".xml", xml.toByteArray());

        // Model two
        modelName += "two";
        prefix += "two";
        uri += "two";
        M2Model modelTwo = M2Model.createModel(prefix + QName.NAMESPACE_PREFIX + modelName);
        modelTwo.createNamespace(uri, prefix);
        modelTwo.setDescription("Model 2");
        xml = new ByteArrayOutputStream();
        modelTwo.toXML(xml);
        ZipEntryContext contextTwo = new ZipEntryContext(modelName + ".xml", xml.toByteArray());

        // Model three
        modelName += "three";
        prefix += "three";
        uri += "three";
        M2Model modelThree = M2Model.createModel(prefix + QName.NAMESPACE_PREFIX + modelName);
        modelThree.createNamespace(uri, prefix);
        modelThree.setDescription("Model 3");
        xml = new ByteArrayOutputStream();
        modelThree.toXML(xml);
        ZipEntryContext contextThree = new ZipEntryContext(modelName + ".xml", xml.toByteArray());

        File zipFile = createZip(contextOne, contextTwo, contextThree);

        PostRequest postRequest = buildMultipartPostRequest(zipFile);
        sendRequest(postRequest, 400); // more than two zip entries
    }

    public PostRequest buildMultipartPostRequest(File file) throws IOException
    {
        Part[] parts = { new FilePart("filedata", file.getName(), file, "application/zip", null) };

        MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(parts, new HttpMethodParams());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        multipartRequestEntity.writeRequest(os);

        PostRequest postReq = new PostRequest(UPLOAD_URL, os.toByteArray(), multipartRequestEntity.getContentType());
        return postReq;
    }

    private void createUser(String userName)
    {
        if (!authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }

        if (!personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            personService.createPerson(ppOne);
        }
    }

    private void deleteUser(String userName)
    {
        if (personService.personExists(userName))
        {
            personService.deletePerson(userName);
        }
    }

    private File getResourceFile(String xmlFileName) throws FileNotFoundException
    {
        URL url = CustomModelImportTest.class.getClassLoader().getResource(RESOURCE_PREFIX + xmlFileName);
        if (url == null)
        {
            fail("Cannot get the resource: " + xmlFileName);
        }
        return ResourceUtils.getFile(url);
    }

    private File createZip(ZipEntryContext... zipEntryContexts)
    {
        File zipFile = TempFileProvider.createTempFile(getClass().getSimpleName(), ".zip");
        tempFiles.add(zipFile);

        byte[] buffer = new byte[BUFFER_SIZE];
        try
        {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(zipFile), BUFFER_SIZE);
            ZipOutputStream zos = new ZipOutputStream(out);

            for (ZipEntryContext context : zipEntryContexts)
            {
                ZipEntry zipEntry = new ZipEntry(context.getZipEntryName());
                zos.putNextEntry(zipEntry);

                InputStream input = context.getEntryContent();
                int len;
                while ((len = input.read(buffer)) > 0)
                {
                    zos.write(buffer, 0, len);
                }
                input.close();
            }
            zos.closeEntry();
            zos.close();
        }
        catch (IOException ex)
        {
            fail("couldn't create zip file.");
        }

        return zipFile;
    }

    private static class ZipEntryContext
    {
        private final String zipEntryName;
        private final InputStream entryContent;

        public ZipEntryContext(String zipEntryName, byte[] zipEntryContent)
        {
            this.zipEntryName = zipEntryName;
            this.entryContent = new ByteArrayInputStream(zipEntryContent);
        }

        public String getZipEntryName()
        {
            return this.zipEntryName;
        }

        public InputStream getEntryContent()
        {
            return this.entryContent;
        }
    }
}
