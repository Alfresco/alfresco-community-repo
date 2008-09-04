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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.alfresco.cmis.dictionary.BaseCMISTest;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.util.CachingDateFormat;

/**
 * @author andyh
 */
public class QueryTest extends BaseCMISTest
{
    @SuppressWarnings("unchecked")
    private <T> T testQuery(String query, int size, boolean dump, String returnPropertyName, T returnType, boolean shouldThrow)
    {
        CMISResultSet rs = null;
        try
        {
            T returnValue = null;
            rs = cmisQueryService.query(query);

            for (CMISResultSetRow row : rs)
            {
                if (row.getIndex() == 0)
                {
                    Serializable sValue = row.getValue(returnPropertyName);
                    returnValue = (T) DefaultTypeConverter.INSTANCE.convert(returnType.getClass(), sValue);
                    if(dump)
                    {
                        System.out.println(cmisPropertyService.getProperties(row.getNodeRef(rs.getMetaData().getSelectorNames()[0])));
                    }
                }
                if (dump)
                {
                    System.out.println("ID ="
                            + row.getValue("OBJECT_ID") + " " + ((returnPropertyName != null) ? (returnPropertyName + "=" + row.getValue(returnPropertyName)) : "") + " Score="
                            + row.getScore() + " " + row.getScores());
                }
            }
            if (size >= 0)
            {
                assertEquals(size, rs.getLength());
            }
            if (shouldThrow)
            {
                fail();
            }
            return returnValue;
        }
        catch (CMISQueryException e)
        {
            if (shouldThrow)
            {
                return null;
            }
            else
            {
                throw e;
            }
        }
        catch (QueryModelException e)
        {
            if (shouldThrow)
            {
                return null;
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                finally
                {
                    rs = null;
                }
            }
        }

    }
    
