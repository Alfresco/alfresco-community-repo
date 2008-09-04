/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.search;

import java.io.Serializable;

import org.alfresco.cmis.dictionary.BaseCMISTest;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * @author andyh
 */
public class QueryTest extends BaseCMISTest
{
    public void testSimpleConjunction()
    {

        String query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME = 'company'";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
        
        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME = 'home'";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
        
        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME = 'home' AND NAME = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
    }
    
    public void testSimpleDisjunction()
    {

        String query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME = 'guest'";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
        
        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
        
        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME = 'guest' OR NAME = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
        
     
    }
    
    public void testExists()
    {
        String query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(33, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
        
        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NULL";
        rs = cmisQueryService.query(query);
        assertEquals(0, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
        
        
        query = "SELECT * FROM DOCUMENT_OBJECT_TYPE WHERE URI IS NOT NULL";
        rs = cmisQueryService.query(query);
        assertEquals(0, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
        
        query = "SELECT * FROM DOCUMENT_OBJECT_TYPE WHERE URI IS NULL";
        rs = cmisQueryService.query(query);
        assertEquals(45, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null; 
    }
    
    public void testObjectEquals()
    {
        
    }

    public void testDocumentEquals()
    {
        
    }

    public void testFolderEquals()
    {
        NodeRef rootNode = cmisService.getDefaultRootNodeRef();

        Serializable ser = cmisPropertyService.getProperty(rootNode, CMISMapping.PROP_NAME);
        String name = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        String query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME = '" + name + "'";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE PARENT = '" + rootNode.toString() + "'";
        rs = cmisQueryService.query(query);
        assertEquals(4, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;
        
        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES = 'meep'";
        rs = cmisQueryService.query(query);
        assertEquals(0, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

    }

    public void test_IN_TREE()
    {
        NodeRef rootNode = cmisService.getDefaultRootNodeRef();

        Serializable ser = cmisPropertyService.getProperty(rootNode, CMISMapping.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        String query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE IN_TREE('" + id + "')";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(32, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();

    }

    public void test_IN_FOLDER()
    {
        NodeRef rootNode = cmisService.getDefaultRootNodeRef();

        Serializable ser = cmisPropertyService.getProperty(rootNode, CMISMapping.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        String query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE IN_FOLDER('" + id + "')";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(4, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
    }

    public void testFTS()
    {
        String query = "SELECT * FROM DOCUMENT_OBJECT_TYPE WHERE CONTAINS('\"Sample demonstrating the listing of AVM folder contents\"')";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
    }

    public void testBasicSelectAsGuest()
    {
        runAs("guest");
        String query = "SELECT * FROM DOCUMENT_OBJECT_TYPE";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(2, rs.length());
        rs.close();
    }

    public void testBasicSelect()
    {
        String query = "SELECT * FROM DOCUMENT_OBJECT_TYPE";
        CMISResultSet rs = cmisQueryService.query(query);
        for (CMISResultSetRow row : rs)
        {
            System.out.println("Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
    }

    public void testBasicDefaultMetaData()
    {
        String query = "SELECT * FROM DOCUMENT_OBJECT_TYPE";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(cmisDictionaryService.getPropertyDefinitions(CMISMapping.DOCUMENT_TYPE_ID).size(), md.getColumnNames().length);
        assertNotNull(md.getColumn(CMISMapping.PROP_OBJECT_ID));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector(""));
        rs.close();
    }

    public void testBasicMetaData()
    {
        String query = "SELECT DOC.OBJECT_ID, DOC.OBJECT_ID AS ID FROM DOCUMENT_OBJECT_TYPE AS DOC";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(2, md.getColumnNames().length);
        assertNotNull(md.getColumn("DOC.OBJECT_ID"));
        assertNotNull(md.getColumn("ID"));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector("DOC"));
        rs.close();
    }

    public void testBasicColumns()
    {
        String query = "SELECT DOC.OBJECT_ID, DOC.OBJECT_TYPE_ID AS ID FROM FOLDER_OBJECT_TYPE AS DOC";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(2, md.getColumnNames().length);
        assertNotNull(md.getColumn("DOC.OBJECT_ID"));
        assertNotNull(md.getColumn("ID"));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector("DOC"));
        for (CMISResultSetRow row : rs)
        {
            System.out.println("Id  " + row.getValue("ID"));
        }
        rs.close();
    }

    public void testBasicAllDocumentColumns()
    {
        String query = "SELECT DOC.*  FROM DOCUMENT_OBJECT_TYPE AS DOC";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();

        for (CMISResultSetRow row : rs)
        {
            for (String column : md.getColumnNames())
            {
                System.out.println("Column  " + column + " value =" + row.getValue(column));
            }
            System.out.println("\n\n");
        }
        rs.close();
    }

    public void testBasicAllFolderColumns()
    {
        String query = "SELECT *  FROM FOLDER_OBJECT_TYPE AS DOC";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();

        for (CMISResultSetRow row : rs)
        {
            for (String column : md.getColumnNames())
            {
                System.out.println("Column  " + column + " value =" + row.getValue(column));
            }
            System.out.println("\n\n");
        }
        rs.close();
    }

    public void testBasicAll_ST_SITES_Columns()
    {
        String query = "SELECT *  FROM ST_SITES AS DOC";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();

        for (CMISResultSetRow row : rs)
        {
            for (String column : md.getColumnNames())
            {
                System.out.println("Column  " + column + " value =" + row.getValue(column));
            }
            System.out.println("\n\n");
            System.out.println(row.getValues());
            System.out.println("\n\n");
        }

        rs.close();
    }

    public void testFunctionColumns()
    {
        String query = "SELECT DOC.NAME AS NAME, \nLOWER(\tDOC.NAME \n), LOWER ( DOC.NAME )  AS LNAME, UPPER ( DOC.NAME ) , UPPER(DOC.NAME) AS UNAME, Score(), SCORE(DOC), SCORE() AS SCORED, SCORE(DOC) AS DOCSCORE FROM FOLDER_OBJECT_TYPE AS DOC";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(9, md.getColumnNames().length);
        assertNotNull(md.getColumn("NAME"));
        assertNotNull(md.getColumn("LOWER(\tDOC.NAME \n)"));
        assertNotNull(md.getColumn("LNAME"));
        assertNotNull(md.getColumn("UPPER ( DOC.NAME )"));
        assertNotNull(md.getColumn("UNAME"));
        assertNotNull(md.getColumn("Score()"));
        assertNotNull(md.getColumn("SCORE(DOC)"));
        assertNotNull(md.getColumn("SCORED"));
        assertNotNull(md.getColumn("DOCSCORE"));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector("DOC"));
        for (CMISResultSetRow row : rs)
        {
            System.out.println("\n\n");
            System.out.println(row.getValues());
            System.out.println("\n\n");
        }
        rs.close();
    }

    public void xtestParse1()
    {
        String query = "SELECT UPPER(1.0) AS WOOF FROM DOCUMENT_OBJECT_TYPE AS DOC LEFT OUTER JOIN FOLDER_OBJECT_TYPE AS FOLDER ON (DOC.NAME = FOLDER.NAME) WHERE LOWER(DOC.NAME = ' woof' AND CONTAINS(, 'one two three') AND  CONTAINS(, 'DOC.NAME:lemur AND woof') AND (DOC.NAME in ('one', 'two') AND IN_FOLDER('meep') AND DOC.NAME like 'woof' and DOC.NAME = 'woof' and DOC.OBJECT_ID = 'meep') ORDER BY DOC.NAME DESC, WOOF";
        cmisQueryService.query(query);
    }

    public void xtestParse2()
    {
        String query = "SELECT TITLE, AUTHORS, DATE FROM WHITE_PAPER WHERE ( IN_TREE( , 'ID00093854763') ) AND ( 'SMITH' = ANY AUTHORS )";
        cmisQueryService.query(query);
    }

    public void xtestParse3()
    {
        String query = "SELECT OBJECT_ID, SCORE() AS X, DESTINATION, DEPARTURE_DATES FROM TRAVEL_BROCHURE WHERE ( CONTAINS(, 'CARIBBEAN CENTRAL AMERICA CRUISE TOUR') ) AND ( '2009-1-1' < ANY DEPARTURE_DATES ) ORDER BY X DESC";
        cmisQueryService.query(query);
    }

    public void xtestParse4()
    {
        String query = "SELECT * FROM CAR_REVIEW WHERE ( LOWER(MAKE) = 'buick' ) OR ( ANY FEATURES IN ('NAVIGATION SYSTEM', 'SATELLITE RADIO', 'MP3' ) )";
        cmisQueryService.query(query);
    }

    public void xtestParse5()
    {
        String query = "SELECT Y.CLAIM_NUM, X.PROPERTY_ADDRESS, Y.DAMAGE_ESTIMATES FROM POLICY AS X JOIN CLAIMS AS Y ON ( X.POLICY_NUM = Y.POLICY_NUM ) WHERE ( 100000 <= ANY Y.DAMAGE_ESTIMATES ) AND ( Y.CAUSE NOT LIKE '%Katrina%' )";
        cmisQueryService.query(query);
    }
}
