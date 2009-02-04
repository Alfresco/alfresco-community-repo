package org.alfresco.repo.web.scripts.forms;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.repo.web.scripts.thumbnail.ThumbnailServiceTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.alfresco.web.scripts.json.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class TestFormRestAPI extends BaseWebScriptTest {
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private NodeService nodeService;
    private Repository repositoryHelper;
    private Response response;
    private String jsonResponseString;
    private NodeRef testRoot;
    private NodeRef testPdfNode;
    private String pathToTestPdfNode;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.fileFolderService = (FileFolderService)getServer().getApplicationContext().getBean("FileFolderService");
        this.contentService = (ContentService)getServer().getApplicationContext().getBean("ContentService");
        this.repositoryHelper = (Repository)getServer().getApplicationContext().getBean("repositoryHelper");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        this.testRoot = this.repositoryHelper.getCompanyHome();

        // Create a dummy node purely for test purposes.
        InputStream pdfStream = ThumbnailServiceTest.class.getClassLoader().getResourceAsStream("org/alfresco/repo/web/scripts/forms/test_doc.pdf");        
        assertNotNull(pdfStream);
        
        String guid = GUID.generate();
        
        FileInfo fileInfoPdf = this.fileFolderService.create(this.testRoot, "test_forms_doc" + guid + ".pdf", ContentModel.TYPE_CONTENT);
        this.testPdfNode = fileInfoPdf.getNodeRef();
        
        // Add an aspect.
        Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>(2);
        aspectProps.put(ContentModel.PROP_TITLE, "Test form title");
        aspectProps.put(ContentModel.PROP_DESCRIPTION, "Test form description");
        nodeService.addAspect(testPdfNode, ContentModel.ASPECT_TITLED, aspectProps);
        
        ContentWriter contentWriter = this.contentService.getWriter(fileInfoPdf.getNodeRef(), ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);
        contentWriter.putContent(pdfStream);
        
        StringBuilder builder = new StringBuilder();
        builder.append("/api/forms/node/workspace/")
            .append(testPdfNode.getStoreRef().getIdentifier())
            .append("/")
            .append(testPdfNode.getId());
        this.pathToTestPdfNode = builder.toString();
    }
    
    //TODO Add a tearDown which deletes the temporary pdf file above.
    
    public void testResponseContentType() throws Exception
    {
        sendGetReqAndInitRspData(pathToTestPdfNode, 200);
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        
        //TODO Remove this.
        System.out.println(jsonResponseString);
    }

    //TODO Perhaps separate positive and negative test cases into two JUnit classes.
    public void testGetFormForNonExistentNode() throws Exception
    {
        sendGetReqAndInitRspData(pathToTestPdfNode.replaceAll("\\d", "x"), 404);
        assertEquals("application/json;charset=UTF-8", response.getContentType());
    }

    public void testJsonContentParsesCorrectly() throws Exception
    {
        sendGetReqAndInitRspData(pathToTestPdfNode, 200);
        
        Object jsonObject = new JSONUtils().toObject(jsonResponseString);
        assertNotNull("JSON object was null.", jsonObject);
    }

    public void testJsonUpperStructure() throws Exception
    {
        sendGetReqAndInitRspData(pathToTestPdfNode, 200);
        
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(jsonResponseString));
        assertNotNull(jsonParsedObject);
        
        Object dataObj = jsonParsedObject.get("data");
        assertEquals(JSONObject.class, dataObj.getClass());
        JSONObject rootDataObject = (JSONObject)dataObj;

        assertEquals(5, rootDataObject.length());
        String item = (String)rootDataObject.get("item");
        String submissionUrl = (String)rootDataObject.get("submissionUrl");
        String type = (String)rootDataObject.get("type");
        JSONObject definitionObject = (JSONObject)rootDataObject.get("definition");
        JSONObject formDataObject = (JSONObject)rootDataObject.get("formData");
        
        assertNotNull(item);
        assertNotNull(submissionUrl);
        assertNotNull(type);
        assertNotNull(definitionObject);
        assertNotNull(formDataObject);
    }

    @SuppressWarnings("unchecked")
    public void testJsonFormData() throws Exception
    {
        sendGetReqAndInitRspData(pathToTestPdfNode, 200);
        
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(jsonResponseString));
        assertNotNull(jsonParsedObject);
        
        JSONObject rootDataObject = (JSONObject)jsonParsedObject.get("data");
        
        JSONObject formDataObject = (JSONObject)rootDataObject.get("formData");
        List<String> keys = new ArrayList<String>();
        for (Iterator iter = formDataObject.keys(); iter.hasNext(); )
        {
            keys.add((String)iter.next());
        }
        // Threshold is a rather arbitrary number. I simply want to ensure that there
        // are *some* entries in the formData hash.
        final int threshold = 5;
        int actualKeyCount = keys.size();
        assertTrue("Expected more than " + threshold +
                " entries in formData. Actual: " + actualKeyCount, actualKeyCount > threshold);
    }
    
    public void testJsonDefinitionFields() throws Exception
    {
        sendGetReqAndInitRspData(pathToTestPdfNode, 200);
        
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(jsonResponseString));
        assertNotNull(jsonParsedObject);
        
        JSONObject rootDataObject = (JSONObject)jsonParsedObject.get("data");
        
        JSONObject definitionObject = (JSONObject)rootDataObject.get("definition");
        
        JSONArray fieldsArray = (JSONArray)definitionObject.get("fields");
        
        //TODO This will all be revamped when I introduce test code based on a known
        //     node. But in the meantime, I'll keep it general.
        for (int i = 0; i < fieldsArray.length(); i++)
        {
            Object nextObj = fieldsArray.get(i);
            
            JSONObject nextJsonObject = (JSONObject)nextObj;
            List<String> fieldKeys = new ArrayList<String>();
            for (Iterator iter2 = nextJsonObject.keys(); iter2.hasNext(); )
            {
                fieldKeys.add((String)iter2.next());
            }
            for (String s : fieldKeys)
            {
                if (s.equals("mandatory") || s.equals("protectedField"))
                {
                    assertEquals("JSON booleans should be actual booleans.", java.lang.Boolean.class, nextJsonObject.get(s).getClass());
                }
            }
        }
    }

    private void sendGetReqAndInitRspData(String url, int expectedStatusCode) throws IOException,
            UnsupportedEncodingException
    {
        response = sendRequest(new GetRequest(url), expectedStatusCode);  
        jsonResponseString = response.getContentAsString();
    }
}
