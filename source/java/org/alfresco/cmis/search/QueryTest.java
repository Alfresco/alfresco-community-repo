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
                            + row.getValue("ObjectId") + " " + ((returnPropertyName != null) ? (returnPropertyName + "=" + row.getValue(returnPropertyName)) : "") + " Score="
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
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds =  'test'", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds <> 'test'", 33, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds <  'test'", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds <= 'test'", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds >  'test'", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds >= 'test'", 0, false, "AllowedChildObjectTypeIds", new String(), false);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds IN     ('test')", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds NOT IN ('test')", 33, false, "AllowedChildObjectTypeIds", new String(), false);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds     LIKE 'test'", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds NOT LIKE 'test'", 0, false, "AllowedChildObjectTypeIds", new String(), false);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds IS NOT NULL", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds IS     NULL", 33, false, "AllowedChildObjectTypeIds", new String(), false);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' =  ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' <> ANY AllowedChildObjectTypeIds", 33, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' <  ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' <= ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' >  ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' >= ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), false);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE ANY AllowedChildObjectTypeIds IN     ('test')", 0, false, "AllowedChildObjectTypeIds", new String(), false);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE ANY AllowedChildObjectTypeIds NOT IN ('test')", 33, false, "AllowedChildObjectTypeIds", new String(), false);
    }
    
    public void test_PARENT()
    {
        NodeRef rootNode = cmisService.getDefaultRootNodeRef();
        
        testQuery("SELECT ParentId FROM Folder WHERE ParentId =  '" + rootNode.toString() + "'", 4, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId <> '" + rootNode.toString() + "'", 29, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId <  '" + rootNode.toString() + "'", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId <= '" + rootNode.toString() + "'", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId >  '" + rootNode.toString() + "'", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId >= '" + rootNode.toString() + "'", 0, false, "ParentId", new String(), true);

        testQuery("SELECT ParentId FROM Folder WHERE ParentId IN     ('" + rootNode.toString() + "')", 4, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId NOT IN ('" + rootNode.toString() + "')", 29, false, "ParentId", new String(), false);

        testQuery("SELECT ParentId FROM Folder WHERE ParentId     LIKE '" + rootNode.toString() + "'", 4, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId NOT LIKE '" + rootNode.toString() + "'", 29, false, "ParentId", new String(), false);

        testQuery("SELECT ParentId FROM Folder WHERE ParentId IS NOT NULL", 33, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId IS     NULL", 0, false, "ParentId", new String(), false);

        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNode.toString() + "' =  ANY ParentId", 4, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNode.toString() + "' <> ANY ParentId", 29, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNode.toString() + "' <  ANY ParentId", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNode.toString() + "' <= ANY ParentId", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNode.toString() + "' >  ANY ParentId", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNode.toString() + "' >= ANY ParentId", 0, false, "ParentId", new String(), true);

        testQuery("SELECT ParentId FROM Folder WHERE ANY ParentId IN     ('" + rootNode.toString() + "')", 4, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ANY ParentId NOT IN ('" + rootNode.toString() + "')", 29, false, "ParentId", new String(), false);
    }

    
    public void test_CONTENT_STREAM_URI()
    {
        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri =  'test'", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri <> 'test'", 45, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri <  'test'", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri <= 'test'", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri >  'test'", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri >= 'test'", 0, false, "ContentStreamUri", new String(), false);

        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri IN     ('test')", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri NOT IN ('test')", 45, false, "ContentStreamUri", new String(), false);

        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri     LIKE 'test'", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri NOT LIKE 'test'", 0, false, "ContentStreamUri", new String(), false);

        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri IS NOT NULL", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE ContentStreamUri IS     NULL", 45, false, "ContentStreamUri", new String(), false);

        testQuery("SELECT ContentStreamUri FROM Document WHERE 'test' =  ANY ContentStreamUri", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE 'test' <> ANY ContentStreamUri", 45, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE 'test' <  ANY ContentStreamUri", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE 'test' <= ANY ContentStreamUri", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE 'test' >  ANY ContentStreamUri", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE 'test' >= ANY ContentStreamUri", 0, false, "ContentStreamUri", new String(), false);

        testQuery("SELECT ContentStreamUri FROM Document WHERE ANY ContentStreamUri IN     ('test')", 0, false, "ContentStreamUri", new String(), false);
        testQuery("SELECT ContentStreamUri FROM Document WHERE ANY ContentStreamUri NOT IN ('test')", 45, false, "ContentStreamUri", new String(), false);
    }

    
    public void test_CONTENT_STREAM_FILENAME()
    {
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'tutorial'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename <> 'tutorial'", 44, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename <  'tutorial'", 45, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename <= 'tutorial'", 45, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename >  'tutorial'", 8, true, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename >= 'tutorial'", 9, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename IN     ('tutorial')", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename NOT IN ('tutorial')", 44, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename     LIKE 'tutorial'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename NOT LIKE 'tutorial'", 44, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename IS NOT NULL", 45, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename IS     NULL", 0, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'tutorial' =  ANY ContentStreamFilename", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'tutorial' <> ANY ContentStreamFilename", 44, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'tutorial' <  ANY ContentStreamFilename", 45, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'tutorial' <= ANY ContentStreamFilename", 45, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'tutorial' >  ANY ContentStreamFilename", 8, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'tutorial' >= ANY ContentStreamFilename", 9, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE ANY ContentStreamFilename IN     ('tutorial')", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ANY ContentStreamFilename NOT IN ('tutorial')", 44, false, "ContentStreamFilename", new String(), false);
    }
    
    public void test_CONTENT_STREAM_MIME_TYPE()
    {
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType =  'text/plain'", 26, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType <> 'text/plain'", 19, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType <  'text/plain'", 14, true, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType <= 'text/plain'", 40, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType >  'text/plain'", 5, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType >= 'text/plain'", 31, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType IN     ('text/plain')", 26, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType NOT IN ('text/plain')", 19, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType     LIKE 'text/plain'", 26, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType NOT LIKE 'text/plain'", 19, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType IS NOT NULL", 45, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType IS     NULL", 0, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' =  ANY ContentStreamMimeType", 26, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' <> ANY ContentStreamMimeType", 19, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' <  ANY ContentStreamMimeType", 14, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' <= ANY ContentStreamMimeType", 40, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' >  ANY ContentStreamMimeType", 5, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' >= ANY ContentStreamMimeType", 31, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ANY ContentStreamMimeType IN     ('text/plain')", 26, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ANY ContentStreamMimeType NOT IN ('text/plain')", 19, false, "ContentStreamMimeType", new String(), false);
    }

    public void test_CONTENT_STREAM_LENGTH()
    {
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength =  750", 1, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength <> 750", 44, true, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength <  750", 28, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength <= 750", 29, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength >  750", 16, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength >= 750", 17, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength IN     (750)", 1, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength NOT IN (750)", 44, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength     LIKE '750'", 1, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength NOT LIKE '750'", 44, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength IS NOT NULL", 45, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength IS     NULL", 0, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 =  ANY ContentStreamLength", 1, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 <> ANY ContentStreamLength", 44, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 <  ANY ContentStreamLength", 28, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 <= ANY ContentStreamLength", 29, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 >  ANY ContentStreamLength", 16, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 >= ANY ContentStreamLength", 17, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE ANY ContentStreamLength IN     (750)", 1, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ANY ContentStreamLength NOT IN (750)", 44, false, "ContentStreamLength", new String(), false);
    }
    
    
    public void test_CONTENT_STREAM_ALLOWED()
    {
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed =  'ALLOWED'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed <> 'ALLOWED'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed <  'ALLOWED'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed <= 'ALLOWED'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed >  'ALLOWED'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed >= 'ALLOWED'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed IN     ('ALLOWED')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed NOT IN ('ALLOWED')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed     LIKE 'ALLOWED'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed NOT LIKE 'ALLOWED'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' =  ANY ContentStreamAllowed", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' <> ANY ContentStreamAllowed", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' <  ANY ContentStreamAllowed", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' <= ANY ContentStreamAllowed", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' >  ANY ContentStreamAllowed", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' >= ANY ContentStreamAllowed", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ANY ContentStreamAllowed IN     ('ALLOWED')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ANY ContentStreamAllowed NOT IN ('ALLOWED')", 0, false, "ObjectId", new String(), false);
    }

    
    public void test_CHECKIN_COMMENT()
    {
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment =  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment <> 'admin'", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment <  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment <= 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment >  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment >= 'admin'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment NOT IN ('admin')", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment     LIKE 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment NOT LIKE 'admin'", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment IS     NULL", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' =  ANY CheckinComment", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' <> ANY CheckinComment", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' <  ANY CheckinComment", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' <= ANY CheckinComment", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' >  ANY CheckinComment", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' >= ANY CheckinComment", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE ANY CheckinComment IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE ANY CheckinComment NOT IN ('admin')", 45, false, "ObjectId", new String(), true);
    }
    
    public void test_VERSION_SERIES_CHECKED_OUT_ID()
    {
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId =  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId <> 'admin'", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId <  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId <= 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId >  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId >= 'admin'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId NOT IN ('admin')", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId     LIKE 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId NOT LIKE 'admin'", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId IS     NULL", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' =  ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' <> ANY VersionSeriesCheckedOutId", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' <  ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' <= ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' >  ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' >= ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE ANY VersionSeriesCheckedOutId IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE ANY VersionSeriesCheckedOutId NOT IN ('admin')", 45, false, "ObjectId", new String(), true);
    }
    
    public void test_VERSION_SERIES_CHECKED_OUT_BY()
    {
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy =  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy <> 'admin'", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy <  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy <= 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy >  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy >= 'admin'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy NOT IN ('admin')", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy     LIKE 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy NOT LIKE 'admin'", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy IS     NULL", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' =  ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' <> ANY VersionSeriesCheckedOutBy", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' <  ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' <= ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' >  ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' >= ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE ANY VersionSeriesCheckedOutBy IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE ANY VersionSeriesCheckedOutBy NOT IN ('admin')", 45, false, "ObjectId", new String(), true);
    }

    
    public void test_VERSION_SERIES_IS_CHECKED_OUT()
    {
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut =  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut <> 'TRUE'", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut <  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut <= 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut >  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut >= 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut NOT IN ('TRUE')", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut     LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut NOT LIKE 'TRUE'", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut IS     NULL", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' =  ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' <> ANY IsVeriesSeriesCheckedOut", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' <  ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' <= ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' >  ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' >= ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE ANY IsVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE ANY IsVeriesSeriesCheckedOut NOT IN ('TRUE')", 45, false, "ObjectId", new String(), true);
    }
    
    
    public void test_VERSION_SERIES_ID()
    {
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId =  'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId <> 'company'", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId <  'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId <= 'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId >  'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId >= 'company'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId IN     ('company')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId NOT IN ('company')", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId     LIKE 'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId NOT LIKE 'company'", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId IS     NULL", 45, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' =  ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' <> ANY VersionSeriesId", 45, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' <  ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' <= ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' >  ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' >= ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE ANY VersionSeriesId IN     ('company')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE ANY VersionSeriesId NOT IN ('company')", 45, false, "ObjectId", new String(), true);
    }

    public void test_VERSION_LABEL()
    {
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel =  'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel <> 'company'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel <  'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel <= 'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel >  'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel >= 'company'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel IN     ('company')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel NOT IN ('company')", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel     LIKE 'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel NOT LIKE 'company'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel IS NOT NULL", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel IS     NULL", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE 'company' =  ANY VersionLabel", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' <> ANY VersionLabel", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' <  ANY VersionLabel", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' <= ANY VersionLabel", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' >  ANY VersionLabel", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' >= ANY VersionLabel", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE ANY VersionLabel IN     ('company')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE ANY VersionLabel NOT IN ('company')", 45, false, "ObjectId", new String(), false);
    }

    public void test_IS_LATEST_MAJOR_VERSION()
    {
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion =  'TRUE'", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion <> 'TRUE'", 32, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion <  'TRUE'", 6, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion <= 'TRUE'", 7, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion >  'TRUE'", 30, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion >= 'TRUE'", 30, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion IN     ('TRUE')", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion NOT IN ('TRUE')", 32, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion     LIKE 'TRUE'", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion NOT LIKE 'TRUE'", 32, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion IS NOT NULL", 33, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion IS     NULL", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' =  ANY IsLatestMajorVersion", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' <> ANY IsLatestMajorVersion", 32, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' <  ANY IsLatestMajorVersion", 6, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' <= ANY IsLatestMajorVersion", 7, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' >  ANY IsLatestMajorVersion", 30, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' >= ANY IsLatestMajorVersion", 30, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE ANY IsLatestMajorVersion IN     ('TRUE')", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE ANY IsLatestMajorVersion NOT IN ('TRUE')", 32, false, "ObjectId", new String(), true);
    }

    public void test_IS_MAJOR_VERSION()
    {
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion =  'TRUE'", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion <> 'TRUE'", 32, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion <  'TRUE'", 6, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion <= 'TRUE'", 7, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion >  'TRUE'", 30, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion >= 'TRUE'", 30, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion IN     ('TRUE')", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion NOT IN ('TRUE')", 32, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion     LIKE 'TRUE'", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion NOT LIKE 'TRUE'", 32, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion IS NOT NULL", 33, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion IS     NULL", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' =  ANY IsMajorVersion", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' <> ANY IsMajorVersion", 32, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' <  ANY IsMajorVersion", 6, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' <= ANY IsMajorVersion", 7, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' >  ANY IsMajorVersion", 30, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' >= ANY IsMajorVersion", 30, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE ANY IsMajorVersion IN     ('TRUE')", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE ANY IsMajorVersion NOT IN ('TRUE')", 32, false, "ObjectId", new String(), true);
    }

    public void test_IS_LATEST_VERSION()
    {
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion =  'TRUE'", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion <> 'TRUE'", 32, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion <  'TRUE'", 6, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion <= 'TRUE'", 7, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion >  'TRUE'", 30, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion >= 'TRUE'", 30, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion IN     ('TRUE')", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion NOT IN ('TRUE')", 32, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion     LIKE 'TRUE'", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion NOT LIKE 'TRUE'", 32, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion IS NOT NULL", 33, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion IS     NULL", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' =  ANY IsLatestVersion", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' <> ANY IsLatestVersion", 32, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' <  ANY IsLatestVersion", 6, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' <= ANY IsLatestVersion", 7, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' >  ANY IsLatestVersion", 30, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' >= ANY IsLatestVersion", 30, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE ANY IsLatestVersion IN     ('TRUE')", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE ANY IsLatestVersion NOT IN ('TRUE')", 32, false, "ObjectId", new String(), true);
    }

    public void test_IS_IMMUTABLE()
    {
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable =  'TRUE'", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable <> 'TRUE'", 32, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable <  'TRUE'", 6, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable <= 'TRUE'", 7, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable >  'TRUE'", 30, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable >= 'TRUE'", 30, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable IN     ('TRUE')", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable NOT IN ('TRUE')", 32, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable     LIKE 'TRUE'", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable NOT LIKE 'TRUE'", 32, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable IS NOT NULL", 33, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable IS     NULL", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' =  ANY IsImmutable", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' <> ANY IsImmutable", 32, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' <  ANY IsImmutable", 6, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' <= ANY IsImmutable", 7, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' >  ANY IsImmutable", 30, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' >= ANY IsImmutable", 30, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE ANY IsImmutable IN     ('TRUE')", 1, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE ANY IsImmutable NOT IN ('TRUE')", 32, false, "ObjectId", new String(), true);
    }

    public void test_folder_NAME()
    {
        testQuery("SELECT Name FROM Folder WHERE Name =  'company'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name <> 'company'", 32, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name <  'company'", 6, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name <= 'company'", 7, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name >  'company'", 30, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name >= 'company'", 30, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE Name IN     ('company')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name NOT IN ('company')", 32, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE Name     LIKE 'company'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name NOT LIKE 'company'", 32, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE Name IS NOT NULL", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE 'company' =  ANY Name", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'company' <> ANY Name", 32, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'company' <  ANY Name", 6, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'company' <= ANY Name", 7, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'company' >  ANY Name", 30, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'company' >= ANY Name", 30, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE ANY Name IN     ('company')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE ANY Name NOT IN ('company')", 32, false, "ObjectId", new String(), false);
    }

    public void test_document_Name()
    {
        testQuery("SELECT Name FROM Document WHERE Name =  'tutorial'", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name <> 'tutorial'", 44, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name <  'tutorial'", 45, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name <= 'tutorial'", 45, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name >  'tutorial'", 8, true, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name >= 'tutorial'", 9, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE Name IN     ('tutorial')", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name NOT IN ('tutorial')", 44, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE Name     LIKE 'tutorial'", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name NOT LIKE 'tutorial'", 44, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE Name IS NOT NULL", 45, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name IS     NULL", 0, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE 'tutorial' =  ANY Name", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'tutorial' <> ANY Name", 44, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'tutorial' <  ANY Name", 45, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'tutorial' <= ANY Name", 45, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'tutorial' >  ANY Name", 8, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'tutorial' >= ANY Name", 9, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE ANY Name IN     ('tutorial')", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE ANY Name NOT IN ('tutorial')", 44, false, "Name", new String(), false);
    }

    public void test_CHANGE_TOKEN()
    {
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken =  'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken <> 'test'", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken <  'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken <= 'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken >  'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken >= 'test'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken IN     ('test')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken NOT IN ('test')", 33, false, "ObjectId", new String(), false);

        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken     LIKE 'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken NOT LIKE 'test'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken IS NOT NULL", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken IS     NULL", 33, false, "ObjectId", new String(), false);

        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' =  ANY ChangeToken", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' <> ANY ChangeToken", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' <  ANY ChangeToken", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' <= ANY ChangeToken", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' >  ANY ChangeToken", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' >= ANY ChangeToken", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ChangeToken FROM Folder WHERE ANY ChangeToken IN     ('test')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ChangeToken FROM Folder WHERE ANY ChangeToken NOT IN ('test')", 33, false, "ObjectId", new String(), false);
    }

    public void test_LAST_MODIFICATION_DATE()
    {
        // By default we are only working to the day

        Calendar today = Calendar.getInstance();
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true);

        Date date = testQuery("SELECT LastModificationDate FROM Document", -1, false, "LastModificationDate", new Date(), false);
        today.setTime(date);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));

        String sDate = df.format(today.getTime());

        // Today (assuming al ws created today)

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate =  '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <> '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <= '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >= '" + sDate + "'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IN     ('" + sDate + "')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT IN ('" + sDate + "')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate     LIKE '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' =  ANY LastModificationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <> ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <= ANY LastModificationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >= ANY LastModificationDate", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate IN     ('" + sDate + "')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate NOT IN ('" + sDate + "')", 0, false, "ObjectId", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate =  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <> '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <= '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >  '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >= '" + sDate + "'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT IN ('" + sDate + "')", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate     LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT LIKE '" + sDate + "'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' =  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <> ANY LastModificationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <= ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >  ANY LastModificationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >= ANY LastModificationDate", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate NOT IN ('" + sDate + "')", 45, false, "ObjectId", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate =  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <> '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <  '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <= '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >= '" + sDate + "'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT IN ('" + sDate + "')", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate     LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT LIKE '" + sDate + "'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' =  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <> ANY LastModificationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <  ANY LastModificationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <= ANY LastModificationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >= ANY LastModificationDate", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate NOT IN ('" + sDate + "')", 45, false, "ObjectId", new String(), false);

    }

    public void test_LAST_MODIFIED_BY()
    {
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy =  'System'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy <> 'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy <  'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy <= 'System'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy >  'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy >= 'System'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy IN     ('System')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy NOT IN ('System')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy     LIKE 'System'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy NOT LIKE 'System'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' =  ANY LastModifiedBy", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' <> ANY LastModifiedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' <  ANY LastModifiedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' <= ANY LastModifiedBy", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' >  ANY LastModifiedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' >= ANY LastModifiedBy", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE ANY LastModifiedBy IN     ('System')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE ANY LastModifiedBy NOT IN ('System')", 0, false, "ObjectId", new String(), false);

    }

    public void test_CREATION_DATE()
    {
        // By default we are only working to the day

        Calendar today = Calendar.getInstance();
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true);

        Date date = testQuery("SELECT CreationDate FROM Document", -1, false, "CreationDate", new Date(), false);
        today.setTime(date);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));

        String sDate = df.format(today.getTime());

        // Today (assuming al ws created today)

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate =  '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <> '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <= '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >= '" + sDate + "'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IN     ('" + sDate + "')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT IN ('" + sDate + "')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate     LIKE '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' =  ANY CreationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <> ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <= ANY CreationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >= ANY CreationDate", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate IN     ('" + sDate + "')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate NOT IN ('" + sDate + "')", 0, false, "ObjectId", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate =  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <> '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <= '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >  '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >= '" + sDate + "'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT IN ('" + sDate + "')", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate     LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT LIKE '" + sDate + "'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' =  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <> ANY CreationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <= ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >  ANY CreationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >= ANY CreationDate", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate NOT IN ('" + sDate + "')", 45, false, "ObjectId", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate =  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <> '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <  '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <= '" + sDate + "'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >= '" + sDate + "'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT IN ('" + sDate + "')", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate     LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT LIKE '" + sDate + "'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' =  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <> ANY CreationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <  ANY CreationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <= ANY CreationDate", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >= ANY CreationDate", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate NOT IN ('" + sDate + "')", 45, false, "ObjectId", new String(), false);

    }

    public void test_CREATED_BY()
    {
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy =  'System'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy <> 'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy <  'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy <= 'System'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy >  'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy >= 'System'", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy IN     ('System')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy NOT IN ('System')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy     LIKE 'System'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy NOT LIKE 'System'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE 'System' =  ANY CreatedBy", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' <> ANY CreatedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' <  ANY CreatedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' <= ANY CreatedBy", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' >  ANY CreatedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' >= ANY CreatedBy", 45, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE ANY CreatedBy IN     ('System')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE ANY CreatedBy NOT IN ('System')", 0, false, "ObjectId", new String(), false);

    }

    public void test_OBJECT_TYPE_ID()
    {
        // DOC

        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId =  'Document'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId <> 'Document'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId <  'Document'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId <= 'Document'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId >  'Document'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId >= 'Document'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId IN     ('Document')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId NOT IN ('Document')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId     LIKE 'Document'", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId NOT LIKE 'Document'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId IS NOT NULL", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' =  ANY ObjectTypeId", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' <> ANY ObjectTypeId", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' <  ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' <= ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' >  ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' >= ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectTypeId FROM Document WHERE ANY ObjectTypeId IN     ('Document')", 45, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ANY ObjectTypeId NOT IN ('Document')", 0, false, "ObjectId", new String(), false);

        // FOLDER

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId =  'Folder'", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId <> 'Folder'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId <  'Folder'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId <= 'Folder'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId >  'Folder'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId >= 'Folder'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId IN     ('Folder')", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId NOT IN ('Folder')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId     LIKE 'Folder'", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId NOT LIKE 'Folder'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId IS NOT NULL", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' =  ANY ObjectTypeId", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' <> ANY ObjectTypeId", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' <  ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' <= ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' >  ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' >= ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ANY ObjectTypeId IN     ('Folder')", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ANY ObjectTypeId NOT IN ('Folder')", 0, false, "ObjectId", new String(), false);

        // RELATIONSHIP

        testQuery("SELECT ObjectTypeId FROM Relationship WHERE ObjectTypeId =  ''", 1, false, "ObjectId", new String(), true);

    }

    public void test_URI()
    {
        testQuery("SELECT Uri FROM Folder WHERE Uri =  'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE Uri <> 'test'", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE Uri <  'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE Uri <= 'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE Uri >  'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE Uri >= 'test'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT Uri FROM Folder WHERE Uri IN     ('test')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE Uri NOT IN ('test')", 33, false, "ObjectId", new String(), false);

        testQuery("SELECT Uri FROM Folder WHERE Uri     LIKE 'test'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE Uri NOT LIKE 'test'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT Uri FROM Folder WHERE Uri IS NOT NULL", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE Uri IS     NULL", 33, false, "ObjectId", new String(), false);

        testQuery("SELECT Uri FROM Folder WHERE 'test' =  ANY Uri", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE 'test' <> ANY Uri", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE 'test' <  ANY Uri", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE 'test' <= ANY Uri", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE 'test' >  ANY Uri", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE 'test' >= ANY Uri", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT Uri FROM Folder WHERE ANY Uri IN     ('test')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT Uri FROM Folder WHERE ANY Uri NOT IN ('test')", 33, false, "ObjectId", new String(), false);
    }

    public void test_ObjectId()
    {
        String companyHomeId = testQuery("SELECT ObjectId FROM Folder WHERE Name = '\"company home\"'", 1, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId =  '" + companyHomeId + "'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId <> '" + companyHomeId + "'", 32, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId <  '" + companyHomeId + "'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId <= '" + companyHomeId + "'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId >  '" + companyHomeId + "'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId >= '" + companyHomeId + "'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId IN     ('" + companyHomeId + "')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId NOT IN ('" + companyHomeId + "')", 32, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId     LIKE '" + companyHomeId + "'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId NOT LIKE '" + companyHomeId + "'", 32, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectId FROM Folder WHERE IN_FOLDER('" + companyHomeId + "')", 4, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE IN_TREE  ('" + companyHomeId + "')", 32, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId IS NOT NULL", 33, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' =  ANY ObjectId", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' <> ANY ObjectId", 32, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' <  ANY ObjectId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' <= ANY ObjectId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' >  ANY ObjectId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' >= ANY ObjectId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectId FROM Folder WHERE ANY ObjectId IN     ('" + companyHomeId + "')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ANY ObjectId NOT IN ('" + companyHomeId + "')", 32, false, "ObjectId", new String(), false);
    }

    public void testOrderBy()
    {

        String query = "SELECT  ObjectId FROM Document ORDER ObjectId";
        CMISResultSet rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT  ObjectId FROM Document ORDER ObjectId ASC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT  ObjectId FROM Document ORDER ObjectId DESC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT SCORE() AS MEEP, ObjectId FROM Folder WHERE Name IN ('company', 'home') ORDER BY MEEP ASC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT SCORE() AS MEEP, ObjectId FROM Folder WHERE Name IN ('company', 'home') ORDER BY MEEP DESC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

    }

    public void testAllSimpleTextPredicates()
    {
        String query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name = 'company'";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND NOT Name = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(32, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND 'company' = ANY Name";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND NOT Name <> 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name <> 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(32, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name < 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(6, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name <= 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(7, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name > 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(30, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name >= 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(30, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name IN ('company', 'home')";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name NOT IN ('company', 'home')";
        rs = cmisQueryService.query(query);
        assertEquals(31, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND ANY Name IN ('company', 'home')";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND ANY Name NOT IN ('company', 'home')";
        rs = cmisQueryService.query(query);
        assertEquals(31, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name LIKE 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name LIKE 'com%'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name LIKE 'c_m_a_y'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name NOT LIKE 'c_m_a_y'";
        rs = cmisQueryService.query(query);
        assertEquals(32, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;
    }

    public void testSimpleConjunction()
    {

        String query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name = 'company'";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name = 'home'";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NOT NULL AND Name = 'home' AND Name = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;
    }

    public void testSimpleDisjunction()
    {

        String query = "SELECT * FROM Folder WHERE Name = 'guest'";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name = 'guest' OR Name = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(2, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

    }

    public void testExists()
    {
        String query = "SELECT * FROM Folder WHERE Name IS NOT NULL";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(33, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name IS NULL";
        rs = cmisQueryService.query(query);
        assertEquals(0, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Document WHERE Uri IS NOT NULL";
        rs = cmisQueryService.query(query);
        assertEquals(0, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Document WHERE Uri IS NULL";
        rs = cmisQueryService.query(query);
        assertEquals(45, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
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
        String Name = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        String query = "SELECT * FROM Folder WHERE Name = '" + Name + "'";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE Name = 'company'";
        rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE ParentId = '" + rootNode.toString() + "'";
        rs = cmisQueryService.query(query);
        assertEquals(4, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT * FROM Folder WHERE AllowedChildObjectTypeIds = 'meep'";
        rs = cmisQueryService.query(query);
        assertEquals(0, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

    }

    public void test_IN_TREE()
    {
        NodeRef rootNode = cmisService.getDefaultRootNodeRef();

        Serializable ser = cmisPropertyService.getProperty(rootNode, CMISMapping.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        String query = "SELECT * FROM Folder WHERE IN_TREE('" + id + "')";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(32, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();

    }

    public void test_IN_FOLDER()
    {
        NodeRef rootNode = cmisService.getDefaultRootNodeRef();

        Serializable ser = cmisPropertyService.getProperty(rootNode, CMISMapping.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        String query = "SELECT * FROM Folder WHERE IN_FOLDER('" + id + "')";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(4, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
    }

    public void testFTS()
    {
        String query = "SELECT * FROM Document WHERE CONTAINS('\"Sample demonstrating the listing of AVM folder contents\"')";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("Name") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
    }

    public void testBasicSelectAsGuest()
    {
        runAs("guest");
        String query = "SELECT * FROM Document";
        CMISResultSet rs = cmisQueryService.query(query);
        assertEquals(2, rs.getLength());
        rs.close();
    }

    public void testBasicSelect()
    {
        String query = "SELECT * FROM Document";
        CMISResultSet rs = cmisQueryService.query(query);
        for (CMISResultSetRow row : rs)
        {
            System.out.println("Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
    }

    public void testBasicDefaultMetaData()
    {
        String query = "SELECT * FROM Document";
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
        String query = "SELECT DOC.ObjectId, DOC.ObjectId AS ID FROM Document AS DOC";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(2, md.getColumnNames().length);
        assertNotNull(md.getColumn("DOC.ObjectId"));
        assertNotNull(md.getColumn("ID"));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector("DOC"));
        rs.close();
    }

    public void testBasicColumns()
    {
        String query = "SELECT DOC.ObjectId, DOC.ObjectTypeId AS ID FROM Folder AS DOC";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(2, md.getColumnNames().length);
        assertNotNull(md.getColumn("DOC.ObjectId"));
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
        String query = "SELECT DOC.*  FROM Document AS DOC";
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
        String query = "SELECT *  FROM Folder AS DOC";
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
        String query = "SELECT DOC.Name AS Name, \nLOWER(\tDOC.Name \n), LOWER ( DOC.Name )  AS LName, UPPER ( DOC.Name ) , UPPER(DOC.Name) AS UName, Score(), SCORE(DOC), SCORE() AS SCORED, SCORE(DOC) AS DOCSCORE FROM Folder AS DOC";
        CMISResultSet rs = cmisQueryService.query(query);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(9, md.getColumnNames().length);
        assertNotNull(md.getColumn("Name"));
        assertNotNull(md.getColumn("LOWER(\tDOC.Name \n)"));
        assertNotNull(md.getColumn("LName"));
        assertNotNull(md.getColumn("UPPER ( DOC.Name )"));
        assertNotNull(md.getColumn("UName"));
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
        String query = "SELECT UPPER(1.0) AS WOOF FROM Document AS DOC LEFT OUTER JOIN Folder AS FOLDER ON (DOC.Name = FOLDER.Name) WHERE LOWER(DOC.Name = ' woof' AND CONTAINS(, 'one two three') AND  CONTAINS(, 'DOC.Name:lemur AND woof') AND (DOC.Name in ('one', 'two') AND IN_FOLDER('meep') AND DOC.Name like 'woof' and DOC.Name = 'woof' and DOC.ObjectId = 'meep') ORDER BY DOC.Name DESC, WOOF";
        cmisQueryService.query(query);
    }

    public void xtestParse2()
    {
        String query = "SELECT TITLE, AUTHORS, DATE FROM WHITE_PAPER WHERE ( IN_TREE( , 'ID00093854763') ) AND ( 'SMITH' = ANY AUTHORS )";
        cmisQueryService.query(query);
    }

    public void xtestParse3()
    {
        String query = "SELECT ObjectId, SCORE() AS X, DESTINATION, DEPARTURE_DATES FROM TRAVEL_BROCHURE WHERE ( CONTAINS(, 'CARIBBEAN CENTRAL AMERICA CRUISE TOUR') ) AND ( '2009-1-1' < ANY DEPARTURE_DATES ) ORDER BY X DESC";
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
