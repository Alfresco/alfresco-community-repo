package org.alfresco.repo.cmis.ws.test;

import java.math.BigInteger;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.repo.cmis.ws.CmisContentStreamType;
import org.alfresco.repo.cmis.ws.CmisException;
import org.alfresco.repo.cmis.ws.CmisExtensionType;
import org.alfresco.repo.cmis.ws.CmisObjectInFolderListType;
import org.alfresco.repo.cmis.ws.CmisObjectInFolderType;
import org.alfresco.repo.cmis.ws.CmisObjectType;
import org.alfresco.repo.cmis.ws.CmisPropertiesType;
import org.alfresco.repo.cmis.ws.CmisProperty;
import org.alfresco.repo.cmis.ws.CmisPropertyId;
import org.alfresco.repo.cmis.ws.CmisPropertyString;
import org.alfresco.repo.cmis.ws.DiscoveryServicePort;
import org.alfresco.repo.cmis.ws.EnumVersioningState;
import org.alfresco.repo.cmis.ws.NavigationServicePort;
import org.alfresco.repo.cmis.ws.ObjectFactory;
import org.alfresco.repo.cmis.ws.Query;
import org.alfresco.repo.cmis.ws.QueryResponse;
import org.alfresco.repo.content.MimetypeMap;

/**
 * The test of paging support of results of DMDiscoveryServicePort.query()
 * 
 * @author Arseny Kovalchuk
 */
public class QueryPagingTest extends BaseCMISTest
{
    private static final int NUMBER_OF_DOCUMENTS = 50;
    private static final String QUERY_DOCUMENTS_TEMPLATE = "select * from cmis:document d where in_folder('%1$s')";

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private String queryDocuments;

    protected DiscoveryServicePort discoveryServicePort;
    protected NavigationServicePort navigationServicePort;

    public QueryPagingTest()
    {
        super();
        discoveryServicePort = (DiscoveryServicePort) ctx.getBean("dmDiscoveryService");
        navigationServicePort = (NavigationServicePort) ctx.getBean("dmNavigationService");
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        queryDocuments = String.format(QUERY_DOCUMENTS_TEMPLATE, testFolderId);
        //System.out.println(queryDocuments);
    }

    /**
     * The main reason for this test is ALF-9566
     * 
     * @throws Exception
     */
    public void testPagingSupport() throws Exception
    {
        clearDocumentsInFolder(testFolderId);

        createDocumentsInFolder(testFolderId, NUMBER_OF_DOCUMENTS);

        // Select 10 of NUMBER_OF_DOCUMENTS, 0 documents to skip, so NUMBER_OF_DOCUMENTS - 10 still available
        int skipCount = 0;
        int maxItems = 10;
        QueryResponse response = discoveryServicePort.query(createQuery(maxItems, skipCount));
        assertEquals(NUMBER_OF_DOCUMENTS - skipCount, response.getObjects().getNumItems().intValue());
        assertEquals(maxItems, response.getObjects().getObjects().size());
        assertTrue(response.getObjects().isHasMoreItems());
        
        // Select 10 of NUMBER_OF_DOCUMENTS, 10 documents to skip, so NUMBER_OF_DOCUMENTS - 10 - 10 still available
        skipCount = 10;
        maxItems = 10;
        response = discoveryServicePort.query(createQuery(maxItems, skipCount));
        assertEquals(NUMBER_OF_DOCUMENTS - skipCount, response.getObjects().getNumItems().intValue());
        assertEquals(maxItems, response.getObjects().getObjects().size());
        assertTrue(response.getObjects().isHasMoreItems());
        
        // Select 10 of NUMBER_OF_DOCUMENTS, NUMBER_OF_DOCUMENTS - 10 to skip, so there are no docs available
        skipCount = NUMBER_OF_DOCUMENTS - 10;
        maxItems = 10;
        response = discoveryServicePort.query(createQuery(maxItems, skipCount));
        assertEquals(NUMBER_OF_DOCUMENTS - skipCount, response.getObjects().getNumItems().intValue());
        assertEquals(maxItems, response.getObjects().getObjects().size());
        assertFalse(response.getObjects().isHasMoreItems());
        
        // Select NUMBER_OF_DOCUMENTS to select, 0 to skip, so, there are no docs available
        skipCount = 0;
        maxItems = NUMBER_OF_DOCUMENTS;
        response = discoveryServicePort.query(createQuery(maxItems, skipCount));
        assertEquals(NUMBER_OF_DOCUMENTS - skipCount, response.getObjects().getNumItems().intValue());
        assertEquals(maxItems, response.getObjects().getObjects().size());
        assertFalse(response.getObjects().isHasMoreItems());

        clearDocumentsInFolder(testFolderId);
    }

    protected void createDocumentsInFolder(String folderId, int docsCount) throws Exception
    {
        for (int i = 0; i < NUMBER_OF_DOCUMENTS; i++)
        {
            String docName = "Test Document " + i + ".txt";
            //System.out.println("Creating " + docName);
            createDocument(docName, folderId);
        }
    }

    protected void clearDocumentsInFolder(String folderId) throws CmisException
    {
        CmisObjectInFolderListType result = navigationServicePort.getChildren(repositoryId, testFolderId, "*", null, false, null, null, false, BigInteger.valueOf(-1),
                BigInteger.ZERO, null);
        if (!result.getObjects().isEmpty())
        {
            for(CmisObjectInFolderType obj : result.getObjects())
            {
                String documentId = getObjectId(obj.getObject());
                //System.out.println("Deleting document with ID:" + documentId);
                objectServicePort.deleteObject(repositoryId, documentId, true, new Holder<CmisExtensionType>());
            }
        }
    }
    
    protected String getObjectId(CmisObjectType object)
    {
        CmisPropertiesType propertiesType = object.getProperties();
        for(CmisProperty property : propertiesType.getProperty())
        {
            if ((property instanceof CmisPropertyId) && CMISDictionaryModel.PROP_OBJECT_ID.equals(property.getPropertyDefinitionId()))
            {
                return ((CmisPropertyId) property).getValue().iterator().next();
            }
        }
        return null;
    }

    protected String createDocument(String name, String parentFolderId) throws Exception
    {
        String content = "This is a test content";
        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_NAME);
        cmisProperty.getValue().add(name);
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());

        propertiesList.add(cmisProperty);
        propertiesList.add(idProperty);

        CmisContentStreamType cmisStream = new CmisContentStreamType();
        cmisStream.setFilename(name);
        cmisStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        DataHandler dataHandler = new DataHandler(content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        cmisStream.setStream(dataHandler);

        Holder<CmisExtensionType> extensions = new Holder<CmisExtensionType>();
        Holder<String> idHolder = new Holder<String>();
        objectServicePort.createDocument(repositoryId, properties, parentFolderId, cmisStream, EnumVersioningState.MAJOR, null, null, null, extensions, idHolder);
        return idHolder.value;
    }

    private Query createQuery(int maxItems, int skipCount)
    {
        Query parameters = new Query();
        parameters.setRepositoryId(repositoryId);
        parameters.setStatement(queryDocuments);
        parameters.setSkipCount(OBJECT_FACTORY.createQuerySkipCount(BigInteger.valueOf(skipCount)));
        parameters.setMaxItems(OBJECT_FACTORY.createQueryMaxItems(BigInteger.valueOf(maxItems)));
        parameters.setIncludeAllowableActions(OBJECT_FACTORY.createQueryIncludeAllowableActions(Boolean.FALSE));
        parameters.setSearchAllVersions(OBJECT_FACTORY.createQuerySearchAllVersions(Boolean.FALSE));
        return parameters;
    }

}
