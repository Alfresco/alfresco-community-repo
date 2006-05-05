package org.alfresco.jcr.query;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.alfresco.jcr.test.BaseJCRTest;

public class QueryManagerImplTest extends BaseJCRTest
{

    protected Session superuserSession;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        SimpleCredentials superuser = new SimpleCredentials("superuser", "".toCharArray());
        superuserSession = repository.login(superuser, getWorkspace());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        superuserSession.logout();
        super.tearDown();
    }


    public void testQuery()
        throws Exception
    {
        QueryManager queryMgr = superuserSession.getWorkspace().getQueryManager();
        String[] languages = queryMgr.getSupportedQueryLanguages();
        assertEquals(1, languages.length);
        assertEquals(Query.XPATH, languages[0]);
        
        Query query = queryMgr.createQuery("//*", Query.XPATH);
        QueryResult result = query.execute();
        String[] columnNames = result.getColumnNames();

        // iterate via row iterator
        int rowCnt = 0;
        RowIterator rowIterator = result.getRows();
        while(rowIterator.hasNext())
        {
            Row row = rowIterator.nextRow();
            for (String columnName : columnNames)
            {
                Value value = row.getValue(columnName);
                if (value != null)
                {
                    String strValue = value.getString();
                    assertNotNull(strValue);
                }
            }
            rowCnt++;
        }
        
        // iterate via node iterator
        int nodeCnt = 0;
        NodeIterator nodeIterator = result.getNodes();
        while(nodeIterator.hasNext())
        {
            Node node = nodeIterator.nextNode();
            Property property = node.getProperty("sys:node-uuid");
            Value value = property.getValue();
            String uuid = value.getString();
            assertNotNull(uuid);
            nodeCnt++;
        }

        // check same number of items returned from each iterator
        assertEquals(rowCnt, nodeCnt);
    }
    
    
}
