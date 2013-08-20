package org.alfresco.repo.cmis.rest.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.uuid.UUIDGenerator;

/**
 * CMIS Relationship integration tests.
 * 
 * @author Alex Strachan (tidy up by Alan Davis)
 */
public class CmisRelationshipSystemTest
{
    static Session session;
    
    static Document doc1;
    
    @BeforeClass
    public static void classSetup() throws Exception
    {
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> sessionParameters = new HashMap<String, String>();
        sessionParameters
                .put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/alfresco/s/cmis");
        sessionParameters.put(SessionParameter.USER, "admin");
        sessionParameters.put(SessionParameter.PASSWORD, "admin");
        sessionParameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        sessionParameters.put(SessionParameter.OBJECT_FACTORY_CLASS,
                AlfrescoObjectFactoryImpl.class.getName());
        session = factory.getRepositories(sessionParameters).get(0).createSession();

        /*
         * Association example from:
         * http://forums.alfresco.com/en/viewtopic.php?f=45&t=27219
         */
        HashMap<String, Object> prop1 = new HashMap<String, Object>();
        prop1.put(PropertyIds.NAME, GUID());
        prop1.put(PropertyIds.OBJECT_TYPE_ID, "D:ws:article");

        HashMap<String, Object> prop2 = new HashMap<String, Object>();
        prop2.put(PropertyIds.NAME, GUID());
        prop2.put(PropertyIds.OBJECT_TYPE_ID, "D:ws:article");

        Folder folder = (Folder) session.getObjectByPath("/");

        doc1 = folder.createDocument(prop1, null, null, null, null, null,
                session.getDefaultContext());
        Document doc2 = folder.createDocument(prop2, null, null, null, null, null,
                session.getDefaultContext());

        Map<String, String> relProps = new HashMap<String, String>();
        relProps.put("cmis:sourceId", doc1.getId());
        relProps.put("cmis:targetId", doc2.getId());
        relProps.put("cmis:objectTypeId", "R:ws:relatedArticles");
        session.createRelationship(relProps, null, null, null);
    }
    
    private static String GUID()
    {
        return UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
    }
    
    @Test
    public void testObjectRelationships() throws Exception
    {
        // Can't use object relationships retrieval due an isValidCmisRelationship issue.
        // TODO Change expected value to 1 rather than 0 once bug is fixed.
        assertEquals(0, doc1.getRelationships().size());
    }

    private void testGetRelationshipsViaSession(RelationshipDirection direction) throws Exception
    {
        // Try and get relationships using the session
        ObjectType typeDefinition = session.getTypeDefinition("R:ws:relatedArticles");
        OperationContext operationContext = session.createOperationContext();

        ItemIterable<Relationship> relationships = session.getRelationships(doc1, true, direction,
                typeDefinition, operationContext);
        int relationshipCount = 0;
        Iterator<Relationship> iterator = relationships.iterator();
        while (iterator.hasNext())
        {
            relationshipCount++;
            iterator.next();
        }
        assertEquals(1, relationshipCount);
    }

    @Test
    public void testRelationshipDirectionEither() throws Exception
    {
        testGetRelationshipsViaSession(RelationshipDirection.EITHER);
    }
    
    @Test
    public void testRelationshipDirectionSource() throws Exception
    {
        testGetRelationshipsViaSession(RelationshipDirection.SOURCE);
    }
}