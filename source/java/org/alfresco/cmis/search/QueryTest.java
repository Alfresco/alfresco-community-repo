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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISQueryException;
import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISResultSetMetaData;
import org.alfresco.cmis.CMISResultSetRow;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISQueryOptions.CMISQueryMode;
import org.alfresco.cmis.mapping.BaseCMISTest;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.parsers.CMISLexer;
import org.alfresco.repo.search.impl.parsers.CMISParser;
import org.alfresco.repo.search.impl.parsers.FTSQueryException;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

/**
 * @author andyh
 */
public class QueryTest extends BaseCMISTest
{
    private int file_count = 0;

    private int folder_count = 0;

    private NodeRef f0;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
      
        f0 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 0", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f0, ContentModel.PROP_NAME, "Folder 0");
        folder_count++;
        
        permissionService.setPermission(f0, "cmis", PermissionService.READ, true);


        NodeRef f1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 1", namespaceService), ContentModel.TYPE_FOLDER)
                .getChildRef();
        nodeService.setProperty(f1, ContentModel.PROP_NAME, "Folder 1");
        folder_count++;

        NodeRef f2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 2", namespaceService), ContentModel.TYPE_FOLDER)
                .getChildRef();
        nodeService.setProperty(f2, ContentModel.PROP_NAME, "Folder 2");
        folder_count++;

        NodeRef f3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 3", namespaceService), ContentModel.TYPE_FOLDER)
                .getChildRef();
        nodeService.setProperty(f3, ContentModel.PROP_NAME, "Folder 3");
        folder_count++;

