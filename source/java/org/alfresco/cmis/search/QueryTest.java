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
                            + row.getValue("cmis:ObjectId") + " " + ((returnPropertyName != null) ? (returnPropertyName + "=" + row.getValue(returnPropertyName)) : "") + " Score="
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
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds =  'test'", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds <> 'test'", 10, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds <  'test'", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds <= 'test'", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds >  'test'", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds >= 'test'", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds IN     ('test')", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds NOT IN ('test')", 10, false, "cmis:AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds     LIKE 'test'", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds NOT LIKE 'test'", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds IS NOT NULL", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE cmis:AllowedChildObjectTypeIds IS     NULL", 10, false, "cmis:AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE 'test' =  ANY cmis:AllowedChildObjectTypeIds", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE 'test' <> ANY cmis:AllowedChildObjectTypeIds", 10, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE 'test' <  ANY cmis:AllowedChildObjectTypeIds", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE 'test' <= ANY cmis:AllowedChildObjectTypeIds", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE 'test' >  ANY cmis:AllowedChildObjectTypeIds", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE 'test' >= ANY cmis:AllowedChildObjectTypeIds", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE ANY cmis:AllowedChildObjectTypeIds IN     ('test')", 0, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:AllowedChildObjectTypeIds FROM Folder WHERE ANY cmis:AllowedChildObjectTypeIds NOT IN ('test')", 10, false, "cmis:AllowedChildObjectTypeIds", new String(), true);
    }

    public void test_PARENT()
    {
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId =  '" + rootNodeRef.toString() + "'", 4, false, "cmis:ParentId", new String(), false);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId <> '" + rootNodeRef.toString() + "'", 6, false, "cmis:ParentId", new String(), false);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId <  '" + rootNodeRef.toString() + "'", 0, false, "cmis:ParentId", new String(), true);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId <= '" + rootNodeRef.toString() + "'", 0, false, "cmis:ParentId", new String(), true);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId >  '" + rootNodeRef.toString() + "'", 0, false, "cmis:ParentId", new String(), true);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId >= '" + rootNodeRef.toString() + "'", 0, false, "cmis:ParentId", new String(), true);

        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId IN     ('" + rootNodeRef.toString() + "')", 4, false, "cmis:ParentId", new String(), false);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId NOT IN ('" + rootNodeRef.toString() + "')", 6, false, "cmis:ParentId", new String(), false);

        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId     LIKE '" + rootNodeRef.toString() + "'", 4, false, "cmis:ParentId", new String(), false);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId NOT LIKE '" + rootNodeRef.toString() + "'", 6, false, "cmis:ParentId", new String(), false);

        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId IS NOT NULL", 10, false, "cmis:ParentId", new String(), false);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE cmis:ParentId IS     NULL", 0, false, "cmis:ParentId", new String(), false);

        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' =  ANY cmis:ParentId", 4, false, "cmis:ParentId", new String(), false);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' <> ANY cmis:ParentId", 6, false, "cmis:ParentId", new String(), false);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' <  ANY cmis:ParentId", 0, false, "cmis:ParentId", new String(), true);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' <= ANY cmis:ParentId", 0, false, "cmis:ParentId", new String(), true);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' >  ANY cmis:ParentId", 0, false, "cmis:ParentId", new String(), true);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' >= ANY cmis:ParentId", 0, false, "cmis:ParentId", new String(), true);

        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE ANY cmis:ParentId IN     ('" + rootNodeRef.toString() + "')", 4, false, "cmis:ParentId", new String(), false);
        testQuery("SELECT cmis:ParentId FROM cmis:folder WHERE ANY cmis:ParentId NOT IN ('" + rootNodeRef.toString() + "')", 6, false, "cmis:ParentId", new String(), false);
    }

    public void test_CONTENT_STREAM_FILENAME()
    {
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'Alfresco Tutorial'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'AA'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'BB'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'CC'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'DD'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'EE'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'FF'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'GG'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'HH'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'aa'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        
        
        
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName =  'Alfresco Tutorial'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName <> 'Alfresco Tutorial'", 9, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName <  'Alfresco Tutorial'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName <= 'Alfresco Tutorial'", 2, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName >  'Alfresco Tutorial'", 8, true, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName >= 'Alfresco Tutorial'", 9, false, "cmis:ContentStreamFileName", new String(), false);

        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName IN     ('Alfresco Tutorial')", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName NOT IN ('Alfresco Tutorial')", 9, false, "cmis:ContentStreamFileName", new String(), false);

        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName     LIKE 'Alfresco Tutorial'", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName NOT LIKE 'Alfresco Tutorial'", 9, false, "cmis:ContentStreamFileName", new String(), false);

        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName IS NOT NULL", 10, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE cmis:ContentStreamFileName IS     NULL", 0, false, "cmis:ContentStreamFileName", new String(), false);

        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' =  ANY cmis:ContentStreamFileName", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' <> ANY cmis:ContentStreamFileName", 9, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' <  ANY cmis:ContentStreamFileName", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' <= ANY cmis:ContentStreamFileName", 2, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' >  ANY cmis:ContentStreamFileName", 8, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' >= ANY cmis:ContentStreamFileName", 9, false, "cmis:ContentStreamFileName", new String(), false);

        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE ANY cmis:ContentStreamFileName IN     ('Alfresco Tutorial')", 1, false, "cmis:ContentStreamFileName", new String(), false);
        testQuery("SELECT cmis:ContentStreamFileName FROM cmis:document WHERE ANY cmis:ContentStreamFileName NOT IN ('Alfresco Tutorial')", 9, false, "cmis:ContentStreamFileName", new String(), false);
    }

    public void test_CONTENT_STREAM_MIME_TYPE()
    {
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType =  'text/plain'", 10, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType <> 'text/plain'", 0, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType <  'text/plain'", 0, true, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType <= 'text/plain'", 10, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType >  'text/plain'", 0, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType >= 'text/plain'", 10, false, "cmis:ContentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType IN     ('text/plain')", 10, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType NOT IN ('text/plain')", 0, false, "cmis:ContentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType     LIKE 'text/plain'", 10, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType NOT LIKE 'text/plain'", 0, false, "cmis:ContentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType IS NOT NULL", 10, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE cmis:ContentStreamMimeType IS     NULL", 0, false, "cmis:ContentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE 'text/plain' =  ANY cmis:ContentStreamMimeType", 10, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE 'text/plain' <> ANY cmis:ContentStreamMimeType", 0, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE 'text/plain' <  ANY cmis:ContentStreamMimeType", 0, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE 'text/plain' <= ANY cmis:ContentStreamMimeType", 10, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE 'text/plain' >  ANY cmis:ContentStreamMimeType", 0, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE 'text/plain' >= ANY cmis:ContentStreamMimeType", 10, false, "cmis:ContentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE ANY cmis:ContentStreamMimeType IN     ('text/plain')", 10, false, "cmis:ContentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:ContentStreamMimeType FROM cmis:document WHERE ANY cmis:ContentStreamMimeType NOT IN ('text/plain')", 0, false, "cmis:ContentStreamMimeType", new String(), false);
    }

    public void test_CONTENT_STREAM_LENGTH()
    {
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength =  750", 0, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength <> 750", 10, true, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength <  750", 10, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength <= 750", 10, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength >  750", 0, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength >= 750", 0, false, "cmis:ContentStreamLength", new String(), false);

        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength IN     (750)", 0, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength NOT IN (750)", 10, false, "cmis:ContentStreamLength", new String(), false);

        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength     LIKE '750'", 0, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength NOT LIKE '750'", 10, false, "cmis:ContentStreamLength", new String(), false);

        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength IS NOT NULL", 10, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE cmis:ContentStreamLength IS     NULL", 0, false, "cmis:ContentStreamLength", new String(), false);

        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE 750 =  ANY cmis:ContentStreamLength", 0, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE 750 <> ANY cmis:ContentStreamLength", 10, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE 750 <  ANY cmis:ContentStreamLength", 10, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE 750 <= ANY cmis:ContentStreamLength", 10, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE 750 >  ANY cmis:ContentStreamLength", 0, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE 750 >= ANY cmis:ContentStreamLength", 0, false, "cmis:ContentStreamLength", new String(), false);

        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE ANY cmis:ContentStreamLength IN     (750)", 0, false, "cmis:ContentStreamLength", new String(), false);
        testQuery("SELECT cmis:ContentStreamLength FROM cmis:document WHERE ANY cmis:ContentStreamLength NOT IN (750)", 10, false, "cmis:ContentStreamLength", new String(), false);
    }

    public void test_CHECKIN_COMMENT()
    {
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment =  'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment <> 'admin'", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment <  'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment <= 'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment >  'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment >= 'admin'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment IN     ('admin')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment NOT IN ('admin')", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment     LIKE 'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment NOT LIKE 'admin'", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE cmis:CheckinComment IS     NULL", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE 'admin' =  ANY cmis:CheckinComment", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE 'admin' <> ANY cmis:CheckinComment", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE 'admin' <  ANY cmis:CheckinComment", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE 'admin' <= ANY cmis:CheckinComment", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE 'admin' >  ANY cmis:CheckinComment", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE 'admin' >= ANY cmis:CheckinComment", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE ANY cmis:CheckinComment IN     ('admin')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:CheckinComment FROM cmis:document WHERE ANY cmis:CheckinComment NOT IN ('admin')", 10, false, "cmis:ObjectId", new String(), true);
    }

    public void test_VERSION_SERIES_CHECKED_OUT_ID()
    {
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId =  'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId <> 'admin'", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId <  'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId <= 'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId >  'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId >= 'admin'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId IN     ('admin')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId NOT IN ('admin')", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId     LIKE 'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId NOT LIKE 'admin'", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE cmis:VersionSeriesCheckedOutId IS     NULL", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE 'admin' =  ANY cmis:VersionSeriesCheckedOutId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE 'admin' <> ANY cmis:VersionSeriesCheckedOutId", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE 'admin' <  ANY cmis:VersionSeriesCheckedOutId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE 'admin' <= ANY cmis:VersionSeriesCheckedOutId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE 'admin' >  ANY cmis:VersionSeriesCheckedOutId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE 'admin' >= ANY cmis:VersionSeriesCheckedOutId", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE ANY cmis:VersionSeriesCheckedOutId IN     ('admin')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutId FROM cmis:document WHERE ANY cmis:VersionSeriesCheckedOutId NOT IN ('admin')", 10, false, "cmis:ObjectId", new String(), true);
    }

    public void test_VERSION_SERIES_CHECKED_OUT_BY()
    {
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy =  'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy <> 'admin'", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy <  'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy <= 'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy >  'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy >= 'admin'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy IN     ('admin')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy NOT IN ('admin')", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy     LIKE 'admin'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy NOT LIKE 'admin'", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE cmis:VersionSeriesCheckedOutBy IS     NULL", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' =  ANY cmis:VersionSeriesCheckedOutBy", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' <> ANY cmis:VersionSeriesCheckedOutBy", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' <  ANY cmis:VersionSeriesCheckedOutBy", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' <= ANY cmis:VersionSeriesCheckedOutBy", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' >  ANY cmis:VersionSeriesCheckedOutBy", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' >= ANY cmis:VersionSeriesCheckedOutBy", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE ANY cmis:VersionSeriesCheckedOutBy IN     ('admin')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesCheckedOutBy FROM cmis:document WHERE ANY cmis:VersionSeriesCheckedOutBy NOT IN ('admin')", 10, false, "cmis:ObjectId", new String(), true);
    }

    public void test_VERSION_SERIES_IS_CHECKED_OUT()
    {
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut =  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut <> 'TRUE'", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut <  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut <= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut >  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut >= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut NOT IN ('TRUE')", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut     LIKE 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut NOT LIKE 'TRUE'", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:IsVeriesSeriesCheckedOut IS     NULL", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' =  ANY cmis:IsVeriesSeriesCheckedOut", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' <> ANY cmis:IsVeriesSeriesCheckedOut", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' <  ANY cmis:IsVeriesSeriesCheckedOut", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' <= ANY cmis:IsVeriesSeriesCheckedOut", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' >  ANY cmis:IsVeriesSeriesCheckedOut", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' >= ANY cmis:IsVeriesSeriesCheckedOut", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE ANY cmis:IsVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsVeriesSeriesCheckedOut FROM cmis:document WHERE ANY cmis:IsVeriesSeriesCheckedOut NOT IN ('TRUE')", 10, false, "cmis:ObjectId", new String(), true);
    }

    public void test_VERSION_SERIES_ID()
    {
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId =  'company'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId <> 'company'", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId <  'company'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId <= 'company'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId >  'company'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId >= 'company'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId IN     ('company')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId NOT IN ('company')", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId     LIKE 'company'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId NOT LIKE 'company'", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE cmis:VersionSeriesId IS     NULL", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE 'company' =  ANY cmis:VersionSeriesId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE 'company' <> ANY cmis:VersionSeriesId", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE 'company' <  ANY cmis:VersionSeriesId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE 'company' <= ANY cmis:VersionSeriesId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE 'company' >  ANY cmis:VersionSeriesId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE 'company' >= ANY cmis:VersionSeriesId", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE ANY cmis:VersionSeriesId IN     ('company')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:VersionSeriesId FROM cmis:document WHERE ANY cmis:VersionSeriesId NOT IN ('company')", 10, false, "cmis:ObjectId", new String(), true);
    }

    public void test_VERSION_LABEL()
    {
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel =  'company'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel <> 'company'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel <  'company'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel <= 'company'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel >  'company'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel >= 'company'", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel IN     ('company')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel NOT IN ('company')", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel     LIKE 'company'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel NOT LIKE 'company'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel IS NOT NULL", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE cmis:VersionLabel IS     NULL", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE 'company' =  ANY cmis:VersionLabel", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE 'company' <> ANY cmis:VersionLabel", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE 'company' <  ANY cmis:VersionLabel", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE 'company' <= ANY cmis:VersionLabel", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE 'company' >  ANY cmis:VersionLabel", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE 'company' >= ANY cmis:VersionLabel", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE ANY cmis:VersionLabel IN     ('company')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:VersionLabel FROM cmis:document WHERE ANY cmis:VersionLabel NOT IN ('company')", 10, false, "cmis:ObjectId", new String(), false);
    }

    public void test_IS_LATEST_MAJOR_VERSION()
    {
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion =  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion <> 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion <  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion <= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion >  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion >= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion NOT IN ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion     LIKE 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion NOT LIKE 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE cmis:IsLatestMajorVersion IS     NULL", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE 'TRUE' =  ANY cmis:IsLatestMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE 'TRUE' <> ANY cmis:IsLatestMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE 'TRUE' <  ANY cmis:IsLatestMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE 'TRUE' <= ANY cmis:IsLatestMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE 'TRUE' >  ANY cmis:IsLatestMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE 'TRUE' >= ANY cmis:IsLatestMajorVersion", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE ANY cmis:IsLatestMajorVersion IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestMajorVersion FROM cmis:document WHERE ANY cmis:IsLatestMajorVersion NOT IN ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
    }

    public void test_IS_MAJOR_VERSION()
    {
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion =  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion <> 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion <  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion <= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion >  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion >= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion NOT IN ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion     LIKE 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion NOT LIKE 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE cmis:IsMajorVersion IS     NULL", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE 'TRUE' =  ANY cmis:IsMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE 'TRUE' <> ANY cmis:IsMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE 'TRUE' <  ANY cmis:IsMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE 'TRUE' <= ANY cmis:IsMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE 'TRUE' >  ANY cmis:IsMajorVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE 'TRUE' >= ANY cmis:IsMajorVersion", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE ANY cmis:IsMajorVersion IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsMajorVersion FROM cmis:document WHERE ANY cmis:IsMajorVersion NOT IN ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
    }

    public void test_IS_LATEST_VERSION()
    {
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion =  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion <> 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion <  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion <= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion >  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion >= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion NOT IN ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion     LIKE 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion NOT LIKE 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE cmis:IsLatestVersion IS     NULL", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE 'TRUE' =  ANY cmis:IsLatestVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE 'TRUE' <> ANY cmis:IsLatestVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE 'TRUE' <  ANY cmis:IsLatestVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE 'TRUE' <= ANY cmis:IsLatestVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE 'TRUE' >  ANY cmis:IsLatestVersion", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE 'TRUE' >= ANY cmis:IsLatestVersion", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE ANY cmis:IsLatestVersion IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsLatestVersion FROM cmis:document WHERE ANY cmis:IsLatestVersion NOT IN ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
    }

    public void test_IS_IMMUTABLE()
    {
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable =  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable <> 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable <  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable <= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable >  'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable >= 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable NOT IN ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable     LIKE 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable NOT LIKE 'TRUE'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE cmis:IsImmutable IS     NULL", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE 'TRUE' =  ANY cmis:IsImmutable", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE 'TRUE' <> ANY cmis:IsImmutable", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE 'TRUE' <  ANY cmis:IsImmutable", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE 'TRUE' <= ANY cmis:IsImmutable", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE 'TRUE' >  ANY cmis:IsImmutable", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE 'TRUE' >= ANY cmis:IsImmutable", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE ANY cmis:IsImmutable IN     ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:IsImmutable FROM cmis:document WHERE ANY cmis:IsImmutable NOT IN ('TRUE')", 0, false, "cmis:ObjectId", new String(), true);
    }

    public void test_folder_NAME()
    {
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name =  'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name <> 'Folder 1'", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name <  'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name <= 'Folder 1'", 2, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name >  'Folder 1'", 8, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name >= 'Folder 1'", 9, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name IN     ('Folder 1')", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name NOT IN ('Folder 1')", 9, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name     LIKE 'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name NOT LIKE 'Folder 1'", 9, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE cmis:Name IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:folder WHERE 'Folder 1' =  ANY cmis:Name", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE 'Folder 1' <> ANY cmis:Name", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE 'Folder 1' <  ANY cmis:Name", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE 'Folder 1' <= ANY cmis:Name", 2, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE 'Folder 1' >  ANY cmis:Name", 8, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE 'Folder 1' >= ANY cmis:Name", 9, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:folder WHERE ANY cmis:Name IN     ('Folder 1')", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:folder WHERE ANY cmis:Name NOT IN ('Folder 1')", 9, false, "cmis:ObjectId", new String(), false);
    }

    public void test_document_Name()
    {
        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name =  'Alfresco Tutorial'", 1, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name <> 'Alfresco Tutorial'", 9, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name <  'Alfresco Tutorial'", 1, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name <= 'Alfresco Tutorial'", 2, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name >  'Alfresco Tutorial'", 8, true, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name >= 'Alfresco Tutorial'", 9, false, "cmis:Name", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name IN     ('Alfresco Tutorial')", 1, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name NOT IN ('Alfresco Tutorial')", 9, false, "cmis:Name", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name     LIKE 'Alfresco Tutorial'", 1, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name NOT LIKE 'Alfresco Tutorial'", 9, false, "cmis:Name", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name IS NOT NULL", 10, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE cmis:Name IS     NULL", 0, false, "cmis:Name", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:document WHERE 'Alfresco Tutorial' =  ANY cmis:Name", 1, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE 'Alfresco Tutorial' <> ANY cmis:Name", 9, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE 'Alfresco Tutorial' <  ANY cmis:Name", 1, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE 'Alfresco Tutorial' <= ANY cmis:Name", 2, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE 'Alfresco Tutorial' >  ANY cmis:Name", 8, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE 'Alfresco Tutorial' >= ANY cmis:Name", 9, false, "cmis:Name", new String(), false);

        testQuery("SELECT cmis:Name FROM cmis:document WHERE ANY cmis:Name IN     ('Alfresco Tutorial')", 1, false, "cmis:Name", new String(), false);
        testQuery("SELECT cmis:Name FROM cmis:document WHERE ANY cmis:Name NOT IN ('Alfresco Tutorial')", 9, false, "cmis:Name", new String(), false);
    }

    public void test_CHANGE_TOKEN()
    {
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken =  'test'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken <> 'test'", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken <  'test'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken <= 'test'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken >  'test'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken >= 'test'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken IN     ('test')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken NOT IN ('test')", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken     LIKE 'test'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken NOT LIKE 'test'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken IS NOT NULL", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE cmis:ChangeToken IS     NULL", 10, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE 'test' =  ANY cmis:ChangeToken", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE 'test' <> ANY cmis:ChangeToken", 10, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE 'test' <  ANY cmis:ChangeToken", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE 'test' <= ANY cmis:ChangeToken", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE 'test' >  ANY cmis:ChangeToken", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE 'test' >= ANY cmis:ChangeToken", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE ANY cmis:ChangeToken IN     ('test')", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ChangeToken FROM cmis:folder WHERE ANY cmis:ChangeToken NOT IN ('test')", 10, false, "cmis:ObjectId", new String(), true);
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

        Date date = testQuery("SELECT cmis:LastModificationDate FROM cmis:document", -1, false, "cmis:LastModificationDate", new Date(), false);
        today.setTime(date);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));

        String sDate = df.format(today.getTime());

        // Today (assuming al ws created today)

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate =  '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate <> '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate <  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate <= '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate >  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate >= '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate IN     ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate NOT IN ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate     LIKE '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate NOT LIKE '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:LastModificationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:LastModificationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:LastModificationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:LastModificationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:LastModificationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:LastModificationDate", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE ANY cmis:LastModificationDate IN     ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE ANY cmis:LastModificationDate NOT IN ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate =  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate <> '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate <  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate <= '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate >  '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate >= '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate NOT IN ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate     LIKE '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate NOT LIKE '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:LastModificationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:LastModificationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:LastModificationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:LastModificationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:LastModificationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:LastModificationDate", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE ANY cmis:LastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE ANY cmis:LastModificationDate NOT IN ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate =  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate <> '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate <  '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate <= '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate >  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate >= '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate NOT IN ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate     LIKE '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate NOT LIKE '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE cmis:LastModificationDate IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:LastModificationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:LastModificationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:LastModificationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:LastModificationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:LastModificationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:LastModificationDate", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE ANY cmis:LastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModificationDate FROM cmis:document WHERE ANY cmis:LastModificationDate NOT IN ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);

    }

    public void test_LAST_MODIFIED_BY()
    {
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy =  'System'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy <> 'System'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy <  'System'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy <= 'System'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy >  'System'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy >= 'System'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy IN     ('System')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy NOT IN ('System')", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy     LIKE 'System'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy NOT LIKE 'System'", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE cmis:LastModifiedBy IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE 'System' =  ANY cmis:LastModifiedBy", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE 'System' <> ANY cmis:LastModifiedBy", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE 'System' <  ANY cmis:LastModifiedBy", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE 'System' <= ANY cmis:LastModifiedBy", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE 'System' >  ANY cmis:LastModifiedBy", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE 'System' >= ANY cmis:LastModifiedBy", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE ANY cmis:LastModifiedBy IN     ('System')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:LastModifiedBy FROM cmis:document WHERE ANY cmis:LastModifiedBy NOT IN ('System')", 0, false, "cmis:ObjectId", new String(), false);

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

        Date date = testQuery("SELECT cmis:CreationDate FROM cmis:document", -1, false, "cmis:CreationDate", new Date(), false);
        today.setTime(date);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));

        String sDate = df.format(today.getTime());

        // Today (assuming al ws created today)

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate =  '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate <> '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate <  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate <= '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate >  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate >= '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate IN     ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate NOT IN ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate     LIKE '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate NOT LIKE '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:CreationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:CreationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:CreationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:CreationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:CreationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:CreationDate", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE ANY cmis:CreationDate IN     ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE ANY cmis:CreationDate NOT IN ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate =  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate <> '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate <  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate <= '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate >  '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate >= '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate IN     ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate NOT IN ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate     LIKE '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate NOT LIKE '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:CreationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:CreationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:CreationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:CreationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:CreationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:CreationDate", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE ANY cmis:CreationDate IN     ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE ANY cmis:CreationDate NOT IN ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate =  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate <> '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate <  '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate <= '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate >  '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate >= '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate IN     ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate NOT IN ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate     LIKE '" + sDate + "'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate NOT LIKE '" + sDate + "'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE cmis:CreationDate IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:CreationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:CreationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:CreationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:CreationDate", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:CreationDate", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:CreationDate", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE ANY cmis:CreationDate IN     ('" + sDate + "')", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreationDate FROM cmis:document WHERE ANY cmis:CreationDate NOT IN ('" + sDate + "')", 10, false, "cmis:ObjectId", new String(), false);

    }

    public void test_CREATED_BY()
    {
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy =  'System'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy <> 'System'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy <  'System'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy <= 'System'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy >  'System'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy >= 'System'", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy IN     ('System')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy NOT IN ('System')", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy     LIKE 'System'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy NOT LIKE 'System'", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE cmis:CreatedBy IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE 'System' =  ANY cmis:CreatedBy", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE 'System' <> ANY cmis:CreatedBy", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE 'System' <  ANY cmis:CreatedBy", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE 'System' <= ANY cmis:CreatedBy", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE 'System' >  ANY cmis:CreatedBy", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE 'System' >= ANY cmis:CreatedBy", 10, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE ANY cmis:CreatedBy IN     ('System')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:CreatedBy FROM cmis:document WHERE ANY cmis:CreatedBy NOT IN ('System')", 0, false, "cmis:ObjectId", new String(), false);

    }

    public void test_OBJECT_TYPE_ID()
    {
        // DOC

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId =  'cmis:document'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId <> 'cmis:document'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId <  'cmis:document'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId <= 'cmis:document'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId >  'cmis:document'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId >= 'cmis:document'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId IN     ('cmis:document')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId NOT IN ('cmis:document')", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId     LIKE 'cmis:document'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId NOT LIKE 'cmis:document'", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE cmis:ObjectTypeId IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE 'cmis:document' =  ANY cmis:ObjectTypeId", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE 'cmis:document' <> ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE 'cmis:document' <  ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE 'cmis:document' <= ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE 'cmis:document' >  ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE 'cmis:document' >= ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE ANY cmis:ObjectTypeId IN     ('cmis:document')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:document WHERE ANY cmis:ObjectTypeId NOT IN ('cmis:document')", 0, false, "cmis:ObjectId", new String(), false);

        // FOLDER

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId =  'cmis:folder'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId <> 'cmis:folder'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId <  'cmis:folder'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId <= 'cmis:folder'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId >  'cmis:folder'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId >= 'cmis:folder'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHEcmis:folderectTypeId IN     ('cmis:folder')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId NOT IN ('cmis:folder')", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId     LIKE 'cmis:folder'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId NOT LIKE 'cmis:folder'", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE cmis:ObjectTypeId IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE 'cmis:folder' =  ANY cmis:ObjectTypeId", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE 'cmis:folder' <> ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE 'cmis:folder' <  ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE 'cmis:folder' <= ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE 'cmis:folder' >  ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE 'cmis:folder' >= ANY cmis:ObjectTypeId", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE ANY cmis:ObjectTypeId IN     ('cmis:folder')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectTypeId FROM cmis:folder WHERE ANY cmis:ObjectTypeId NOT IN ('cmis:folder')", 0, false, "cmis:ObjectId", new String(), false);

        // RELATIONSHIP

        testQuery("SELECT cmis:ObjectTypeId FROM Relationship WHERE cmis:ObjectTypeId =  ''", 1, false, "cmis:ObjectId", new String(), true);

    }

    public void test_ObjectId()
    {
        String companyHomeId = testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:Name = 'Folder 0'", 1, false, "cmis:ObjectId", new String(), false);

        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        assertEquals(companyHomeId, id);

        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId =  '" + companyHomeId + "'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId <> '" + companyHomeId + "'", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId <  '" + companyHomeId + "'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId <= '" + companyHomeId + "'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId >  '" + companyHomeId + "'", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId >= '" + companyHomeId + "'", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId IN     ('" + companyHomeId + "')", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId NOT IN ('" + companyHomeId + "')", 9, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId     LIKE '" + companyHomeId + "'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId NOT LIKE '" + companyHomeId + "'", 9, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE IN_FOLDER('" + companyHomeId + "')", 2, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE IN_TREE  ('" + companyHomeId + "')", 6, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE cmis:ObjectId IS     NULL", 0, false, "cmis:ObjectId", new String(), false);

        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE '" + companyHomeId + "' =  ANY cmis:ObjectId", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE '" + companyHomeId + "' <> ANY cmis:ObjectId", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE '" + companyHomeId + "' <  ANY cmis:ObjectId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE '" + companyHomeId + "' <= ANY cmis:ObjectId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE '" + companyHomeId + "' >  ANY cmis:ObjectId", 0, false, "cmis:ObjectId", new String(), true);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE '" + companyHomeId + "' >= ANY cmis:ObjectId", 0, false, "cmis:ObjectId", new String(), true);

        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE ANY cmis:ObjectId IN     ('" + companyHomeId + "')", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:ObjectId FROM cmis:folder WHERE ANY cmis:ObjectId NOT IN ('" + companyHomeId + "')", 9, false, "cmis:ObjectId", new String(), false);
    }

    public void testOrderBy()
    {
        String query = "SELECT  cmis:ObjectId FROM cmis:document ORDER cmis:ObjectId";
        CMISResultSet rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT  cmis:ObjectId FROM cmis:document ORDER cmis:ObjectId ASC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT  cmis:ObjectId FROM cmis:document ORDER cmis:ObjectId DESC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT SCORE() AS MEEP, cmis:ObjectId FROM cmis:folder WHERE cmis:Name IN ('company', 'home') ORDER BY MEEP ASC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        query = "SELECT SCORE() AS MEEP, cmis:ObjectId FROM cmis:folder WHERE cmis:Name IN ('company', 'home') ORDER BY MEEP DESC";
        rs = cmisQueryService.query(query);
        // assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:ObjectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

    }
    
    public void testUpperAndLower()
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name = 'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name = 'FOLDER 1'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name = 'folder 1'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:Name) = 'FOLDER 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE Lower(cmis:Name) = 'folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:Name) = 'folder 1'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE Lower(cmis:Name) = 'FOLDER 1'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:Name) = 'Folder 1'", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE Lower(cmis:Name) = 'Folder 1'", 0, false, "cmis:ObjectId", new String(), false);
        
        testQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:Name) <> 'FOLDER 1'", 9, false, "cmis:ObjectId", new String(), false);
        
        testQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:Name) <= 'FOLDER 1'", 2, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:Name) < 'FOLDER 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:Name) >= 'FOLDER 1'", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:Name) > 'FOLDER 1'", 8, false, "cmis:ObjectId", new String(), false);
    }

    public void testAllSimpleTextPredicates()
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name = 'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND NOT cmis:Name = 'Folder 1'", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND 'Folder 1' = ANY cmis:Name", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND NOT cmis:Name <> 'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name <> 'Folder 1'", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name < 'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name <= 'Folder 1'", 2, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name > 'Folder 1'", 8, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name >= 'Folder 1'", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name IN ('Folder 1', '1')", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name NOT IN ('Folder 1', 'Folder 9')", 8, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND ANY cmis:Name IN ('Folder 1', 'Folder 9')", 2, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND ANY cmis:Name NOT IN ('2', '3')", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name LIKE 'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name LIKE 'Fol%'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name LIKE 'F_l_e_ 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name NOT LIKE 'F_l_e_ 1'", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name LIKE 'F_l_e_ %'", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name NOT LIKE 'F_l_e_ %'", 0, false, "cmis:ObjectId", new String(), false);
        // TODO: Fix below which fail??
        //testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name LIKE 'F_l_e_ _'", 10, false, "cmis:ObjectId", new String(), false);
        //testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name NOT LIKE 'F_l_e_ _'", 0, false, "cmis:ObjectId", new String(), false);
    }

    public void testSimpleConjunction()
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name = 'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL AND cmis:Name = 'Folder'", 0, false, "cmis:ObjectId", new String(), false);
    }

    public void testSimpleDisjunction()
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name = 'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name = 'Folder 2'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name = 'Folder 1' OR cmis:Name = 'Folder 2'", 2, false, "cmis:ObjectId", new String(), false);
    }

    public void testExists()
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name IS NULL", 0, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE cmis:Name IS NOT NULL", 10, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE cmis:Name IS NULL", 0, false, "cmis:ObjectId", new String(), false);
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

        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name = '" + Name + "'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:Name = 'Folder 1'", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:ParentId = '" + rootNodeRef.toString() + "'", 4, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:AllowedChildObjectTypeIds = 'meep'", 0, false, "cmis:ObjectId", new String(), true);
    }

    public void test_IN_TREE()
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM cmis:folder WHERE IN_TREE('" + id + "')", 6, false, "cmis:ObjectId", new String(), false);
    }

    public void test_IN_FOLDER()
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM cmis:folder WHERE IN_FOLDER('" + id + "')", 2, false, "cmis:ObjectId", new String(), false);
    }

    public void testFTS()
    {
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\"zebra\"')", 9, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\"quick\"')", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('TEXT:\"quick\"')", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT * FROM cmis:document D WHERE CONTAINS(D, 'cmis:Name:\"Tutorial\"')", 1, false, "cmis:ObjectId", new String(), false);
        testQuery("SELECT cmis:Name as BOO FROM cmis:document D WHERE CONTAINS('BOO:\"Tutorial\"')", 1, false, "cmis:ObjectId", new String(), false);
    }

    public void testBasicSelectAsGuest()
    {
        runAs("guest");
        testQuery("SELECT * FROM cmis:document", 0, false, "cmis:ObjectId", new String(), false);

    }
    
    public void testBasicSelectAsCmis()
    {
        runAs("cmis");
        testQuery("SELECT * FROM cmis:document", 7, false, "cmis:ObjectId", new String(), false);

    }

    public void testBasicSelect()
    {
        testQuery("SELECT * FROM cmis:document", 10, false, "cmis:ObjectId", new String(), false);
    }

    public void testBasicDefaultMetaData()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
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
        CMISQueryOptions options = new CMISQueryOptions("SELECT DOC.cmis:ObjectId, DOC.cmis:ObjectId AS ID FROM cmis:document AS DOC", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(2, md.getColumnNames().length);
        assertNotNull(md.getColumn("DOC.cmis:ObjectId"));
        assertNotNull(md.getColumn("ID"));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector("DOC"));
        rs.close();
    }

    public void testBasicColumns()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT DOC.cmis:ObjectId, DOC.cmis:ObjectTypeId AS ID FROM cmis:folder AS DOC", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(2, md.getColumnNames().length);
        assertNotNull(md.getColumn("DOC.cmis:ObjectId"));
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
        CMISQueryOptions options = new CMISQueryOptions("SELECT DOC.*  FROM cmis:document AS DOC", rootNodeRef.getStoreRef());
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
        CMISQueryOptions options = new CMISQueryOptions("SELECT *  FROM cmis:folder AS DOC", rootNodeRef.getStoreRef());
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
        CMISQueryOptions options = new CMISQueryOptions("SELECT *  FROM ST:SITES AS DOC", rootNodeRef.getStoreRef());
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
                "SELECT DOC.cmis:Name AS cmis:Name, \nLOWER(\tDOC.cmis:Name \n), LOWER ( DOC.cmis:Name )  AS Lcmis:Name, UPPER ( DOC.cmis:Name ) , UPPER(DOC.cmis:Name) AS Ucmis:Name, Score(), SCORE(DOC), SCORE() AS SCORED, SCORE(DOC) AS DOCSCORE FROM cmis:folder AS DOC",
                rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);

        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(9, md.getColumnNames().length);
        assertNotNull(md.getColumn("cmis:Name"));
        assertNotNull(md.getColumn("LOWER(\tDOC.cmis:Name \n)"));
        assertNotNull(md.getColumn("Lcmis:Name"));
        assertNotNull(md.getColumn("UPPER ( DOC.cmis:Name )"));
        assertNotNull(md.getColumn("Ucmis:Name"));
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
        String query = "SELECT UPPER(1.0) AS WOOF FROM cmis:document AS DOC LEFT OUTER JOIN cmis:folder AS FOLDER ON (DOC.cmis:Name = FOLDER.cmis:Name) WHERE LOWER(DOC.cmis:Name = ' woof' AND CONTAINS(, 'one two three') AND  CONTAINS(, 'DOC.cmis:Name:lemur AND woof') AND (DOC.cmis:Name in ('one', 'two') AND IN_FOLDER('meep') AND DOC.cmis:Name like 'woof' and DOC.cmis:Name = 'woof' and DOC.cmis:ObjectId = 'meep') ORDER BY DOC.cmis:Name DESC, WOOF";
        parse(query);
    }

    public void testParse2() throws RecognitionException
    {
        String query = "SELECT TITLE, AUTHORS, DATE FROM WHITE_PAPER WHERE ( IN_TREE( , 'ID00093854763') ) AND ( 'SMITH' = ANY AUTHORS )";
        parse(query);
    }

    public void testParse3() throws RecognitionException
    {
        String query = "SELECT cmis:ObjectId, SCORE() AS X, DESTINATION, DEPARTURE_DATES FROM TRAVEL_BROCHURE WHERE ( CONTAINS(, 'CARIBBEAN CENTRAL AMERICA CRUISE TOUR') ) AND ( '2009-1-1' < ANY DEPARTURE_DATES ) ORDER BY X DESC";
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
        String query = "SELECT cmis:Name, cmis:ObjectId, asdf asdfasdf asdfasdf asdfasdfasdf FROM DOCUMENT";
        parse(query);
    }

    
    
    public void testAspectProperties()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM CM:OWNABLE O", rootNodeRef.getStoreRef());
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        CMISResultSet rs = cmisQueryService.query(options);

        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(1, md.getColumnNames().length);
        assertNotNull(md.getColumn("O.cm:owner"));
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
                "select o.*, t.* from ( cm:ownable o join cm:titled t on (o.cmis:objectid = t.cmis:objectid)  JOIN CMIS:DOCUMENT AS D ON (D.cmis:ObjectId = o.cmis:Objectid ) ) where o.cm:owner = 'andy' and t.cm:title = 'Alfresco tutorial' and CONTAINS(D, '\"jumped\"') and 2 <> D.cmis:ContentStreamLength",
                1, false, "cmis:Objectid", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        
        testQuery("SELECT * FROM CM:OWNABLE", 1, false, "cmis:ObjectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM CM:OWNABLE where CM:oWNER = 'andy'", 1, false, "cmis:ObjectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM CM:OWNABLE where CM:OWNER = 'bob'", 0, false, "cmis:ObjectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON (D.cmis:ObjectId = O.cmis:ObjectId)", 1, false, "cmis:ObjectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON (D.cmis:OBJECTID = O.cmis:OBJECTID)", 1, false, "cmis:ObjectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.*, T.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON (D.cmis:OBJECTID = O.cmis:OBJECTID) JOIN CM:TITLED T ON (T.cmis:OBJECTID = D.cmis:OBJECTID)", 1, false, "cmis:ObjectId",
                new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CM:OWNABLE O JOIN CMIS:DOCUMENT D ON (D.cmis:ObjectId = O.cmis:ObjectId)", 1, false, "cmis:ObjectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, F.* FROM CMIS:FOLDER F JOIN CMIS:DOCUMENT D ON (D.cmis:ObjectId = F.cmis:ObjectId)", 0, false, "cmis:ObjectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT O.*, T.* FROM CM:OWNABLE O JOIN CM:TITLED T ON (O.cmis:ObjectId = T.cmis:ObjectId)", 1, false, "cmis:ObJeCtId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("select o.*, t.* from cm:ownable o join cm:titled t on (o.cmis:objectid = t.cmis:objectid)", 1, false, "cmis:objectid", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("sElEcT o.*, T.* fRoM cM:oWnAbLe o JoIn Cm:TiTlEd T oN (o.cmis:oBjEcTiD = T.cmis:ObJeCtId)", 1, false, "cmis:OBJECTID", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("select o.*, t.* from ( cm:ownable o join cm:titled t on (o.cmis:objectid = t.cmis:objectid) )", 1, false, "cmis:objectid", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("select o.*, t.* from ( cm:ownable o join cm:titled t on (o.cmis:objectid = t.cmis:objectid)  JOIN CMIS:DOCUMENT AS D ON (D.cmis:objectid = o.cmis:objectid ) )", 1, false, "cmis:objectid",
                new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery(
                "select o.*, t.* from ( cm:ownable o join cm:titled t on (o.cmis:objectid = t.cmis:objectid)  JOIN CMIS:DOCUMENT AS D ON (D.cmis:objectid = o.cmis:objectid ) ) where o.cm:owner = 'andy' and t.cm:title = 'Alfresco tutorial' and CONTAINS(D, '\"jumped\"') and 2 <> D.cmis:ContentStreamLength",
                1, false, "cmis:objectid", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery(
                "select o.*, t.* from ( cm:ownable o join cm:titled t on (o.cmis:objectid = t.cmis:objectid)  JOIN CMIS:DOCUMENT AS D ON (D.cmis:objectid = o.cmis:objectid ) ) where o.cm:owner = 'andy' and t.cm:title = 'Alfresco tutorial' and CONTAINS(D, 'jumped') and 2 <> D.cmis:ContentStreamLength",
                1, false, "cmis:objectid", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
    }

    public void testPaging()
    {

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:folder", rootNodeRef.getStoreRef());
        List<String> expected = new ArrayList<String>(10);

        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(10, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:ObjectId");
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
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:folder", rootNodeRef.getStoreRef());
        options.setSkipCount(skip);
        options.setMaxItems(max);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals("Skip = "+skip+ " max  = "+max, skip+max > 10 ? 10 - skip : max, rs.getLength());
        assertEquals("Skip = "+skip+ " max  = "+max, (skip+max) < 10, rs.hasMore());
        assertEquals("Skip = "+skip+ " max  = "+max, skip, rs.getStart());
        int actualPosition = skip;
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:ObjectId");
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
