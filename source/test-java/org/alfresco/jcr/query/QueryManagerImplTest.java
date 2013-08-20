/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