    public void test_ALLOWED_CHILD_OBJECT_TYPES()
    {
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES =  'test'", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES <> 'test'", 33, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES <  'test'", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES <= 'test'", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES >  'test'", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES >= 'test'", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);

        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES IN     ('test')", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES NOT IN ('test')", 33, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);

        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES     LIKE 'test'", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES NOT LIKE 'test'", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);

        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES IS NOT NULL", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES IS     NULL", 33, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);

        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE 'test' =  ANY ALLOWED_CHILD_OBJECT_TYPES", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE 'test' <> ANY ALLOWED_CHILD_OBJECT_TYPES", 33, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE 'test' <  ANY ALLOWED_CHILD_OBJECT_TYPES", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE 'test' <= ANY ALLOWED_CHILD_OBJECT_TYPES", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE 'test' >  ANY ALLOWED_CHILD_OBJECT_TYPES", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE 'test' >= ANY ALLOWED_CHILD_OBJECT_TYPES", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);

        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ANY ALLOWED_CHILD_OBJECT_TYPES IN     ('test')", 0, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
        testQuery("SELECT ALLOWED_CHILD_OBJECT_TYPES FROM FOLDER_OBJECT_TYPE WHERE ANY ALLOWED_CHILD_OBJECT_TYPES NOT IN ('test')", 33, false, "ALLOWED_CHILD_OBJECT_TYPES", new String(), false);
    }
    
    public void test_PARENT()
    {
        NodeRef rootNode = cmisService.getDefaultRootNodeRef();
        
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT =  '" + rootNode.toString() + "'", 4, false, "PARENT", new String(), false);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT <> '" + rootNode.toString() + "'", 29, false, "PARENT", new String(), false);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT <  '" + rootNode.toString() + "'", 0, false, "PARENT", new String(), true);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT <= '" + rootNode.toString() + "'", 0, false, "PARENT", new String(), true);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT >  '" + rootNode.toString() + "'", 0, false, "PARENT", new String(), true);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT >= '" + rootNode.toString() + "'", 0, false, "PARENT", new String(), true);

        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT IN     ('" + rootNode.toString() + "')", 4, false, "PARENT", new String(), false);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT NOT IN ('" + rootNode.toString() + "')", 29, false, "PARENT", new String(), false);

        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT     LIKE '" + rootNode.toString() + "'", 4, false, "PARENT", new String(), false);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT NOT LIKE '" + rootNode.toString() + "'", 29, false, "PARENT", new String(), false);

        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT IS NOT NULL", 33, false, "PARENT", new String(), false);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE PARENT IS     NULL", 0, false, "PARENT", new String(), false);

        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE '" + rootNode.toString() + "' =  ANY PARENT", 4, false, "PARENT", new String(), false);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE '" + rootNode.toString() + "' <> ANY PARENT", 29, false, "PARENT", new String(), false);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE '" + rootNode.toString() + "' <  ANY PARENT", 0, false, "PARENT", new String(), true);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE '" + rootNode.toString() + "' <= ANY PARENT", 0, false, "PARENT", new String(), true);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE '" + rootNode.toString() + "' >  ANY PARENT", 0, false, "PARENT", new String(), true);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE '" + rootNode.toString() + "' >= ANY PARENT", 0, false, "PARENT", new String(), true);

        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE ANY PARENT IN     ('" + rootNode.toString() + "')", 4, false, "PARENT", new String(), false);
        testQuery("SELECT PARENT FROM FOLDER_OBJECT_TYPE WHERE ANY PARENT NOT IN ('" + rootNode.toString() + "')", 29, false, "PARENT", new String(), false);
    }

    
    public void test_CONTENT_STREAM_URI()
    {
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI =  'test'", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI <> 'test'", 45, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI <  'test'", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI <= 'test'", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI >  'test'", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI >= 'test'", 0, false, "CONTENT_STREAM_URI", new String(), false);

        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI IN     ('test')", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI NOT IN ('test')", 45, false, "CONTENT_STREAM_URI", new String(), false);

        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI     LIKE 'test'", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI NOT LIKE 'test'", 0, false, "CONTENT_STREAM_URI", new String(), false);

        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI IS NOT NULL", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_URI IS     NULL", 45, false, "CONTENT_STREAM_URI", new String(), false);

        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE 'test' =  ANY CONTENT_STREAM_URI", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE 'test' <> ANY CONTENT_STREAM_URI", 45, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE 'test' <  ANY CONTENT_STREAM_URI", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE 'test' <= ANY CONTENT_STREAM_URI", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE 'test' >  ANY CONTENT_STREAM_URI", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE 'test' >= ANY CONTENT_STREAM_URI", 0, false, "CONTENT_STREAM_URI", new String(), false);

        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_URI IN     ('test')", 0, false, "CONTENT_STREAM_URI", new String(), false);
        testQuery("SELECT CONTENT_STREAM_URI FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_URI NOT IN ('test')", 45, false, "CONTENT_STREAM_URI", new String(), false);
    }

    
    public void test_CONTENT_STREAM_FILENAME()
    {
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME =  'tutorial'", 1, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME <> 'tutorial'", 44, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME <  'tutorial'", 45, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME <= 'tutorial'", 45, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME >  'tutorial'", 8, true, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME >= 'tutorial'", 9, false, "CONTENT_STREAM_FILENAME", new String(), false);

        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME IN     ('tutorial')", 1, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME NOT IN ('tutorial')", 44, false, "CONTENT_STREAM_FILENAME", new String(), false);

        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME     LIKE 'tutorial'", 1, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME NOT LIKE 'tutorial'", 44, false, "CONTENT_STREAM_FILENAME", new String(), false);

        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME IS NOT NULL", 45, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_FILENAME IS     NULL", 0, false, "CONTENT_STREAM_FILENAME", new String(), false);

        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' =  ANY CONTENT_STREAM_FILENAME", 1, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' <> ANY CONTENT_STREAM_FILENAME", 44, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' <  ANY CONTENT_STREAM_FILENAME", 45, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' <= ANY CONTENT_STREAM_FILENAME", 45, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' >  ANY CONTENT_STREAM_FILENAME", 8, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' >= ANY CONTENT_STREAM_FILENAME", 9, false, "CONTENT_STREAM_FILENAME", new String(), false);

        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_FILENAME IN     ('tutorial')", 1, false, "CONTENT_STREAM_FILENAME", new String(), false);
        testQuery("SELECT CONTENT_STREAM_FILENAME FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_FILENAME NOT IN ('tutorial')", 44, false, "CONTENT_STREAM_FILENAME", new String(), false);
    }
    
    public void test_CONTENT_STREAM_MIME_TYPE()
    {
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE =  'text/plain'", 26, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE <> 'text/plain'", 19, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE <  'text/plain'", 14, true, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE <= 'text/plain'", 40, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE >  'text/plain'", 5, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE >= 'text/plain'", 31, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);

        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE IN     ('text/plain')", 26, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE NOT IN ('text/plain')", 19, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);

        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE     LIKE 'text/plain'", 26, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE NOT LIKE 'text/plain'", 19, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);

        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE IS NOT NULL", 45, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_MIME_TYPE IS     NULL", 0, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);

        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE 'text/plain' =  ANY CONTENT_STREAM_MIME_TYPE", 26, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE 'text/plain' <> ANY CONTENT_STREAM_MIME_TYPE", 19, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE 'text/plain' <  ANY CONTENT_STREAM_MIME_TYPE", 14, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE 'text/plain' <= ANY CONTENT_STREAM_MIME_TYPE", 40, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE 'text/plain' >  ANY CONTENT_STREAM_MIME_TYPE", 5, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE 'text/plain' >= ANY CONTENT_STREAM_MIME_TYPE", 31, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);

        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_MIME_TYPE IN     ('text/plain')", 26, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
        testQuery("SELECT CONTENT_STREAM_MIME_TYPE FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_MIME_TYPE NOT IN ('text/plain')", 19, false, "CONTENT_STREAM_MIME_TYPE", new String(), false);
    }

    public void test_CONTENT_STREAM_LENGTH()
    {
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH =  750", 1, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH <> 750", 44, true, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH <  750", 28, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH <= 750", 29, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH >  750", 16, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH >= 750", 17, false, "CONTENT_STREAM_LENGTH", new String(), false);

        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH IN     (750)", 1, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH NOT IN (750)", 44, false, "CONTENT_STREAM_LENGTH", new String(), false);

        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH     LIKE '750'", 1, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH NOT LIKE '750'", 44, false, "CONTENT_STREAM_LENGTH", new String(), false);

        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH IS NOT NULL", 45, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_LENGTH IS     NULL", 0, false, "CONTENT_STREAM_LENGTH", new String(), false);

        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE 750 =  ANY CONTENT_STREAM_LENGTH", 1, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE 750 <> ANY CONTENT_STREAM_LENGTH", 44, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE 750 <  ANY CONTENT_STREAM_LENGTH", 28, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE 750 <= ANY CONTENT_STREAM_LENGTH", 29, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE 750 >  ANY CONTENT_STREAM_LENGTH", 16, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE 750 >= ANY CONTENT_STREAM_LENGTH", 17, false, "CONTENT_STREAM_LENGTH", new String(), false);

        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_LENGTH IN     (750)", 1, false, "CONTENT_STREAM_LENGTH", new String(), false);
        testQuery("SELECT CONTENT_STREAM_LENGTH FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_LENGTH NOT IN (750)", 44, false, "CONTENT_STREAM_LENGTH", new String(), false);
    }
    
    
    public void test_CONTENT_STREAM_ALLOWED()
    {
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED =  'ALLOWED'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED <> 'ALLOWED'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED <  'ALLOWED'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED <= 'ALLOWED'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED >  'ALLOWED'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED >= 'ALLOWED'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED IN     ('ALLOWED')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED NOT IN ('ALLOWED')", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED     LIKE 'ALLOWED'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED NOT LIKE 'ALLOWED'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE CONTENT_STREAM_ALLOWED IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE 'ALLOWED' =  ANY CONTENT_STREAM_ALLOWED", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE 'ALLOWED' <> ANY CONTENT_STREAM_ALLOWED", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE 'ALLOWED' <  ANY CONTENT_STREAM_ALLOWED", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE 'ALLOWED' <= ANY CONTENT_STREAM_ALLOWED", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE 'ALLOWED' >  ANY CONTENT_STREAM_ALLOWED", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE 'ALLOWED' >= ANY CONTENT_STREAM_ALLOWED", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_ALLOWED IN     ('ALLOWED')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CONTENT_STREAM_ALLOWED FROM DOCUMENT_OBJECT_TYPE WHERE ANY CONTENT_STREAM_ALLOWED NOT IN ('ALLOWED')", 0, false, "OBJECT_ID", new String(), false);
    }

    
    public void test_CHECKIN_COMMENT()
    {
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT =  'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT <> 'admin'", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT <  'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT <= 'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT >  'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT >= 'admin'", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT IN     ('admin')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT NOT IN ('admin')", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT     LIKE 'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT NOT LIKE 'admin'", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT IS NOT NULL", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE CHECKIN_COMMENT IS     NULL", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' =  ANY CHECKIN_COMMENT", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' <> ANY CHECKIN_COMMENT", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' <  ANY CHECKIN_COMMENT", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' <= ANY CHECKIN_COMMENT", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' >  ANY CHECKIN_COMMENT", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' >= ANY CHECKIN_COMMENT", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE ANY CHECKIN_COMMENT IN     ('admin')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT CHECKIN_COMMENT FROM DOCUMENT_OBJECT_TYPE WHERE ANY CHECKIN_COMMENT NOT IN ('admin')", 45, false, "OBJECT_ID", new String(), true);
    }
    
    public void test_VERSION_SERIES_CHECKED_OUT_ID()
    {
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID =  'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID <> 'admin'", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID <  'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID <= 'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID >  'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID >= 'admin'", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID IN     ('admin')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID NOT IN ('admin')", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID     LIKE 'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID NOT LIKE 'admin'", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID IS NOT NULL", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_ID IS     NULL", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' =  ANY VERSION_SERIES_CHECKED_OUT_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' <> ANY VERSION_SERIES_CHECKED_OUT_ID", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' <  ANY VERSION_SERIES_CHECKED_OUT_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' <= ANY VERSION_SERIES_CHECKED_OUT_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' >  ANY VERSION_SERIES_CHECKED_OUT_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' >= ANY VERSION_SERIES_CHECKED_OUT_ID", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_SERIES_CHECKED_OUT_ID IN     ('admin')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_ID FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_SERIES_CHECKED_OUT_ID NOT IN ('admin')", 45, false, "OBJECT_ID", new String(), true);
    }
    
    public void test_VERSION_SERIES_CHECKED_OUT_BY()
    {
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY =  'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY <> 'admin'", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY <  'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY <= 'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY >  'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY >= 'admin'", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY IN     ('admin')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY NOT IN ('admin')", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY     LIKE 'admin'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY NOT LIKE 'admin'", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY IS NOT NULL", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_CHECKED_OUT_BY IS     NULL", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' =  ANY VERSION_SERIES_CHECKED_OUT_BY", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' <> ANY VERSION_SERIES_CHECKED_OUT_BY", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' <  ANY VERSION_SERIES_CHECKED_OUT_BY", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' <= ANY VERSION_SERIES_CHECKED_OUT_BY", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' >  ANY VERSION_SERIES_CHECKED_OUT_BY", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'admin' >= ANY VERSION_SERIES_CHECKED_OUT_BY", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_SERIES_CHECKED_OUT_BY IN     ('admin')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_CHECKED_OUT_BY FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_SERIES_CHECKED_OUT_BY NOT IN ('admin')", 45, false, "OBJECT_ID", new String(), true);
    }

    
    public void test_VERSION_SERIES_IS_CHECKED_OUT()
    {
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT =  'TRUE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT <> 'TRUE'", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT <  'TRUE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT <= 'TRUE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT >  'TRUE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT >= 'TRUE'", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT IN     ('TRUE')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT NOT IN ('TRUE')", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT     LIKE 'TRUE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT NOT LIKE 'TRUE'", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT IS NOT NULL", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_IS_CHECKED_OUT IS     NULL", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' =  ANY VERSION_SERIES_IS_CHECKED_OUT", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <> ANY VERSION_SERIES_IS_CHECKED_OUT", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <  ANY VERSION_SERIES_IS_CHECKED_OUT", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <= ANY VERSION_SERIES_IS_CHECKED_OUT", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >  ANY VERSION_SERIES_IS_CHECKED_OUT", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >= ANY VERSION_SERIES_IS_CHECKED_OUT", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_SERIES_IS_CHECKED_OUT IN     ('TRUE')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_IS_CHECKED_OUT FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_SERIES_IS_CHECKED_OUT NOT IN ('TRUE')", 45, false, "OBJECT_ID", new String(), true);
    }
    
    
    public void test_VERSION_SERIES_ID()
    {
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID =  'company'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID <> 'company'", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID <  'company'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID <= 'company'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID >  'company'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID >= 'company'", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID IN     ('company')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID NOT IN ('company')", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID     LIKE 'company'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID NOT LIKE 'company'", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID IS NOT NULL", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_SERIES_ID IS     NULL", 45, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'company' =  ANY VERSION_SERIES_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'company' <> ANY VERSION_SERIES_ID", 45, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'company' <  ANY VERSION_SERIES_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'company' <= ANY VERSION_SERIES_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'company' >  ANY VERSION_SERIES_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'company' >= ANY VERSION_SERIES_ID", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_SERIES_ID IN     ('company')", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT VERSION_SERIES_ID FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_SERIES_ID NOT IN ('company')", 45, false, "OBJECT_ID", new String(), true);
    }

    public void test_VERSION_LABEL()
    {
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL =  'company'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL <> 'company'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL <  'company'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL <= 'company'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL >  'company'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL >= 'company'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL IN     ('company')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL NOT IN ('company')", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL     LIKE 'company'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL NOT LIKE 'company'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL IS NOT NULL", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE VERSION_LABEL IS     NULL", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE 'company' =  ANY VERSION_LABEL", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE 'company' <> ANY VERSION_LABEL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE 'company' <  ANY VERSION_LABEL", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE 'company' <= ANY VERSION_LABEL", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE 'company' >  ANY VERSION_LABEL", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE 'company' >= ANY VERSION_LABEL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_LABEL IN     ('company')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT VERSION_LABEL FROM DOCUMENT_OBJECT_TYPE WHERE ANY VERSION_LABEL NOT IN ('company')", 45, false, "OBJECT_ID", new String(), false);
    }

    public void test_IS_LATEST_MAJOR_VERSION()
    {
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION =  'TRUE'", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION <> 'TRUE'", 32, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION <  'TRUE'", 6, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION <= 'TRUE'", 7, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION >  'TRUE'", 30, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION >= 'TRUE'", 30, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION IN     ('TRUE')", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION NOT IN ('TRUE')", 32, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION     LIKE 'TRUE'", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION NOT LIKE 'TRUE'", 32, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION IS NOT NULL", 33, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_MAJOR_VERSION IS     NULL", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' =  ANY IS_LATEST_MAJOR_VERSION", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <> ANY IS_LATEST_MAJOR_VERSION", 32, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <  ANY IS_LATEST_MAJOR_VERSION", 6, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <= ANY IS_LATEST_MAJOR_VERSION", 7, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >  ANY IS_LATEST_MAJOR_VERSION", 30, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >= ANY IS_LATEST_MAJOR_VERSION", 30, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE ANY IS_LATEST_MAJOR_VERSION IN     ('TRUE')", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE ANY IS_LATEST_MAJOR_VERSION NOT IN ('TRUE')", 32, false, "OBJECT_ID", new String(), true);
    }

    public void test_IS_MAJOR_VERSION()
    {
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION =  'TRUE'", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION <> 'TRUE'", 32, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION <  'TRUE'", 6, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION <= 'TRUE'", 7, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION >  'TRUE'", 30, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION >= 'TRUE'", 30, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION IN     ('TRUE')", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION NOT IN ('TRUE')", 32, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION     LIKE 'TRUE'", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION NOT LIKE 'TRUE'", 32, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION IS NOT NULL", 33, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_MAJOR_VERSION IS     NULL", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' =  ANY IS_MAJOR_VERSION", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <> ANY IS_MAJOR_VERSION", 32, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <  ANY IS_MAJOR_VERSION", 6, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <= ANY IS_MAJOR_VERSION", 7, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >  ANY IS_MAJOR_VERSION", 30, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >= ANY IS_MAJOR_VERSION", 30, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE ANY IS_MAJOR_VERSION IN     ('TRUE')", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_MAJOR_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE ANY IS_MAJOR_VERSION NOT IN ('TRUE')", 32, false, "OBJECT_ID", new String(), true);
    }

    public void test_IS_LATEST_VERSION()
    {
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION =  'TRUE'", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION <> 'TRUE'", 32, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION <  'TRUE'", 6, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION <= 'TRUE'", 7, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION >  'TRUE'", 30, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION >= 'TRUE'", 30, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION IN     ('TRUE')", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION NOT IN ('TRUE')", 32, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION     LIKE 'TRUE'", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION NOT LIKE 'TRUE'", 32, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION IS NOT NULL", 33, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE IS_LATEST_VERSION IS     NULL", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' =  ANY IS_LATEST_VERSION", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <> ANY IS_LATEST_VERSION", 32, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <  ANY IS_LATEST_VERSION", 6, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <= ANY IS_LATEST_VERSION", 7, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >  ANY IS_LATEST_VERSION", 30, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >= ANY IS_LATEST_VERSION", 30, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE ANY IS_LATEST_VERSION IN     ('TRUE')", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_LATEST_VERSION FROM DOCUMENT_OBJECT_TYPE WHERE ANY IS_LATEST_VERSION NOT IN ('TRUE')", 32, false, "OBJECT_ID", new String(), true);
    }

    public void test_IS_IMMUTABLE()
    {
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE =  'TRUE'", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE <> 'TRUE'", 32, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE <  'TRUE'", 6, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE <= 'TRUE'", 7, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE >  'TRUE'", 30, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE >= 'TRUE'", 30, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE IN     ('TRUE')", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE NOT IN ('TRUE')", 32, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE     LIKE 'TRUE'", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE NOT LIKE 'TRUE'", 32, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE IS NOT NULL", 33, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE IS_IMMUTABLE IS     NULL", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' =  ANY IS_IMMUTABLE", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <> ANY IS_IMMUTABLE", 32, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <  ANY IS_IMMUTABLE", 6, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' <= ANY IS_IMMUTABLE", 7, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >  ANY IS_IMMUTABLE", 30, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE 'TRUE' >= ANY IS_IMMUTABLE", 30, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE ANY IS_IMMUTABLE IN     ('TRUE')", 1, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT IS_IMMUTABLE FROM DOCUMENT_OBJECT_TYPE WHERE ANY IS_IMMUTABLE NOT IN ('TRUE')", 32, false, "OBJECT_ID", new String(), true);
    }

    public void test_folder_NAME()
    {
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME =  'company'", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME <> 'company'", 32, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME <  'company'", 6, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME <= 'company'", 7, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME >  'company'", 30, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME >= 'company'", 30, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME IN     ('company')", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME NOT IN ('company')", 32, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME     LIKE 'company'", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME NOT LIKE 'company'", 32, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE NAME IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE 'company' =  ANY NAME", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE 'company' <> ANY NAME", 32, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE 'company' <  ANY NAME", 6, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE 'company' <= ANY NAME", 7, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE 'company' >  ANY NAME", 30, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE 'company' >= ANY NAME", 30, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE ANY NAME IN     ('company')", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT NAME FROM FOLDER_OBJECT_TYPE WHERE ANY NAME NOT IN ('company')", 32, false, "OBJECT_ID", new String(), false);
    }

    public void test_document_NAME()
    {
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME =  'tutorial'", 1, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME <> 'tutorial'", 44, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME <  'tutorial'", 45, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME <= 'tutorial'", 45, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME >  'tutorial'", 8, true, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME >= 'tutorial'", 9, false, "NAME", new String(), false);

        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME IN     ('tutorial')", 1, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME NOT IN ('tutorial')", 44, false, "NAME", new String(), false);

        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME     LIKE 'tutorial'", 1, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME NOT LIKE 'tutorial'", 44, false, "NAME", new String(), false);

        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME IS NOT NULL", 45, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE NAME IS     NULL", 0, false, "NAME", new String(), false);

        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' =  ANY NAME", 1, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' <> ANY NAME", 44, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' <  ANY NAME", 45, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' <= ANY NAME", 45, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' >  ANY NAME", 8, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE 'tutorial' >= ANY NAME", 9, false, "NAME", new String(), false);

        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE ANY NAME IN     ('tutorial')", 1, false, "NAME", new String(), false);
        testQuery("SELECT NAME FROM DOCUMENT_OBJECT_TYPE WHERE ANY NAME NOT IN ('tutorial')", 44, false, "NAME", new String(), false);
    }

    public void test_CHANGE_TOKEN()
    {
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN =  'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN <> 'test'", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN <  'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN <= 'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN >  'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN >= 'test'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN IN     ('test')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN NOT IN ('test')", 33, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN     LIKE 'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN NOT LIKE 'test'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN IS NOT NULL", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE CHANGE_TOKEN IS     NULL", 33, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE 'test' =  ANY CHANGE_TOKEN", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE 'test' <> ANY CHANGE_TOKEN", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE 'test' <  ANY CHANGE_TOKEN", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE 'test' <= ANY CHANGE_TOKEN", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE 'test' >  ANY CHANGE_TOKEN", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE 'test' >= ANY CHANGE_TOKEN", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE ANY CHANGE_TOKEN IN     ('test')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CHANGE_TOKEN FROM FOLDER_OBJECT_TYPE WHERE ANY CHANGE_TOKEN NOT IN ('test')", 33, false, "OBJECT_ID", new String(), false);
    }

    public void test_LAST_MODIFICATION_DATE()
    {
        // By default we are only working to the day

        Calendar today = Calendar.getInstance();
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true);

        Date date = testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE", -1, false, "LAST_MODIFICATION_DATE", new Date(), false);
        today.setTime(date);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));

        String sDate = df.format(today.getTime());

        // Today (assuming al ws created today)

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE =  '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE <> '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE <  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE <= '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE >  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE >= '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE IN     ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE NOT IN ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE     LIKE '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE NOT LIKE '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' =  ANY LAST_MODIFICATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <> ANY LAST_MODIFICATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <  ANY LAST_MODIFICATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <= ANY LAST_MODIFICATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >  ANY LAST_MODIFICATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >= ANY LAST_MODIFICATION_DATE", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY LAST_MODIFICATION_DATE IN     ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY LAST_MODIFICATION_DATE NOT IN ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE =  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE <> '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE <  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE <= '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE >  '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE >= '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE IN     ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE NOT IN ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE     LIKE '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE NOT LIKE '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' =  ANY LAST_MODIFICATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <> ANY LAST_MODIFICATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <  ANY LAST_MODIFICATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <= ANY LAST_MODIFICATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >  ANY LAST_MODIFICATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >= ANY LAST_MODIFICATION_DATE", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY LAST_MODIFICATION_DATE IN     ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY LAST_MODIFICATION_DATE NOT IN ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE =  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE <> '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE <  '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE <= '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE >  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE >= '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE IN     ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE NOT IN ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE     LIKE '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE NOT LIKE '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFICATION_DATE IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' =  ANY LAST_MODIFICATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <> ANY LAST_MODIFICATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <  ANY LAST_MODIFICATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <= ANY LAST_MODIFICATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >  ANY LAST_MODIFICATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >= ANY LAST_MODIFICATION_DATE", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY LAST_MODIFICATION_DATE IN     ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFICATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY LAST_MODIFICATION_DATE NOT IN ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);

    }

    public void test_LAST_MODIFIED_BY()
    {
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY =  'System'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY <> 'System'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY <  'System'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY <= 'System'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY >  'System'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY >= 'System'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY IN     ('System')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY NOT IN ('System')", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY     LIKE 'System'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY NOT LIKE 'System'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE LAST_MODIFIED_BY IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' =  ANY LAST_MODIFIED_BY", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' <> ANY LAST_MODIFIED_BY", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' <  ANY LAST_MODIFIED_BY", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' <= ANY LAST_MODIFIED_BY", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' >  ANY LAST_MODIFIED_BY", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' >= ANY LAST_MODIFIED_BY", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE ANY LAST_MODIFIED_BY IN     ('System')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT LAST_MODIFIED_BY FROM DOCUMENT_OBJECT_TYPE WHERE ANY LAST_MODIFIED_BY NOT IN ('System')", 0, false, "OBJECT_ID", new String(), false);

    }

    public void test_CREATION_DATE()
    {
        // By default we are only working to the day

        Calendar today = Calendar.getInstance();
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true);

        Date date = testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE", -1, false, "CREATION_DATE", new Date(), false);
        today.setTime(date);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));

        String sDate = df.format(today.getTime());

        // Today (assuming al ws created today)

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE =  '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE <> '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE <  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE <= '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE >  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE >= '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE IN     ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE NOT IN ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE     LIKE '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE NOT LIKE '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' =  ANY CREATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <> ANY CREATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <  ANY CREATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <= ANY CREATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >  ANY CREATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >= ANY CREATION_DATE", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY CREATION_DATE IN     ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY CREATION_DATE NOT IN ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE =  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE <> '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE <  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE <= '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE >  '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE >= '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE IN     ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE NOT IN ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE     LIKE '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE NOT LIKE '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' =  ANY CREATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <> ANY CREATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <  ANY CREATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <= ANY CREATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >  ANY CREATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >= ANY CREATION_DATE", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY CREATION_DATE IN     ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY CREATION_DATE NOT IN ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE =  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE <> '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE <  '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE <= '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE >  '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE >= '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE IN     ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE NOT IN ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE     LIKE '" + sDate + "'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE NOT LIKE '" + sDate + "'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE CREATION_DATE IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' =  ANY CREATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <> ANY CREATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <  ANY CREATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' <= ANY CREATION_DATE", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >  ANY CREATION_DATE", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE '" + sDate + "' >= ANY CREATION_DATE", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY CREATION_DATE IN     ('" + sDate + "')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATION_DATE FROM DOCUMENT_OBJECT_TYPE WHERE ANY CREATION_DATE NOT IN ('" + sDate + "')", 45, false, "OBJECT_ID", new String(), false);

    }

    public void test_CREATED_BY()
    {
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY =  'System'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY <> 'System'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY <  'System'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY <= 'System'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY >  'System'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY >= 'System'", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY IN     ('System')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY NOT IN ('System')", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY     LIKE 'System'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY NOT LIKE 'System'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE CREATED_BY IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' =  ANY CREATED_BY", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' <> ANY CREATED_BY", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' <  ANY CREATED_BY", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' <= ANY CREATED_BY", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' >  ANY CREATED_BY", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE 'System' >= ANY CREATED_BY", 45, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE ANY CREATED_BY IN     ('System')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT CREATED_BY FROM DOCUMENT_OBJECT_TYPE WHERE ANY CREATED_BY NOT IN ('System')", 0, false, "OBJECT_ID", new String(), false);

    }

    public void test_OBJECT_TYPE_ID()
    {
        // DOC

        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID =  'DOCUMENT_OBJECT_TYPE'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID <> 'DOCUMENT_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID <  'DOCUMENT_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID <= 'DOCUMENT_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID >  'DOCUMENT_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID >= 'DOCUMENT_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID IN     ('DOCUMENT_OBJECT_TYPE')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID NOT IN ('DOCUMENT_OBJECT_TYPE')", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID     LIKE 'DOCUMENT_OBJECT_TYPE'", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID NOT LIKE 'DOCUMENT_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID IS NOT NULL", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE OBJECT_TYPE_ID IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'DOCUMENT_OBJECT_TYPE' =  ANY OBJECT_TYPE_ID", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'DOCUMENT_OBJECT_TYPE' <> ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'DOCUMENT_OBJECT_TYPE' <  ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'DOCUMENT_OBJECT_TYPE' <= ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'DOCUMENT_OBJECT_TYPE' >  ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE 'DOCUMENT_OBJECT_TYPE' >= ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE ANY OBJECT_TYPE_ID IN     ('DOCUMENT_OBJECT_TYPE')", 45, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM DOCUMENT_OBJECT_TYPE WHERE ANY OBJECT_TYPE_ID NOT IN ('DOCUMENT_OBJECT_TYPE')", 0, false, "OBJECT_ID", new String(), false);

        // FOLDER

        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID =  'FOLDER_OBJECT_TYPE'", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID <> 'FOLDER_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID <  'FOLDER_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID <= 'FOLDER_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID >  'FOLDER_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID >= 'FOLDER_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID IN     ('FOLDER_OBJECT_TYPE')", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID NOT IN ('FOLDER_OBJECT_TYPE')", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID     LIKE 'FOLDER_OBJECT_TYPE'", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID NOT LIKE 'FOLDER_OBJECT_TYPE'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID IS NOT NULL", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_TYPE_ID IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE 'FOLDER_OBJECT_TYPE' =  ANY OBJECT_TYPE_ID", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE 'FOLDER_OBJECT_TYPE' <> ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE 'FOLDER_OBJECT_TYPE' <  ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE 'FOLDER_OBJECT_TYPE' <= ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE 'FOLDER_OBJECT_TYPE' >  ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE 'FOLDER_OBJECT_TYPE' >= ANY OBJECT_TYPE_ID", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE ANY OBJECT_TYPE_ID IN     ('FOLDER_OBJECT_TYPE')", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_TYPE_ID FROM FOLDER_OBJECT_TYPE WHERE ANY OBJECT_TYPE_ID NOT IN ('FOLDER_OBJECT_TYPE')", 0, false, "OBJECT_ID", new String(), false);

        // RELATIONSHIP

        testQuery("SELECT OBJECT_TYPE_ID FROM RELATIONSHIP_OBJECT_TYPE WHERE OBJECT_TYPE_ID =  ''", 1, false, "OBJECT_ID", new String(), true);

    }

    public void test_URI()
    {
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI =  'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI <> 'test'", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI <  'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI <= 'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI >  'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI >= 'test'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI IN     ('test')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI NOT IN ('test')", 33, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI     LIKE 'test'", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI NOT LIKE 'test'", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI IS NOT NULL", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE URI IS     NULL", 33, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE 'test' =  ANY URI", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE 'test' <> ANY URI", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE 'test' <  ANY URI", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE 'test' <= ANY URI", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE 'test' >  ANY URI", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE 'test' >= ANY URI", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE ANY URI IN     ('test')", 0, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT URI FROM FOLDER_OBJECT_TYPE WHERE ANY URI NOT IN ('test')", 33, false, "OBJECT_ID", new String(), false);
    }

    public void test_OBJECT_ID()
    {
        String companyHomeId = testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE NAME = '\"company home\"'", 1, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID =  '" + companyHomeId + "'", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID <> '" + companyHomeId + "'", 32, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID <  '" + companyHomeId + "'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID <= '" + companyHomeId + "'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID >  '" + companyHomeId + "'", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID >= '" + companyHomeId + "'", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID IN     ('" + companyHomeId + "')", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID NOT IN ('" + companyHomeId + "')", 32, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID     LIKE '" + companyHomeId + "'", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID NOT LIKE '" + companyHomeId + "'", 32, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE IN_FOLDER('" + companyHomeId + "')", 4, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE IN_TREE  ('" + companyHomeId + "')", 32, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID IS NOT NULL", 33, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE OBJECT_ID IS     NULL", 0, false, "OBJECT_ID", new String(), false);

        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE '" + companyHomeId + "' =  ANY OBJECT_ID", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE '" + companyHomeId + "' <> ANY OBJECT_ID", 32, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE '" + companyHomeId + "' <  ANY OBJECT_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE '" + companyHomeId + "' <= ANY OBJECT_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE '" + companyHomeId + "' >  ANY OBJECT_ID", 0, false, "OBJECT_ID", new String(), true);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE '" + companyHomeId + "' >= ANY OBJECT_ID", 0, false, "OBJECT_ID", new String(), true);

        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE ANY OBJECT_ID IN     ('" + companyHomeId + "')", 1, false, "OBJECT_ID", new String(), false);
        testQuery("SELECT OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE ANY OBJECT_ID NOT IN ('" + companyHomeId + "')", 32, false, "OBJECT_ID", new String(), false);
    }

    public void testOrderBy()
    {

        String query = "SELECT  OBJECT_ID FROM DOCUMENT_OBJECT_TYPE ORDER OBJECT_ID";
        CMISResultSet rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("OBJECT_ID") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT  OBJECT_ID FROM DOCUMENT_OBJECT_TYPE ORDER OBJECT_ID ASC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("OBJECT_ID") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT  OBJECT_ID FROM DOCUMENT_OBJECT_TYPE ORDER OBJECT_ID DESC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("OBJECT_ID") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT SCORE() AS MEEP, OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE NAME IN ('company', 'home') ORDER BY MEEP ASC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("OBJECT_ID") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT SCORE() AS MEEP, OBJECT_ID FROM FOLDER_OBJECT_TYPE WHERE NAME IN ('company', 'home') ORDER BY MEEP DESC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("OBJECT_ID") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

    }

    public void testAllSimpleTextPredicates()
    {
        String query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME = 'company'";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NOT NAME = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(32, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND 'company' = ANY NAME";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NOT NAME <> 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME <> 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(32, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME < 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(6, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME <= 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(7, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME > 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(30, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME >= 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(30, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME IN ('company', 'home')";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME NOT IN ('company', 'home')";
        rs = cmisQueryService.query(query);
        assertEquals(31, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND ANY NAME IN ('company', 'home')";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND ANY NAME NOT IN ('company', 'home')";
        rs = cmisQueryService.query(query);
        assertEquals(31, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME LIKE 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME LIKE 'com%'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME LIKE 'c_m_a_y'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME NOT LIKE 'c_m_a_y'";
        rs = cmisQueryService.query(query);
        assertEquals(32, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;
    }

    public void testSimpleConjunction()
    {

        String query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME = 'company'";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME = 'home'";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NOT NULL AND NAME = 'home' AND NAME = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
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
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME = 'guest' OR NAME = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.getLength());
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
        assertEquals(33, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME IS NULL";
        rs = cmisQueryService.query(query);
        assertEquals(0, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM DOCUMENT_OBJECT_TYPE WHERE URI IS NOT NULL";
        rs = cmisQueryService.query(query);
        assertEquals(0, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM DOCUMENT_OBJECT_TYPE WHERE URI IS NULL";
        rs = cmisQueryService.query(query);
        assertEquals(45, rs.getLength());
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
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE NAME = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE PARENT = '" + rootNode.toString() + "'";
        rs = cmisQueryService.query(query);
        assertEquals(4, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("NAME") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM FOLDER_OBJECT_TYPE WHERE ALLOWED_CHILD_OBJECT_TYPES = 'meep'";
        rs = cmisQueryService.query(query);
        assertEquals(0, rs.getLength());
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
        assertEquals(32, rs.getLength());
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
        assertEquals(4, rs.getLength());
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
        assertEquals(1, rs.getLength());
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
        assertEquals(2, rs.getLength());
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
