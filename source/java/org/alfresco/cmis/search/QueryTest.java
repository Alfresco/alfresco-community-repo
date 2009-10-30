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
import org.alfresco.cmis.CMISPropertyDefinition;
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
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
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
    
    private <T> T testExtendedQuery(String query, int size, boolean dump, String returnPropertyName, T returnType, boolean shouldThrow)
    {
        return testQuery(query, size, dump, returnPropertyName, returnType, shouldThrow, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
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
                            + row.getValue("cmis:objectId") + " " + ((returnPropertyName != null) ? (returnPropertyName + "=" + row.getValue(returnPropertyName)) : "") + " Score="
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
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds =  'test'", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds <> 'test'", 10, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds <  'test'", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds <= 'test'", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds >  'test'", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds >= 'test'", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds IN     ('test')", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds NOT IN ('test')", 10, false, "cmis:allowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds     LIKE 'test'", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds NOT LIKE 'test'", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds IS NOT NULL", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE cmis:allowedChildObjectTypeIds IS     NULL", 10, false, "cmis:allowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE 'test' =  ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE 'test' <> ANY cmis:allowedChildObjectTypeIds", 10, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE 'test' <  ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE 'test' <= ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE 'test' >  ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE 'test' >= ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE ANY cmis:allowedChildObjectTypeIds IN     ('test')", 0, false, "cmis:allowedChildObjectTypeIds", new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM Folder WHERE ANY cmis:allowedChildObjectTypeIds NOT IN ('test')", 10, false, "cmis:allowedChildObjectTypeIds", new String(), true);
    }

    public void test_PARENT()
    {
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId =  '" + rootNodeRef.toString() + "'", 4, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId <> '" + rootNodeRef.toString() + "'", 6, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId <  '" + rootNodeRef.toString() + "'", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId <= '" + rootNodeRef.toString() + "'", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId >  '" + rootNodeRef.toString() + "'", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId >= '" + rootNodeRef.toString() + "'", 0, false, "cmis:parentId", new String(), true);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId IN     ('" + rootNodeRef.toString() + "')", 4, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId NOT IN ('" + rootNodeRef.toString() + "')", 6, false, "cmis:parentId", new String(), false);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId     LIKE '" + rootNodeRef.toString() + "'", 4, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId NOT LIKE '" + rootNodeRef.toString() + "'", 6, false, "cmis:parentId", new String(), false);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId IS NOT NULL", 10, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId IS     NULL", 0, false, "cmis:parentId", new String(), false);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' =  ANY cmis:parentId", 4, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' <> ANY cmis:parentId", 6, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' <  ANY cmis:parentId", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' <= ANY cmis:parentId", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' >  ANY cmis:parentId", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' >= ANY cmis:parentId", 0, false, "cmis:parentId", new String(), true);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE ANY cmis:parentId IN     ('" + rootNodeRef.toString() + "')", 4, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE ANY cmis:parentId NOT IN ('" + rootNodeRef.toString() + "')", 6, false, "cmis:parentId", new String(), false);
    }

    public void test_CONTENT_STREAM_FILENAME()
    {
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'Alfresco Tutorial'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'AA'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'BB'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'CC'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'DD'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'EE'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'FF'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'GG'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'HH'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'aa'", 1, false, "cmis:contentStreamFileName", new String(), false);
        
        
        
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'Alfresco Tutorial'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName <> 'Alfresco Tutorial'", 9, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName <  'Alfresco Tutorial'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName <= 'Alfresco Tutorial'", 2, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName >  'Alfresco Tutorial'", 8, true, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName >= 'Alfresco Tutorial'", 9, false, "cmis:contentStreamFileName", new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName IN     ('Alfresco Tutorial')", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName NOT IN ('Alfresco Tutorial')", 9, false, "cmis:contentStreamFileName", new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName     LIKE 'Alfresco Tutorial'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName NOT LIKE 'Alfresco Tutorial'", 9, false, "cmis:contentStreamFileName", new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName IS NOT NULL", 10, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName IS     NULL", 0, false, "cmis:contentStreamFileName", new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' =  ANY cmis:contentStreamFileName", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' <> ANY cmis:contentStreamFileName", 9, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' <  ANY cmis:contentStreamFileName", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' <= ANY cmis:contentStreamFileName", 2, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' >  ANY cmis:contentStreamFileName", 8, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' >= ANY cmis:contentStreamFileName", 9, false, "cmis:contentStreamFileName", new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE ANY cmis:contentStreamFileName IN     ('Alfresco Tutorial')", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE ANY cmis:contentStreamFileName NOT IN ('Alfresco Tutorial')", 9, false, "cmis:contentStreamFileName", new String(), false);
    }

    public void test_CONTENT_STREAM_MIME_TYPE()
    {
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType =  'text/plain'", 10, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType <> 'text/plain'", 0, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType <  'text/plain'", 0, true, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType <= 'text/plain'", 10, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType >  'text/plain'", 0, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType >= 'text/plain'", 10, false, "cmis:contentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType IN     ('text/plain')", 10, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType NOT IN ('text/plain')", 0, false, "cmis:contentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType     LIKE 'text/plain'", 10, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType NOT LIKE 'text/plain'", 0, false, "cmis:contentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType IS NOT NULL", 10, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType IS     NULL", 0, false, "cmis:contentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' =  ANY cmis:contentStreamMimeType", 10, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' <> ANY cmis:contentStreamMimeType", 0, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' <  ANY cmis:contentStreamMimeType", 0, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' <= ANY cmis:contentStreamMimeType", 10, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' >  ANY cmis:contentStreamMimeType", 0, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' >= ANY cmis:contentStreamMimeType", 10, false, "cmis:contentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE ANY cmis:contentStreamMimeType IN     ('text/plain')", 10, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE ANY cmis:contentStreamMimeType NOT IN ('text/plain')", 0, false, "cmis:contentStreamMimeType", new String(), false);
    }

    public void test_CONTENT_STREAM_LENGTH()
    {
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength =  750", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength <> 750", 10, true, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength <  750", 10, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength <= 750", 10, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength >  750", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength >= 750", 0, false, "cmis:contentStreamLength", new String(), false);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength IN     (750)", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength NOT IN (750)", 10, false, "cmis:contentStreamLength", new String(), false);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength     LIKE '750'", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength NOT LIKE '750'", 10, false, "cmis:contentStreamLength", new String(), false);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength IS NOT NULL", 10, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength IS     NULL", 0, false, "cmis:contentStreamLength", new String(), false);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 =  ANY cmis:contentStreamLength", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 <> ANY cmis:contentStreamLength", 10, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 <  ANY cmis:contentStreamLength", 10, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 <= ANY cmis:contentStreamLength", 10, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 >  ANY cmis:contentStreamLength", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 >= ANY cmis:contentStreamLength", 0, false, "cmis:contentStreamLength", new String(), false);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE ANY cmis:contentStreamLength IN     (750)", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE ANY cmis:contentStreamLength NOT IN (750)", 10, false, "cmis:contentStreamLength", new String(), false);
    }

    public void test_CHECKIN_COMMENT()
    {
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment =  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment <> 'admin'", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment <  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment <= 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment >  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment >= 'admin'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment NOT IN ('admin')", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment     LIKE 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment NOT LIKE 'admin'", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment IS     NULL", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' =  ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' <> ANY cmis:checkinComment", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' <  ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' <= ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' >  ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' >= ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE ANY cmis:checkinComment IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE ANY cmis:checkinComment NOT IN ('admin')", 10, false, "cmis:objectId", new String(), true);
    }

    public void test_VERSION_SERIES_CHECKED_OUT_ID()
    {
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId =  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId <> 'admin'", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId <  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId <= 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId >  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId >= 'admin'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId NOT IN ('admin')", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId     LIKE 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId NOT LIKE 'admin'", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId IS     NULL", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' =  ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' <> ANY cmis:versionSeriesCheckedOutId", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' <  ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' <= ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' >  ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' >= ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE ANY cmis:versionSeriesCheckedOutId IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE ANY cmis:versionSeriesCheckedOutId NOT IN ('admin')", 10, false, "cmis:objectId", new String(), true);
    }

    public void test_VERSION_SERIES_CHECKED_OUT_BY()
    {
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy =  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy <> 'admin'", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy <  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy <= 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy >  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy >= 'admin'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy NOT IN ('admin')", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy     LIKE 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy NOT LIKE 'admin'", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy IS     NULL", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' =  ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' <> ANY cmis:versionSeriesCheckedOutBy", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' <  ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' <= ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' >  ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' >= ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE ANY cmis:versionSeriesCheckedOutBy IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE ANY cmis:versionSeriesCheckedOutBy NOT IN ('admin')", 10, false, "cmis:objectId", new String(), true);
    }

    public void test_VERSION_SERIES_IS_CHECKED_OUT()
    {
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut =  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut <> 'TRUE'", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut <  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut <= 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut >  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut >= 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut NOT IN ('TRUE')", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut     LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut NOT LIKE 'TRUE'", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut IS     NULL", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' =  ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' <> ANY cmis:isVeriesSeriesCheckedOut", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' <  ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' <= ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' >  ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE 'TRUE' >= ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE ANY cmis:isVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVeriesSeriesCheckedOut FROM cmis:document WHERE ANY cmis:isVeriesSeriesCheckedOut NOT IN ('TRUE')", 10, false, "cmis:objectId", new String(), true);
    }

    public void test_VERSION_SERIES_ID()
    {
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId =  'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId <> 'company'", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId <  'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId <= 'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId >  'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId >= 'company'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId IN     ('company')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId NOT IN ('company')", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId     LIKE 'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId NOT LIKE 'company'", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId IS     NULL", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' =  ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' <> ANY cmis:versionSeriesId", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' <  ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' <= ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' >  ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' >= ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE ANY cmis:versionSeriesId IN     ('company')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE ANY cmis:versionSeriesId NOT IN ('company')", 10, false, "cmis:objectId", new String(), true);
    }

    public void test_VERSION_LABEL()
    {
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel =  'company'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel <> 'company'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel <  'company'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel <= 'company'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel >  'company'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel >= 'company'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel IN     ('company')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel NOT IN ('company')", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel     LIKE 'company'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel NOT LIKE 'company'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel IS NOT NULL", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel IS     NULL", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' =  ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' <> ANY cmis:versionLabel", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' <  ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' <= ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' >  ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' >= ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE ANY cmis:versionLabel IN     ('company')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE ANY cmis:versionLabel NOT IN ('company')", 10, false, "cmis:objectId", new String(), false);
    }

    public void test_IS_LATEST_MAJOR_VERSION()
    {
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion =  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion <> 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion <  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion <= 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion >  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion >= 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion NOT IN ('TRUE')", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion     LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion NOT LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE cmis:isLatestMajorVersion IS     NULL", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE 'TRUE' =  ANY cmis:isLatestMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE 'TRUE' <> ANY cmis:isLatestMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE 'TRUE' <  ANY cmis:isLatestMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE 'TRUE' <= ANY cmis:isLatestMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE 'TRUE' >  ANY cmis:isLatestMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE 'TRUE' >= ANY cmis:isLatestMajorVersion", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE ANY cmis:isLatestMajorVersion IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document WHERE ANY cmis:isLatestMajorVersion NOT IN ('TRUE')", 0, false, "cmis:objectId", new String(), true);
    }

    public void test_IS_MAJOR_VERSION()
    {
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion =  TRUE", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion =  true", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion =  FALSE", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion =  false", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion <> 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion <  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion <= 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion >  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion >= 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion NOT IN ('TRUE')", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion     LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion NOT LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE cmis:isMajorVersion IS     NULL", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE 'TRUE' =  ANY cmis:isMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE 'TRUE' <> ANY cmis:isMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE 'TRUE' <  ANY cmis:isMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE 'TRUE' <= ANY cmis:isMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE 'TRUE' >  ANY cmis:isMajorVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE 'TRUE' >= ANY cmis:isMajorVersion", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE ANY cmis:isMajorVersion IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isMajorVersion FROM cmis:document WHERE ANY cmis:isMajorVersion NOT IN ('TRUE')", 0, false, "cmis:objectId", new String(), true);
    }

    public void test_IS_LATEST_VERSION()
    {
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion =  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion <> 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion <  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion <= 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion >  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion >= 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion NOT IN ('TRUE')", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion     LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion NOT LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE cmis:isLatestVersion IS     NULL", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE 'TRUE' =  ANY cmis:isLatestVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE 'TRUE' <> ANY cmis:isLatestVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE 'TRUE' <  ANY cmis:isLatestVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE 'TRUE' <= ANY cmis:isLatestVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE 'TRUE' >  ANY cmis:isLatestVersion", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE 'TRUE' >= ANY cmis:isLatestVersion", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE ANY cmis:isLatestVersion IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document WHERE ANY cmis:isLatestVersion NOT IN ('TRUE')", 0, false, "cmis:objectId", new String(), true);
    }

    public void test_IS_IMMUTABLE()
    {
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable =  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable <> 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable <  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable <= 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable >  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable >= 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable NOT IN ('TRUE')", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable     LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable NOT LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE cmis:isImmutable IS     NULL", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE 'TRUE' =  ANY cmis:isImmutable", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE 'TRUE' <> ANY cmis:isImmutable", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE 'TRUE' <  ANY cmis:isImmutable", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE 'TRUE' <= ANY cmis:isImmutable", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE 'TRUE' >  ANY cmis:isImmutable", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE 'TRUE' >= ANY cmis:isImmutable", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE ANY cmis:isImmutable IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isImmutable FROM cmis:document WHERE ANY cmis:isImmutable NOT IN ('TRUE')", 0, false, "cmis:objectId", new String(), true);
    }

    public void test_folder_NAME()
    {
        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name =  'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name <> 'Folder 1'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name <  'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name <= 'Folder 1'", 2, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name >  'Folder 1'", 8, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name >= 'Folder 1'", 9, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name IN     ('Folder 1')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name NOT IN ('Folder 1')", 9, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name     LIKE 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name NOT LIKE 'Folder 1'", 9, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE cmis:name IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' =  ANY cmis:name", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' <> ANY cmis:name", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' <  ANY cmis:name", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' <= ANY cmis:name", 2, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' >  ANY cmis:name", 8, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' >= ANY cmis:name", 9, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:folder WHERE ANY cmis:name IN     ('Folder 1')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE ANY cmis:name NOT IN ('Folder 1')", 9, false, "cmis:objectId", new String(), false);
    }

    public void test_document_Name()
    {
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name =  'Alfresco Tutorial'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name <> 'Alfresco Tutorial'", 9, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name <  'Alfresco Tutorial'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name <= 'Alfresco Tutorial'", 2, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name >  'Alfresco Tutorial'", 8, true, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name >= 'Alfresco Tutorial'", 9, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name IN     ('Alfresco Tutorial')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name NOT IN ('Alfresco Tutorial')", 9, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco Tutorial'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name NOT LIKE 'Alfresco Tutorial'", 9, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name IS NOT NULL", 10, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name IS     NULL", 0, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' =  ANY cmis:name", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' <> ANY cmis:name", 9, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' <  ANY cmis:name", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' <= ANY cmis:name", 2, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' >  ANY cmis:name", 8, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' >= ANY cmis:name", 9, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE ANY cmis:name IN     ('Alfresco Tutorial')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE ANY cmis:name NOT IN ('Alfresco Tutorial')", 9, false, "cmis:name", new String(), false);
    }

    public void test_CHANGE_TOKEN()
    {
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken =  'test'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken <> 'test'", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken <  'test'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken <= 'test'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken >  'test'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken >= 'test'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken IN     ('test')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken NOT IN ('test')", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken     LIKE 'test'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken NOT LIKE 'test'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE cmis:changeToken IS     NULL", 10, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE 'test' =  ANY cmis:changeToken", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE 'test' <> ANY cmis:changeToken", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE 'test' <  ANY cmis:changeToken", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE 'test' <= ANY cmis:changeToken", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE 'test' >  ANY cmis:changeToken", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE 'test' >= ANY cmis:changeToken", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE ANY cmis:changeToken IN     ('test')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:changeToken FROM cmis:folder WHERE ANY cmis:changeToken NOT IN ('test')", 10, false, "cmis:objectId", new String(), true);
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

        Date date = testQuery("SELECT cmis:lastModificationDate FROM cmis:document", -1, false, "cmis:lastModificationDate", new Date(), false);
        today.setTime(date);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));

        String sDate = df.format(today.getTime());
        String sDate2 = sDate.substring(0, sDate.length()-1) + "+00:00";

        // Today (assuming al ws created today)

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate = TIMESTAMP '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate = TIMESTAMP '" + sDate2 + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <> '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <= '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >= '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IN     ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT IN ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate     LIKE '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:lastModificationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:lastModificationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:lastModificationDate", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate IN     ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "') order by cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <> '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >  '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >= '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT IN ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT LIKE '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:lastModificationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:lastModificationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:lastModificationDate", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <> '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <  '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <= '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT IN ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT LIKE '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:lastModificationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:lastModificationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:lastModificationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);

    }

    public void test_LAST_MODIFIED_BY()
    {
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy =  'System'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy <> 'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy <  'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy <= 'System'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy >  'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy >= 'System'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy IN     ('System')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy NOT IN ('System')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy     LIKE 'System'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy NOT LIKE 'System'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' =  ANY cmis:lastModifiedBy", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' <> ANY cmis:lastModifiedBy", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' <  ANY cmis:lastModifiedBy", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' <= ANY cmis:lastModifiedBy", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' >  ANY cmis:lastModifiedBy", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' >= ANY cmis:lastModifiedBy", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE ANY cmis:lastModifiedBy IN     ('System')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE ANY cmis:lastModifiedBy NOT IN ('System')", 0, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE ANY cmis:lastModifiedBy NOT IN ('System') order by cmis:lastModifiedBy", 0, false, "cmis:objectId", new String(), false);
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

        Date date = testQuery("SELECT cmis:creationDate FROM cmis:document", -1, false, "cmis:creationDate", new Date(), false);
        today.setTime(date);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));

        String sDate = df.format(today.getTime());

        // Today (assuming al ws created today)

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate =  '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <> '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <= '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >= '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IN     ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT IN ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate     LIKE '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:creationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:creationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:creationDate", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate IN     ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "') order by cmis:creationDate", 0, false, "cmis:objectId", new String(), false);

        // using yesterday

        date = Duration.subtract(date, new Duration("P1D"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(yesterday.getTime());

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <> '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >  '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >= '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT IN ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT LIKE '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:creationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:creationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:creationDate", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);

        // using tomorrow

        date = Duration.add(date, new Duration("P2D"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(date);
        tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        sDate = df.format(tomorrow.getTime());

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <> '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <  '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <= '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT IN ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT LIKE '" + sDate + "'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:creationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:creationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:creationDate", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "')", 10, false, "cmis:objectId", new String(), false);

    }

    public void test_CREATED_BY()
    {
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy =  'System'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy <> 'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy <  'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy <= 'System'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy >  'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy >= 'System'", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy IN     ('System')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy NOT IN ('System')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy     LIKE 'System'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy NOT LIKE 'System'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' =  ANY cmis:createdBy", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' <> ANY cmis:createdBy", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' <  ANY cmis:createdBy", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' <= ANY cmis:createdBy", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' >  ANY cmis:createdBy", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' >= ANY cmis:createdBy", 10, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE ANY cmis:createdBy IN     ('System')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE ANY cmis:createdBy NOT IN ('System')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE ANY cmis:createdBy IN     ('System') order by cmis:createdBy", 10, false, "cmis:objectId", new String(), false);
        
    }

    public void test_OBJECT_TYPE_ID()
    {
        // DOC

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId =  'cmis:document'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId <> 'cmis:document'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId <  'cmis:document'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId <= 'cmis:document'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId >  'cmis:document'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId >= 'cmis:document'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId IN     ('cmis:document')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId NOT IN ('cmis:document')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId     LIKE 'cmis:document'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId NOT LIKE 'cmis:document'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' =  ANY cmis:objectTypeId", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' <> ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' <  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' <= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' >  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' >= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE ANY cmis:objectTypeId IN     ('cmis:document')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE ANY cmis:objectTypeId NOT IN ('cmis:document')", 0, false, "cmis:objectId", new String(), false);

        // FOLDER

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId =  'cmis:folder'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId <> 'cmis:folder'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId <  'cmis:folder'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId <= 'cmis:folder'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId >  'cmis:folder'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId >= 'cmis:folder'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId IN     ('cmis:folder')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId NOT IN ('cmis:folder')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId     LIKE 'cmis:folder'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId NOT LIKE 'cmis:folder'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' =  ANY cmis:objectTypeId", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' <> ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' <  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' <= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' >  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' >= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE ANY cmis:objectTypeId IN     ('cmis:folder')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE ANY cmis:objectTypeId NOT IN ('cmis:folder')", 0, false, "cmis:objectId", new String(), false);

        // RELATIONSHIP

        testQuery("SELECT cmis:objectTypeId FROM Relationship WHERE cmis:objectTypeId =  ''", 1, false, "cmis:objectId", new String(), true);

    }

    public void test_ObjectId()
    {
        String companyHomeId = testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:name = 'Folder 0'", 1, false, "cmis:objectId", new String(), false);

        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        assertEquals(companyHomeId, id);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId =  '" + companyHomeId + "'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId <> '" + companyHomeId + "'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId <  '" + companyHomeId + "'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId <= '" + companyHomeId + "'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId >  '" + companyHomeId + "'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId >= '" + companyHomeId + "'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId IN     ('" + companyHomeId + "')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId NOT IN ('" + companyHomeId + "')", 9, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId     LIKE '" + companyHomeId + "'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId NOT LIKE '" + companyHomeId + "'", 9, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE IN_FOLDER('" + companyHomeId + "')", 2, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE IN_TREE  ('" + companyHomeId + "')", 6, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' =  ANY cmis:objectId", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' <> ANY cmis:objectId", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' <  ANY cmis:objectId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' <= ANY cmis:objectId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' >  ANY cmis:objectId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' >= ANY cmis:objectId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE ANY cmis:objectId IN     ('" + companyHomeId + "')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE ANY cmis:objectId NOT IN ('" + companyHomeId + "')", 9, false, "cmis:objectId", new String(), false);
    }

    public void testOrderBy()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT  cmis:objectId FROM cmis:folder ORDER BY cmis:objectId", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        options = new CMISQueryOptions("SELECT  cmis:objectId FROM cmis:folder ORDER BY cmis:objectId ASC", rootNodeRef.getStoreRef());
        rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        options = new CMISQueryOptions("SELECT  cmis:objectId FROM cmis:folder ORDER BY cmis:objectId DESC", rootNodeRef.getStoreRef());
        rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;
        
        options = new CMISQueryOptions("SELECT  D.cmis:objectId FROM cmis:folder D ORDER BY D.cmis:objectId DESC", rootNodeRef.getStoreRef());
        rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        options = new CMISQueryOptions("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder ORDER BY MEEP ASC", rootNodeRef.getStoreRef());
        rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;
        
        options = new CMISQueryOptions("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder ORDER BY MEEP ASC", rootNodeRef.getStoreRef());
        rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        options = new CMISQueryOptions("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder ORDER BY MEEP DESC", rootNodeRef.getStoreRef());
        rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder WHERE cmis:name = 'compan home') ORDER BY SCORE() DESC", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder WHERE cmis:name IN ('company', 'home') ORDER BY MEEEP DESC", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder WHERE cmis:name IN ('company', 'home') ORDER BY cmis:parentId DESC", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId, cmis:parentId FROM cmis:folder  ORDER BY cmis:parentId DESC", folder_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder WHERE cmis:name IN ('company', 'home') ORDER BY cmis:notThere DESC", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder as F WHERE cmis:name IN ('company', 'home') ORDER BY F.cmis:parentId DESC", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder F WHERE cmis:name IN ('company', 'home') ORDER BY F.cmis:notThere DESC", 1, false, "cmis:objectId", new String(), true);
        
    }
    
    public void testUpperAndLower()
    {
        testExtendedQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'FOLDER 1'", 0, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'folder 1'", 0, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:name) = 'FOLDER 1'", 1, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Lower(cmis:name) = 'folder 1'", 1, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:name) = 'folder 1'", 0, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Lower(cmis:name) = 'FOLDER 1'", 0, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:name) = 'Folder 1'", 0, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Lower(cmis:name) = 'Folder 1'", 0, false, "cmis:objectId", new String(), false);
        
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:name) <> 'FOLDER 1'", 9, false, "cmis:objectId", new String(), false);
        
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:name) <= 'FOLDER 1'", 2, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:name) < 'FOLDER 1'", 1, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:name) >= 'FOLDER 1'", 9, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:name) > 'FOLDER 1'", 8, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT * FROM cmis:folder WHERE Upper(cmis:name) > 'FOLDER 1'", 8, false, "cmis:objectId", new String(), true);
    }

    public void testAllSimpleTextPredicates()
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name = 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND NOT cmis:name = 'Folder 1'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND 'Folder 1' = ANY cmis:name", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND NOT cmis:name <> 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name <> 'Folder 1'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name < 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name <= 'Folder 1'", 2, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name > 'Folder 1'", 8, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name >= 'Folder 1'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name IN ('Folder 1', '1')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name NOT IN ('Folder 1', 'Folder 9')", 8, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND ANY cmis:name IN ('Folder 1', 'Folder 9')", 2, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND ANY cmis:name NOT IN ('2', '3')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'Fol%'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'F_l_e_ 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name NOT LIKE 'F_l_e_ 1'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'F_l_e_ %'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name NOT LIKE 'F_l_e_ %'", 0, false, "cmis:objectId", new String(), false);
        // TODO: Fix below which fail??
        //testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'F_l_e_ _'", 10, false, "cmis:objectId", new String(), false);
        //testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name NOT LIKE 'F_l_e_ _'", 0, false, "cmis:objectId", new String(), false);
    }

    public void testSimpleConjunction()
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name = 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name = 'Folder'", 0, false, "cmis:objectId", new String(), false);
    }

    public void testSimpleDisjunction()
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'Folder 2'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'Folder 1' OR cmis:name = 'Folder 2'", 2, false, "cmis:objectId", new String(), false);
    }

    public void testExists()
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NULL", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE cmis:name IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE cmis:name IS NULL", 0, false, "cmis:objectId", new String(), false);
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

        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = '" + Name + "'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:parentId = '" + rootNodeRef.toString() + "'", 4, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:allowedChildObjectTypeIds = 'meep'", 0, false, "cmis:objectId", new String(), true);
    }

    public void test_IN_TREE()
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM cmis:folder WHERE IN_TREE('" + id + "')", 6, false, "cmis:objectId", new String(), false);
    }

    public void test_IN_FOLDER()
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM cmis:folder WHERE IN_FOLDER('" + id + "')", 2, false, "cmis:objectId", new String(), false);
    }

    public void testFTS()
    {
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\"zebra\"')", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\"quick\"')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('TEXT:\"quick\"')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document D WHERE CONTAINS(D, 'cmis:name:\"Tutorial\"')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:name as BOO FROM cmis:document D WHERE CONTAINS('BOO:\"Tutorial\"')", 1, false, "cmis:objectId", new String(), false);
    }

    public void testBasicSelectAsGuest()
    {
        runAs("guest");
        testQuery("SELECT * FROM cmis:document", 0, false, "cmis:objectId", new String(), false);

    }
    
    public void testBasicSelectAsCmis()
    {
        runAs("cmis");
        testQuery("SELECT * FROM cmis:document", 7, false, "cmis:objectId", new String(), false);

    }

    public void testBasicSelect()
    {
        testQuery("SELECT * FROM cmis:document", 10, false, "cmis:objectId", new String(), false);
    }

    public void testBasicDefaultMetaData()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        CMISTypeDefinition typeDef = cmisDictionaryService.findType(CMISDictionaryModel.DOCUMENT_TYPE_ID);
        int count = 0;
        for(CMISPropertyDefinition pdef : typeDef.getPropertyDefinitions().values())
        {
            if(pdef.isQueryable())
            {
                count++;
            }
        }
        assertEquals(count, md.getColumnNames().length);
        assertNotNull(md.getColumn(CMISDictionaryModel.PROP_OBJECT_ID));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector(""));
        rs.close();
    }

    public void testBasicMetaData()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT DOC.cmis:objectId, DOC.cmis:objectId AS ID FROM cmis:document AS DOC", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(2, md.getColumnNames().length);
        assertNotNull(md.getColumn("DOC.cmis:objectId"));
        assertNotNull(md.getColumn("ID"));
        assertEquals(1, md.getSelectors().length);
        assertNotNull(md.getSelector("DOC"));
        rs.close();
    }

    public void testBasicColumns()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT DOC.cmis:objectId, DOC.cmis:objectTypeId AS ID FROM cmis:folder AS DOC", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(2, md.getColumnNames().length);
        assertNotNull(md.getColumn("DOC.cmis:objectId"));
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
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:folder AS DOC", cmisService.getDefaultRootStoreRef());
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
        CMISQueryOptions options = new CMISQueryOptions("SELECT *  FROM ST:SITES AS DOC", cmisService.getDefaultRootStoreRef());
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
                "SELECT DOC.cmis:name AS cmis:name, \nLOWER(\tDOC.cmis:name \n), LOWER ( DOC.cmis:name )  AS Lcmis:name, UPPER ( DOC.cmis:name ) , UPPER(DOC.cmis:name) AS Ucmis:name, Score(), SCORE() AS S1, SCORE() AS SCORED FROM cmis:folder AS DOC",
                rootNodeRef.getStoreRef());
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        CMISResultSet rs = cmisQueryService.query(options);

        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        assertEquals(8, md.getColumnNames().length);
        assertNotNull(md.getColumn("cmis:name"));
        assertNotNull(md.getColumn("LOWER(\tDOC.cmis:name \n)"));
        assertNotNull(md.getColumn("Lcmis:name"));
        assertNotNull(md.getColumn("UPPER ( DOC.cmis:name )"));
        assertNotNull(md.getColumn("Ucmis:name"));
        assertNotNull(md.getColumn("SEARCH_SCORE"));
        assertNotNull(md.getColumn("S1"));
        assertNotNull(md.getColumn("SCORED"));
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
        String query = "SELECT UPPER(1.0) AS WOOF FROM cmis:document AS DOC LEFT OUTER JOIN cmis:folder AS FOLDER ON DOC.cmis:name = FOLDER.cmis:name WHERE LOWER(DOC.cmis:name) = ' woof' AND CONTAINS('one two three') AND  CONTAINS('DOC.cmis:name:lemur AND woof') AND (DOC.cmis:name in ('one', 'two') AND IN_FOLDER('meep') AND DOC.cmis:name like 'woof' and DOC.cmis:name = 'woof' and DOC.cmis:objectId = 'meep') ORDER BY DOC.cmis:name DESC, WOOF";
        parse(query);
    }

    public void testParse2() throws RecognitionException
    {
        String query = "SELECT TITLE, AUTHORS, DATE FROM WHITE_PAPER WHERE ( IN_TREE( 'ID00093854763') ) AND ( 'SMITH' = ANY AUTHORS )";
        parse(query);
    }

    public void testParse3() throws RecognitionException
    {
        String query = "SELECT cmis:objectId, SCORE() AS X, DESTINATION, DEPARTURE_DATES FROM TRAVEL_BROCHURE WHERE ( CONTAINS('CARIBBEAN CENTRAL AMERICA CRUISE TOUR') ) AND ( '2009-1-1' < ANY DEPARTURE_DATES ) ORDER BY X DESC";
        parse(query);
    }

    public void testParse4() throws RecognitionException
    {
        String query = "SELECT * FROM CAR_REVIEW WHERE ( LOWER(MAKE) = 'buick' ) OR ( ANY FEATURES IN ('NAVIGATION SYSTEM', 'SATELLITE RADIO', 'MP3' ) )";
        parse(query);
    }

    public void testParse5() throws RecognitionException
    {
        String query = "SELECT Y.CLAIM_NUM, X.PROPERTY_ADDRESS, Y.DAMAGE_ESTIMATES FROM POLICY AS X JOIN CLAIMS AS Y ON X.POLICY_NUM = Y.POLICY_NUM WHERE ( 100000 <= ANY Y.DAMAGE_ESTIMATES ) AND ( Y.CAUSE NOT LIKE '%Katrina%' )";
        parse(query);
    }

    public void testParse6() throws RecognitionException
    {
        String query = "SELECT * FROM CM_TITLED";
        parse(query);
        query = "SELECT D.*, T.* FROM DOCUMENT AS D JOIN CM_TITLED AS T ON D.OBJECTID = T.OBJECTID";
        parse(query);
        query = "SELECT D.*, T.* FROM CM_TITLED T JOIN DOCUMENT D ON D.OBJECTID = T.OBJECTID";
        parse(query);
    }
    
    public void testParse7() throws RecognitionException
    {
        String query = "SELECT * from DOCUMENT D JOIN DOCUMENT DD ON D.ID = DD.ID ";
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
                "select o.*, t.* from ( cm:ownable o join cm:titled t on o.cmis:objectId = t.cmis:objectId JOIN CMIS:DOCUMENT AS D ON D.cmis:objectId = o.cmis:objectId  ) where o.cm:owner = 'andy' and t.cm:title = 'Alfresco tutorial' and CONTAINS(D, '\"jumped\"') and D.cmis:contentStreamLength <> 2",
                1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        
        testQuery("SELECT * FROM CM:OWNABLE", 1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM CM:OWNABLE where CM:oWNER = 'andy'", 1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM CM:OWNABLE where CM:OWNER = 'bob'", 0, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.*, T.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId JOIN CM:TITLED AS T ON T.cmis:objectId = D.cmis:objectId", 1, false, "cmis:objectId",
                new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CM:OWNABLE O JOIN CMIS:DOCUMENT D ON D.cmis:objectId = O.cmis:objectId", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, F.* FROM CMIS:FOLDER F JOIN CMIS:DOCUMENT D ON D.cmis:objectId = F.cmis:objectId", 0, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT O.*, T.* FROM CM:OWNABLE O JOIN CM:TITLED T ON O.cmis:objectId = T.cmis:objectId", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("select o.*, t.* from cm:ownable o join cm:titled t on o.cmis:objectId = t.cmis:objectId", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("sElEcT o.*, T.* fRoM cM:oWnAbLe o JoIn Cm:TiTlEd T oN o.cmis:objectId = T.cmis:objectId", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("select o.*, t.* from ( cm:ownable o join cm:titled t on o.cmis:objectId = t.cmis:objectId )", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("select o.*, t.* from ( cm:ownable o join cm:titled t on o.cmis:objectId = t.cmis:objectId  JOIN CMIS:DOCUMENT AS D ON D.cmis:objectId = o.cmis:objectId  )", 1, false, "cmis:objectId",
                new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery(
                "select o.*, t.* from ( cm:ownable o join cm:titled t on o.cmis:objectId = t.cmis:objectId JOIN CMIS:DOCUMENT AS D ON D.cmis:objectId = o.cmis:objectId ) where o.cm:owner = 'andy' and t.cm:title = 'Alfresco tutorial' and CONTAINS(D, '\"jumped\"') and D.cmis:contentStreamLength <> 2",
                1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery(
                "select o.*, t.* from ( cm:ownable o join cm:titled t on o.cmis:objectId = t.cmis:objectId JOIN CMIS:DOCUMENT AS D ON D.cmis:objectId = o.cmis:objectId ) where o.cm:owner = 'andy' and t.cm:title = 'Alfresco tutorial' and CONTAINS(D, 'jumped') and D.cmis:contentStreamLength <> 2",
                1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
    }

    public void testPaging()
    {

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:folder", rootNodeRef.getStoreRef());
        List<String> expected = new ArrayList<String>(10);

        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(10, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:objectId");
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

    public void testFTSConnectives()
    {
        testQuery("SELECT * FROM cmis:document where contains('\"one\" and \"zebra\"')", 1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_STRICT);
        testQuery("SELECT * FROM cmis:document where contains('\"one\" or \"zebra\"')", 9, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_STRICT);
        testQuery("SELECT * FROM cmis:document where contains('\"one\" \"zebra\"')", 1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_STRICT);
        testQuery("SELECT * FROM cmis:document where contains('\"one\" and \"zebra\"')", 1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM cmis:document where contains('\"one\" or \"zebra\"')", 9, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM cmis:document where contains('\"one\"  \"zebra\"')", 1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document where contains('\"one\"  \"zebra\"')", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        rs.close();
        
        options = new CMISQueryOptions("SELECT * FROM cmis:document where contains('\"one\"  \"zebra\"')", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        rs = cmisQueryService.query(options);
        assertEquals(9, rs.length());
        rs.close();
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
            Serializable sValue = row.getValue("cmis:objectId");
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