        NodeRef f4 = nodeService.createNode(f0, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 4", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f4, ContentModel.PROP_NAME, "Folder 4");
        folder_count++;

        NodeRef f5 = nodeService.createNode(f0, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 5", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f5, ContentModel.PROP_NAME, "Folder 5");
        folder_count++;

        NodeRef f6 = nodeService.createNode(f5, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 6", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f6, ContentModel.PROP_NAME, "Folder 6");
        folder_count++;

        NodeRef f7 = nodeService.createNode(f6, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 7", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f7, ContentModel.PROP_NAME, "Folder 7");
        folder_count++;

        NodeRef f8 = nodeService.createNode(f7, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 8", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f8, ContentModel.PROP_NAME, "Folder 8");
        folder_count++;

        NodeRef f9 = nodeService.createNode(f8, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 9", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f9, ContentModel.PROP_NAME, "Folder 9");
        folder_count++;

        Map<QName, Serializable> properties0 = new HashMap<QName, Serializable>();
        MLText desc0 = new MLText();
        desc0.addValue(Locale.ENGLISH, "Alfresco tutorial");
        desc0.addValue(Locale.US, "Alfresco tutorial");
        properties0.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties0.put(ContentModel.PROP_DESCRIPTION, desc0);
        properties0.put(ContentModel.PROP_TITLE, desc0);
        properties0.put(ContentModel.PROP_NAME, "Alfresco Tutorial");
        properties0.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c0 = nodeService
                .createNode(f0, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Alfresco Tutorial", namespaceService), ContentModel.TYPE_CONTENT, properties0).getChildRef();
        ContentWriter writer0 = contentService.getWriter(c0, ContentModel.PROP_CONTENT, true);
        writer0.setEncoding("UTF-8");
        writer0.putContent("The quick brown fox jumped over the lazy dog and ate the Alfresco Tutorial, in pdf format, along with the following stop words;  a an and are"
                + " as at be but by for if in into is it no not of on or such that the their then there these they this to was will with: "
                + " and random charcters \u00E0\u00EA\u00EE\u00F0\u00F1\u00F6\u00FB\u00FF");
        nodeService.addAspect(c0, ContentModel.ASPECT_TITLED, null);
        nodeService.addAspect(c0, ContentModel.ASPECT_OWNABLE, null);
        nodeService.setProperty(c0, ContentModel.PROP_OWNER, "andy");
        file_count++;

        Map<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        MLText desc1 = new MLText();
        desc1.addValue(Locale.ENGLISH, "One");
        desc1.addValue(Locale.US, "One");
        properties1.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties1.put(ContentModel.PROP_DESCRIPTION, desc1);
        properties1.put(ContentModel.PROP_TITLE, desc1);
        properties1.put(ContentModel.PROP_NAME, "AA");
        properties1.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c1 = nodeService.createNode(f1, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "One", namespaceService), ContentModel.TYPE_CONTENT, properties1)
                .getChildRef();
        ContentWriter writer1 = contentService.getWriter(c1, ContentModel.PROP_CONTENT, true);
        writer1.setEncoding("UTF-8");
        writer1.putContent("One Zebra Apple");
        nodeService.addAspect(c1, ContentModel.ASPECT_TITLED, null);
        file_count++;

        Map<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        MLText desc2 = new MLText();
        desc2.addValue(Locale.ENGLISH, "Two");
        desc2.addValue(Locale.US, "Two");
        properties2.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties2.put(ContentModel.PROP_DESCRIPTION, desc2);
        properties2.put(ContentModel.PROP_TITLE, desc2);
        properties2.put(ContentModel.PROP_NAME, "BB");
        properties2.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c2 = nodeService.createNode(f2, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Two", namespaceService), ContentModel.TYPE_CONTENT, properties2)
                .getChildRef();
        ContentWriter writer2 = contentService.getWriter(c2, ContentModel.PROP_CONTENT, true);
        writer2.setEncoding("UTF-8");
        writer2.putContent("Two Zebra Banana");
        nodeService.addAspect(c2, ContentModel.ASPECT_TITLED, null);
        file_count++;

        Map<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        MLText desc3 = new MLText();
        desc3.addValue(Locale.ENGLISH, "Three");
        desc3.addValue(Locale.US, "Three");
        properties3.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties3.put(ContentModel.PROP_DESCRIPTION, desc3);
        properties3.put(ContentModel.PROP_TITLE, desc3);
        properties3.put(ContentModel.PROP_NAME, "CC");
        properties3.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c3 = nodeService.createNode(f3, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Three", namespaceService), ContentModel.TYPE_CONTENT, properties3)
                .getChildRef();
        ContentWriter writer3 = contentService.getWriter(c3, ContentModel.PROP_CONTENT, true);
        writer3.setEncoding("UTF-8");
        writer3.putContent("Three Zebra Clementine");
        nodeService.addAspect(c3, ContentModel.ASPECT_TITLED, null);
        file_count++;

        Map<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        MLText desc4 = new MLText();
        desc4.addValue(Locale.ENGLISH, "Four");
        desc4.addValue(Locale.US, "Four");
        properties4.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties4.put(ContentModel.PROP_DESCRIPTION, desc4);
        properties4.put(ContentModel.PROP_TITLE, desc4);
        properties4.put(ContentModel.PROP_NAME, "DD");
        properties4.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c4 = nodeService.createNode(f4, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Four", namespaceService), ContentModel.TYPE_CONTENT, properties4)
                .getChildRef();
        ContentWriter writer4 = contentService.getWriter(c4, ContentModel.PROP_CONTENT, true);
        writer4.setEncoding("UTF-8");
        writer4.putContent("Four zebra durian");
        nodeService.addAspect(c4, ContentModel.ASPECT_TITLED, null);
        file_count++;

        Map<QName, Serializable> properties5 = new HashMap<QName, Serializable>();
        MLText desc5 = new MLText();
        desc5.addValue(Locale.ENGLISH, "Five");
        desc5.addValue(Locale.US, "Five");
        properties5.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties5.put(ContentModel.PROP_DESCRIPTION, desc5);
        properties5.put(ContentModel.PROP_TITLE, desc5);
        properties5.put(ContentModel.PROP_NAME, "EE");
        properties5.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c5 = nodeService.createNode(f5, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Five", namespaceService), ContentModel.TYPE_CONTENT, properties5)
                .getChildRef();
        ContentWriter writer5 = contentService.getWriter(c5, ContentModel.PROP_CONTENT, true);
        writer5.setEncoding("UTF-8");
        writer5.putContent("Five zebra Ebury");
        nodeService.addAspect(c5, ContentModel.ASPECT_TITLED, null);
        file_count++;

        Map<QName, Serializable> properties6 = new HashMap<QName, Serializable>();
        MLText desc6 = new MLText();
        desc6.addValue(Locale.ENGLISH, "Six");
        desc6.addValue(Locale.US, "Six");
        properties6.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties6.put(ContentModel.PROP_DESCRIPTION, desc6);
        properties6.put(ContentModel.PROP_TITLE, desc6);
        properties6.put(ContentModel.PROP_NAME, "FF");
        properties6.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c6 = nodeService.createNode(f6, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Six", namespaceService), ContentModel.TYPE_CONTENT, properties6)
                .getChildRef();
        ContentWriter writer6 = contentService.getWriter(c6, ContentModel.PROP_CONTENT, true);
        writer6.setEncoding("UTF-8");
        writer6.putContent("Six zebra fig");
        nodeService.addAspect(c6, ContentModel.ASPECT_TITLED, null);
        file_count++;

        Map<QName, Serializable> properties7 = new HashMap<QName, Serializable>();
        MLText desc7 = new MLText();
        desc7.addValue(Locale.ENGLISH, "Seven");
        desc7.addValue(Locale.US, "Seven");
        properties7.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties7.put(ContentModel.PROP_DESCRIPTION, desc7);
        properties7.put(ContentModel.PROP_TITLE, desc7);
        properties7.put(ContentModel.PROP_NAME, "GG");
        properties7.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c7 = nodeService.createNode(f7, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Seven", namespaceService), ContentModel.TYPE_CONTENT, properties7)
                .getChildRef();
        ContentWriter writer7 = contentService.getWriter(c7, ContentModel.PROP_CONTENT, true);
        writer7.setEncoding("UTF-8");
        writer7.putContent("Seven zebra grapefruit");
        nodeService.addAspect(c7, ContentModel.ASPECT_TITLED, null);
        file_count++;

        Map<QName, Serializable> properties8 = new HashMap<QName, Serializable>();
        MLText desc8 = new MLText();
        desc8.addValue(Locale.ENGLISH, "Eight");
        desc8.addValue(Locale.US, "Eight");
        properties8.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties8.put(ContentModel.PROP_DESCRIPTION, desc8);
        properties8.put(ContentModel.PROP_TITLE, desc8);
        properties8.put(ContentModel.PROP_NAME, "HH");
        properties8.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c8 = nodeService.createNode(f8, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Eight", namespaceService), ContentModel.TYPE_CONTENT, properties8)
                .getChildRef();
        ContentWriter writer8 = contentService.getWriter(c8, ContentModel.PROP_CONTENT, true);
        writer8.setEncoding("UTF-8");
        writer8.putContent("Eight zebra jackfruit");
        nodeService.addAspect(c8, ContentModel.ASPECT_TITLED, null);
        file_count++;

        Map<QName, Serializable> properties9 = new HashMap<QName, Serializable>();
        MLText desc9 = new MLText();
        desc9.addValue(Locale.ENGLISH, "Nine");
        desc9.addValue(Locale.US, "Nine");
        properties9.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties9.put(ContentModel.PROP_DESCRIPTION, desc9);
        properties9.put(ContentModel.PROP_TITLE, desc9);
        properties9.put(ContentModel.PROP_NAME, "aa");
        properties9.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c9 = nodeService.createNode(f9, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Nine", namespaceService), ContentModel.TYPE_CONTENT, properties9)
                .getChildRef();
        ContentWriter writer9 = contentService.getWriter(c9, ContentModel.PROP_CONTENT, true);
        writer9.setEncoding("UTF-8");
        writer9.putContent("Nine zebra kiwi");
        nodeService.addAspect(c9, ContentModel.ASPECT_TITLED, null);
        file_count++;
    }

    private <T> T testQuery(String query, int size, boolean dump, String returnPropertyName, T returnType, boolean shouldThrow)
    {
        return testQuery(query, size, dump, returnPropertyName, returnType, shouldThrow, CMISQueryMode.CMS_STRICT);
    }

    @SuppressWarnings("unchecked")
    private <T> T testQuery(String query, int size, boolean dump, String returnPropertyName, T returnType, boolean shouldThrow, CMISQueryMode mode)
    {
        CMISResultSet rs = null;
        try
        {
            T returnValue = null;
            CMISQueryOptions options = new CMISQueryOptions(query, rootNodeRef.getStoreRef());
            options.setQueryMode(mode);
            rs = cmisQueryService.query(options);

            for (CMISResultSetRow row : rs)
            {
                if (row.getIndex() == 0)
                {
                    Serializable sValue = row.getValue(returnPropertyName);
                    returnValue = (T) DefaultTypeConverter.INSTANCE.convert(returnType.getClass(), sValue);
                    if (dump)
                    {
                        System.out.println(cmisService.getProperties(row.getNodeRef(rs.getMetaData().getSelectorNames()[0])));
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
        catch(FTSQueryException e)
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
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds =  'test'", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds <> 'test'", 10, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds <  'test'", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds <= 'test'", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds >  'test'", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds >= 'test'", 0, false, "AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds IN     ('test')", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds NOT IN ('test')", 10, false, "AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds     LIKE 'test'", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds NOT LIKE 'test'", 0, false, "AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds IS NOT NULL", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE AllowedChildObjectTypeIds IS     NULL", 10, false, "AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' =  ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' <> ANY AllowedChildObjectTypeIds", 10, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' <  ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' <= ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' >  ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE 'test' >= ANY AllowedChildObjectTypeIds", 0, false, "AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE ANY AllowedChildObjectTypeIds IN     ('test')", 0, false, "AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT AllowedChildObjectTypeIds FROM Folder WHERE ANY AllowedChildObjectTypeIds NOT IN ('test')", 10, false, "AllowedChildObjectTypeIds", new String(), true);
    }

    public void test_PARENT()
    {
        testQuery("SELECT cmis:ParentId FROM cmis:Folder WHERE cmis:ParentId =  '" + rootNodeRef.toString() + "'", 4, false, "cmis:ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId <> '" + rootNodeRef.toString() + "'", 6, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId <  '" + rootNodeRef.toString() + "'", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId <= '" + rootNodeRef.toString() + "'", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId >  '" + rootNodeRef.toString() + "'", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId >= '" + rootNodeRef.toString() + "'", 0, false, "ParentId", new String(), true);

        testQuery("SELECT ParentId FROM Folder WHERE ParentId IN     ('" + rootNodeRef.toString() + "')", 4, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId NOT IN ('" + rootNodeRef.toString() + "')", 6, false, "ParentId", new String(), false);

        testQuery("SELECT ParentId FROM Folder WHERE ParentId     LIKE '" + rootNodeRef.toString() + "'", 4, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId NOT LIKE '" + rootNodeRef.toString() + "'", 6, false, "ParentId", new String(), false);

        testQuery("SELECT ParentId FROM Folder WHERE ParentId IS NOT NULL", 10, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ParentId IS     NULL", 0, false, "ParentId", new String(), false);

        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNodeRef.toString() + "' =  ANY ParentId", 4, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNodeRef.toString() + "' <> ANY ParentId", 6, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNodeRef.toString() + "' <  ANY ParentId", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNodeRef.toString() + "' <= ANY ParentId", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNodeRef.toString() + "' >  ANY ParentId", 0, false, "ParentId", new String(), true);
        testQuery("SELECT ParentId FROM Folder WHERE '" + rootNodeRef.toString() + "' >= ANY ParentId", 0, false, "ParentId", new String(), true);

        testQuery("SELECT ParentId FROM Folder WHERE ANY ParentId IN     ('" + rootNodeRef.toString() + "')", 4, false, "ParentId", new String(), false);
        testQuery("SELECT ParentId FROM Folder WHERE ANY ParentId NOT IN ('" + rootNodeRef.toString() + "')", 6, false, "ParentId", new String(), false);
    }

    public void test_CONTENT_STREAM_FILENAME()
    {
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'Alfresco Tutorial'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'AA'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'BB'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'CC'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'DD'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'EE'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'FF'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'GG'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'HH'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'aa'", 1, false, "ContentStreamFilename", new String(), false);
        
        
        
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename =  'Alfresco Tutorial'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename <> 'Alfresco Tutorial'", 9, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename <  'Alfresco Tutorial'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename <= 'Alfresco Tutorial'", 2, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename >  'Alfresco Tutorial'", 8, true, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename >= 'Alfresco Tutorial'", 9, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename IN     ('Alfresco Tutorial')", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename NOT IN ('Alfresco Tutorial')", 9, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename     LIKE 'Alfresco Tutorial'", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename NOT LIKE 'Alfresco Tutorial'", 9, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename IS NOT NULL", 10, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ContentStreamFilename IS     NULL", 0, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'Alfresco Tutorial' =  ANY ContentStreamFilename", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'Alfresco Tutorial' <> ANY ContentStreamFilename", 9, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'Alfresco Tutorial' <  ANY ContentStreamFilename", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'Alfresco Tutorial' <= ANY ContentStreamFilename", 2, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'Alfresco Tutorial' >  ANY ContentStreamFilename", 8, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE 'Alfresco Tutorial' >= ANY ContentStreamFilename", 9, false, "ContentStreamFilename", new String(), false);

        testQuery("SELECT ContentStreamFilename FROM Document WHERE ANY ContentStreamFilename IN     ('Alfresco Tutorial')", 1, false, "ContentStreamFilename", new String(), false);
        testQuery("SELECT ContentStreamFilename FROM Document WHERE ANY ContentStreamFilename NOT IN ('Alfresco Tutorial')", 9, false, "ContentStreamFilename", new String(), false);
    }

    public void test_CONTENT_STREAM_MIME_TYPE()
    {
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType =  'text/plain'", 10, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType <> 'text/plain'", 0, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType <  'text/plain'", 0, true, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType <= 'text/plain'", 10, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType >  'text/plain'", 0, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType >= 'text/plain'", 10, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType IN     ('text/plain')", 10, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType NOT IN ('text/plain')", 0, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType     LIKE 'text/plain'", 10, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType NOT LIKE 'text/plain'", 0, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType IS NOT NULL", 10, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ContentStreamMimeType IS     NULL", 0, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' =  ANY ContentStreamMimeType", 10, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' <> ANY ContentStreamMimeType", 0, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' <  ANY ContentStreamMimeType", 0, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' <= ANY ContentStreamMimeType", 10, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' >  ANY ContentStreamMimeType", 0, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE 'text/plain' >= ANY ContentStreamMimeType", 10, false, "ContentStreamMimeType", new String(), false);

        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ANY ContentStreamMimeType IN     ('text/plain')", 10, false, "ContentStreamMimeType", new String(), false);
        testQuery("SELECT ContentStreamMimeType FROM Document WHERE ANY ContentStreamMimeType NOT IN ('text/plain')", 0, false, "ContentStreamMimeType", new String(), false);
    }

    public void test_CONTENT_STREAM_LENGTH()
    {
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength =  750", 0, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength <> 750", 10, true, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength <  750", 10, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength <= 750", 10, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength >  750", 0, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength >= 750", 0, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength IN     (750)", 0, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength NOT IN (750)", 10, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength     LIKE '750'", 0, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength NOT LIKE '750'", 10, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength IS NOT NULL", 10, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ContentStreamLength IS     NULL", 0, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 =  ANY ContentStreamLength", 0, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 <> ANY ContentStreamLength", 10, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 <  ANY ContentStreamLength", 10, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 <= ANY ContentStreamLength", 10, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 >  ANY ContentStreamLength", 0, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE 750 >= ANY ContentStreamLength", 0, false, "ContentStreamLength", new String(), false);

        testQuery("SELECT ContentStreamLength FROM Document WHERE ANY ContentStreamLength IN     (750)", 0, false, "ContentStreamLength", new String(), false);
        testQuery("SELECT ContentStreamLength FROM Document WHERE ANY ContentStreamLength NOT IN (750)", 10, false, "ContentStreamLength", new String(), false);
    }

    public void test_CONTENT_STREAM_ALLOWED()
    {
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed =  'ALLOWED'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed <> 'ALLOWED'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed <  'ALLOWED'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed <= 'ALLOWED'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed >  'ALLOWED'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed >= 'ALLOWED'", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed IN     ('ALLOWED')", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed NOT IN ('ALLOWED')", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed     LIKE 'ALLOWED'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed NOT LIKE 'ALLOWED'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed IS NOT NULL", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ContentStreamAllowed IS     NULL", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' =  ANY ContentStreamAllowed", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' <> ANY ContentStreamAllowed", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' <  ANY ContentStreamAllowed", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' <= ANY ContentStreamAllowed", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' >  ANY ContentStreamAllowed", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE 'ALLOWED' >= ANY ContentStreamAllowed", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ANY ContentStreamAllowed IN     ('ALLOWED')", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ContentStreamAllowed FROM Document WHERE ANY ContentStreamAllowed NOT IN ('ALLOWED')", 0, false, "ObjectId", new String(), true);
    }

    public void test_CHECKIN_COMMENT()
    {
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment =  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment <> 'admin'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment <  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment <= 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment >  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment >= 'admin'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment NOT IN ('admin')", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment     LIKE 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment NOT LIKE 'admin'", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE CheckinComment IS     NULL", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' =  ANY CheckinComment", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' <> ANY CheckinComment", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' <  ANY CheckinComment", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' <= ANY CheckinComment", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' >  ANY CheckinComment", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE 'admin' >= ANY CheckinComment", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT CheckinComment FROM Document WHERE ANY CheckinComment IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT CheckinComment FROM Document WHERE ANY CheckinComment NOT IN ('admin')", 10, false, "ObjectId", new String(), true);
    }

    public void test_VERSION_SERIES_CHECKED_OUT_ID()
    {
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId =  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId <> 'admin'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId <  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId <= 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId >  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId >= 'admin'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId NOT IN ('admin')", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId     LIKE 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId NOT LIKE 'admin'", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE VersionSeriesCheckedOutId IS     NULL", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' =  ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' <> ANY VersionSeriesCheckedOutId", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' <  ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' <= ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' >  ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE 'admin' >= ANY VersionSeriesCheckedOutId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE ANY VersionSeriesCheckedOutId IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutId FROM Document WHERE ANY VersionSeriesCheckedOutId NOT IN ('admin')", 10, false, "ObjectId", new String(), true);
    }

    public void test_VERSION_SERIES_CHECKED_OUT_BY()
    {
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy =  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy <> 'admin'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy <  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy <= 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy >  'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy >= 'admin'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy NOT IN ('admin')", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy     LIKE 'admin'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy NOT LIKE 'admin'", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE VersionSeriesCheckedOutBy IS     NULL", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' =  ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' <> ANY VersionSeriesCheckedOutBy", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' <  ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' <= ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' >  ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE 'admin' >= ANY VersionSeriesCheckedOutBy", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE ANY VersionSeriesCheckedOutBy IN     ('admin')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesCheckedOutBy FROM Document WHERE ANY VersionSeriesCheckedOutBy NOT IN ('admin')", 10, false, "ObjectId", new String(), true);
    }

    public void test_VERSION_SERIES_IS_CHECKED_OUT()
    {
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut =  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut <> 'TRUE'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut <  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut <= 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut >  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut >= 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut NOT IN ('TRUE')", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut     LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut NOT LIKE 'TRUE'", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE IsVeriesSeriesCheckedOut IS     NULL", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' =  ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' <> ANY IsVeriesSeriesCheckedOut", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' <  ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' <= ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' >  ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE 'TRUE' >= ANY IsVeriesSeriesCheckedOut", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE ANY IsVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsVeriesSeriesCheckedOut FROM Document WHERE ANY IsVeriesSeriesCheckedOut NOT IN ('TRUE')", 10, false, "ObjectId", new String(), true);
    }

    public void test_VERSION_SERIES_ID()
    {
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId =  'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId <> 'company'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId <  'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId <= 'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId >  'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId >= 'company'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId IN     ('company')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId NOT IN ('company')", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId     LIKE 'company'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId NOT LIKE 'company'", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE VersionSeriesId IS     NULL", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' =  ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' <> ANY VersionSeriesId", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' <  ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' <= ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' >  ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE 'company' >= ANY VersionSeriesId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT VersionSeriesId FROM Document WHERE ANY VersionSeriesId IN     ('company')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT VersionSeriesId FROM Document WHERE ANY VersionSeriesId NOT IN ('company')", 10, false, "ObjectId", new String(), true);
    }

    public void test_VERSION_LABEL()
    {
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel =  'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel <> 'company'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel <  'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel <= 'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel >  'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel >= 'company'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel IN     ('company')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel NOT IN ('company')", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel     LIKE 'company'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel NOT LIKE 'company'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel IS NOT NULL", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE VersionLabel IS     NULL", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE 'company' =  ANY VersionLabel", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' <> ANY VersionLabel", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' <  ANY VersionLabel", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' <= ANY VersionLabel", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' >  ANY VersionLabel", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE 'company' >= ANY VersionLabel", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT VersionLabel FROM Document WHERE ANY VersionLabel IN     ('company')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT VersionLabel FROM Document WHERE ANY VersionLabel NOT IN ('company')", 10, false, "ObjectId", new String(), false);
    }

    public void test_IS_LATEST_MAJOR_VERSION()
    {
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion =  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion <> 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion <  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion <= 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion >  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion >= 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion NOT IN ('TRUE')", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion     LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion NOT LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE IsLatestMajorVersion IS     NULL", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' =  ANY IsLatestMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' <> ANY IsLatestMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' <  ANY IsLatestMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' <= ANY IsLatestMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' >  ANY IsLatestMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE 'TRUE' >= ANY IsLatestMajorVersion", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE ANY IsLatestMajorVersion IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestMajorVersion FROM Document WHERE ANY IsLatestMajorVersion NOT IN ('TRUE')", 0, false, "ObjectId", new String(), true);
    }

    public void test_IS_MAJOR_VERSION()
    {
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion =  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion <> 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion <  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion <= 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion >  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion >= 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion NOT IN ('TRUE')", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion     LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion NOT LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE IsMajorVersion IS     NULL", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' =  ANY IsMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' <> ANY IsMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' <  ANY IsMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' <= ANY IsMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' >  ANY IsMajorVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE 'TRUE' >= ANY IsMajorVersion", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsMajorVersion FROM Document WHERE ANY IsMajorVersion IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsMajorVersion FROM Document WHERE ANY IsMajorVersion NOT IN ('TRUE')", 0, false, "ObjectId", new String(), true);
    }

    public void test_IS_LATEST_VERSION()
    {
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion =  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion <> 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion <  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion <= 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion >  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion >= 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion NOT IN ('TRUE')", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion     LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion NOT LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE IsLatestVersion IS     NULL", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' =  ANY IsLatestVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' <> ANY IsLatestVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' <  ANY IsLatestVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' <= ANY IsLatestVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' >  ANY IsLatestVersion", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE 'TRUE' >= ANY IsLatestVersion", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsLatestVersion FROM Document WHERE ANY IsLatestVersion IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsLatestVersion FROM Document WHERE ANY IsLatestVersion NOT IN ('TRUE')", 0, false, "ObjectId", new String(), true);
    }

    public void test_IS_IMMUTABLE()
    {
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable =  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable <> 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable <  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable <= 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable >  'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable >= 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable NOT IN ('TRUE')", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable     LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable NOT LIKE 'TRUE'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE IsImmutable IS     NULL", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' =  ANY IsImmutable", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' <> ANY IsImmutable", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' <  ANY IsImmutable", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' <= ANY IsImmutable", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' >  ANY IsImmutable", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE 'TRUE' >= ANY IsImmutable", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT IsImmutable FROM Document WHERE ANY IsImmutable IN     ('TRUE')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT IsImmutable FROM Document WHERE ANY IsImmutable NOT IN ('TRUE')", 0, false, "ObjectId", new String(), true);
    }

    public void test_folder_NAME()
    {
        testQuery("SELECT Name FROM Folder WHERE Name =  'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name <> 'Folder 1'", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name <  'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name <= 'Folder 1'", 2, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name >  'Folder 1'", 8, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name >= 'Folder 1'", 9, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE Name IN     ('Folder 1')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name NOT IN ('Folder 1')", 9, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE Name     LIKE 'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name NOT LIKE 'Folder 1'", 9, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE Name IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE Name IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE 'Folder 1' =  ANY Name", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'Folder 1' <> ANY Name", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'Folder 1' <  ANY Name", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'Folder 1' <= ANY Name", 2, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'Folder 1' >  ANY Name", 8, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE 'Folder 1' >= ANY Name", 9, false, "ObjectId", new String(), false);

        testQuery("SELECT Name FROM Folder WHERE ANY Name IN     ('Folder 1')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name FROM Folder WHERE ANY Name NOT IN ('Folder 1')", 9, false, "ObjectId", new String(), false);
    }

    public void test_document_Name()
    {
        testQuery("SELECT Name FROM Document WHERE Name =  'Alfresco Tutorial'", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name <> 'Alfresco Tutorial'", 9, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name <  'Alfresco Tutorial'", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name <= 'Alfresco Tutorial'", 2, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name >  'Alfresco Tutorial'", 8, true, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name >= 'Alfresco Tutorial'", 9, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE Name IN     ('Alfresco Tutorial')", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name NOT IN ('Alfresco Tutorial')", 9, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE Name     LIKE 'Alfresco Tutorial'", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name NOT LIKE 'Alfresco Tutorial'", 9, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE Name IS NOT NULL", 10, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE Name IS     NULL", 0, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE 'Alfresco Tutorial' =  ANY Name", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'Alfresco Tutorial' <> ANY Name", 9, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'Alfresco Tutorial' <  ANY Name", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'Alfresco Tutorial' <= ANY Name", 2, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'Alfresco Tutorial' >  ANY Name", 8, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE 'Alfresco Tutorial' >= ANY Name", 9, false, "Name", new String(), false);

        testQuery("SELECT Name FROM Document WHERE ANY Name IN     ('Alfresco Tutorial')", 1, false, "Name", new String(), false);
        testQuery("SELECT Name FROM Document WHERE ANY Name NOT IN ('Alfresco Tutorial')", 9, false, "Name", new String(), false);
    }

    public void test_CHANGE_TOKEN()
    {
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken =  'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken <> 'test'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken <  'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken <= 'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken >  'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken >= 'test'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken IN     ('test')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken NOT IN ('test')", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken     LIKE 'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken NOT LIKE 'test'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE ChangeToken IS     NULL", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' =  ANY ChangeToken", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' <> ANY ChangeToken", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' <  ANY ChangeToken", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' <= ANY ChangeToken", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' >  ANY ChangeToken", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE 'test' >= ANY ChangeToken", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ChangeToken FROM Folder WHERE ANY ChangeToken IN     ('test')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ChangeToken FROM Folder WHERE ANY ChangeToken NOT IN ('test')", 10, false, "ObjectId", new String(), true);
    }

    public void test_LAST_MODIFICATION_DATE()
    {
        // By default we are only working to the day

        Calendar today = Calendar.getInstance();

        if ((today.get(Calendar.HOUR_OF_DAY) == 0) || (today.get(Calendar.HOUR_OF_DAY) == 23))
        {
            return;
        }

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

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate =  '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <> '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <= '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >= '" + sDate + "'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IN     ('" + sDate + "')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT IN ('" + sDate + "')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate     LIKE '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' =  ANY LastModificationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <> ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <= ANY LastModificationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >= ANY LastModificationDate", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate IN     ('" + sDate + "')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate NOT IN ('" + sDate + "')", 0, false, "ObjectId", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate =  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <> '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <= '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >  '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >= '" + sDate + "'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT IN ('" + sDate + "')", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate     LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT LIKE '" + sDate + "'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' =  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <> ANY LastModificationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <= ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >  ANY LastModificationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >= ANY LastModificationDate", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate NOT IN ('" + sDate + "')", 10, false, "ObjectId", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate =  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <> '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <  '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate <= '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate >= '" + sDate + "'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT IN ('" + sDate + "')", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate     LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate NOT LIKE '" + sDate + "'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE LastModificationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' =  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <> ANY LastModificationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <  ANY LastModificationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' <= ANY LastModificationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >  ANY LastModificationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE '" + sDate + "' >= ANY LastModificationDate", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModificationDate FROM Document WHERE ANY LastModificationDate NOT IN ('" + sDate + "')", 10, false, "ObjectId", new String(), false);

    }

    public void test_LAST_MODIFIED_BY()
    {
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy =  'System'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy <> 'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy <  'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy <= 'System'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy >  'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy >= 'System'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy IN     ('System')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy NOT IN ('System')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy     LIKE 'System'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy NOT LIKE 'System'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE LastModifiedBy IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' =  ANY LastModifiedBy", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' <> ANY LastModifiedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' <  ANY LastModifiedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' <= ANY LastModifiedBy", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' >  ANY LastModifiedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE 'System' >= ANY LastModifiedBy", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT LastModifiedBy FROM Document WHERE ANY LastModifiedBy IN     ('System')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT LastModifiedBy FROM Document WHERE ANY LastModifiedBy NOT IN ('System')", 0, false, "ObjectId", new String(), false);

    }

    public void test_CREATION_DATE()
    {
        // By default we are only working to the day

        Calendar today = Calendar.getInstance();

        if ((today.get(Calendar.HOUR_OF_DAY) == 0) || (today.get(Calendar.HOUR_OF_DAY) == 23))
        {
            return;
        }

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

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate =  '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <> '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <= '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >= '" + sDate + "'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IN     ('" + sDate + "')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT IN ('" + sDate + "')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate     LIKE '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' =  ANY CreationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <> ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <= ANY CreationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >= ANY CreationDate", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate IN     ('" + sDate + "')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate NOT IN ('" + sDate + "')", 0, false, "ObjectId", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate =  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <> '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <= '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >  '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >= '" + sDate + "'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT IN ('" + sDate + "')", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate     LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT LIKE '" + sDate + "'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' =  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <> ANY CreationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <= ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >  ANY CreationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >= ANY CreationDate", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate NOT IN ('" + sDate + "')", 10, false, "ObjectId", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate =  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <> '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <  '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate <= '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >  '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate >= '" + sDate + "'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT IN ('" + sDate + "')", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE CreationDate     LIKE '" + sDate + "'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate NOT LIKE '" + sDate + "'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE CreationDate IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' =  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <> ANY CreationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <  ANY CreationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' <= ANY CreationDate", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >  ANY CreationDate", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE '" + sDate + "' >= ANY CreationDate", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate IN     ('" + sDate + "')", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreationDate FROM Document WHERE ANY CreationDate NOT IN ('" + sDate + "')", 10, false, "ObjectId", new String(), false);

    }

    public void test_CREATED_BY()
    {
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy =  'System'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy <> 'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy <  'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy <= 'System'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy >  'System'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy >= 'System'", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy IN     ('System')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy NOT IN ('System')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy     LIKE 'System'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy NOT LIKE 'System'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE CreatedBy IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE 'System' =  ANY CreatedBy", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' <> ANY CreatedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' <  ANY CreatedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' <= ANY CreatedBy", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' >  ANY CreatedBy", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE 'System' >= ANY CreatedBy", 10, false, "ObjectId", new String(), false);

        testQuery("SELECT CreatedBy FROM Document WHERE ANY CreatedBy IN     ('System')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT CreatedBy FROM Document WHERE ANY CreatedBy NOT IN ('System')", 0, false, "ObjectId", new String(), false);

    }

    public void test_OBJECT_TYPE_ID()
    {
        // DOC

        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId =  'Document'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId <> 'Document'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId <  'Document'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId <= 'Document'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId >  'Document'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId >= 'Document'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId IN     ('Document')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId NOT IN ('Document')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId     LIKE 'Document'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId NOT LIKE 'Document'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ObjectTypeId IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' =  ANY ObjectTypeId", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' <> ANY ObjectTypeId", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' <  ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' <= ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' >  ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Document WHERE 'Document' >= ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectTypeId FROM Document WHERE ANY ObjectTypeId IN     ('Document')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Document WHERE ANY ObjectTypeId NOT IN ('Document')", 0, false, "ObjectId", new String(), false);

        // FOLDER

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId =  'Folder'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId <> 'Folder'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId <  'Folder'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId <= 'Folder'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId >  'Folder'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId >= 'Folder'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId IN     ('Folder')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId NOT IN ('Folder')", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId     LIKE 'Folder'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId NOT LIKE 'Folder'", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ObjectTypeId IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' =  ANY ObjectTypeId", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' <> ANY ObjectTypeId", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' <  ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' <= ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' >  ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE 'Folder' >= ANY ObjectTypeId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectTypeId FROM Folder WHERE ANY ObjectTypeId IN     ('Folder')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectTypeId FROM Folder WHERE ANY ObjectTypeId NOT IN ('Folder')", 0, false, "ObjectId", new String(), false);

        // RELATIONSHIP

        testQuery("SELECT ObjectTypeId FROM Relationship WHERE ObjectTypeId =  ''", 1, false, "ObjectId", new String(), true);

    }

    public void test_URI()
    {
        testQuery("SELECT Uri FROM Folder WHERE Uri =  'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE Uri <> 'test'", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE Uri <  'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE Uri <= 'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE Uri >  'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE Uri >= 'test'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT Uri FROM Folder WHERE Uri IN     ('test')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE Uri NOT IN ('test')", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT Uri FROM Folder WHERE Uri     LIKE 'test'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE Uri NOT LIKE 'test'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT Uri FROM Folder WHERE Uri IS NOT NULL", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE Uri IS     NULL", 10, false, "ObjectId", new String(), true);

        testQuery("SELECT Uri FROM Folder WHERE 'test' =  ANY Uri", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE 'test' <> ANY Uri", 10, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE 'test' <  ANY Uri", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE 'test' <= ANY Uri", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE 'test' >  ANY Uri", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE 'test' >= ANY Uri", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT Uri FROM Folder WHERE ANY Uri IN     ('test')", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT Uri FROM Folder WHERE ANY Uri NOT IN ('test')", 10, false, "ObjectId", new String(), true);
    }

    public void test_ObjectId()
    {
        String companyHomeId = testQuery("SELECT ObjectId FROM Folder WHERE Name = 'Folder 0'", 1, false, "ObjectId", new String(), false);

        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        assertEquals(companyHomeId, id);

        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId =  '" + companyHomeId + "'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId <> '" + companyHomeId + "'", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId <  '" + companyHomeId + "'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId <= '" + companyHomeId + "'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId >  '" + companyHomeId + "'", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId >= '" + companyHomeId + "'", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId IN     ('" + companyHomeId + "')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId NOT IN ('" + companyHomeId + "')", 9, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId     LIKE '" + companyHomeId + "'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId NOT LIKE '" + companyHomeId + "'", 9, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectId FROM Folder WHERE IN_FOLDER('" + companyHomeId + "')", 2, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE IN_TREE  ('" + companyHomeId + "')", 6, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ObjectId IS     NULL", 0, false, "ObjectId", new String(), false);

        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' =  ANY ObjectId", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' <> ANY ObjectId", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' <  ANY ObjectId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' <= ANY ObjectId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' >  ANY ObjectId", 0, false, "ObjectId", new String(), true);
        testQuery("SELECT ObjectId FROM Folder WHERE '" + companyHomeId + "' >= ANY ObjectId", 0, false, "ObjectId", new String(), true);

        testQuery("SELECT ObjectId FROM Folder WHERE ANY ObjectId IN     ('" + companyHomeId + "')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT ObjectId FROM Folder WHERE ANY ObjectId NOT IN ('" + companyHomeId + "')", 9, false, "ObjectId", new String(), false);
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
    
    public void testUpperAndLower()
    {
        testQuery("SELECT * FROM Folder WHERE Name = 'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name = 'FOLDER 1'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name = 'folder 1'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Upper(Name) = 'FOLDER 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Lower(Name) = 'folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Upper(Name) = 'folder 1'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Lower(Name) = 'FOLDER 1'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Upper(Name) = 'Folder 1'", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Lower(Name) = 'Folder 1'", 0, false, "ObjectId", new String(), false);
        
        testQuery("SELECT * FROM Folder WHERE Upper(Name) <> 'FOLDER 1'", 9, false, "ObjectId", new String(), false);
        
        testQuery("SELECT * FROM Folder WHERE Upper(Name) <= 'FOLDER 1'", 2, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Upper(Name) < 'FOLDER 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Upper(Name) >= 'FOLDER 1'", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Upper(Name) > 'FOLDER 1'", 8, false, "ObjectId", new String(), false);
    }

    public void testAllSimpleTextPredicates()
    {
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name = 'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND NOT Name = 'Folder 1'", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND 'Folder 1' = ANY Name", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND NOT Name <> 'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name <> 'Folder 1'", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name < 'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name <= 'Folder 1'", 2, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name > 'Folder 1'", 8, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name >= 'Folder 1'", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name IN ('Folder 1', '1')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name NOT IN ('Folder 1', 'Folder 9')", 8, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND ANY Name IN ('Folder 1', 'Folder 9')", 2, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND ANY Name NOT IN ('2', '3')", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name LIKE 'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name LIKE 'Fol%'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name LIKE 'F_l_e_ 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name NOT LIKE 'F_l_e_ 1'", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name LIKE 'F_l_e_ %'", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name NOT LIKE 'F_l_e_ %'", 0, false, "ObjectId", new String(), false);
        // TODO: Fix below which fail??
        //testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name LIKE 'F_l_e_ _'", 10, false, "ObjectId", new String(), false);
        //testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name NOT LIKE 'F_l_e_ _'", 0, false, "ObjectId", new String(), false);
    }

    public void testSimpleConjunction()
    {
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name = 'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL AND Name = 'Folder'", 0, false, "ObjectId", new String(), false);
    }

    public void testSimpleDisjunction()
    {
        testQuery("SELECT * FROM Folder WHERE Name = 'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name = 'Folder 2'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name = 'Folder 1' OR Name = 'Folder 2'", 2, false, "ObjectId", new String(), false);
    }

    public void testExists()
    {
        testQuery("SELECT * FROM Folder WHERE Name IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name IS NULL", 0, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Document WHERE Name IS NOT NULL", 10, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Document WHERE Name IS NULL", 0, false, "ObjectId", new String(), false);
    }

    public void testObjectEquals()
    {

    }

    public void testDocumentEquals()
    {

    }

    public void testFolderEquals()
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_NAME);
        String Name = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM Folder WHERE Name = '" + Name + "'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE Name = 'Folder 1'", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE ParentId = '" + rootNodeRef.toString() + "'", 4, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Folder WHERE AllowedChildObjectTypeIds = 'meep'", 0, false, "ObjectId", new String(), true);
    }

    public void test_IN_TREE()
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM Folder WHERE IN_TREE('" + id + "')", 6, false, "ObjectId", new String(), false);
    }

    public void test_IN_FOLDER()
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM Folder WHERE IN_FOLDER('" + id + "')", 2, false, "ObjectId", new String(), false);
    }

    public void testFTS()
    {
        testQuery("SELECT * FROM Document WHERE CONTAINS('\"zebra\"')", 9, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Document WHERE CONTAINS('\"quick\"')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Document WHERE CONTAINS('TEXT:\"quick\"')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT * FROM Document D WHERE CONTAINS(D, 'Name:\"Tutorial\"')", 1, false, "ObjectId", new String(), false);
        testQuery("SELECT Name as BOO FROM Document D WHERE CONTAINS('BOO:\"Tutorial\"')", 1, false, "ObjectId", new String(), false);
    }

    public void testBasicSelectAsGuest()
    {
        runAs("guest");
        testQuery("SELECT * FROM Document", 0, false, "ObjectId", new String(), false);

    }
    
    public void testBasicSelectAsCmis()
    {
        runAs("cmis");
        testQuery("SELECT * FROM Document", 7, false, "ObjectId", new String(), false);

    }

    public void testBasicSelect()
    {
        testQuery("SELECT * FROM Document", 10, false, "ObjectId", new String(), false);
    }

    public void testBasicDefaultMetaData()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM Document", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        CMISTypeDefinition typeDef = cmisDictionaryService.findType(CMISDictionaryModel.DOCUMENT_TYPE_ID);
        assertEquals(typeDef.getPropertyDefinitions().size(), md.getColumnNames().length);
        assertNotNull(md.getColumn(CMISDictionaryModel.PROP_OBJECT_ID));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector(""));
        rs.close();
    }

    public void testBasicMetaData()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT DOC.ObjectId, DOC.ObjectId AS ID FROM Document AS DOC", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
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
        CMISQueryOptions options = new CMISQueryOptions("SELECT DOC.ObjectId, DOC.ObjectTypeId AS ID FROM Folder AS DOC", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
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
        CMISQueryOptions options = new CMISQueryOptions("SELECT DOC.*  FROM Document AS DOC", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);

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
        CMISQueryOptions options = new CMISQueryOptions("SELECT *  FROM Folder AS DOC", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);

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
        CMISQueryOptions options = new CMISQueryOptions("SELECT *  FROM ST_SITES AS DOC", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);

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
        CMISQueryOptions options = new CMISQueryOptions(
                "SELECT DOC.Name AS Name, \nLOWER(\tDOC.Name \n), LOWER ( DOC.Name )  AS LName, UPPER ( DOC.Name ) , UPPER(DOC.Name) AS UName, Score(), SCORE(DOC), SCORE() AS SCORED, SCORE(DOC) AS DOCSCORE FROM Folder AS DOC",
                rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);

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

    public void testParse1() throws RecognitionException
    {
        String query = "SELECT UPPER(1.0) AS WOOF FROM Document AS DOC LEFT OUTER JOIN Folder AS FOLDER ON (DOC.Name = FOLDER.Name) WHERE LOWER(DOC.Name = ' woof' AND CONTAINS(, 'one two three') AND  CONTAINS(, 'DOC.Name:lemur AND woof') AND (DOC.Name in ('one', 'two') AND IN_FOLDER('meep') AND DOC.Name like 'woof' and DOC.Name = 'woof' and DOC.ObjectId = 'meep') ORDER BY DOC.Name DESC, WOOF";
        parse(query);
    }

    public void testParse2() throws RecognitionException
    {
        String query = "SELECT TITLE, AUTHORS, DATE FROM WHITE_PAPER WHERE ( IN_TREE( , 'ID00093854763') ) AND ( 'SMITH' = ANY AUTHORS )";
        parse(query);
    }

    public void testParse3() throws RecognitionException
    {
        String query = "SELECT ObjectId, SCORE() AS X, DESTINATION, DEPARTURE_DATES FROM TRAVEL_BROCHURE WHERE ( CONTAINS(, 'CARIBBEAN CENTRAL AMERICA CRUISE TOUR') ) AND ( '2009-1-1' < ANY DEPARTURE_DATES ) ORDER BY X DESC";
        parse(query);
    }

    public void testParse4() throws RecognitionException
    {
        String query = "SELECT * FROM CAR_REVIEW WHERE ( LOWER(MAKE) = 'buick' ) OR ( ANY FEATURES IN ('NAVIGATION SYSTEM', 'SATELLITE RADIO', 'MP3' ) )";
        parse(query);
    }

    public void testParse5() throws RecognitionException
    {
        String query = "SELECT Y.CLAIM_NUM, X.PROPERTY_ADDRESS, Y.DAMAGE_ESTIMATES FROM POLICY AS X JOIN CLAIMS AS Y ON ( X.POLICY_NUM = Y.POLICY_NUM ) WHERE ( 100000 <= ANY Y.DAMAGE_ESTIMATES ) AND ( Y.CAUSE NOT LIKE '%Katrina%' )";
        parse(query);
    }

    public void testParse6() throws RecognitionException
    {
        String query = "SELECT * FROM CM_TITLED";
        parse(query);
        query = "SELECT D.*, T.* FROM DOCUMENT AS D JOIN CM_TITLED AS T ON (D.OBJECTID = T.OBJECTID)";
        parse(query);
        query = "SELECT D.*, T.* FROM CM_TITLED T JOIN DOCUMENT D ON (D.OBJECTID = T.OBJECTID)";
        parse(query);
    }
    
    public void testParseIssues() throws RecognitionException
    {
        String query = "SELECT Name, ObjectId, asdf asdfasdf asdfasdf asdfasdfasdf FROM DOCUMENT";
        parse(query);
    }

    
    
    public void testAspectProperties()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM CM_OWNABLE O", rootNodeRef.getStoreRef());
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        CMISResultSet rs = cmisQueryService.query(options);

        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(1, md.getColumnNames().length);
        assertNotNull(md.getColumn("O.cm_owner"));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector("O"));
        for (CMISResultSetRow row : rs)
        {
            System.out.println("\n\n");
            System.out.println(row.getValues());
            System.out.println("\n\n");
        }
        rs.close();
    }

    public void testAspectJoin()
    {
        testQuery(
                "select o.*, t.* from ( cm_ownable o join cm_titled t on (o.objectid = t.objectid)  JOIN DOCUMENT AS D ON (D.objectid = o.objectid ) ) where o.cm_owner = 'andy' and t.cm_title = 'Alfresco tutorial' and CONTAINS(D, '\"jumped\"') and 2 <> D.ContentStreamLength  ",
                1, false, "objectid", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        
        testQuery("SELECT * FROM CM_OWNABLE", 1, false, "ObjectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM CM_OWNABLE where CM_oWNER = 'andy'", 1, false, "ObjectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM CM_OWNABLE where CM_OWNER = 'bob'", 0, false, "ObjectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM DOCUMENT AS D JOIN CM_OWNABLE AS O ON (D.ObjectId = O.ObjectId)", 1, false, "ObjectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM DOCUMENT AS D JOIN CM_OWNABLE AS O ON (D.OBJECTID = O.OBJECTID)", 1, false, "ObjectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.*, T.* FROM DOCUMENT AS D JOIN CM_OWNABLE AS O ON (D.OBJECTID = O.OBJECTID) JOIN CM_TITLED T ON (T.OBJECTID = D.OBJECTID)", 1, false, "ObjectId",
                new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CM_OWNABLE O JOIN DOCUMENT D ON (D.ObjectId = O.ObjectId)", 1, false, "ObjectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, F.* FROM FOLDER F JOIN DOCUMENT D ON (D.ObjectId = F.ObjectId)", 0, false, "ObjectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT O.*, T.* FROM CM_OWNABLE O JOIN CM_TITLED T ON (O.ObjectId = T.ObjectId)", 1, false, "ObJeCtId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("select o.*, t.* from cm_ownable o join cm_titled t on (o.objectid = t.objectid)", 1, false, "objectid", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("sElEcT o.*, T.* fRoM cM_oWnAbLe o JoIn Cm_TiTlEd T oN (o.oBjEcTiD = T.ObJeCtId)", 1, false, "OBJECTID", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("select o.*, t.* from ( cm_ownable o join cm_titled t on (o.objectid = t.objectid) )", 1, false, "objectid", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("select o.*, t.* from ( cm_ownable o join cm_titled t on (o.objectid = t.objectid)  JOIN DOCUMENT AS D ON (D.objectid = o.objectid ) )", 1, false, "objectid",
                new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery(
                "select o.*, t.* from ( cm_ownable o join cm_titled t on (o.objectid = t.objectid)  JOIN DOCUMENT AS D ON (D.objectid = o.objectid ) ) where o.cm_owner = 'andy' and t.cm_title = 'Alfresco tutorial' and CONTAINS(D, '\"jumped\"') and 2 <> D.ContentStreamLength  ",
                1, false, "objectid", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery(
                "select o.*, t.* from ( cm_ownable o join cm_titled t on (o.objectid = t.objectid)  JOIN DOCUMENT AS D ON (D.objectid = o.objectid ) ) where o.cm_owner = 'andy' and t.cm_title = 'Alfresco tutorial' and CONTAINS(D, 'jumped') and 2 <> D.ContentStreamLength  ",
                1, false, "objectid", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
    }

    public void testPaging()
    {

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM FOLDER", rootNodeRef.getStoreRef());
        List<String> expected = new ArrayList<String>(10);

        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(10, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("ObjectId");
            String id = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            expected.add(id);
        }
        rs.close();

        for (int skip = 0; skip < 20; skip++)
        {
            for(int max = 0; max < 20; max++)
            {
                doPage(expected, skip, max);
            }
        }

    }

    private void doPage(List<String> expected, int skip, int max)
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM FOLDER", rootNodeRef.getStoreRef());
        options.setSkipCount(skip);
        options.setMaxItems(max);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals("Skip = "+skip+ " max  = "+max, skip+max > 10 ? 10 - skip : max, rs.getLength());
        assertEquals("Skip = "+skip+ " max  = "+max, (skip+max) < 10, rs.hasMore());
        assertEquals("Skip = "+skip+ " max  = "+max, skip, rs.getStart());
        int actualPosition = skip;
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("ObjectId");
            String id = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertEquals("Skip = "+skip+ " max  = "+max+" actual = "+actualPosition, expected.get(actualPosition), id);
            actualPosition++;
        }
    }

    private void parse(String query) throws RecognitionException
    {
        CharStream cs = new ANTLRStringStream(query);
        CMISLexer lexer = new CMISLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CMISParser parser = new CMISParser(tokens);
        CommonTree queryNode = (CommonTree) parser.query().getTree();
    }
}
