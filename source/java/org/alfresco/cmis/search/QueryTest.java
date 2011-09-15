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
package org.alfresco.cmis.search;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.cmis.CMISCardinalityEnum;
import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISQueryException;
import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISQueryOptions.CMISQueryMode;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISResultSetColumn;
import org.alfresco.cmis.CMISResultSetMetaData;
import org.alfresco.cmis.CMISResultSetRow;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.dictionary.CMISAbstractDictionaryService;
import org.alfresco.cmis.mapping.BaseCMISTest;
import org.alfresco.cmis.mapping.BaseTypeIdProperty;
import org.alfresco.cmis.mapping.CheckinCommentProperty;
import org.alfresco.cmis.mapping.ContentStreamIdProperty;
import org.alfresco.cmis.mapping.ContentStreamLengthProperty;
import org.alfresco.cmis.mapping.ContentStreamMimetypeProperty;
import org.alfresco.cmis.mapping.DirectProperty;
import org.alfresco.cmis.mapping.FixedValueProperty;
import org.alfresco.cmis.mapping.IsImmutableProperty;
import org.alfresco.cmis.mapping.IsLatestMajorVersionProperty;
import org.alfresco.cmis.mapping.IsLatestVersionProperty;
import org.alfresco.cmis.mapping.IsMajorVersionProperty;
import org.alfresco.cmis.mapping.IsVersionSeriesCheckedOutProperty;
import org.alfresco.cmis.mapping.ObjectIdProperty;
import org.alfresco.cmis.mapping.ObjectTypeIdProperty;
import org.alfresco.cmis.mapping.ParentProperty;
import org.alfresco.cmis.mapping.PathProperty;
import org.alfresco.cmis.mapping.VersionLabelProperty;
import org.alfresco.cmis.mapping.VersionSeriesCheckedOutByProperty;
import org.alfresco.cmis.mapping.VersionSeriesCheckedOutIdProperty;
import org.alfresco.cmis.mapping.VersionSeriesIdProperty;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.repo.search.impl.parsers.CMISLexer;
import org.alfresco.repo.search.impl.parsers.CMISParser;
import org.alfresco.repo.search.impl.parsers.FTSQueryException;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.util.ISO9075;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author andyh
 */
public class QueryTest extends BaseCMISTest
{
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/cmis-query-test";

    
    
    QName typeThatRequiresEncoding = QName.createQName(TEST_NAMESPACE, "type-that-requires-encoding");
    
    QName aspectThatRequiresEncoding = QName.createQName(TEST_NAMESPACE, "aspect-that-requires-encoding");
    
    QName propertyThatRequiresEncoding = QName.createQName(TEST_NAMESPACE, "property-that-requires-encoding");
    
    QName extendedContent = QName.createQName(TEST_NAMESPACE, "extendedContent");

    QName singleTextBoth = QName.createQName(TEST_NAMESPACE, "singleTextBoth");

    QName singleTextUntokenised = QName.createQName(TEST_NAMESPACE, "singleTextUntokenised");

    QName singleTextTokenised = QName.createQName(TEST_NAMESPACE, "singleTextTokenised");

    QName multipleTextBoth = QName.createQName(TEST_NAMESPACE, "multipleTextBoth");

    QName multipleTextUntokenised = QName.createQName(TEST_NAMESPACE, "multipleTextUntokenised");

    QName multipleTextTokenised = QName.createQName(TEST_NAMESPACE, "multipleTextTokenised");

    QName singleMLTextBoth = QName.createQName(TEST_NAMESPACE, "singleMLTextBoth");

    QName singleMLTextUntokenised = QName.createQName(TEST_NAMESPACE, "singleMLTextUntokenised");

    QName singleMLTextTokenised = QName.createQName(TEST_NAMESPACE, "singleMLTextTokenised");

    QName multipleMLTextBoth = QName.createQName(TEST_NAMESPACE, "multipleMLTextBoth");

    QName multipleMLTextUntokenised = QName.createQName(TEST_NAMESPACE, "multipleMLTextUntokenised");

    QName multipleMLTextTokenised = QName.createQName(TEST_NAMESPACE, "multipleMLTextTokenised");

    QName singleFloat = QName.createQName(TEST_NAMESPACE, "singleFloat");

    QName multipleFloat = QName.createQName(TEST_NAMESPACE, "multipleFloat");

    QName singleDouble = QName.createQName(TEST_NAMESPACE, "singleDouble");

    QName multipleDouble = QName.createQName(TEST_NAMESPACE, "multipleDouble");

    QName singleInteger = QName.createQName(TEST_NAMESPACE, "singleInteger");

    QName multipleInteger = QName.createQName(TEST_NAMESPACE, "multipleInteger");

    QName singleLong = QName.createQName(TEST_NAMESPACE, "singleLong");

    QName multipleLong = QName.createQName(TEST_NAMESPACE, "multipleLong");

    QName singleBoolean = QName.createQName(TEST_NAMESPACE, "singleBoolean");

    QName multipleBoolean = QName.createQName(TEST_NAMESPACE, "multipleBoolean");

    QName singleDate = QName.createQName(TEST_NAMESPACE, "singleDate");

    QName multipleDate = QName.createQName(TEST_NAMESPACE, "multipleDate");

    QName singleDatetime = QName.createQName(TEST_NAMESPACE, "singleDatetime");

    QName multipleDatetime = QName.createQName(TEST_NAMESPACE, "multipleDatetime");

    private int content_only_count;
    
    private int doc_count = 0;

    private int folder_count = 0;

    private NodeRef f0;

    private NodeRef f1;

    private NodeRef f2;

    private NodeRef f3;

    private NodeRef f4;

    private NodeRef f5;

    private NodeRef f6;

    private NodeRef f7;

    private NodeRef f8;

    private NodeRef f9;

    private NodeRef c0;

    private NodeRef c1;

    private NodeRef c2;

    private NodeRef c3;

    private NodeRef c4;

    private NodeRef c5;

    private NodeRef c6;

    private NodeRef c7;

    private NodeRef c8;

    private NodeRef c9;
    
    private NodeRef c10;

    private Date date1;

    private Date date2;

    private Date date0;

    private long contentLength0;

    private String contentUrl0;

    private boolean usesDateTimeAnalyser;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        DataTypeDefinition dataType = dictionaryService.getDataType(DataTypeDefinition.DATETIME);
        String analyserClassName = dataType.resolveAnalyserClassName();
        usesDateTimeAnalyser = analyserClassName.equals(DateTimeAnalyser.class.getCanonicalName());

        f0 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 0", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f0, ContentModel.PROP_NAME, "Folder 0");
        folder_count++;

        permissionService.setPermission(f0, "cmis", PermissionService.READ, true);

        f1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 1", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f1, ContentModel.PROP_NAME, "Folder 1");
        folder_count++;

        f2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 2", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f2, ContentModel.PROP_NAME, "Folder 2");
        folder_count++;

        f3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 3", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f3, ContentModel.PROP_NAME, "Folder 3");
        folder_count++;

        f4 = nodeService.createNode(f0, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 4", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f4, ContentModel.PROP_NAME, "Folder 4");
        folder_count++;

        f5 = nodeService.createNode(f0, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 5", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f5, ContentModel.PROP_NAME, "Folder 5");
        folder_count++;

        f6 = nodeService.createNode(f5, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 6", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f6, ContentModel.PROP_NAME, "Folder 6");
        folder_count++;

        f7 = nodeService.createNode(f6, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 7", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f7, ContentModel.PROP_NAME, "Folder 7");
        folder_count++;

        f8 = nodeService.createNode(f7, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 8", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f8, ContentModel.PROP_NAME, "Folder 8");
        folder_count++;

        f9 = nodeService.createNode(f8, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Folder 9", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(f9, ContentModel.PROP_NAME, "Folder 9'");
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
        c0 = nodeService.createNode(f0, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Alfresco Tutorial", namespaceService), ContentModel.TYPE_CONTENT, properties0)
                .getChildRef();
        ContentWriter writer0 = contentService.getWriter(c0, ContentModel.PROP_CONTENT, true);
        writer0.setEncoding("UTF-8");
        writer0.putContent("The quick brown fox jumped over the lazy dog and ate the Alfresco Tutorial, in pdf format, along with the following stop words;  a an and are"
                + " as at be but by for if in into is it no not of on or such that the their then there these they this to was will with: "
                + " and random charcters \u00E0\u00EA\u00EE\u00F0\u00F1\u00F6\u00FB\u00FF");
        contentLength0 = writer0.getSize();
        contentUrl0 = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(c0, ContentModel.PROP_CONTENT)).getContentUrl();
        nodeService.addAspect(c0, ContentModel.ASPECT_TITLED, null);
        nodeService.addAspect(c0, ContentModel.ASPECT_OWNABLE, null);
        nodeService.setProperty(c0, ContentModel.PROP_OWNER, "andy");
        content_only_count++;
        doc_count++;

        Map<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        MLText desc1 = new MLText();
        desc1.addValue(Locale.ENGLISH, "One");
        desc1.addValue(Locale.US, "One");
        properties1.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties1.put(ContentModel.PROP_DESCRIPTION, desc1);
        properties1.put(ContentModel.PROP_TITLE, desc1);
        properties1.put(ContentModel.PROP_NAME, "AA%");
        properties1.put(ContentModel.PROP_CREATED, new Date());
        c1 = nodeService.createNode(f1, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "One", namespaceService), ContentModel.TYPE_CONTENT, properties1).getChildRef();
        ContentWriter writer1 = contentService.getWriter(c1, ContentModel.PROP_CONTENT, true);
        writer1.setEncoding("UTF-8");
        writer1.putContent("One Zebra Apple");
        nodeService.addAspect(c1, ContentModel.ASPECT_TITLED, null);
        content_only_count++;
        doc_count++;

        Map<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        MLText desc2 = new MLText();
        desc2.addValue(Locale.ENGLISH, "Two");
        desc2.addValue(Locale.US, "Two");
        properties2.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties2.put(ContentModel.PROP_DESCRIPTION, desc2);
        properties2.put(ContentModel.PROP_TITLE, desc2);
        properties2.put(ContentModel.PROP_NAME, "BB_");
        properties2.put(ContentModel.PROP_CREATED, new Date());
        c2 = nodeService.createNode(f2, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Two", namespaceService), ContentModel.TYPE_CONTENT, properties2).getChildRef();
        ContentWriter writer2 = contentService.getWriter(c2, ContentModel.PROP_CONTENT, true);
        writer2.setEncoding("UTF-8");
        writer2.putContent("Two Zebra Banana");
        nodeService.addAspect(c2, ContentModel.ASPECT_TITLED, null);
        content_only_count++;
        doc_count++;
        Map<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        MLText desc3 = new MLText();
        desc3.addValue(Locale.ENGLISH, "Three");
        desc3.addValue(Locale.US, "Three");
        properties3.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties3.put(ContentModel.PROP_DESCRIPTION, desc3);
        properties3.put(ContentModel.PROP_TITLE, desc3);
        properties3.put(ContentModel.PROP_NAME, "CC\\");
        properties3.put(ContentModel.PROP_CREATED, new Date());
        c3 = nodeService.createNode(f3, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Three", namespaceService), ContentModel.TYPE_CONTENT, properties3).getChildRef();
        ContentWriter writer3 = contentService.getWriter(c3, ContentModel.PROP_CONTENT, true);
        writer3.setEncoding("UTF-8");
        writer3.putContent("Three Zebra Clementine");
        nodeService.addAspect(c3, ContentModel.ASPECT_TITLED, null);
        content_only_count++;
        doc_count++;

        Map<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        MLText desc4 = new MLText();
        desc4.addValue(Locale.ENGLISH, "Four");
        desc4.addValue(Locale.US, "Four");
        properties4.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties4.put(ContentModel.PROP_DESCRIPTION, desc4);
        properties4.put(ContentModel.PROP_TITLE, desc4);
        properties4.put(ContentModel.PROP_NAME, "DD\'");
        properties4.put(ContentModel.PROP_CREATED, new Date());
        c4 = nodeService.createNode(f4, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Four", namespaceService), ContentModel.TYPE_CONTENT, properties4).getChildRef();
        ContentWriter writer4 = contentService.getWriter(c4, ContentModel.PROP_CONTENT, true);
        writer4.setEncoding("UTF-8");
        writer4.putContent("Four zebra durian");
        nodeService.addAspect(c4, ContentModel.ASPECT_TITLED, null);
        content_only_count++;
        doc_count++;

        Map<QName, Serializable> properties5 = new HashMap<QName, Serializable>();
        MLText desc5 = new MLText();
        desc5.addValue(Locale.ENGLISH, "Five");
        desc5.addValue(Locale.US, "Five");
        properties5.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties5.put(ContentModel.PROP_DESCRIPTION, desc5);
        properties5.put(ContentModel.PROP_TITLE, desc5);
        properties5.put(ContentModel.PROP_NAME, "EE.aa");
        properties5.put(ContentModel.PROP_CREATED, new Date());

        c5 = nodeService.createNode(f5, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Five", namespaceService), ContentModel.TYPE_CONTENT, properties5).getChildRef();
        ContentWriter writer5 = contentService.getWriter(c5, ContentModel.PROP_CONTENT, true);
        writer5.setEncoding("UTF-8");
        writer5.putContent("Five zebra Ebury");
        nodeService.addAspect(c5, ContentModel.ASPECT_TITLED, null);
        content_only_count++;
        doc_count++;

        Map<QName, Serializable> lockProperties = new HashMap<QName, Serializable>();
        lockProperties.put(ContentModel.PROP_EXPIRY_DATE, DefaultTypeConverter.INSTANCE.convert(Date.class, "2012-12-12T12:12:12.012Z"));
        lockProperties.put(ContentModel.PROP_LOCK_OWNER, "andy");
        lockProperties.put(ContentModel.PROP_LOCK_TYPE, "WRITE_LOCK");

        nodeService.addAspect(c5, ContentModel.ASPECT_LOCKABLE, lockProperties);

        Map<QName, Serializable> properties6 = new HashMap<QName, Serializable>();
        MLText desc6 = new MLText();
        desc6.addValue(Locale.ENGLISH, "Six");
        desc6.addValue(Locale.US, "Six");
        properties6.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties6.put(ContentModel.PROP_DESCRIPTION, desc6);
        properties6.put(ContentModel.PROP_TITLE, desc6);
        properties6.put(ContentModel.PROP_NAME, "FF.EE");
        properties6.put(ContentModel.PROP_CREATED, new Date());
        c6 = nodeService.createNode(f6, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Six", namespaceService), ContentModel.TYPE_CONTENT, properties6).getChildRef();
        ContentWriter writer6 = contentService.getWriter(c6, ContentModel.PROP_CONTENT, true);
        writer6.setEncoding("UTF-8");
        writer6.putContent("Six zebra fig");
        nodeService.addAspect(c6, ContentModel.ASPECT_TITLED, null);
        content_only_count++;
        doc_count++;

        Map<QName, Serializable> properties7 = new HashMap<QName, Serializable>();
        MLText desc7 = new MLText();
        desc7.addValue(Locale.ENGLISH, "Seven");
        desc7.addValue(Locale.US, "Seven");
        properties7.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties7.put(ContentModel.PROP_DESCRIPTION, desc7);
        properties7.put(ContentModel.PROP_TITLE, desc7);
        properties7.put(ContentModel.PROP_NAME, "GG*GG");
        properties7.put(ContentModel.PROP_CREATED, new Date());
        c7 = nodeService.createNode(f7, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Seven", namespaceService), ContentModel.TYPE_CONTENT, properties7).getChildRef();
        ContentWriter writer7 = contentService.getWriter(c7, ContentModel.PROP_CONTENT, true);
        writer7.setEncoding("UTF-8");
        writer7.putContent("Seven zebra grapefruit");
        nodeService.addAspect(c7, ContentModel.ASPECT_TITLED, null);
        content_only_count++;
        doc_count++;

        Map<QName, Serializable> properties8 = new HashMap<QName, Serializable>();
        MLText desc8 = new MLText();
        desc8.addValue(Locale.ENGLISH, "Eight");
        desc8.addValue(Locale.US, "Eight");
        properties8.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties8.put(ContentModel.PROP_DESCRIPTION, desc8);
        properties8.put(ContentModel.PROP_TITLE, desc8);
        properties8.put(ContentModel.PROP_NAME, "HH?HH");
        properties8.put(ContentModel.PROP_CREATED, new Date());
        c8 = nodeService.createNode(f8, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Eight", namespaceService), ContentModel.TYPE_CONTENT, properties8).getChildRef();
        ContentWriter writer8 = contentService.getWriter(c8, ContentModel.PROP_CONTENT, true);
        writer8.setEncoding("UTF-8");
        writer8.putContent("Eight zebra jackfruit");
        nodeService.addAspect(c8, ContentModel.ASPECT_TITLED, null);
        content_only_count++;
        doc_count++;

        Map<QName, Serializable> properties9 = new HashMap<QName, Serializable>();
        MLText desc9 = new MLText();
        desc9.addValue(Locale.ENGLISH, "Nine");
        desc9.addValue(Locale.US, "Nine");
        properties9.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties9.put(ContentModel.PROP_DESCRIPTION, desc9);
        properties9.put(ContentModel.PROP_TITLE, desc9);
        properties9.put(ContentModel.PROP_NAME, "aa");
        properties9.put(ContentModel.PROP_CREATED, new Date());
        c9 = nodeService.createNode(f9, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Nine", namespaceService), ContentModel.TYPE_CONTENT, properties9).getChildRef();
        ContentWriter writer9 = contentService.getWriter(c9, ContentModel.PROP_CONTENT, true);
        writer9.setEncoding("UTF-8");
        writer9.putContent("Nine zebra kiwi");
        nodeService.addAspect(c9, ContentModel.ASPECT_TITLED, null);
        content_only_count++;
        doc_count++;
        nodeService.setProperty(c9, ContentModel.PROP_VERSION_LABEL, "label");
        
        Map<QName, Serializable> properties10 = new HashMap<QName, Serializable>();
        MLText desc10 = new MLText();
        desc10.addValue(Locale.ENGLISH, "Ten");
        desc10.addValue(Locale.US, "Ten");
        properties10.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties10.put(ContentModel.PROP_DESCRIPTION, desc10);
        properties10.put(ContentModel.PROP_TITLE, desc10);
        properties10.put(ContentModel.PROP_NAME, "aa-thumb");
        properties10.put(ContentModel.PROP_CREATED, new Date());
        c10 = nodeService.createNode(f9, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Ten", namespaceService), ContentModel.TYPE_DICTIONARY_MODEL, properties10).getChildRef();
        ContentWriter writer10 = contentService.getWriter(c10, ContentModel.PROP_CONTENT, true);
        writer10.setEncoding("UTF-8");
        writer10.putContent("Tem zebra kiwi thumb");
        nodeService.addAspect(c10, ContentModel.ASPECT_TITLED, null);
        doc_count++;
        nodeService.setProperty(c10, ContentModel.PROP_VERSION_LABEL, "label");
    }

    private <T> T testQuery(String query, int size, boolean dump, String returnPropertyName, T returnType, boolean shouldThrow) throws Exception
    {
        return testQuery(query, size, dump, returnPropertyName, returnType, shouldThrow, CMISQueryMode.CMS_STRICT);
    }

    private <T> T testExtendedQuery(String query, int size, boolean dump, String returnPropertyName, T returnType, boolean shouldThrow) throws Exception
    {
        return testQuery(query, size, dump, returnPropertyName, returnType, shouldThrow, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
    }

    @SuppressWarnings("unchecked")
    private <T> T testQuery(String query, int size, boolean dump, String returnPropertyName, T returnType, boolean shouldThrow, CMISQueryMode mode) throws Exception
    {
        CMISResultSet rs = null;
        try
        {
            T returnValue = null;
            CMISQueryOptions options = new CMISQueryOptions(query, rootNodeRef.getStoreRef());
            options.setQueryMode(mode);
            options.setIncludeInTransactionData(true);
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
        catch (FTSQueryException e)
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
        catch (CmisInvalidArgumentException e)
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
        catch (UnsupportedOperationException e)
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

    public void testEncodingOfTypeAndPropertyNames()
    {
        addTypeTestDataModel();
        assertNotNull("Type not found by query name "+ISO9075.encodeSQL(typeThatRequiresEncoding.toPrefixString(namespaceService)), cmisDictionaryService.findTypeByQueryName(ISO9075.encodeSQL(typeThatRequiresEncoding.toPrefixString(namespaceService))));
        assertNotNull("Aspect not found by query name "+ISO9075.encodeSQL(aspectThatRequiresEncoding.toPrefixString(namespaceService)), cmisDictionaryService.findTypeByQueryName(ISO9075.encodeSQL(aspectThatRequiresEncoding.toPrefixString(namespaceService))));
        assertNotNull("Prpo not found by query name "+ISO9075.encodeSQL(propertyThatRequiresEncoding.toPrefixString(namespaceService)), cmisDictionaryService.findPropertyByQueryName(ISO9075.encodeSQL(propertyThatRequiresEncoding.toPrefixString(namespaceService))));
       
    }
    
    public void test_ALLOWED_CHILD_OBJECT_TYPES() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Folder", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:allowedChildObjectTypeIds");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:allowedChildObjectTypeIds");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds =  'test'", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds <> 'test'", 10, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds <  'test'", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds <= 'test'", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds >  'test'", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds >= 'test'", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds IN     ('test')", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds NOT IN ('test')", 10, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds     LIKE 'test'", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds NOT LIKE 'test'", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds IS NOT NULL", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE cmis:allowedChildObjectTypeIds IS     NULL", 10, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE 'test' =  ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE 'test' <> ANY cmis:allowedChildObjectTypeIds", 10, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE 'test' <  ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE 'test' <= ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE 'test' >  ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE 'test' >= ANY cmis:allowedChildObjectTypeIds", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);

        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE ANY cmis:allowedChildObjectTypeIds IN     ('test')", 0, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
        testQuery("SELECT cmis:allowedChildObjectTypeIds FROM cmis:Folder WHERE ANY cmis:allowedChildObjectTypeIds NOT IN ('test')", 10, false, "cmis:allowedChildObjectTypeIds",
                new String(), true);
    }

    public void test_PARENT() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Folder", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:parentId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:parentId");
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ParentProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:folder where cmis:parentId = '" + f8.toString() + "'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:parentId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals(f8.toString(), value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:parentId");
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ParentProperty);
        }
        rs.close();

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId =  '" + rootNodeRef.toString() + "'", 4, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId <> '" + rootNodeRef.toString() + "'", 6, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId <  '" + rootNodeRef.toString() + "'", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId <= '" + rootNodeRef.toString() + "'", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId >  '" + rootNodeRef.toString() + "'", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId >= '" + rootNodeRef.toString() + "'", 0, false, "cmis:parentId", new String(), true);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId IN     ('" + rootNodeRef.toString() + "')", 4, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId NOT IN ('" + rootNodeRef.toString() + "')", 6, false, "cmis:parentId", new String(), false);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId     LIKE '" + rootNodeRef.toString() + "'", 4, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId NOT LIKE '" + rootNodeRef.toString() + "'", 6, false, "cmis:parentId", new String(), true);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId IS NOT NULL", 10, false, "cmis:parentId", new String(), false);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE cmis:parentId IS     NULL", 0, false, "cmis:parentId", new String(), false);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' =  ANY cmis:parentId", 4, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' <> ANY cmis:parentId", 6, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' <  ANY cmis:parentId", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' <= ANY cmis:parentId", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' >  ANY cmis:parentId", 0, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE '" + rootNodeRef.toString() + "' >= ANY cmis:parentId", 0, false, "cmis:parentId", new String(), true);

        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE ANY cmis:parentId IN     ('" + rootNodeRef.toString() + "')", 4, false, "cmis:parentId", new String(), true);
        testQuery("SELECT cmis:parentId FROM cmis:folder WHERE ANY cmis:parentId NOT IN ('" + rootNodeRef.toString() + "')", 6, false, "cmis:parentId", new String(), true);
    }

    public void test_PATH() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:folder", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:path");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:path");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof PathProperty);
        }
        rs.close();

        testQuery("SELECT cmis:path FROM cmis:folder", folder_count, false, "cmis:path", new String(), false);
        testQuery("SELECT cmis:path FROM cmis:folder WHERE cmis:path =  'anything'", folder_count, false, "cmis:path", new String(), true);

    }

    public void test_CONTENT_STREAM_ID() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:contentStreamId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            ContentData cd = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_CONTENT));
            assertEquals(cd.getContentUrl(), value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:contentStreamId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ContentStreamIdProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:document WHERE cmis:contentStreamFileName =  'Alfresco Tutorial'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:contentStreamId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals(contentUrl0, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:contentStreamId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ContentStreamIdProperty);
        }
        rs.close();

        testQuery("SELECT cmis:contentStreamId FROM cmis:document", doc_count, false, "cmis:contentStreamId", new String(), false);

        // not allowed in predicates
        testQuery("SELECT cmis:contentStreamId FROM cmis:document WHERE cmis:contentStreamId =  '" + contentUrl0 + "'", 1, false, "cmis:contentStreamId", new String(), true);

    }

    public void test_CONTENT_STREAM_FILENAME() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:contentStreamFileName");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:contentStreamFileName");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:document WHERE cmis:contentStreamFileName =  'Alfresco Tutorial'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:contentStreamFileName");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals("Alfresco Tutorial", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:contentStreamFileName");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'Alfresco Tutorial'", 1, false, "cmis:contentStreamFileName",
                new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'AA%'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'BB_'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'CC\\\\'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'DD\\''", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'EE.aa'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'FF.EE'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'GG*GG'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'HH?HH'", 1, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'aa'", 1, false, "cmis:contentStreamFileName", new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName =  'Alfresco Tutorial'", 1, false, "cmis:contentStreamFileName",
                new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName <> 'Alfresco Tutorial'", 10, false, "cmis:contentStreamFileName",
                new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName <  'Alfresco Tutorial'", 1, false, "cmis:contentStreamFileName",
                new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName <= 'Alfresco Tutorial'", 2, false, "cmis:contentStreamFileName",
                new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName >  'Alfresco Tutorial'", 9, true, "cmis:contentStreamFileName",
                new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName >= 'Alfresco Tutorial'", 10, false, "cmis:contentStreamFileName",
                new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName IN     ('Alfresco Tutorial')", 1, false, "cmis:contentStreamFileName",
                new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName NOT IN ('Alfresco Tutorial')", 10, false, "cmis:contentStreamFileName",
                new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName     LIKE 'Alfresco Tutorial'", 1, false, "cmis:contentStreamFileName",
                new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName NOT LIKE 'Alfresco Tutorial'", 10, false, "cmis:contentStreamFileName",
                new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName IS NOT NULL", 11, false, "cmis:contentStreamFileName", new String(), false);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE cmis:contentStreamFileName IS     NULL", 0, false, "cmis:contentStreamFileName", new String(), false);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' =  ANY cmis:contentStreamFileName", 1, false, "cmis:contentStreamFileName",
                new String(), true);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' <> ANY cmis:contentStreamFileName", 11, false, "cmis:contentStreamFileName",
                new String(), true);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' <  ANY cmis:contentStreamFileName", 1, false, "cmis:contentStreamFileName",
                new String(), true);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' <= ANY cmis:contentStreamFileName", 2, false, "cmis:contentStreamFileName",
                new String(), true);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' >  ANY cmis:contentStreamFileName", 9, false, "cmis:contentStreamFileName",
                new String(), true);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE 'Alfresco Tutorial' >= ANY cmis:contentStreamFileName", 11, false, "cmis:contentStreamFileName",
                new String(), true);

        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE ANY cmis:contentStreamFileName IN     ('Alfresco Tutorial')", 1, false, "cmis:contentStreamFileName",
                new String(), true);
        testQuery("SELECT cmis:contentStreamFileName FROM cmis:document WHERE ANY cmis:contentStreamFileName NOT IN ('Alfresco Tutorial')", 10, false, "cmis:contentStreamFileName",
                new String(), true);
    }

    public void test_CONTENT_STREAM_MIME_TYPE() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:contentStreamMimeType");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:contentStreamMimeType");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ContentStreamMimetypeProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:document WHERE cmis:contentStreamMimeType =  'text/plain'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertTrue(rs.length() > 0);
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:contentStreamMimeType");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals("text/plain", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:contentStreamMimeType");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ContentStreamMimetypeProperty);
        }
        rs.close();

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType =  'text/plain'", doc_count, false, "cmis:contentStreamMimeType", new String(),
                false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType <> 'text/plain'", 0, false, "cmis:contentStreamMimeType", new String(),
                false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType <  'text/plain'", 0, true, "cmis:contentStreamMimeType", new String(),
                false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType <= 'text/plain'", doc_count, false, "cmis:contentStreamMimeType", new String(),
                false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType >  'text/plain'", 0, false, "cmis:contentStreamMimeType", new String(),
                false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType >= 'text/plain'", doc_count, false, "cmis:contentStreamMimeType", new String(),
                false);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType IN     ('text/plain')", doc_count, false, "cmis:contentStreamMimeType",
                new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType NOT IN ('text/plain')", 0, false, "cmis:contentStreamMimeType",
                new String(), false);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType     LIKE 'text/plain'", doc_count, false, "cmis:contentStreamMimeType",
                new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType NOT LIKE 'text/plain'", 0, false, "cmis:contentStreamMimeType",
                new String(), false);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType IS NOT NULL", doc_count, false, "cmis:contentStreamMimeType", new String(), false);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE cmis:contentStreamMimeType IS     NULL", 0, false, "cmis:contentStreamMimeType", new String(), false);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' =  ANY cmis:contentStreamMimeType", doc_count, false, "cmis:contentStreamMimeType",
                new String(), true);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' <> ANY cmis:contentStreamMimeType", 0, false, "cmis:contentStreamMimeType",
                new String(), true);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' <  ANY cmis:contentStreamMimeType", 0, false, "cmis:contentStreamMimeType",
                new String(), true);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' <= ANY cmis:contentStreamMimeType", doc_count, false, "cmis:contentStreamMimeType",
                new String(), true);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' >  ANY cmis:contentStreamMimeType", 0, false, "cmis:contentStreamMimeType",
                new String(), true);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE 'text/plain' >= ANY cmis:contentStreamMimeType", doc_count, false, "cmis:contentStreamMimeType",
                new String(), true);

        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE ANY cmis:contentStreamMimeType IN     ('text/plain')", doc_count, false, "cmis:contentStreamMimeType",
                new String(), true);
        testQuery("SELECT cmis:contentStreamMimeType FROM cmis:document WHERE ANY cmis:contentStreamMimeType NOT IN ('text/plain')", 0, false, "cmis:contentStreamMimeType",
                new String(), true);
    }

    public void test_CONTENT_STREAM_LENGTH() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:contentStreamLength");
            Long value = DefaultTypeConverter.INSTANCE.convert(Long.class, sValue);
            assertNotNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:contentStreamLength");
            assertEquals(CMISDataTypeEnum.INTEGER, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ContentStreamLengthProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:document WHERE cmis:contentStreamLength = " + contentLength0, rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:contentStreamLength");
            Long value = DefaultTypeConverter.INSTANCE.convert(Long.class, sValue);
            assertNotNull(value);
            assertEquals(Long.valueOf(contentLength0), value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:contentStreamLength");
            assertEquals(CMISDataTypeEnum.INTEGER, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ContentStreamLengthProperty);
        }
        rs.close();

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength =  750", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength <> 750", doc_count, true, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength <  750", doc_count, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength <= 750", doc_count, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength >  750", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength >= 750", 0, false, "cmis:contentStreamLength", new String(), false);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength IN     (750)", 0, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength NOT IN (750)", doc_count, false, "cmis:contentStreamLength", new String(), false);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength     LIKE '750'", 0, false, "cmis:contentStreamLength", new String(), true);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength NOT LIKE '750'", doc_count, false, "cmis:contentStreamLength", new String(), true);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength IS NOT NULL", doc_count, false, "cmis:contentStreamLength", new String(), false);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE cmis:contentStreamLength IS     NULL", 0, false, "cmis:contentStreamLength", new String(), false);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 =  ANY cmis:contentStreamLength", 0, false, "cmis:contentStreamLength", new String(), true);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 <> ANY cmis:contentStreamLength", doc_count, false, "cmis:contentStreamLength", new String(), true);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 <  ANY cmis:contentStreamLength", doc_count, false, "cmis:contentStreamLength", new String(), true);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 <= ANY cmis:contentStreamLength", doc_count, false, "cmis:contentStreamLength", new String(), true);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 >  ANY cmis:contentStreamLength", 0, false, "cmis:contentStreamLength", new String(), true);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE 750 >= ANY cmis:contentStreamLength", 0, false, "cmis:contentStreamLength", new String(), true);

        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE ANY cmis:contentStreamLength IN     (750)", 0, false, "cmis:contentStreamLength", new String(), true);
        testQuery("SELECT cmis:contentStreamLength FROM cmis:document WHERE ANY cmis:contentStreamLength NOT IN (750)", doc_count, false, "cmis:contentStreamLength", new String(), true);
    }

    public void test_CHECKIN_COMMENT() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:checkinComment");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:checkinComment");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof CheckinCommentProperty);
        }
        rs.close();

        testQuery("SELECT cmis:checkinComment FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment =  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment <> 'admin'", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment <  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment <= 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment >  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment >= 'admin'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment NOT IN ('admin')", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment     LIKE 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment NOT LIKE 'admin'", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE cmis:checkinComment IS     NULL", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' =  ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' <> ANY cmis:checkinComment", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' <  ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' <= ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' >  ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE 'admin' >= ANY cmis:checkinComment", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE ANY cmis:checkinComment IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:checkinComment FROM cmis:document WHERE ANY cmis:checkinComment NOT IN ('admin')", doc_count, false, "cmis:objectId", new String(), true);
    }

    public void test_VERSION_SERIES_CHECKED_OUT_ID() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:versionSeriesCheckedOutId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:versionSeriesCheckedOutId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof VersionSeriesCheckedOutIdProperty);
        }
        rs.close();
        
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId =  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId <> 'admin'", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId <  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId <= 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId >  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId >= 'admin'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId NOT IN ('admin')", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId     LIKE 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId NOT LIKE 'admin'", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE cmis:versionSeriesCheckedOutId IS     NULL", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' =  ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' <> ANY cmis:versionSeriesCheckedOutId", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' <  ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' <= ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' >  ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE 'admin' >= ANY cmis:versionSeriesCheckedOutId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE ANY cmis:versionSeriesCheckedOutId IN     ('admin')", 0, false, "cmis:objectId", new String(),
                true);
        testQuery("SELECT cmis:versionSeriesCheckedOutId FROM cmis:document WHERE ANY cmis:versionSeriesCheckedOutId NOT IN ('admin')", doc_count, false, "cmis:objectId", new String(),
                true);
    }

    public void test_VERSION_SERIES_CHECKED_OUT_BY() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:versionSeriesCheckedOutBy");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:versionSeriesCheckedOutBy");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof VersionSeriesCheckedOutByProperty);
        }
        rs.close();
        
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy =  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy <> 'admin'", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy <  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy <= 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy >  'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy >= 'admin'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy IN     ('admin')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy NOT IN ('admin')", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy     LIKE 'admin'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy NOT LIKE 'admin'", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE cmis:versionSeriesCheckedOutBy IS     NULL", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' =  ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' <> ANY cmis:versionSeriesCheckedOutBy", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' <  ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' <= ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' >  ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE 'admin' >= ANY cmis:versionSeriesCheckedOutBy", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE ANY cmis:versionSeriesCheckedOutBy IN     ('admin')", 0, false, "cmis:objectId", new String(),
                true);
        testQuery("SELECT cmis:versionSeriesCheckedOutBy FROM cmis:document WHERE ANY cmis:versionSeriesCheckedOutBy NOT IN ('admin')", doc_count, false, "cmis:objectId", new String(),
                true);
    }

    public void test_IS_VERSION_SERIES_CHECKED_OUT() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:isVersionSeriesCheckedOut");
            Boolean value = DefaultTypeConverter.INSTANCE.convert(Boolean.class, sValue);
            assertNotNull(value);
            assertEquals(Boolean.FALSE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:isVersionSeriesCheckedOut");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.BOOLEAN, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof IsVersionSeriesCheckedOutProperty);
        }
        rs.close();

        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut =  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut <> 'TRUE'", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut <  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut <= 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut >  'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut >= 'TRUE'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut NOT IN ('TRUE')", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut     LIKE 'TRUE'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut NOT LIKE 'TRUE'", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE cmis:isVeriesSeriesCheckedOut IS     NULL", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE 'TRUE' =  ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE 'TRUE' <> ANY cmis:isVeriesSeriesCheckedOut", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE 'TRUE' <  ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE 'TRUE' <= ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE 'TRUE' >  ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE 'TRUE' >= ANY cmis:isVeriesSeriesCheckedOut", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE ANY cmis:isVeriesSeriesCheckedOut IN     ('TRUE')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:isVersionSeriesCheckedOut FROM cmis:document WHERE ANY cmis:isVeriesSeriesCheckedOut NOT IN ('TRUE')", doc_count, false, "cmis:objectId", new String(), true);
    }

    public void test_VERSION_SERIES_ID() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:versionSeriesId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals(row.getNodeRef().toString(), value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:versionSeriesId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof VersionSeriesIdProperty);
        }
        rs.close();

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);
        
        
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId =  'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId <> 'company'", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId <  'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId <= 'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId >  'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId >= 'company'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId IN     ('company')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId NOT IN ('company')", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId     LIKE 'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId NOT LIKE 'company'", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId IS NOT NULL", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE cmis:versionSeriesId IS     NULL", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' =  ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' <> ANY cmis:versionSeriesId", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' <  ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' <= ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' >  ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE 'company' >= ANY cmis:versionSeriesId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE ANY cmis:versionSeriesId IN     ('company')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionSeriesId FROM cmis:document WHERE ANY cmis:versionSeriesId NOT IN ('company')", doc_count, false, "cmis:objectId", new String(), true);
    }

    public void test_VERSION_LABEL() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:versionLabel");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:versionLabel");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof VersionLabelProperty);
        }
        rs.close();
        
        testQuery("SELECT cmis:versionLabel FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel =  'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel <> 'company'", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel <  'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel <= 'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel >  'company'", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel >= 'company'", 1, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel IN     ('company')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel NOT IN ('company')", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel     LIKE 'company'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel NOT LIKE 'company'", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel IS NOT NULL", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE cmis:versionLabel IS     NULL", doc_count-1, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' =  ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' <> ANY cmis:versionLabel", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' <  ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' <= ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' >  ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE 'company' >= ANY cmis:versionLabel", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE ANY cmis:versionLabel IN     ('company')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:versionLabel FROM cmis:document WHERE ANY cmis:versionLabel NOT IN ('company')", doc_count, false, "cmis:objectId", new String(), true);
    }

    public void test_IS_LATEST_MAJOR_VERSION() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:isLatestMajorVersion");
            Boolean value = DefaultTypeConverter.INSTANCE.convert(Boolean.class, sValue);
            assertNotNull(value);
            assertEquals(Boolean.FALSE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:isLatestMajorVersion");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.BOOLEAN, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof IsLatestMajorVersionProperty);
        }
        rs.close();
        
        testQuery("SELECT cmis:isLatestMajorVersion FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);

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

    public void test_IS_MAJOR_VERSION() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:isMajorVersion");
            Boolean value = DefaultTypeConverter.INSTANCE.convert(Boolean.class, sValue);
            assertNotNull(value);
            assertEquals(Boolean.FALSE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:isMajorVersion");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.BOOLEAN, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof IsMajorVersionProperty);
        }
        rs.close();

        testQuery("SELECT cmis:isMajorVersion FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);
        
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

    public void test_IS_LATEST_VERSION() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:isLatestVersion");
            Boolean value = DefaultTypeConverter.INSTANCE.convert(Boolean.class, sValue);
            assertNotNull(value);
            assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:isLatestVersion");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.BOOLEAN, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof IsLatestVersionProperty);
        }
        rs.close();
        
        testQuery("SELECT cmis:isLatestVersion FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);

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

    public void test_IS_IMMUTABLE() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:Document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:isImmutable");
            Boolean value = DefaultTypeConverter.INSTANCE.convert(Boolean.class, sValue);
            assertNotNull(value);
            assertEquals(Boolean.FALSE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:isImmutable");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.BOOLEAN, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof IsImmutableProperty);
        }
        rs.close();

        testQuery("SELECT cmis:isImmutable FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);
        
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

    public void test_folder_NAME() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:folder", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:name");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            // assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:name");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:folder WHERE cmis:name =  'Folder 1'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:name");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals("Folder 1", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:name");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

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

        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' =  ANY cmis:name", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' <> ANY cmis:name", 9, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' <  ANY cmis:name", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' <= ANY cmis:name", 2, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' >  ANY cmis:name", 8, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE 'Folder 1' >= ANY cmis:name", 9, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:name FROM cmis:folder WHERE ANY cmis:name IN     ('Folder 1')", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:folder WHERE ANY cmis:name NOT IN ('Folder 1')", 9, false, "cmis:objectId", new String(), true);
    }

    public void test_document_Name() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:name");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            // assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:name");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:document WHERE cmis:name =  'Alfresco Tutorial'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:name");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals("Alfresco Tutorial", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:name");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name =  'Alfresco Tutorial'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name <> 'Alfresco Tutorial'", doc_count-1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name <  'Alfresco Tutorial'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name <= 'Alfresco Tutorial'", 2, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name >  'Alfresco Tutorial'", doc_count-2, true, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name >= 'Alfresco Tutorial'", doc_count-1, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name IN     ('Alfresco Tutorial')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name NOT IN ('Alfresco Tutorial')", doc_count-1, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco Tutorial'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name NOT LIKE 'Alfresco Tutorial'", doc_count-1, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name IS NOT NULL", doc_count, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name IS     NULL", 0, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' =  ANY cmis:name", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' <> ANY cmis:name", doc_count-1, false, "cmis:name", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' <  ANY cmis:name", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' <= ANY cmis:name", 2, false, "cmis:name", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' >  ANY cmis:name", doc_count-2, false, "cmis:name", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:document WHERE 'Alfresco Tutorial' >= ANY cmis:name", doc_count-1, false, "cmis:name", new String(), true);

        testQuery("SELECT cmis:name FROM cmis:document WHERE ANY cmis:name IN     ('Alfresco Tutorial')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT cmis:name FROM cmis:document WHERE ANY cmis:name NOT IN ('Alfresco Tutorial')", doc_count-1, false, "cmis:name", new String(), true);
    }

    public void test_CHANGE_TOKEN() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:changeToken");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNull(value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:changeToken");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof FixedValueProperty);
        }
        rs.close();

        testQuery("SELECT cmis:changeToken FROM cmis:folder", 10, false, "cmis:objectId", new String(), false);
        
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

    public void test_LAST_MODIFICATION_DATE() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:lastModificationDate");
            Date value = DefaultTypeConverter.INSTANCE.convert(Date.class, sValue);
            assertNotNull(value);
            // assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:lastModificationDate");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.DATETIME, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        // By default we are only working to the day

        Calendar today = Calendar.getInstance();

        if ((today.get(Calendar.HOUR_OF_DAY) == 0) || (today.get(Calendar.HOUR_OF_DAY) == 23))
        {
            return;
        }

        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true);

        Date lmd0 = DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(c0, ContentModel.PROP_MODIFIED));
        String lmds0 = df.format(lmd0);
        options = new CMISQueryOptions("SELECT * FROM cmis:document WHERE cmis:lastModificationDate = TIMESTAMP '" + lmds0 + "' and cmis:objectId = '" + c0.toString() + "'",
                rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:lastModificationDate");
            Date value = DefaultTypeConverter.INSTANCE.convert(Date.class, sValue);
            assertNotNull(value);
            assertEquals(lmd0, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:lastModificationDate");
            assertEquals(CMISDataTypeEnum.DATETIME, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        Date date = testQuery("SELECT cmis:lastModificationDate FROM cmis:document", -1, false, "cmis:lastModificationDate", new Date(), false);
        today.setTime(date);
        if(!usesDateTimeAnalyser)
        {
            today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        }

        String sDate = df.format(today.getTime());
        String sDate2 = sDate.substring(0, sDate.length() - 1) + "+00:00";

        // Today (assuming al ws created today)

        if(usesDateTimeAnalyser)
        {
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate = TIMESTAMP '" + sDate + "'", 1, false, "cmis:objectId", new String(),
                    false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate = TIMESTAMP '" + sDate2 + "'", 1, false, "cmis:objectId", new String(),
                    false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <> '" + sDate + "'", doc_count-1, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <  '" + sDate + "'", doc_count-1, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >= '" + sDate + "'", 1, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IN     ('" + sDate + "')", 1, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count-1, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate     LIKE '" + sDate + "'", 1, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT LIKE '" + sDate + "'", doc_count-1, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:lastModificationDate", 1, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:lastModificationDate", doc_count-1, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:lastModificationDate", doc_count-1, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:lastModificationDate", 1, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate IN     ('" + sDate + "')", 1, false, "cmis:objectId", new String(),
                    true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count-1, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "') order by cmis:lastModificationDate", doc_count-1, false,
                    "cmis:objectId", new String(), true);

            // using yesterday

            date = Duration.subtract(date, new Duration("P1D"));
            Calendar yesterday = Calendar.getInstance();
            yesterday.setTime(date);
            yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
            sDate = df.format(yesterday.getTime());

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <> '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >  '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(),
                    true);

            // using tomorrow

            date = Duration.add(date, new Duration("P2D"));
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.setTime(date);
            tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
            sDate = df.format(tomorrow.getTime());

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <> '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <  '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(),
                    true);
        }
        else
        {
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate = TIMESTAMP '" + sDate + "'", doc_count, false, "cmis:objectId", new String(),
                    false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate = TIMESTAMP '" + sDate2 + "'", doc_count, false, "cmis:objectId", new String(),
                    false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <> '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IN     ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT IN ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate     LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate IN     ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(),
                    true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "') order by cmis:lastModificationDate", 0, false,
                    "cmis:objectId", new String(), true);

            // using yesterday

            date = Duration.subtract(date, new Duration("P1D"));
            Calendar yesterday = Calendar.getInstance();
            yesterday.setTime(date);
            yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
            sDate = df.format(yesterday.getTime());

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <> '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >  '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(),
                    true);

            // using tomorrow

            date = Duration.add(date, new Duration("P2D"));
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.setTime(date);
            tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
            sDate = df.format(tomorrow.getTime());

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <> '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <  '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate <= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate >= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate NOT LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE cmis:lastModificationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:lastModificationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:lastModificationDate", 0, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:lastModificationDate FROM cmis:document WHERE ANY cmis:lastModificationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(),
                    true);
        }


    }

    public void test_LAST_MODIFIED_BY() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:lastModifiedBy");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            // assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:lastModifiedBy");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:document WHERE cmis:lastModifiedBy =  'System'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:lastModifiedBy");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals("System", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:lastModifiedBy");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy =  'System'", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy <> 'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy <  'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy <= 'System'", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy >  'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy >= 'System'", doc_count, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy IN     ('System')", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy NOT IN ('System')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy     LIKE 'System'", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy NOT LIKE 'System'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE cmis:lastModifiedBy IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' =  ANY cmis:lastModifiedBy", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' <> ANY cmis:lastModifiedBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' <  ANY cmis:lastModifiedBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' <= ANY cmis:lastModifiedBy", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' >  ANY cmis:lastModifiedBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE 'System' >= ANY cmis:lastModifiedBy", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE ANY cmis:lastModifiedBy IN     ('System')", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE ANY cmis:lastModifiedBy NOT IN ('System')", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:lastModifiedBy FROM cmis:document WHERE ANY cmis:lastModifiedBy NOT IN ('System') order by cmis:lastModifiedBy", 0, true, "cmis:objectId",
                new String(), true);
    }

    public void test_CREATION_DATE() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:creationDate");
            Date value = DefaultTypeConverter.INSTANCE.convert(Date.class, sValue);
            assertNotNull(value);
            // assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:creationDate");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.DATETIME, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        // By default we are only working to the day

        Calendar today = Calendar.getInstance();

        if ((today.get(Calendar.HOUR_OF_DAY) == 0) || (today.get(Calendar.HOUR_OF_DAY) == 23))
        {
            return;
        }

        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", true);

        Date cd0 = DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(c0, ContentModel.PROP_CREATED));
        String cds0 = df.format(cd0);
        options = new CMISQueryOptions("SELECT * FROM cmis:document WHERE cmis:creationDate = TIMESTAMP '" + cds0 + "' and cmis:objectId = '" + c0.toString() + "'", rootNodeRef
                .getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:creationDate");
            Date value = DefaultTypeConverter.INSTANCE.convert(Date.class, sValue);
            assertNotNull(value);
            assertEquals(cd0, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:creationDate");
            assertEquals(CMISDataTypeEnum.DATETIME, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        Date date = testQuery("SELECT cmis:creationDate FROM cmis:document", -1, false, "cmis:creationDate", new Date(), false);
        today.setTime(date);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        if(!usesDateTimeAnalyser)
        {
            today.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
        }

        String sDate = df.format(today.getTime());

        // Today (assuming al ws created today)

        if(usesDateTimeAnalyser)
        {
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate =  '" + sDate + "'", 1, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <> '" + sDate + "'", doc_count-1, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <  '" + sDate + "'", doc_count-1, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >= '" + sDate + "'", 1, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IN     ('" + sDate + "')", 1, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT IN ('" + sDate + "')", doc_count-1, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate     LIKE '" + sDate + "'", 1, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT LIKE '" + sDate + "'", doc_count-1, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:creationDate", 1, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:creationDate", doc_count-1, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:creationDate", doc_count-1, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:creationDate", 1, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate IN     ('" + sDate + "')", 1, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "')", doc_count-1, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "') order by cmis:creationDate", doc_count-1, false, "cmis:objectId",
                    new String(), true);

            // using yesterday

            date = Duration.subtract(date, new Duration("P1D"));
            Calendar yesterday = Calendar.getInstance();
            yesterday.setTime(date);
            yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
            sDate = df.format(yesterday.getTime());

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <> '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >  '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), true);

            // using tomorrow

            date = Duration.add(date, new Duration("P2D"));
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.setTime(date);
            tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
            sDate = df.format(tomorrow.getTime());

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <> '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <  '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), true);
        }
        else
        {
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate =  '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <> '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IN     ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT IN ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate     LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate IN     ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "') order by cmis:creationDate", 0, false, "cmis:objectId",
                    new String(), true);

            // using yesterday

            date = Duration.subtract(date, new Duration("P1D"));
            Calendar yesterday = Calendar.getInstance();
            yesterday.setTime(date);
            yesterday.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
            sDate = df.format(yesterday.getTime());

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <> '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >  '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), true);

            // using tomorrow

            date = Duration.add(date, new Duration("P2D"));
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.setTime(date);
            tomorrow.set(Calendar.MILLISECOND, today.getMinimum(Calendar.MILLISECOND));
            sDate = df.format(tomorrow.getTime());

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate =  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <> '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <  '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate <= '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >  '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate >= '" + sDate + "'", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate     LIKE '" + sDate + "'", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate NOT LIKE '" + sDate + "'", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE cmis:creationDate IS     NULL", 0, false, "cmis:objectId", new String(), false);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' =  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <> ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <  ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' <= ANY cmis:creationDate", doc_count, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >  ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE '" + sDate + "' >= ANY cmis:creationDate", 0, false, "cmis:objectId", new String(), true);

            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate IN     ('" + sDate + "')", 0, false, "cmis:objectId", new String(), true);
            testQuery("SELECT cmis:creationDate FROM cmis:document WHERE ANY cmis:creationDate NOT IN ('" + sDate + "')", doc_count, false, "cmis:objectId", new String(), true);

        }

    }

    public void test_CREATED_BY() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:createdBy");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            // assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:createdBy");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:document WHERE cmis:createdBy =  'System'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:createdBy");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals("System", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:createdBy");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof DirectProperty);
        }
        rs.close();

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy =  'System'", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy <> 'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy <  'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy <= 'System'", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy >  'System'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy >= 'System'", doc_count, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy IN     ('System')", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy NOT IN ('System')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy     LIKE 'System'", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy NOT LIKE 'System'", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE cmis:createdBy IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' =  ANY cmis:createdBy", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' <> ANY cmis:createdBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' <  ANY cmis:createdBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' <= ANY cmis:createdBy", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' >  ANY cmis:createdBy", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE 'System' >= ANY cmis:createdBy", doc_count, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE ANY cmis:createdBy IN     ('System')", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE ANY cmis:createdBy NOT IN ('System')", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:createdBy FROM cmis:document WHERE ANY cmis:createdBy IN     ('System') order by cmis:createdBy", doc_count, false, "cmis:objectId", new String(), true);

    }

    public void test_OBJECT_TYPE_ID() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:objectTypeId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertTrue(value.equals("cmis:document") || value.equals("D:cm:dictionaryModel"));
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:objectTypeId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ObjectTypeIdProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:folder", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:objectTypeId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals("cmis:folder", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:objectTypeId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ObjectTypeIdProperty);
        }
        rs.close();

        // DOC

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId =  'cmis:document'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId <> 'cmis:document'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId <  'cmis:document'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId <= 'cmis:document'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId >  'cmis:document'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId >= 'cmis:document'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId IN     ('cmis:document')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId NOT IN ('cmis:document')", 1, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId     LIKE 'cmis:document'", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId NOT LIKE 'cmis:document'", 1, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE cmis:objectTypeId IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' =  ANY cmis:objectTypeId", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' <> ANY cmis:objectTypeId", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' <  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' <= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' >  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE 'cmis:document' >= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE ANY cmis:objectTypeId IN     ('cmis:document')", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:document WHERE ANY cmis:objectTypeId NOT IN ('cmis:document')", 1, false, "cmis:objectId", new String(), true);

        // FOLDER

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId =  'cmis:folder'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId <> 'cmis:folder'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId <  'cmis:folder'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId <= 'cmis:folder'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId >  'cmis:folder'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId >= 'cmis:folder'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId IN     ('cmis:folder')", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId NOT IN ('cmis:folder')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId     LIKE 'cmis:folder'", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId NOT LIKE 'cmis:folder'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' =  ANY cmis:objectTypeId", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' <> ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' <  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' <= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' >  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' >= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE ANY cmis:objectTypeId IN     ('cmis:folder')", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE ANY cmis:objectTypeId NOT IN ('cmis:folder')", 0, false, "cmis:objectId", new String(), true);

        // RELATIONSHIP

        testQuery("SELECT cmis:objectTypeId FROM Relationship WHERE cmis:objectTypeId =  ''", 1, false, "cmis:objectId", new String(), true);

    }

    public void test_BASE_TYPE_ID() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:baseTypeId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals("cmis:document", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:baseTypeId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof BaseTypeIdProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:folder", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:baseTypeId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals("cmis:folder", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:baseTypeId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof BaseTypeIdProperty);
        }
        rs.close();

        // DOC

        testQuery("SELECT cmis:baseTypeId FROM cmis:document", doc_count, false, "cmis:baseTypeId", new String(), false);
        
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId =  'cmis:document'", doc_count, false, "cmis:baseTypeId", new String(), false);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId <> 'cmis:document'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId <  'cmis:document'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId <= 'cmis:document'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId >  'cmis:document'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId >= 'cmis:document'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId IN     ('cmis:document')", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId NOT IN ('cmis:document')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId     LIKE 'cmis:document'", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId NOT LIKE 'cmis:document'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE 'cmis:document' =  ANY cmis:baseTypeId", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE 'cmis:document' <> ANY cmis:baseTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE 'cmis:document' <  ANY cmis:baseTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE 'cmis:document' <= ANY cmis:baseTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE 'cmis:document' >  ANY cmis:baseTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE 'cmis:document' >= ANY cmis:baseTypeId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE ANY cmis:baseTypeId IN     ('cmis:document')", doc_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE ANY cmis:baseTypeId NOT IN ('cmis:document')", 0, false, "cmis:objectId", new String(), true);
        
        
        
        
        testQuery("SELECT cmis:baseTypeId FROM cmis:folder", folder_count, false, "cmis:baseTypeId", new String(), false);
        
        testQuery("SELECT cmis:baseTypeId FROM cmis:folder WHERE cmis:baseTypeId =  'cmis:folder'", folder_count, false, "cmis:baseTypeId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId <> 'cmis:folder'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId <  'cmis:folder'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId <= 'cmis:folder'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId >  'cmis:folder'", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId >= 'cmis:folder'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId IN     ('cmis:folder')", folder_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId NOT IN ('cmis:folder')", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId     LIKE 'cmis:folder'", folder_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId NOT LIKE 'cmis:folder'", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId IS NOT NULL", folder_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE cmis:objectTypeId IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' =  ANY cmis:objectTypeId", folder_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' <> ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' <  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' <= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' >  ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE 'cmis:folder' >= ANY cmis:objectTypeId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE ANY cmis:objectTypeId IN     ('cmis:folder')", folder_count, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectTypeId FROM cmis:folder WHERE ANY cmis:objectTypeId NOT IN ('cmis:folder')", 0, false, "cmis:objectId", new String(), true);

        // RELATIONSHIP

        testQuery("SELECT cmis:baseTypeId FROM cmis:relationship WHERE cmis:baseTypeId =  'cmis:relationship'", 1, false, "cmis:objectId", new String(), true);
        
        testQuery("SELECT cmis:baseTypeId FROM cmis:policy WHERE cmis:baseTypeId =  'cmis:relationship'", 1, false, "cmis:objectId", new String(), true);
        
        testQuery("SELECT cmis:baseTypeId FROM cmis:folder WHERE cmis:baseTypeId =  'cmis:policy'", 0, false, "cmis:baseTypeId", new String(), false);
        testQuery("SELECT cmis:baseTypeId FROM cmis:folder WHERE cmis:baseTypeId =  'cmis:relationship'", 0, false, "cmis:baseTypeId", new String(), true);
        
        testQuery("SELECT cmis:baseTypeId FROM cmis:folder WHERE cmis:baseTypeId =  'cmis:document'", 0, false, "cmis:baseTypeId", new String(), false);
        testQuery("SELECT cmis:baseTypeId FROM cmis:document WHERE cmis:baseTypeId =  'cmis:folder'", 0, false, "cmis:baseTypeId", new String(), false);

    }

    public void test_ObjectId() throws Exception
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(doc_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:objectId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            // assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:objectId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ObjectIdProperty);
        }
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:folder", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(folder_count, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:objectId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            // assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:objectId");
            assertNotNull(column);
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ObjectIdProperty);
        }
        rs.close();

        String companyHomeId = testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:name = 'Folder 0'", 1, false, "cmis:objectId", new String(), false);

        options = new CMISQueryOptions("SELECT * FROM cmis:folder WHERE cmis:objectId =  '" + companyHomeId + "'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:objectId");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertNotNull(value);
            assertEquals(companyHomeId, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("cmis:objectId");
            assertEquals(CMISDataTypeEnum.ID, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());
            assertTrue(column.getCMISPropertyDefinition().getPropertyAccessor() instanceof ObjectIdProperty);
        }
        rs.close();

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

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId     LIKE '" + companyHomeId + "'", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId NOT LIKE '" + companyHomeId + "'", 9, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE IN_FOLDER('" + companyHomeId + "')", 2, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE IN_TREE  ('" + companyHomeId + "')", 6, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId IS     NULL", 0, false, "cmis:objectId", new String(), false);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' =  ANY cmis:objectId", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' <> ANY cmis:objectId", 9, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' <  ANY cmis:objectId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' <= ANY cmis:objectId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' >  ANY cmis:objectId", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE '" + companyHomeId + "' >= ANY cmis:objectId", 0, false, "cmis:objectId", new String(), true);

        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE ANY cmis:objectId IN     ('" + companyHomeId + "')", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE ANY cmis:objectId NOT IN ('" + companyHomeId + "')", 9, false, "cmis:objectId", new String(), true);
        
        // Folder versions which are ignored ....
        
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId =  '" + companyHomeId + ";1.0'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId <> '" + companyHomeId + ";1.0'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId IN     ('" + companyHomeId + ";1.0')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId NOT IN ('" + companyHomeId + ";1.0')", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId IS NOT NULL", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:folder WHERE cmis:objectId IS     NULL", 0, false, "cmis:objectId", new String(), false);
        
        // Docs
        
        
        String docId = testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:name = 'Alfresco Tutorial'", 1, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId =  '" + docId + "'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId <> '" + docId + "'", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IN     ('" + docId + "')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId NOT IN ('" + docId + "')", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IS     NULL", 0, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId =  '" + docId + ";1.0'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId <> '" + docId + ";1.0'", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IN     ('" + docId + ";1.0')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId NOT IN ('" + docId + ";1.0')", doc_count-1, false, "cmis:objectId", new String(), false);

        nodeService.setProperty(c0, ContentModel.PROP_VERSION_LABEL, "1.0");
        
        docId = testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:name = 'Alfresco Tutorial'", 1, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId =  '" + docId + "'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId <> '" + docId + "'", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IN     ('" + docId + "')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId NOT IN ('" + docId + "')", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IS     NULL", 0, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId =  '" + docId + ";1.0'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId <> '" + docId + ";1.0'", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IN     ('" + docId + ";1.0')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId NOT IN ('" + docId + ";1.0')", doc_count-1, false, "cmis:objectId", new String(), false);
 
        nodeService.setProperty(c0, ContentModel.PROP_VERSION_LABEL, "2.1");
        
        docId = testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:name = 'Alfresco Tutorial'", 1, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId =  '" + docId + "'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId <> '" + docId + "'", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IN     ('" + docId + "')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId NOT IN ('" + docId + "')", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IS     NULL", 0, false, "cmis:objectId", new String(), false);
        
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId =  '" + docId + ";2.1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId <> '" + docId + ";2.1'", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId IN     ('" + docId + ";2.1')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT cmis:objectId FROM cmis:document WHERE cmis:objectId NOT IN ('" + docId + ";2.1')", doc_count-1, false, "cmis:objectId", new String(), false);

    }

    public void testOrderBy() throws Exception
    {
        testOrderBy("SELECT  cmis:objectId FROM cmis:folder ORDER BY cmis:objectId", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:objectId");
        testOrderBy("SELECT  cmis:objectId FROM cmis:folder ORDER BY cmis:objectTypeId", folder_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:objectTypeId");
        // testOrderBy("SELECT  cmis:objectId FROM cmis:folder ORDER BY cmis:objectTypeId", folder_count, false,
        // Order.ASCENDING, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS,
        // "cmis:objectTypeId");
        testOrderBy("SELECT  cmis:objectId FROM cmis:folder ORDER BY cmis:objectId ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:objectId");
        testOrderBy("SELECT  cmis:objectId FROM cmis:folder ORDER BY cmis:objectId DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT, "cmis:objectId");
        testOrderBy("SELECT  cmis:objectId FROM cmis:folder ORDER BY cmis:objectId DESC", folder_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:objectId");

        testOrderBy("SELECT  cmis:objectId Meep FROM cmis:folder ORDER BY Meep", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "Meep");
        testOrderBy("SELECT  cmis:objectId Meep FROM cmis:folder ORDER BY cmis:objectId", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "Meep");
        testOrderBy("SELECT  cmis:objectId Meep FROM cmis:folder ORDER BY Meep ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "Meep");
        testOrderBy("SELECT  cmis:objectId Meep FROM cmis:folder ORDER BY Meep DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT, "Meep");

        testOrderBy("SELECT  cmis:objectId FROM cmis:folder F ORDER BY F.cmis:objectId", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:objectId");
        testOrderBy("SELECT  cmis:objectId FROM cmis:folder F ORDER BY cmis:objectId", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:objectId");
        testOrderBy("SELECT  cmis:objectId FROM cmis:folder F ORDER BY F.cmis:objectId ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "F.cmis:objectId");
        testOrderBy("SELECT  cmis:objectId FROM cmis:folder F ORDER BY F.cmis:objectId DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT, "F.cmis:objectId");

        testOrderBy("SELECT  F.cmis:objectId Meep FROM cmis:folder F ORDER BY Meep", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "Meep");
        testOrderBy("SELECT  F.cmis:objectId Meep FROM cmis:folder F ORDER BY F.cmis:objectId", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "F.cmis:objectId");
        testOrderBy("SELECT  F.cmis:objectId Meep FROM cmis:folder F ORDER BY cmis:objectId", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "F.cmis:objectId");
        testOrderBy("SELECT  F.cmis:objectId Meep FROM cmis:folder F ORDER BY Meep ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:objectId");
        testOrderBy("SELECT  F.cmis:objectId Meep FROM cmis:folder F ORDER BY Meep DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT, "Meep");

        testOrderBy("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:document where CONTAINS('*') ORDER BY MEEP", doc_count, false, Order.ASCENDING, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS,
                "MEEP");
        testOrderBy("SELECT SCORE(), cmis:objectId FROM cmis:document where CONTAINS('*') ORDER BY SEARCH_SCORE", doc_count, false, Order.ASCENDING, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS,
                "SEARCH_SCORE");
        testOrderBy("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:document where CONTAINS('*') ORDER BY MEEP ASC", doc_count, false, Order.ASCENDING, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS,
                "MEEP");
        testOrderBy("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:document where CONTAINS('*') ORDER BY MEEP DESC", doc_count, false, Order.DESCENDING,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS, "MEEP");

        testOrderBy("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder where CONTAINS('cmis:name:*') ORDER BY MEEP", folder_count, false, Order.ASCENDING,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS, "MEEP");
        testOrderBy("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder where CONTAINS('cmis:name:*') ORDER BY MEEP ASC", folder_count, false, Order.ASCENDING,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS, "MEEP");
        testOrderBy("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder where CONTAINS('cmis:name:*') ORDER BY MEEP DESC", folder_count, false, Order.DESCENDING,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS, "MEEP");

        // other orderable properties

        testOrderBy("SELECT cmis:objectTypeId FROM cmis:folder ORDER BY cmis:objectTypeId ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:objectTypeId");
        testOrderBy("SELECT cmis:objectTypeId FROM cmis:folder ORDER BY cmis:objectTypeId DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:objectTypeId");
        // all are equal ...
        testOrderBy("SELECT cmis:objectTypeId FROM cmis:folder ORDER BY cmis:objectTypeId ASC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:objectTypeId");

        testOrderBy("SELECT cmis:createdBy FROM cmis:folder ORDER BY cmis:createdBy ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:createdBy");
        testOrderBy("SELECT cmis:createdBy FROM cmis:folder ORDER BY cmis:createdBy DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT, "cmis:createdBy");
        // all are equal
        testOrderBy("SELECT cmis:createdBy FROM cmis:folder ORDER BY cmis:createdBy ASC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT, "cmis:createdBy");

        testOrderBy("SELECT cmis:creationDate FROM cmis:folder ORDER BY cmis:creationDate ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:creationDate");
        testOrderBy("SELECT cmis:creationDate FROM cmis:folder ORDER BY cmis:creationDate DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:creationDate");
        testOrderBy("SELECT cmis:creationDate FROM cmis:folder ORDER BY cmis:creationDate DESC", folder_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:creationDate");

        testOrderBy("SELECT cmis:lastModifiedBy FROM cmis:folder ORDER BY cmis:lastModifiedBy ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:lastModifiedBy");
        testOrderBy("SELECT cmis:lastModifiedBy FROM cmis:folder ORDER BY cmis:lastModifiedBy DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:lastModifiedBy");
        // all equals ..
        testOrderBy("SELECT cmis:lastModifiedBy FROM cmis:folder ORDER BY cmis:lastModifiedBy DESC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:lastModifiedBy");

        testOrderBy("SELECT cmis:lastModificationDate FROM cmis:folder ORDER BY cmis:lastModificationDate ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:lastModificationDate");
        testOrderBy("SELECT cmis:lastModificationDate FROM cmis:folder ORDER BY cmis:lastModificationDate DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:lastModificationDate");
        testOrderBy("SELECT cmis:lastModificationDate FROM cmis:folder ORDER BY cmis:lastModificationDate DESC", folder_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:lastModificationDate");

        testOrderBy("SELECT cmis:name FROM cmis:folder ORDER BY cmis:name ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:name");
        testOrderBy("SELECT cmis:name FROM cmis:folder ORDER BY cmis:name DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT, "cmis:name");
        testOrderBy("SELECT cmis:name FROM cmis:folder ORDER BY cmis:name DESC", folder_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:name");
        
        testOrderBy("SELECT cmis:name FROM cmis:document ORDER BY cmis:name ASC", doc_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:name");
        testOrderBy("SELECT cmis:name FROM cmis:document ORDER BY cmis:name DESC", doc_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT, "cmis:name");
        testOrderBy("SELECT cmis:name FROM cmis:document ORDER BY cmis:name DESC", doc_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:name");

        // version label is not orderable as indexed and tokenised
        testOrderBy("SELECT cmis:versionLabel FROM cmis:document ORDER BY cmis:versionLabel ASC", doc_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:versionLabel");
        testOrderBy("SELECT cmis:versionLabel FROM cmis:document ORDER BY cmis:versionLabel DESC", doc_count, true, Order.DESCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:versionLabel");
        testOrderBy("SELECT cmis:versionLabel FROM cmis:document ORDER BY cmis:versionLabel DESC", doc_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:versionLabel");

        // cmis:contentStreamFileName is not orderable as indexed and tokenised
        testOrderBy("SELECT cmis:contentStreamFileName FROM cmis:document ORDER BY cmis:contentStreamFileName ASC", doc_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:contentStreamFileName");
        testOrderBy("SELECT cmis:contentStreamFileName FROM cmis:document ORDER BY cmis:contentStreamFileName DESC", doc_count, true, Order.DESCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:contentStreamFileName");
        testOrderBy("SELECT cmis:contentStreamFileName FROM cmis:document ORDER BY cmis:contentStreamFileName DESC", doc_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                "cmis:contentStreamFileName");

        testOrderBy("SELECT cmis:parentId FROM cmis:folder ORDER BY cmis:parentId ASC", folder_count, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:parentId");
        testOrderBy("SELECT cmis:parentId FROM cmis:folder ORDER BY cmis:parentId DESC", folder_count, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT, "cmis:parentId");
        testOrderBy("SELECT cmis:parentId FROM cmis:folder ORDER BY cmis:parentId DESC", folder_count, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT, "cmis:parentId");

        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder WHERE CONTAINS('cmis:name:*') AND cmis:name = 'compan home' ORDER BY SCORE() DESC", 1, false,
                "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder WHERE CONTAINS('cmis:name:*') AND cmis:name IN ('company', 'home') ORDER BY MEEEP DESC", 1, false,
                "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder WHERE CONTAINS('cmis:name:*') AND cmis:name IN ('company', 'home') ORDER BY cmis:parentId DESC", 1,
                false, "cmis:objectId", new String(), true);
        testExtendedQuery("SELECT SCORE() AS MEEP, cmis:objectId, cmis:parentId FROM cmis:folder WHERE CONTAINS('cmis:name:*') ORDER BY cmis:parentId DESC", folder_count, false,
                "cmis:objectId", new String(), false);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder WHERE CONTAINS('cmis:name:*') AND cmis:name IN ('company', 'home') ORDER BY cmis:notThere DESC", 1,
                false, "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder as F WHERE CONTAINS('cmis:name:*') AND cmis:name IN ('company', 'home') ORDER BY F.cmis:parentId DESC",
                1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE() AS MEEP, cmis:objectId FROM cmis:folder F WHERE CONTAINS('cmis:name:*') AND cmis:name IN ('company', 'home') ORDER BY F.cmis:notThere DESC", 1,
                false, "cmis:objectId", new String(), true);

    }

    private <T> void testOrderBy(String query, int size, boolean shouldThrow, Order order, CMISQueryMode mode, String... orderByPropertyName)
    {
        CMISResultSet rs = null;
        try
        {
            CMISQueryOptions options = new CMISQueryOptions(query, rootNodeRef.getStoreRef());
            options.setQueryMode(mode);
            rs = cmisQueryService.query(options);
            Comparable<?>[] previous = null;
            boolean[] wasNull = null;
            boolean[] hasValue = null;

            for (CMISResultSetRow row : rs)
            {
                if (previous == null)
                {
                    previous = new Comparable[orderByPropertyName.length];
                    wasNull = new boolean[orderByPropertyName.length];
                    hasValue = new boolean[orderByPropertyName.length];
                    for (int i = 0; i < orderByPropertyName.length; i++)
                    {
                        Serializable sValue = row.getValue(orderByPropertyName[i]);
                        if (sValue instanceof Comparable<?>)
                        {
                            Comparable<?> comparable = (Comparable<?>) sValue;
                            previous[i] = comparable;
                            hasValue[i] = true;
                        }
                        else
                        {
                            previous[i] = null;
                            wasNull[i] = true;
                        }

                    }
                }
                // if (row.getIndex() == 0)
                // {
                // Serializable sValue = row.getValue(returnPropertyName);
                // returnValue = (T) DefaultTypeConverter.INSTANCE.convert(returnType.getClass(), sValue);
                //    
                // }
                else
                {
                    for (int i = 0; i < orderByPropertyName.length; i++)
                    {
                        Serializable current = row.getValue(orderByPropertyName[i]);
                        Comparable<?> last = previous[i];

                        if (last != null)
                        {
                            if (current == null)
                            {
                                switch (order)
                                {
                                case ASCENDING:
                                    if (shouldThrow)
                                    {
                                        throw new IllegalStateException("Incorrect Order");
                                    }
                                    else
                                    {
                                        fail("Null found after value ascending");
                                    }
                                case DESCENDING:
                                    // OK
                                    break;
                                default:
                                    throw new UnsupportedOperationException();
                                }
                            }

                            int comparison = 0;
                            if(last instanceof String )
                            {
                                Collator myCollator = Collator.getInstance();
                                if(last == null)
                                {
                                    if(current == null)
                                    {
                                        comparison = 0;
                                    }
                                    else
                                    {
                                        comparison = -1;
                                    }
                                }
                                else
                                {
                                    if(current == null)
                                    {
                                        comparison = 1;
                                    }
                                    else
                                    {
                                        comparison = myCollator.compare(last, current);
                                    }
                                }
                                

                            }
                            else
                            {
                                Method ct = null;
                                Method[] methods = last.getClass().getMethods();
                                for (int m = 0; m < methods.length; m++)
                                {
                                    if (methods[m].getName().equals("compareTo")
                                            && (methods[m].getParameterTypes().length == 1) && (methods[m].getParameterTypes()[0].equals(methods[m].getDeclaringClass())))
                                    {
                                        if (ct != null)
                                        {
                                            throw new IllegalStateException("Found 2 or more compareTo methods");
                                        }
                                        ct = methods[m];
                                    }
                                }
                                comparison = (Integer) ct.invoke(last, current);
                            }

                           
                            switch (order)
                            {
                            case ASCENDING:
                                if (comparison <= 0)
                                { // as expected
                                    break;
                                }
                                else
                                {
                                    if (shouldThrow)
                                    {
                                        throw new IllegalStateException("Incorrect Order");
                                    }
                                    else
                                    {
                                        fail("Incorrect Order");
                                    }
                                }
                            case DESCENDING:
                                if (comparison >= 0)
                                { // as expected
                                    break;
                                }
                                else
                                {
                                    if (shouldThrow)
                                    {
                                        throw new IllegalStateException("Incorrect Order");
                                    }
                                    else
                                    {
                                        fail("Incorrect Order");
                                    }
                                }
                            default:
                                throw new UnsupportedOperationException("Column data type is not comparable " + orderByPropertyName[i]);
                            }
                        }
                        else
                        {
                            if (current != null)
                            {
                                switch (order)
                                {
                                case ASCENDING:
                                    // OK
                                    break;
                                case DESCENDING:
                                    if (shouldThrow)
                                    {
                                        throw new IllegalStateException("Incorrect Order");
                                    }
                                    else
                                    {
                                        fail("Null found descending");
                                    }
                                default:
                                    throw new UnsupportedOperationException();
                                }
                            }
                        }

                    }
                    for (int i = 0; i < orderByPropertyName.length; i++)
                    {
                        Serializable sValue = row.getValue(orderByPropertyName[i]);
                        if (sValue instanceof Comparable<?>)
                        {
                            Comparable<?> comparable = (Comparable<?>) sValue;
                            previous[i] = comparable;
                            hasValue[i] = true;
                        }
                        else
                        {
                            previous[i] = null;
                            wasNull[i] = true;
                        }

                    }
                }

            }
            for (int i = 0; i < hasValue.length; i++)
            {
                if (!hasValue[i])
                {
                    throw new UnsupportedOperationException("Only nulls found for " + orderByPropertyName[i]);
                }
            }
            if (size >= 0)
            {
                assertEquals(size, rs.getLength());
            }
            if (shouldThrow)
            {
                fail("Should have thrown an exception");
            }

        }
        catch (CMISQueryException e)
        {
            if (shouldThrow)
            {
                return;
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
                return;
            }
            else
            {
                throw e;
            }
        }
        catch (FTSQueryException e)
        {
            if (shouldThrow)
            {
                return;
            }
            else
            {
                throw e;
            }
        }
        catch (UnsupportedOperationException e)
        {
            if (shouldThrow)
            {
                return;
            }
            else
            {
                throw e;
            }
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalStateException e)
        {
            if (shouldThrow)
            {
                return;
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

    public void testUpperAndLower() throws Exception
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
    }

    public void testAllSimpleTextPredicates() throws Exception
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name = 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name = 'Folder 9'", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name = 'Folder 9\\''", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND NOT cmis:name = 'Folder 1'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND 'Folder 1' = ANY cmis:name", 1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND NOT cmis:name <> 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name <> 'Folder 1'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name < 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name <= 'Folder 1'", 2, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name > 'Folder 1'", 8, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name >= 'Folder 1'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name IN ('Folder 1', '1')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name NOT IN ('Folder 1', 'Folder 9\\'')", 8, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND ANY cmis:name IN ('Folder 1', 'Folder 9\\'')", 2, false, "cmis:objectId", new String(), true);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND ANY cmis:name NOT IN ('2', '3')", 10, false, "cmis:objectId", new String(), true);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'Fol%'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'F_l_e_ 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name NOT LIKE 'F_l_e_ 1'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'F_l_e_ %'", 10, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name NOT LIKE 'F_l_e_ %'", 0, false, "cmis:objectId", new String(), false);
        // TODO: Fix below which fail??
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name LIKE 'F_l_e_ _'", 9, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name NOT LIKE 'F_l_e_ _'", 1, false, "cmis:objectId", new String(), false);
    }

    public void testSimpleConjunction() throws Exception
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name = 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL AND cmis:name = 'Folder'", 0, false, "cmis:objectId", new String(), false);
    }

    public void testSimpleDisjunction() throws Exception
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'Folder 2'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'Folder 1' OR cmis:name = 'Folder 2'", 2, false, "cmis:objectId", new String(), false);
    }

    /**
     * In strict mode you should not be able to refer to aspect properties direct from the type
     * @throws Exception 
     */
    public void testPropertyToSelectorBinding() throws Exception
    {
        testQuery("SELECT cmis:parentId FROM cmis:document", 10, false, "cmis:objectId", new String(), true, CMISQueryMode.CMS_STRICT);
        testQuery("SELECT * FROM cmis:document where cmis:parentId <> 'woof://woof/woof'", 10, false, "cmis:objectId", new String(), true, CMISQueryMode.CMS_STRICT);
        testQuery("SELECT D.*, O.cmis:name FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId", 1, false, "cmis:objectId", new String(), true,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM cmis:document order by cmis:parentId", 10, false, "cmis:objectId", new String(), true, CMISQueryMode.CMS_STRICT);
        testQuery("SELECT * FROM cmis:document where CONTAINS('cmis:parentId:*')", 10, false, "cmis:objectId", new String(), true, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
    }

    public void testExists() throws Exception
    {
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NOT NULL", folder_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name IS NULL", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE cmis:name IS NOT NULL", doc_count, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE cmis:name IS NULL", 0, false, "cmis:objectId", new String(), false);
    }

    public void testObjectEquals()
    {

    }

    public void testDocumentEquals()
    {

    }

    public void testFolderEquals() throws Exception
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_NAME);
        String Name = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = '" + Name + "'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:name = 'Folder 1'", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:parentId = '" + rootNodeRef.toString() + "'", 4, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE cmis:allowedChildObjectTypeIds = 'meep'", 0, false, "cmis:objectId", new String(), true);
    }

    public void test_IN_TREE() throws Exception
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM cmis:folder WHERE IN_TREE('" + id + "')", 6, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder F WHERE IN_TREE(F, '" + id + "')", 6, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder X WHERE IN_TREE(F, '" + id + "')", 6, false, "cmis:objectId", new String(), true);

        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId WHERE IN_TREE(D, '" + id + "')", 1, false, "cmis:objectId",
                new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId WHERE IN_TREE('" + id + "')", 1, false, "cmis:objectId",
                new String(), true, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId WHERE IN_TREE('" + id + ";versionLabel" +"')", 0, false, "cmis:objectId",
                new String(), true, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        testQuery("SELECT * FROM cmis:folder WHERE IN_TREE('woof://woof/woof')", 0, false, "cmis:objectId", new String(), true);
        testQuery("SELECT * FROM cmis:folder WHERE IN_TREE('woof://woof/woof;woof')", 0, false, "cmis:objectId", new String(), true);
    }

    public void test_IN_FOLDER() throws Exception
    {
        Serializable ser = cmisService.getProperty(f0, CMISDictionaryModel.PROP_OBJECT_ID);
        String id = DefaultTypeConverter.INSTANCE.convert(String.class, ser);

        testQuery("SELECT * FROM cmis:folder WHERE IN_FOLDER('" + id + "')", 2, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder F WHERE IN_FOLDER(F, '" + id + "')", 2, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder X WHERE IN_FOLDER(F, '" + id + "')", 2, false, "cmis:objectId", new String(), true);

        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId WHERE IN_FOLDER(D, '" + id + "')", 1, false, "cmis:objectId",
                new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId WHERE IN_FOLDER('" + id + "')", 1, false, "cmis:objectId",
                new String(), true, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId WHERE IN_FOLDER('" + id + ";versionLabel"+"')", 0, false, "cmis:objectId",
                new String(), true, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        // Note folders are unversioned - using a versoin lablel should find nothing 
        testQuery("SELECT * FROM cmis:folder WHERE IN_FOLDER('woof://woof/woof')", 0, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:folder WHERE IN_FOLDER('woof://woof/woof;woof')", 0, false, "cmis:objectId", new String(), false);
    }

    public void testFTS() throws Exception
    {
        testQuery("SELECT SCORE(), D.* FROM cmis:document D WHERE D.cmis:contentStreamFileName = 'zebra'", doc_count-1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\\'zebra\\'') AND CONTAINS('\\'quick\\'')", doc_count-1, false, "cmis:objectId", new String(), true);
        testQuery("SELECT SCORE()as ONE, SCORE()as TWO, D.* FROM cmis:document D WHERE CONTAINS('\\'zebra\\'')", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\\'zebra\\'')", doc_count-1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\\'quick\\'')", 1, false, "cmis:objectId", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\\'quick\\'')", 1, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:document D WHERE CONTAINS(D, 'cmis:name:\\'Tutorial\\'')", 1, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT cmis:name as BOO FROM cmis:document D WHERE CONTAINS('BOO:\\'Tutorial\\'')", 1, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:document D WHERE CONTAINS('TEXT:\\'zebra\\'')", doc_count-1, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:document D WHERE CONTAINS('ALL:\\'zebra\\'')", doc_count-1, false, "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:document D WHERE CONTAINS('d:content:\\'zebra\\'')", doc_count-1, false, "cmis:objectId", new String(), false);
    }

    public void testScoreValues()
    {

        CMISQueryOptions options = new CMISQueryOptions("SELECT SCORE() AS ONE FROM cmis:document WHERE CONTAINS('cmis:name:\\'DD\\' and \\'Four\\'') AND cmis:name = 'DD\\''",
                rootNodeRef.getStoreRef());
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        options = new CMISQueryOptions("SELECT SCORE() AS ONE FROM cmis:document WHERE CONTAINS('\\'Four zebra durian\\'')", rootNodeRef.getStoreRef());
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;

        options = new CMISQueryOptions("SELECT SCORE() AS ONE FROM cmis:document WHERE CONTAINS('\\'Zebra\\'')", rootNodeRef.getStoreRef());
        rs = cmisQueryService.query(options);
        assertEquals(doc_count-1, rs.getLength());
        for (CMISResultSetRow row : rs)
        {
            System.out.println(row.getValue("cmis:objectId") + " Score " + row.getScore() + " " + row.getScores());
        }
        rs.close();
        rs = null;
    }

    public void testBasicSelectAsGuest() throws Exception
    {
        runAs("guest");
        testQuery("SELECT * FROM cmis:document", 0, false, "cmis:objectId", new String(), false);

    }

    public void testBasicSelectAsCmis() throws Exception
    {
        runAs("cmis");
        testQuery("SELECT * FROM cmis:document", doc_count-3, false, "cmis:objectId", new String(), false);

    }

    public void testBasicSelect() throws Exception
    {
        testQuery("SELECT * FROM cmis:document", doc_count, false, "cmis:objectId", new String(), false);
    }

    public void testBasicDefaultMetaData()
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document", rootNodeRef.getStoreRef());
        CMISResultSet rs = cmisQueryService.query(options);
        CMISResultSetMetaData md = rs.getMetaData();
        assertNotNull(md.getQueryOptions());
        CMISTypeDefinition typeDef = cmisDictionaryService.findType(CMISDictionaryModel.DOCUMENT_TYPE_ID);
        int count = 0;
        for (CMISPropertyDefinition pdef : typeDef.getPropertyDefinitions().values())
        {            
            count++;   
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

    public void testParse8() throws RecognitionException
    {
        String query = "SELECT * from ((FOLDER F JOIN RELATIONSHIP RL ON F.ID = RL.ID))";
        parse(query);
    }

    public void testDateFormattingErrors() throws Exception
    {
        testQuery("SELECT * FROM cm:lockable L WHERE L.cm:expiryDate =  TIMESTAMP '2012-12-12T12:12:12.012Z'", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM cm:lockable L WHERE L.cm:expiryDate =  TIMESTAMP '2012-012-12T12:12:12.012Z'", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM cm:lockable L WHERE L.cm:expiryDate =  TIMESTAMP '2012-2-12T12:12:12.012Z'", 0, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM cm:lockable L WHERE L.cm:expiryDate =  TIMESTAMP 'Mon Dec 12 12:12:12.012 GMT 2012'", 1, false, "cmis:objectId", new String(), true,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
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

    public void testAspectJoin() throws Exception
    {
        testQuery(
                "select o.*, t.* from ( cm:ownable o join cm:titled t on o.cmis:objectId = t.cmis:objectId JOIN CMIS:DOCUMENT AS D ON D.cmis:objectId = o.cmis:objectId  ) where o.cm:owner = 'andy' and t.cm:title = 'Alfresco tutorial' and CONTAINS(D, '\\'jumped\\'') and D.cmis:contentStreamLength <> 2",
                1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        testQuery("SELECT * FROM CM:OWNABLE", 1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM CM:OWNABLE where CM:oWNER = 'andy'", 1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM CM:OWNABLE where CM:OWNER = 'bob'", 0, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT D.*, O.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery(
                "SELECT D.*, O.*, T.* FROM CMIS:DOCUMENT AS D JOIN CM:OWNABLE AS O ON D.cmis:objectId = O.cmis:objectId JOIN CM:TITLED AS T ON T.cmis:objectId = D.cmis:objectId",
                1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
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
        testQuery("select o.*, t.* from ( cm:ownable o join cm:titled t on o.cmis:objectId = t.cmis:objectId  JOIN CMIS:DOCUMENT AS D ON D.cmis:objectId = o.cmis:objectId  )", 1,
                false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery(
                "select o.*, t.* from ( cm:ownable o join cm:titled t on o.cmis:objectId = t.cmis:objectId JOIN CMIS:DOCUMENT AS D ON D.cmis:objectId = o.cmis:objectId ) where o.cm:owner = 'andy' and t.cm:title = 'Alfresco tutorial' and CONTAINS(D, '\\'jumped\\'') and D.cmis:contentStreamLength <> 2",
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
            for (int max = 0; max < 20; max++)
            {
                doPage(expected, skip, max);
            }
        }

    }

    public void testFTSConnectives() throws Exception
    {
        testQuery("SELECT * FROM cmis:document where contains('\\'one\\' OR \\'zebra\\'')", doc_count-1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_STRICT);
        testQuery("SELECT * FROM cmis:document where contains('\\'one\\' or \\'zebra\\'')", doc_count-1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_STRICT);
        testQuery("SELECT * FROM cmis:document where contains('\\'one\\' \\'zebra\\'')", 1, false, "cmis:objectId", new String(), false, CMISQueryMode.CMS_STRICT);
        testQuery("SELECT * FROM cmis:document where contains('\\'one\\' and \\'zebra\\'')", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM cmis:document where contains('\\'one\\' or \\'zebra\\'')", doc_count-1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        testQuery("SELECT * FROM cmis:document where contains('\\'one\\'  \\'zebra\\'')", 1, false, "cmis:objectId", new String(), false,
                CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:document where contains('\\'one\\'  \\'zebra\\'')", rootNodeRef.getStoreRef());
        // options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM cmis:document where contains('\\'one\\'  \\'zebra\\'')", rootNodeRef.getStoreRef());
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        rs = cmisQueryService.query(options);
        assertEquals(doc_count-1, rs.length());
        rs.close();
    }

    public void testLikeEscaping() throws Exception
    {
        // TODO:
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco Tutorial'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco Tutoria_'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco T_______'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco T______\\_'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco T%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'Alfresco T\\%'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'GG*GG'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE '__*__'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE '%*%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'HH?HH'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE '__?__'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE '%?%'", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'AA%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'AA\\%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'A%'", 2, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'a%'", 2, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'A\\%'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'BB_'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'BB\\_'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'B__'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'B_\\_'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'B\\_\\_'", 0, false, "cmis:name", new String(), false);

        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'CC\\\\'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT cmis:name FROM cmis:document WHERE cmis:name     LIKE 'DD\\''", 1, false, "cmis:name", new String(), false);
    }

    public void testColumnAliasUse() throws Exception
    {
        testQuery("SELECT cmis:name as myname FROM cmis:document WHERE myname LIKE 'Alfresco Tutorial'", 1, false, "myname", new String(), false);
        testQuery("SELECT cmis:name as myname FROM cmis:document WHERE myname LIKE 'A%' order by myname", 2, false, "cmis:name", new String(), false);
        testExtendedQuery("SELECT SCORE() as myscore, D.cmis:name as myname FROM cmis:document D WHERE CONTAINS(D, 'myname:\\'Tutorial\\'') order by myscore", 1, false,
                "cmis:objectId", new String(), false);
        testExtendedQuery("SELECT SCORE() as myscore, D.cmis:name FROM cmis:document D WHERE CONTAINS(D, 'cmis:name:\\'Tutorial\\'') and myscore > 0.5 order by myscore", 9, false,
                "cmis:objectId", new String(), true);

    }

    private void doPage(List<String> expected, int skip, int max)
    {
        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM cmis:folder", rootNodeRef.getStoreRef());
        options.setSkipCount(skip);
        options.setMaxItems(max);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals("Skip = " + skip + " max  = " + max, skip + max > 10 ? 10 - skip : max, rs.getLength());
        assertEquals("Skip = " + skip + " max  = " + max, (skip + max) < 10, rs.hasMore());
        assertEquals("Skip = " + skip + " max  = " + max, skip, rs.getStart());
        int actualPosition = skip;
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("cmis:objectId");
            String id = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertEquals("Skip = " + skip + " max  = " + max + " actual = " + actualPosition, expected.get(actualPosition), id);
            actualPosition++;
        }
    }

    private CommonTree parse(String query) throws RecognitionException
    {
        CharStream cs = new ANTLRStringStream(query);
        CMISLexer lexer = new CMISLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CMISParser parser = new CMISParser(tokens);
        CommonTree queryNode = (CommonTree) parser.query().getTree();
        return queryNode;
    }

    public void test_d_text() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("test:singleTextBoth");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertEquals("Un tokenised", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("test:singleTextBoth");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:singleTextUntokenised");
            value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertEquals("Un tokenised", value);
            column = rs.getResultSetMetaData().getColumn("test:singleTextUntokenised");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:singleTextTokenised");
            value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertEquals("Un tokenised", value);
            column = rs.getResultSetMetaData().getColumn("test:singleTextTokenised");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleTextBoth");
            assert (sValue instanceof Collection<?>);
            Collection<String> collection = DefaultTypeConverter.INSTANCE.getCollection(String.class, sValue);
            assertEquals(2, collection.size());
            String[] members = new String[2];
            members = collection.toArray(members);
            assertEquals("Un tokenised", members[0]);
            assertEquals("two parts", members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleTextBoth");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleTextUntokenised");
            assert (sValue instanceof Collection<?>);
            collection = DefaultTypeConverter.INSTANCE.getCollection(String.class, sValue);
            assertEquals(2, collection.size());
            members = new String[2];
            members = collection.toArray(members);
            assertEquals("Un tokenised", members[0]);
            assertEquals("two parts", members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleTextUntokenised");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleTextTokenised");
            assert (sValue instanceof Collection<?>);
            collection = DefaultTypeConverter.INSTANCE.getCollection(String.class, sValue);
            assertEquals(2, collection.size());
            members = new String[2];
            members = collection.toArray(members);
            assertEquals("Un tokenised", members[0]);
            assertEquals("two parts", members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleTextTokenised");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        // d:text single

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth = 'Un tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth <> 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth LIKE 'U_ to%sed'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth NOT LIKE 't__eni%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'Un tokenised' =  ANY test:singleTextBoth ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleTextBoth IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleTextBoth NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth < 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth < 'Un tokenised'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth < 'V'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth < 'U'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth <= 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth <= 'Un tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth <= 'V'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth <= 'U'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth > 'tokenised'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth > 'Un tokenised'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth > 'V'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth > 'U'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth >= 'tokenised'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth >= 'Un tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth >= 'V'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextBoth >= 'U'", 1, false, "cmis:name", new String(), false);
        

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised = 'Un tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised <> 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised LIKE 'U_ to%sed'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised NOT LIKE 't__eni%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'Un tokenised' =  ANY test:singleTextUntokenised ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleTextUntokenised IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleTextUntokenised NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised < 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised < 'Un tokenised'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised < 'V'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised < 'U'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised <= 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised <= 'Un tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised <= 'V'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised <= 'U'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised > 'tokenised'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised > 'Un tokenised'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised > 'V'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised > 'U'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised >= 'tokenised'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised >= 'Un tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised >= 'V'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextUntokenised >= 'U'", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextTokenised = 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextTokenised <> 'tokenized'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextTokenised LIKE 'to%sed'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextTokenised NOT LIKE 'Ut__eniz%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextTokenised IN ('tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleTextTokenised NOT IN ('tokenized')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'tokenised' =  ANY test:singleTextTokenised ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleTextTokenised IN ('tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleTextTokenised NOT IN ('tokenized')", 1, false, "cmis:name", new String(), true);
        // Ranges do not make a lot of sense for tokenized fields

        // d:text single by alias

        testQuery("SELECT T.test:singleTextBoth as alias FROM test:extendedContent as T WHERE alias = 'Un tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextBoth as alias FROM test:extendedContent as T WHERE alias <> 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextBoth as alias FROM test:extendedContent as T WHERE alias LIKE 'U_ to%sed'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextBoth as alias FROM test:extendedContent as T WHERE alias NOT LIKE 't__eni%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextBoth as alias FROM test:extendedContent as T WHERE alias IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextBoth as alias FROM test:extendedContent as T WHERE alias NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextBoth as alias FROM test:extendedContent as T WHERE 'Un tokenised' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT T.test:singleTextBoth as alias FROM test:extendedContent as T WHERE ANY alias IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT T.test:singleTextBoth as alias FROM test:extendedContent as T WHERE ANY alias NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), true);

        testQuery("SELECT T.test:singleTextUntokenised as alias FROM test:extendedContent as T WHERE alias = 'Un tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextUntokenised as alias FROM test:extendedContent as T WHERE alias <> 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextUntokenised as alias FROM test:extendedContent as T WHERE alias LIKE 'U_ to%sed'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextUntokenised as alias FROM test:extendedContent as T WHERE alias NOT LIKE 't__eni%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextUntokenised as alias FROM test:extendedContent as T WHERE alias IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(),
                false);
        testQuery("SELECT T.test:singleTextUntokenised as alias FROM test:extendedContent as T WHERE alias NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:singleTextUntokenised as alias FROM test:extendedContent as T WHERE 'Un tokenised' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT T.test:singleTextUntokenised as alias FROM test:extendedContent as T WHERE ANY alias IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(),
                true);
        testQuery("SELECT T.test:singleTextUntokenised as alias FROM test:extendedContent as T WHERE ANY alias NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), true);

        testQuery("SELECT test:singleTextTokenised as alias FROM test:extendedContent WHERE alias = 'tokenised'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleTextTokenised as alias FROM test:extendedContent WHERE alias <> 'tokenized'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleTextTokenised as alias FROM test:extendedContent WHERE alias LIKE 'to%sed'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleTextTokenised as alias FROM test:extendedContent WHERE alias NOT LIKE 'Ut__eniz%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleTextTokenised as alias FROM test:extendedContent WHERE alias IN ('tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleTextTokenised as alias FROM test:extendedContent WHERE alias NOT IN ('tokenized')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleTextTokenised as alias FROM test:extendedContent WHERE 'tokenised' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleTextTokenised as alias FROM test:extendedContent WHERE ANY alias IN ('tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleTextTokenised as alias FROM test:extendedContent WHERE ANY alias NOT IN ('tokenized')", 1, false, "cmis:name", new String(), true);

        // d:text multiple

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextBoth = 'Un tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextBoth <> 'tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextBoth LIKE 'U_ to%sed'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextBoth NOT LIKE 't__eni%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextBoth IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextBoth NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'Un tokenised' =  ANY test:multipleTextBoth ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleTextBoth IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleTextBoth NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextUntokenised = 'Un tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextUntokenised <> 'tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextUntokenised LIKE 'U_ to%sed'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextUntokenised NOT LIKE 't__eni%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextUntokenised IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextUntokenised NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'Un tokenised' =  ANY test:multipleTextUntokenised ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleTextUntokenised IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleTextUntokenised NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextTokenised = 'tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextTokenised <> 'tokenized'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextTokenised LIKE 'to%sed'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextTokenised NOT LIKE 'Ut__eniz%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextTokenised IN ('tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleTextTokenised NOT IN ('tokenized')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'tokenised' =  ANY test:multipleTextTokenised ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleTextTokenised IN ('tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleTextTokenised NOT IN ('tokenized')", 1, false, "cmis:name", new String(), false);

        // d:text multiple by alias

        testQuery("SELECT test:multipleTextBoth as alias FROM test:extendedContent WHERE alias = 'Un tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextBoth as alias FROM test:extendedContent WHERE alias <> 'tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextBoth as alias FROM test:extendedContent WHERE alias LIKE 'U_ to%sed'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextBoth as alias FROM test:extendedContent WHERE alias NOT LIKE 't__eni%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextBoth as alias FROM test:extendedContent WHERE alias IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextBoth as alias FROM test:extendedContent WHERE alias NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextBoth as alias FROM test:extendedContent WHERE 'Un tokenised' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleTextBoth as alias FROM test:extendedContent WHERE ANY alias IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleTextBoth as alias FROM test:extendedContent WHERE ANY alias NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT test:multipleTextUntokenised alias FROM test:extendedContent WHERE alias = 'Un tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextUntokenised alias FROM test:extendedContent WHERE alias <> 'tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextUntokenised alias FROM test:extendedContent WHERE alias LIKE 'U_ to%sed'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextUntokenised alias FROM test:extendedContent WHERE alias NOT LIKE 't__eni%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextUntokenised alias FROM test:extendedContent WHERE alias IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextUntokenised alias FROM test:extendedContent WHERE alias NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleTextUntokenised alias FROM test:extendedContent WHERE 'Un tokenised' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleTextUntokenised alias FROM test:extendedContent WHERE ANY alias IN ('Un tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleTextUntokenised alias FROM test:extendedContent WHERE ANY alias NOT IN ('Un tokenized')", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT T.test:multipleTextTokenised alias FROM test:extendedContent T WHERE alias = 'tokenised'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT T.test:multipleTextTokenised alias FROM test:extendedContent T WHERE alias <> 'tokenized'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT T.test:multipleTextTokenised alias FROM test:extendedContent T WHERE alias LIKE 'to%sed'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT T.test:multipleTextTokenised alias FROM test:extendedContent T WHERE alias NOT LIKE 'Ut__eniz%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT T.test:multipleTextTokenised alias FROM test:extendedContent T WHERE alias IN ('tokenised', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT T.test:multipleTextTokenised alias FROM test:extendedContent T WHERE alias NOT IN ('tokenized')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT T.test:multipleTextTokenised alias FROM test:extendedContent T WHERE 'tokenised' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:multipleTextTokenised alias FROM test:extendedContent T WHERE ANY alias IN ('tokenised', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT T.test:multipleTextTokenised alias FROM test:extendedContent T WHERE ANY alias NOT IN ('tokenized')", 1, false, "cmis:name", new String(), false);
    }

    public void test_locale() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'AAAA BBBB'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setLocales(Collections.singletonList(Locale.ENGLISH));
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'AAAA BBBB'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setLocales(Collections.singletonList(Locale.FRENCH));
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'AAAA BBBB'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setLocales(Collections.singletonList(Locale.FRENCH));
        options.setMlAnalaysisMode(MLAnalysisMode.ALL_LANGUAGES);
        rs = cmisQueryService.query(options);
        assertEquals(0, rs.length());
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'CCCC DDDD'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setLocales(Collections.singletonList(Locale.ENGLISH));
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'CCCC DDDD'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setLocales(Collections.singletonList(Locale.ENGLISH));
        options.setMlAnalaysisMode(MLAnalysisMode.ALL_LANGUAGES);
        rs = cmisQueryService.query(options);
        assertEquals(0, rs.length());
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'CCCC DDDD'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setLocales(Collections.singletonList(Locale.FRENCH));
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'CCCC DDDD'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setLocales(Collections.singletonList(Locale.FRENCH));
        options.setIncludeInTransactionData(false);
        rs = cmisQueryService.query(options);
        assertEquals(0, rs.length());
        rs.close();

        I18NUtil.setLocale(Locale.UK);

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'AAAA BBBB'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setMlAnalaysisMode(MLAnalysisMode.ALL_LANGUAGES);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'CCCC DDDD'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setMlAnalaysisMode(MLAnalysisMode.ALL_LANGUAGES);
        rs = cmisQueryService.query(options);
        assertEquals(0, rs.length());
        rs.close();

        I18NUtil.setLocale(Locale.FRANCE);

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'AAAA BBBB'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setMlAnalaysisMode(MLAnalysisMode.ALL_LANGUAGES);
        rs = cmisQueryService.query(options);
        assertEquals(0, rs.length());
        rs.close();

        options = new CMISQueryOptions("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'CCCC DDDD'", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        options.setMlAnalaysisMode(MLAnalysisMode.ALL_LANGUAGES);
        rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        rs.close();
    }

    public void test_d_mltext() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("test:singleMLTextBoth");
            String value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertEquals("AAAA BBBB", value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("test:singleMLTextBoth");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:singleMLTextUntokenised");
            value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertEquals("AAAA BBBB", value);
            column = rs.getResultSetMetaData().getColumn("test:singleMLTextUntokenised");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:singleMLTextTokenised");
            value = DefaultTypeConverter.INSTANCE.convert(String.class, sValue);
            assertEquals("AAAA BBBB", value);
            column = rs.getResultSetMetaData().getColumn("test:singleMLTextTokenised");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleMLTextBoth");
            assert (sValue instanceof Collection<?>);
            Collection<String> collection = DefaultTypeConverter.INSTANCE.getCollection(String.class, sValue);
            assertEquals(1, collection.size());
            String[] members = new String[1];
            members = collection.toArray(members);
            assertEquals("AAAA BBBB", members[0]);
            column = rs.getResultSetMetaData().getColumn("test:multipleMLTextBoth");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleMLTextUntokenised");
            assert (sValue instanceof Collection<?>);
            collection = DefaultTypeConverter.INSTANCE.getCollection(String.class, sValue);
            assertEquals(1, collection.size());
            members = new String[1];
            members = collection.toArray(members);
            assertEquals("AAAA BBBB", members[0]);
            column = rs.getResultSetMetaData().getColumn("test:multipleMLTextUntokenised");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleMLTextTokenised");
            assert (sValue instanceof Collection<?>);
            collection = DefaultTypeConverter.INSTANCE.getCollection(String.class, sValue);
            assertEquals(1, collection.size());
            members = new String[1];
            members = collection.toArray(members);
            assertEquals("AAAA BBBB", members[0]);
            column = rs.getResultSetMetaData().getColumn("test:multipleMLTextTokenised");
            assertEquals(CMISDataTypeEnum.STRING, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        // Note language agnostic tokenisation included in the default settings includes matches you may not expect
        // Corss language search support
        
        // d:mltext single

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'AAAA BBBB'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'AAAA'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = '%AAAA'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = '%AAA'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'BBBB'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth = 'CCCC DDDD'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth <> 'EEEE FFFF'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth LIKE 'AAA_ B%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth LIKE 'CCC_ D%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth NOT LIKE 'B%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth NOT LIKE 'D%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextBoth NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'AAAA BBBB' =  ANY test:singleMLTextBoth ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'CCCC DDDD' =  ANY test:singleMLTextBoth ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextBoth IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextBoth IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextBoth NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), true);

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised = 'AAAA BBBB'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised = 'CCCC DDDD'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised <> 'EEEE FFFF'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised LIKE 'AAA_ B%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised LIKE 'CCC_ D%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised NOT LIKE 'B%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised NOT LIKE 'D%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextUntokenised NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'AAAA BBBB' =  ANY test:singleMLTextUntokenised ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'CCCC DDDD' =  ANY test:singleMLTextUntokenised ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextUntokenised IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextUntokenised IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextUntokenised NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), true);

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised = 'AAAA'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised = 'BBBB'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised = 'CCCC'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised = 'DDDD'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised <> 'EEEE'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised LIKE 'A%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised LIKE '_B__'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised LIKE '%C'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised LIKE 'D%D'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised NOT LIKE 'CCCC_'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised IN ('AAAA', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised IN ('BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised IN ('CCCC', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised IN ('DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleMLTextTokenised NOT IN ('EEEE')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'AAAA' =  ANY test:singleMLTextTokenised ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'BBBB' =  ANY test:singleMLTextTokenised ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'CCCC' =  ANY test:singleMLTextTokenised ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'DDDD' =  ANY test:singleMLTextTokenised ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextTokenised IN ('AAAA', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextTokenised IN ('BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextTokenised IN ('CCCC', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextTokenised IN ('DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleMLTextTokenised NOT IN ('EEEE')", 1, false, "cmis:name", new String(), true);

        // d:mltext single by alias

        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias = 'AAAA BBBB'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias = 'AAAA'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias = 'BBBB'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias = 'CCCC DDDD'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias <> 'EEEE FFFF'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias LIKE 'AAA_ B%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias LIKE 'CCC_ D%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias NOT LIKE 'B%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias NOT LIKE 'D%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE alias NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE 'AAAA BBBB' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE 'CCCC DDDD' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE ANY alias IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE ANY alias IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextBoth as alias FROM test:extendedContent WHERE ANY alias NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), true);

        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias = 'AAAA BBBB'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias = 'CCCC DDDD'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias <> 'EEEE FFFF'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias LIKE 'AAA_ B%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias LIKE 'CCC_ D%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias NOT LIKE 'B%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias NOT LIKE 'D%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE alias NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE 'AAAA BBBB' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE 'CCCC DDDD' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE ANY alias IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE ANY alias IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextUntokenised as alias FROM test:extendedContent WHERE ANY alias NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), true);

        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias = 'AAAA'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias = 'BBBB'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias = 'CCCC'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias = 'DDDD'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias <> 'EEEE'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias LIKE 'A%'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias LIKE '_B__'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias LIKE '%C'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias LIKE 'D%D'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias NOT LIKE 'CCCC_'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias IN ('AAAA', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias IN ('BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias IN ('CCCC', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias IN ('DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE alias NOT IN ('EEEE')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE 'AAAA' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE 'BBBB' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE 'CCCC' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE 'DDDD' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE ANY alias IN ('AAAA', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE ANY alias IN ('BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE ANY alias IN ('CCCC', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE ANY alias IN ('DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleMLTextTokenised as alias FROM test:extendedContent WHERE ANY alias NOT IN ('EEEE')", 1, false, "cmis:name", new String(), true);

        // d:mltext multiple

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth = 'AAAA BBBB'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth = 'AAAA'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth = 'BBBB'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth = 'CCCC DDDD'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth <> 'EEEE FFFF'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth LIKE 'AAA_ B%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth LIKE 'CCC_ D%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth NOT LIKE 'B%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth NOT LIKE 'D%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextBoth NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'AAAA BBBB' =  ANY test:multipleMLTextBoth ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'CCCC DDDD' =  ANY test:multipleMLTextBoth ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextBoth IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextBoth IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextBoth NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised = 'AAAA BBBB'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised = 'CCCC DDDD'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised <> 'EEEE FFFF'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised LIKE 'AAA_ B%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised LIKE 'CCC_ D%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised NOT LIKE 'B%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised NOT LIKE 'D%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextUntokenised NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'AAAA BBBB' =  ANY test:multipleMLTextUntokenised ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'CCCC DDDD' =  ANY test:multipleMLTextUntokenised ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextUntokenised IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextUntokenised IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextUntokenised NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised = 'AAAA'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised = 'BBBB'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised = 'CCCC'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised = 'DDDD'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised <> 'EEEE'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised LIKE 'A%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised LIKE '_B__'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised LIKE '%C'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised LIKE 'D%D'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised NOT LIKE 'CCCC_'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised IN ('AAAA', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised IN ('BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised IN ('CCCC', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised IN ('DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleMLTextTokenised NOT IN ('EEEE')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE 'AAAA' =  ANY test:multipleMLTextTokenised ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'BBBB' =  ANY test:multipleMLTextTokenised ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'CCCC' =  ANY test:multipleMLTextTokenised ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE 'DDDD' =  ANY test:multipleMLTextTokenised ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextTokenised IN ('AAAA', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextTokenised IN ('BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextTokenised IN ('CCCC', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextTokenised IN ('DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleMLTextTokenised NOT IN ('EEEE')", 1, false, "cmis:name", new String(), false);

        // d:mltext multiple by alias

        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias = 'AAAA BBBB'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias = 'AAAA'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias = 'BBBB'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias = 'CCCC DDDD'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias <> 'EEEE FFFF'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias LIKE 'AAA_ B%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias LIKE 'CCC_ D%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias NOT LIKE 'B%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias NOT LIKE 'D%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE alias NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE 'AAAA BBBB' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE 'CCCC DDDD' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE ANY alias IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE ANY alias IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextBoth alias FROM test:extendedContent WHERE ANY alias NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias = 'AAAA BBBB'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias = 'CCCC DDDD'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias <> 'EEEE FFFF'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias LIKE 'AAA_ B%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias LIKE 'CCC_ D%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias NOT LIKE 'B%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias NOT LIKE 'D%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE alias NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE 'AAAA BBBB' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE 'CCCC DDDD' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE ANY alias IN ('AAAA BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE ANY alias IN ('CCCC DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextUntokenised alias FROM test:extendedContent WHERE ANY alias NOT IN ('EEEE FFFF')", 1, false, "cmis:name", new String(), false);

        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias = 'AAAA'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias = 'BBBB'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias = 'CCCC'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias = 'DDDD'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias <> 'EEEE'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias LIKE 'A%'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias LIKE '_B__'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias LIKE '%C'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias LIKE 'D%D'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias NOT LIKE 'CCCC_'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias IN ('AAAA', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias IN ('BBBB', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias IN ('CCCC', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias IN ('DDDD', 'Monkey')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE alias NOT IN ('EEEE')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE 'AAAA' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE 'BBBB' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE 'CCCC' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE 'DDDD' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE ANY alias IN ('AAAA', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE ANY alias IN ('BBBB', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE ANY alias IN ('CCCC', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE ANY alias IN ('DDDD', 'Monkey')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleMLTextTokenised alias FROM test:extendedContent WHERE ANY alias NOT IN ('EEEE')", 1, false, "cmis:name", new String(), false);
    }

    public void test_d_float() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("test:singleFloat");
            Float value = DefaultTypeConverter.INSTANCE.convert(Float.class, sValue);
            assertEquals(1.0f, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("test:singleFloat");
            assertEquals(CMISDataTypeEnum.DECIMAL, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleFloat");
            assert (sValue instanceof Collection<?>);
            Collection<Float> collection = DefaultTypeConverter.INSTANCE.getCollection(Float.class, sValue);
            assertEquals(2, collection.size());
            Float[] members = new Float[2];
            members = collection.toArray(members);
            assertEquals(1.0f, members[0]);
            assertEquals(1.1f, members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleFloat");
            assertEquals(CMISDataTypeEnum.DECIMAL, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        // d:float single

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat = 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat = 1.1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat <> 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat <> 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat < 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat < 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat <= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat <= 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat > 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat > 0.9", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat >= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat >= 0.9", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleFloat NOT IN (1.1)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE '1' =  ANY test:singleFloat ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE '1.1' =  ANY test:singleFloat ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleFloat IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleFloat NOT IN (1.1, 2.2)", 1, false, "cmis:name", new String(), true);

        // d:float single by alias

        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias = 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias = 1.1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias <> 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias <> 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias < 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias < 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias <= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias <= 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias > 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias > 0.9", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias >= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias >= 0.9", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE alias NOT IN (1.1)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE '1' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE '1.1' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE ANY alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleFloat as alias FROM test:extendedContent WHERE ANY alias NOT IN (1.1, 2.2)", 1, false, "cmis:name", new String(), true);

        // d:float multiple

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat = 1.1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat <> 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat < 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat <= 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat > 0.9", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat >= 0.9", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleFloat NOT IN (1.1)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE '1' =  ANY test:multipleFloat ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE '1.1' =  ANY test:multipleFloat ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleFloat IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleFloat IN (1.1, 2.2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleFloat NOT IN (1.1, 2.2)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleFloat NOT IN (1.3, 2.3)", 1, false, "cmis:name", new String(), false);

        // d:float multiple by alias

        testQuery("SELECT test:multipleFloat as alias  FROM test:extendedContent WHERE alias = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias = 1.1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias <> 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias < 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias <= 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias > 0.9", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias >= 0.9", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE alias NOT IN (1.1)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE '1' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE '1.1' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE ANY alias IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE ANY alias IN (1.1, 2.2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE ANY alias NOT IN (1.1, 2.2)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleFloat as alias FROM test:extendedContent WHERE ANY alias NOT IN (1.3, 2.3)", 1, false, "cmis:name", new String(), false);
    }

    public void test_d_double() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("test:singleDouble");
            Double value = DefaultTypeConverter.INSTANCE.convert(Double.class, sValue);
            assertEquals(1.0d, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("test:singleDouble");
            assertEquals(CMISDataTypeEnum.DECIMAL, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleDouble");
            assert (sValue instanceof Collection<?>);
            Collection<Double> collection = DefaultTypeConverter.INSTANCE.getCollection(Double.class, sValue);
            assertEquals(2, collection.size());
            Double[] members = new Double[2];
            members = collection.toArray(members);
            assertEquals(1.0d, members[0]);
            assertEquals(1.1d, members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleDouble");
            assertEquals(CMISDataTypeEnum.DECIMAL, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        // d:double single

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble = 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble = 1.1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble <> 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble <> 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble < 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble < 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble <= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble <= 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble > 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble > 0.9", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble >= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble >= 0.9", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDouble NOT IN (1.1)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE '1' =  ANY test:singleDouble ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE '1.1' =  ANY test:singleDouble ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleDouble IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleDouble NOT IN (1.1, 2.2)", 1, false, "cmis:name", new String(), true);

        // d:double single by alias

        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias = 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias = 1.1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias <> 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias <> 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias < 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias < 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias <= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias <= 1.1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias > 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias > 0.9", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias >= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias >= 0.9", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE alias NOT IN (1.1)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE '1' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE '1.1' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE ANY alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDouble alias FROM test:extendedContent WHERE ANY alias NOT IN (1.1, 2.2)", 1, false, "cmis:name", new String(), true);

        // d:double multiple

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble = 1.1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble <> 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble < 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble <= 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble > 0.9", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble >= 0.9", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDouble NOT IN (1.1)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE '1' =  ANY test:multipleDouble ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE '1.1' =  ANY test:multipleDouble ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDouble IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDouble IN (1.1, 2.2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDouble NOT IN (1.1, 2.2)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDouble NOT IN (1.3, 2.3)", 1, false, "cmis:name", new String(), false);

        // d:double multiple by alias

        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias = 1.1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias <> 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias < 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias <= 1.1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias > 0.9", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias >= 0.9", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE alias NOT IN (1.1)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE '1' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE '1.1' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE ANY alias IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE ANY alias IN (1.1, 2.2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE ANY alias NOT IN (1.1, 2.2)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleDouble alias FROM test:extendedContent WHERE ANY alias NOT IN (1.3, 2.3)", 1, false, "cmis:name", new String(), false);
    }

    public void test_d_int() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("test:singleInteger");
            Integer value = DefaultTypeConverter.INSTANCE.convert(Integer.class, sValue);
            assertEquals(Integer.valueOf(1), value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("test:singleInteger");
            assertEquals(CMISDataTypeEnum.INTEGER, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleInteger");
            assert (sValue instanceof Collection<?>);
            Collection<Integer> collection = DefaultTypeConverter.INSTANCE.getCollection(Integer.class, sValue);
            assertEquals(2, collection.size());
            Integer[] members = new Integer[2];
            members = collection.toArray(members);
            assertEquals(Integer.valueOf(1), members[0]);
            assertEquals(Integer.valueOf(2), members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleInteger");
            assertEquals(CMISDataTypeEnum.INTEGER, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        // d:int single

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger = 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger = 2", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger <> 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger <> 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger < 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger < 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger <= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger <= 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger > 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger > 0", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger >= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger >= 0", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleInteger NOT IN (2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE '1' =  ANY test:singleInteger ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE '2' =  ANY test:singleInteger ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleInteger IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleInteger NOT IN (2, 3)", 1, false, "cmis:name", new String(), true);

        // d:int single by alias

        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias = 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias = 2", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias <> 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias <> 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias < 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias < 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias <= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias <= 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias > 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias > 0", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias >= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias >= 0", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE alias NOT IN (2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE '1' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE '2' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE ANY alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleInteger alias FROM test:extendedContent WHERE ANY alias NOT IN (2, 3)", 1, false, "cmis:name", new String(), true);

        // d:int multiple

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger = 2", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger <> 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger < 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger <= 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger > 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger >= 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleInteger NOT IN (2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE '1' =  ANY test:multipleInteger ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE '2' =  ANY test:multipleInteger ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleInteger IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleInteger IN (2, 3)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleInteger NOT IN (1, 2)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleInteger NOT IN (2, 3)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleInteger NOT IN (3, 4)", 1, false, "cmis:name", new String(), false);

        // d:int multiple by alias

        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias = 2", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias <> 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias < 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias <= 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias > 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias >= 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE alias NOT IN (2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE '1' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE '2' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE ANY alias IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE ANY alias IN (2, 3)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE ANY alias NOT IN (1, 2)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE ANY alias NOT IN (2, 3)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleInteger as alias FROM test:extendedContent WHERE ANY alias NOT IN (3, 4)", 1, false, "cmis:name", new String(), false);
    }

    public void test_d_long() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("test:singleLong");
            Long value = DefaultTypeConverter.INSTANCE.convert(Long.class, sValue);
            assertEquals(Long.valueOf(1), value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("test:singleLong");
            assertEquals(CMISDataTypeEnum.INTEGER, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleLong");
            assert (sValue instanceof Collection<?>);
            Collection<Long> collection = DefaultTypeConverter.INSTANCE.getCollection(Long.class, sValue);
            assertEquals(2, collection.size());
            Long[] members = new Long[2];
            members = collection.toArray(members);
            assertEquals(Long.valueOf(1), members[0]);
            assertEquals(Long.valueOf(2), members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleLong");
            assertEquals(CMISDataTypeEnum.INTEGER, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        // d:long single

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong = 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong = 2", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong <> 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong <> 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong < 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong < 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong <= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong <= 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong > 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong > 0", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong >= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong >= 0", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleLong NOT IN (2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE '1' =  ANY test:singleLong ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE '2' =  ANY test:singleLong ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleLong IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleLong NOT IN (2, 3)", 1, false, "cmis:name", new String(), true);

        // d:long single by alias

        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias = 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias = 2", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias <> 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias <> 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias < 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias < 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias <= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias <= 2", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias > 1", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias > 0", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias >= 1", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias >= 0", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE alias NOT IN (2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE '1' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE '2' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE ANY alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleLong as alias FROM test:extendedContent WHERE ANY alias NOT IN (2, 3)", 1, false, "cmis:name", new String(), true);

        // d:long multiple

        testQuery("SELECT alias FROM test:extendedContent WHERE test:multipleLong = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong = 2", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong <> 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong < 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong <= 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong > 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong >= 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleLong NOT IN (2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE '1' =  ANY test:multipleLong ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE '2' =  ANY test:multipleLong ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleLong IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleLong IN (2, 3)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleLong NOT IN (1, 2)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleLong NOT IN (2, 3)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleLong NOT IN (3, 4)", 1, false, "cmis:name", new String(), false);

        // d:long multiple by alias

        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias = 2", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias <> 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias < 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias <= 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias > 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias >= 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE alias NOT IN (2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE '1' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE '2' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE ANY alias IN (1, 2)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE ANY alias IN (2, 3)", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE ANY alias NOT IN (1, 2)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE ANY alias NOT IN (2, 3)", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleLong alias FROM test:extendedContent WHERE ANY alias NOT IN (3, 4)", 1, false, "cmis:name", new String(), false);
    }

    public void test_d_date() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("test:singleDate");
            Date value = DefaultTypeConverter.INSTANCE.convert(Date.class, sValue);
            assertEquals(date1, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("test:singleDate");
            assertEquals(CMISDataTypeEnum.DATETIME, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleDate");
            assert (sValue instanceof Collection<?>);
            Collection<Date> collection = DefaultTypeConverter.INSTANCE.getCollection(Date.class, sValue);
            assertEquals(2, collection.size());
            Date[] members = new Date[2];
            members = collection.toArray(members);
            assertEquals(date1, members[0]);
            assertEquals(date2, members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleDate");
            assertEquals(CMISDataTypeEnum.DATETIME, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        // d:date single

        SimpleDateFormat df1 = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSSZ");

        String d0 = df1.format(date0);
        StringBuilder builder = new StringBuilder();
        builder.append(d0);
        builder.insert(builder.length() - 2, ':');
        d0 = builder.toString();

        String d1 = df1.format(date1);
        builder = new StringBuilder();
        builder.append(d1);
        builder.insert(builder.length() - 2, ':');
        d1 = builder.toString();

        String d2 = df1.format(date2);
        builder = new StringBuilder();
        builder.append(d2);
        builder.insert(builder.length() - 2, ':');
        d2 = builder.toString();

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate = TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate = TIMESTAMP '" + d2 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate <> TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate <> TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate < TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate < TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate <= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate <= TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate > TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate > TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate >= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate >= TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate LIKE TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate NOT LIKE TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate IN (TIMESTAMP '" + d0 + "' ,TIMESTAMP '" + d1 + "')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDate NOT IN (TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE TIMESTAMP '" + d1 + "' =  ANY test:singleDate ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE TIMESTAMP '" + d2 + "' =  ANY test:singleDate ", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleDate IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleDate NOT IN (TIMESTAMP '" + d0 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), true);

        // d:date single by alias

        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias = TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias = TIMESTAMP '" + d2 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias <> TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias <> TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias < TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias < TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias <= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias <= TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias > TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias > TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias >= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias >= TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias LIKE TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias NOT LIKE TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias IN (TIMESTAMP '" + d0 + "' ,TIMESTAMP '" + d1 + "')", 1, false, "cmis:name", new String(),
                false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE alias NOT IN (TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE TIMESTAMP '" + d1 + "' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE TIMESTAMP '" + d2 + "' =  ANY alias ", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE ANY alias IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name",
                new String(), true);
        testQuery("SELECT test:singleDate as alias FROM test:extendedContent WHERE ANY alias NOT IN (TIMESTAMP '" + d0 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name",
                new String(), true);

        // d:date multiple

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate = TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate = TIMESTAMP '" + d2 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate <> TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate <> TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate < TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate < TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate <= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate <= TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate > TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate > TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate >= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate >= TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate LIKE TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate NOT LIKE TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDate NOT IN (TIMESTAMP '" + d1 + "')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE TIMESTAMP '" + d1 + "' =  ANY test:multipleDate ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE TIMESTAMP '" + d2 + "' =  ANY test:multipleDate ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDate IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDate IN (TIMESTAMP '" + d2 + "', TIMESTAMP '" + d0 + "')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDate NOT IN (TIMESTAMP '" + d0 + "', TIMESTAMP '" + d1 + "')", 0, false, "cmis:name", new String(),
                false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDate NOT IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 0, false, "cmis:name", new String(),
                false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDate NOT IN (TIMESTAMP '" + d0 + "')", 1, false, "cmis:name", new String(), false);

        // d:date multiple by alias

        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias = TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias = TIMESTAMP '" + d2 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias <> TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias <> TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias < TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias < TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias <= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias <= TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias > TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias > TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias >= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias >= TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias LIKE TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias NOT LIKE TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(),
                true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE alias NOT IN (TIMESTAMP '" + d1 + "')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE TIMESTAMP '" + d1 + "' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE TIMESTAMP '" + d2 + "' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE ANY alias IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name",
                new String(), false);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE ANY alias IN (TIMESTAMP '" + d2 + "', TIMESTAMP '" + d0 + "')", 1, false, "cmis:name",
                new String(), false);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE ANY alias NOT IN (TIMESTAMP '" + d0 + "', TIMESTAMP '" + d1 + "')", 0, false, "cmis:name",
                new String(), false);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE ANY alias NOT IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 0, false, "cmis:name",
                new String(), false);
        testQuery("SELECT test:multipleDate alias FROM test:extendedContent WHERE ANY alias NOT IN (TIMESTAMP '" + d0 + "')", 1, false, "cmis:name", new String(), false);

    }

    public void test_d_datetime() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("test:singleDatetime");
            Date value = DefaultTypeConverter.INSTANCE.convert(Date.class, sValue);
            assertEquals(date1, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("test:singleDatetime");
            assertEquals(CMISDataTypeEnum.DATETIME, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleDatetime");
            assert (sValue instanceof Collection<?>);
            Collection<Date> collection = DefaultTypeConverter.INSTANCE.getCollection(Date.class, sValue);
            assertEquals(2, collection.size());
            Date[] members = new Date[2];
            members = collection.toArray(members);
            assertEquals(date1, members[0]);
            assertEquals(date2, members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleDatetime");
            assertEquals(CMISDataTypeEnum.DATETIME, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        SimpleDateFormat df1 = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSSZ");

        String d0 = df1.format(date0);
        StringBuilder builder = new StringBuilder();
        builder.append(d0);
        builder.insert(builder.length() - 2, ':');
        d0 = builder.toString();

        String d1 = df1.format(date1);
        builder = new StringBuilder();
        builder.append(d1);
        builder.insert(builder.length() - 2, ':');
        d1 = builder.toString();

        String d2 = df1.format(date2);
        builder = new StringBuilder();
        builder.append(d2);
        builder.insert(builder.length() - 2, ':');
        d2 = builder.toString();

        // d:datetime single

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime = TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime = TIMESTAMP '" + d2 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime <> TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime <> TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime < TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime < TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime <= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime <= TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime > TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime > TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime >= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime >= TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime LIKE TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime NOT LIKE TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime IN (TIMESTAMP '" + d0 + "' ,TIMESTAMP '" + d1 + "')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleDatetime NOT IN (TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE TIMESTAMP '" + d1 + "' =  ANY test:singleDatetime ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE TIMESTAMP '" + d2 + "' =  ANY test:singleDatetime ", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleDatetime IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleDatetime NOT IN (TIMESTAMP '" + d0 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(),
                true);

        // d:datetime single by alias

        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias = TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias = TIMESTAMP '" + d2 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias <> TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias <> TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias < TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias < TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias <= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias <= TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias > TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias > TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias >= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias >= TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias LIKE TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias NOT LIKE TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias IN (TIMESTAMP '" + d0 + "' ,TIMESTAMP '" + d1 + "')", 1, false, "cmis:name",
                new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE alias NOT IN (TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE TIMESTAMP '" + d1 + "' =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE TIMESTAMP '" + d2 + "' =  ANY alias ", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE ANY alias IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name",
                new String(), true);
        testQuery("SELECT test:singleDatetime alias FROM test:extendedContent WHERE ANY alias NOT IN (TIMESTAMP '" + d0 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name",
                new String(), true);

        // d:date multiple

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime = TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime = TIMESTAMP '" + d2 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime <> TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime <> TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime < TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime < TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime <= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime <= TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime > TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime > TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime >= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime >= TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime LIKE TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime NOT LIKE TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleDatetime NOT IN (TIMESTAMP '" + d1 + "')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE TIMESTAMP '" + d1 + "' =  ANY test:multipleDatetime ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE TIMESTAMP '" + d2 + "' =  ANY test:multipleDatetime ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDatetime IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name", new String(),
                false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDatetime IN (TIMESTAMP '" + d2 + "', TIMESTAMP '" + d0 + "')", 1, false, "cmis:name", new String(),
                false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDatetime NOT IN (TIMESTAMP '" + d0 + "', TIMESTAMP '" + d1 + "')", 0, false, "cmis:name",
                new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDatetime NOT IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 0, false, "cmis:name",
                new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleDatetime NOT IN (TIMESTAMP '" + d0 + "')", 1, false, "cmis:name", new String(), false);

        // d:date multiple by alias

        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias = TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias = TIMESTAMP '" + d2 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias <> TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias <> TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias < TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias < TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias <= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias <= TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias > TIMESTAMP '" + d1 + "'", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias > TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias >= TIMESTAMP '" + d1 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias >= TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias LIKE TIMESTAMP '" + d0 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias NOT LIKE TIMESTAMP '" + d2 + "'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name",
                new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE alias NOT IN (TIMESTAMP '" + d1 + "')", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE TIMESTAMP '" + d1 + "' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE TIMESTAMP '" + d2 + "' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE ANY alias IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 1, false, "cmis:name",
                new String(), false);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE ANY alias IN (TIMESTAMP '" + d2 + "', TIMESTAMP '" + d0 + "')", 1, false, "cmis:name",
                new String(), false);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE ANY alias NOT IN (TIMESTAMP '" + d0 + "', TIMESTAMP '" + d1 + "')", 0, false, "cmis:name",
                new String(), false);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE ANY alias NOT IN (TIMESTAMP '" + d1 + "', TIMESTAMP '" + d2 + "')", 0, false, "cmis:name",
                new String(), false);
        testQuery("SELECT test:multipleDatetime alias FROM test:extendedContent WHERE ANY alias NOT IN (TIMESTAMP '" + d0 + "')", 1, false, "cmis:name", new String(), false);

    }

    public void test_d_boolean() throws Exception
    {
        addTypeTestData();
        assertNotNull(dictionaryService.getType(extendedContent));
        assertNotNull(cmisDictionaryService.findTypeByQueryName("test:extendedContent"));

        testQuery("SELECT * FROM test:extendedContent", 1, false, "cmis:name", new String(), false);

        CMISQueryOptions options = new CMISQueryOptions("SELECT * FROM test:extendedContent", rootNodeRef.getStoreRef());
        options.setDefaultFTSConnective(Connective.OR);
        options.setDefaultFTSFieldConnective(Connective.OR);
        CMISResultSet rs = cmisQueryService.query(options);
        assertEquals(1, rs.length());
        for (CMISResultSetRow row : rs)
        {
            Serializable sValue = row.getValue("test:singleBoolean");
            Boolean value = DefaultTypeConverter.INSTANCE.convert(Boolean.class, sValue);
            assertEquals(Boolean.TRUE, value);
            CMISResultSetColumn column = rs.getResultSetMetaData().getColumn("test:singleBoolean");
            assertEquals(CMISDataTypeEnum.BOOLEAN, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.SINGLE_VALUED, column.getCMISPropertyDefinition().getCardinality());

            sValue = row.getValue("test:multipleBoolean");
            assert (sValue instanceof Collection<?>);
            Collection<Boolean> collection = DefaultTypeConverter.INSTANCE.getCollection(Boolean.class, sValue);
            assertEquals(2, collection.size());
            Boolean[] members = new Boolean[2];
            members = collection.toArray(members);
            assertEquals(Boolean.TRUE, members[0]);
            assertEquals(Boolean.FALSE, members[1]);
            column = rs.getResultSetMetaData().getColumn("test:multipleBoolean");
            assertEquals(CMISDataTypeEnum.BOOLEAN, column.getCMISDataType());
            assertEquals(CMISCardinalityEnum.MULTI_VALUED, column.getCMISPropertyDefinition().getCardinality());
        }
        rs.close();

        // d:boolean single

        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean = TRUE", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean = true", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean = FALSE", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean = false", 0, false, "cmis:name", new String(), false);
        // not strictly compliant...
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean = TRue", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean <> TRUE", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean <> FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean < TRUE", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean < FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean <= TRUE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean <= FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean > TRUE", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean > FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean >= TRUE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean >= FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean LIKE 'TRUE'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean NOT LIKE 'FALSE'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean IN (TRUE)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:singleBoolean NOT IN (FALSE)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE TRUE =  ANY test:singleBoolean ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE FALSE =  ANY test:singleBoolean ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleBoolean IN (TRUE)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:singleBoolean NOT IN (FALSE)", 1, false, "cmis:name", new String(), true);

        // d:boolean single by alias

        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias = TRUE", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias = true", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias = FALSE", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias = false", 0, false, "cmis:name", new String(), false);
        // not strictly compliant...
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias = TRue", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias <> TRUE", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias <> FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias < TRUE", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias < FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias <= TRUE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias <= FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias > TRUE", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias > FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias >= TRUE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias >= FALSE", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias LIKE 'TRUE'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias NOT LIKE 'FALSE'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias IN (TRUE)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE alias NOT IN (FALSE)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE TRUE =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE FALSE =  ANY alias ", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE ANY alias IN (TRUE)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:singleBoolean alias FROM test:extendedContent WHERE ANY alias NOT IN (FALSE)", 1, false, "cmis:name", new String(), true);

        // d:boolean multiple

        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean = 2", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean <> 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean < 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean <= 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean > 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean >= 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE test:multipleBoolean NOT IN (2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE '1' =  ANY test:multipleBoolean ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE '2' =  ANY test:multipleBoolean ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleBoolean IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleBoolean IN (2, 3)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleBoolean NOT IN (1, 2)", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleBoolean NOT IN (2, 3)", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM test:extendedContent WHERE ANY test:multipleBoolean NOT IN (3, 4)", 1, false, "cmis:name", new String(), true);

        // d:boolean multiple by alias

        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias = 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias = 2", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias <> 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias <> 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias < 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias < 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias <= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias <= 2", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias > 1", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias > 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias >= 1", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias >= 0", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias LIKE '1'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias NOT LIKE '2'", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE alias NOT IN (2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE '1' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE '2' =  ANY alias ", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE ANY alias IN (1, 2)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE ANY alias IN (2, 3)", 1, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE ANY alias NOT IN (1, 2)", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE ANY alias NOT IN (2, 3)", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT test:multipleBoolean as alias FROM test:extendedContent WHERE ANY alias NOT IN (3, 4)", 1, false, "cmis:name", new String(), true);
    }

    public void testBasicContainsSyntax() throws Exception
    {
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('one')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('-quick')", doc_count-1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick brown fox')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick one')", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick -one')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('-quick one')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('-quick -one')", doc_count-2, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('fox brown quick')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick OR one')", 2, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick OR -one')", doc_count-1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('-quick OR -one')", doc_count, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\\'quick brown fox\\'')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\\'fox brown quick\\'')", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\\'quick brown fox\\' one')", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('\\'quick brown fox\\' -one')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('-\\'quick brown fox\\' one')", 1, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('-\\'quick brown fox\\' -one')", doc_count-2, false, "cmis:name", new String(), false);

        // escaping
        testExtendedQuery("SELECT * FROM cmis:folder WHERE CONTAINS('cmis:name:\\'Folder 9\\\\\\'\\'')", 1, false, "cmis:name", new String(), false);

        // precedence
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick OR brown one')", 1, false, "cmis:name", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick OR brown AND one')", 1, false, "cmis:name", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick OR (brown AND one)')", 1, false, "cmis:name", new String(), false);
        testExtendedQuery("SELECT * FROM cmis:document WHERE CONTAINS('(quick OR brown) AND one')", 0, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick OR brown OR one')", 2, false, "cmis:name", new String(), false);
        testQuery("SELECT * FROM cmis:document WHERE CONTAINS('quick OR brown one')", 1, false, "cmis:name", new String(), false);
    }

    public void testOrderableProperties()
    {

        addTypeSortTestData();

        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleTextUntokenised").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:singleTextTokenised").isOrderable());
        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleTextBoth").isOrderable());

        testOrderableProperty("test:singleTextUntokenised");
        testOrderablePropertyFail("test:singleTextTokenised");
        testOrderableProperty("test:singleTextBoth");

        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleTextUntokenised").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleTextTokenised").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleTextBoth").isOrderable());

        testOrderablePropertyFail("test:multipleTextUntokenised");
        testOrderablePropertyFail("test:multipleTextTokenised");
        testOrderablePropertyFail("test:multipleTextBoth");

        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleMLTextUntokenised").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:singleMLTextTokenised").isOrderable());
        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleMLTextBoth").isOrderable());

        testOrderableProperty("test:singleMLTextUntokenised");
        testOrderablePropertyFail("test:singleMLTextTokenised");
        testOrderableProperty("test:singleMLTextBoth");

        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleMLTextUntokenised").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleMLTextTokenised").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleMLTextBoth").isOrderable());

        testOrderablePropertyFail("test:multipleMLTextUntokenised");
        testOrderablePropertyFail("test:multipleMLTextTokenised");
        testOrderablePropertyFail("test:multipleMLTextBoth");

        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleFloat").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleFloat").isOrderable());

        testOrderableProperty("test:singleFloat");
        testOrderablePropertyFail("test:multipleFloat");

        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleDouble").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleDouble").isOrderable());

        testOrderableProperty("test:singleDouble");
        testOrderablePropertyFail("test:multipleDouble");

        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleInteger").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleInteger").isOrderable());

        testOrderableProperty("test:singleInteger");
        testOrderablePropertyFail("test:multipleInteger");

        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleLong").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleLong").isOrderable());

        testOrderableProperty("test:singleLong");
        testOrderablePropertyFail("test:multipleLong");

        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleDate").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleDate").isOrderable());

        testOrderableProperty("test:singleDate");
        testOrderablePropertyFail("test:multipleDate");

        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleDatetime").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleDatetime").isOrderable());

        testOrderableProperty("test:singleDatetime");
        testOrderablePropertyFail("test:multipleDatetime");

        assertTrue(cmisDictionaryService.findPropertyByQueryName("test:singleBoolean").isOrderable());
        assertFalse(cmisDictionaryService.findPropertyByQueryName("test:multipleBoolean").isOrderable());

        testOrderableProperty("test:singleBoolean");
        testOrderablePropertyFail("test:multipleBoolean");
    }

    public void testNonQueryableTypes() throws Exception
    {
        testQuery("SELECT * FROM cmis:policy", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM cmis:relationship ", 0, false, "cmis:name", new String(), true);
        testQuery("SELECT * FROM cm:ownable ", 0, false, "cmis:name", new String(), true);
        testExtendedQuery("SELECT * FROM cm:ownable ", 1, false, "cmis:name", new String(), false);
    }
    
    private void testOrderableProperty(String propertyQueryName)
    {
        testOrderBy("SELECT " + propertyQueryName + " FROM test:extendedContent ORDER BY " + propertyQueryName + " ASC", 13, false, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                propertyQueryName);
        testOrderBy("SELECT " + propertyQueryName + " FROM test:extendedContent ORDER BY " + propertyQueryName + " DESC", 13, false, Order.DESCENDING, CMISQueryMode.CMS_STRICT,
                propertyQueryName);
        testOrderBy("SELECT " + propertyQueryName + " FROM test:extendedContent ORDER BY " + propertyQueryName + " DESC", 13, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                propertyQueryName);
    }

    private void testOrderablePropertyFail(String propertyQueryName)
    {
        testOrderBy("SELECT " + propertyQueryName + " FROM test:extendedContent ORDER BY " + propertyQueryName + " ASC", 13, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                propertyQueryName);
        testOrderBy("SELECT " + propertyQueryName + " FROM test:extendedContent ORDER BY " + propertyQueryName + " DESC", 13, true, Order.DESCENDING, CMISQueryMode.CMS_STRICT,
                propertyQueryName);
        testOrderBy("SELECT " + propertyQueryName + " FROM test:extendedContent ORDER BY " + propertyQueryName + " DESC", 13, true, Order.ASCENDING, CMISQueryMode.CMS_STRICT,
                propertyQueryName);
    }

    private void addTypeTestDataModel()
    {
        // load in the test model
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/cmis/search/CMIS-query-test-model.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDAO.putModel(model);

        ((CMISAbstractDictionaryService) cmisDictionaryService).afterDictionaryDestroy();
        ((CMISAbstractDictionaryService) cmisDictionaryService).afterDictionaryInit();

        namespaceDao.addPrefix("test", "http://www.alfresco.org/test/cmis-query-test");
    }

    private void addTypeSortTestData()
    {
        addTypeTestDataModel();

        addSortableNull();
        for (int i = 0; i < 10; i++)
        {
            addSortableNode(i);
            if (i == 5)
            {
                addSortableNull();
            }
        }

        addSortableNull();
    }

    private NodeRef addSortableNull()
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        MLText ml = new MLText();
        ml.addValue(Locale.ENGLISH, "Test null");
        properties.put(ContentModel.PROP_DESCRIPTION, ml);
        properties.put(ContentModel.PROP_TITLE, ml);
        properties.put(ContentModel.PROP_NAME, "Test null");
        properties.put(ContentModel.PROP_CREATED, new Date());
        NodeRef c0 = nodeService.createNode(f0, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Test One", namespaceService), extendedContent, properties).getChildRef();
        return c0;
    }

    private static String[] orderable = new String[] { "zero loons", "one banana", "two apples", "three fruit", "four lemurs", "five rats", "six badgers", "seven cards",
            "eight cabbages", "nine zebras", "ten lemons" };

    private NodeRef addSortableNode(int position)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        MLText ml = new MLText();
        ml.addValue(Locale.ENGLISH, "Test " + position);
        properties.put(ContentModel.PROP_DESCRIPTION, ml);
        properties.put(ContentModel.PROP_TITLE, ml);
        properties.put(ContentModel.PROP_NAME, "Test " + position);
        properties.put(ContentModel.PROP_CREATED, new Date());
        properties.put(singleTextUntokenised, orderable[position]);
        properties.put(singleTextTokenised, orderable[position]);
        properties.put(singleTextBoth, orderable[position]);
        properties.put(multipleTextUntokenised, asArray(orderable[position], orderable[position + 1]));
        properties.put(multipleTextTokenised, asArray(orderable[position], orderable[position + 1]));
        properties.put(multipleTextBoth, asArray(orderable[position], orderable[position + 1]));
        properties.put(singleMLTextUntokenised, makeMLText(position));
        properties.put(singleMLTextTokenised, makeMLText(position));
        properties.put(singleMLTextBoth, makeMLText(position));
        properties.put(multipleMLTextUntokenised, makeMLTextMVP(position));
        properties.put(multipleMLTextTokenised, makeMLTextMVP(position));
        properties.put(multipleMLTextBoth, makeMLTextMVP(position));
        properties.put(singleFloat, 1.1f * position);
        properties.put(multipleFloat, asArray(1.1f * position, 2.2f * position));
        properties.put(singleDouble, 1.1d * position);
        properties.put(multipleDouble, asArray(1.1d * position, 2.2d * position));
        properties.put(singleInteger, 1 * position);
        properties.put(multipleInteger, asArray(1 * position, 2 * position));
        properties.put(singleLong, 1l * position);
        properties.put(multipleLong, asArray(1l * position, 2l * position));
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, position);
        Date d1 = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        // Date d0 = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date d2 = cal.getTime();
        properties.put(singleDate, d1);
        properties.put(multipleDate, asArray(d1, d2));
        properties.put(singleDatetime, d1);
        properties.put(multipleDatetime, asArray(d1, d2));
        properties.put(singleBoolean, position % 2 == 0 ? true : false);
        properties.put(multipleBoolean, asArray(true, false));
        NodeRef c0 = nodeService.createNode(f0, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Test One", namespaceService), extendedContent, properties).getChildRef();
        return c0;
    }

    private NodeRef addTypeTestData()
    {
        addTypeTestDataModel();

        I18NUtil.setLocale(Locale.UK);

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        MLText ml = new MLText();
        ml.addValue(Locale.ENGLISH, "Test one");
        ml.addValue(Locale.US, "Test 1");
        properties.put(ContentModel.PROP_DESCRIPTION, ml);
        properties.put(ContentModel.PROP_TITLE, ml);
        properties.put(ContentModel.PROP_NAME, "Test one");
        properties.put(ContentModel.PROP_CREATED, new Date());
        properties.put(singleTextUntokenised, "Un tokenised");
        properties.put(singleTextTokenised, "Un tokenised");
        properties.put(singleTextBoth, "Un tokenised");
        properties.put(multipleTextUntokenised, asArray("Un tokenised", "two parts"));
        properties.put(multipleTextTokenised, asArray("Un tokenised", "two parts"));
        properties.put(multipleTextBoth, asArray("Un tokenised", "two parts"));
        properties.put(singleMLTextUntokenised, makeMLText());
        properties.put(singleMLTextTokenised, makeMLText());
        properties.put(singleMLTextBoth, makeMLText());
        properties.put(multipleMLTextUntokenised, makeMLTextMVP());
        properties.put(multipleMLTextTokenised, makeMLTextMVP());
        properties.put(multipleMLTextBoth, makeMLTextMVP());
        properties.put(singleFloat, 1f);
        properties.put(multipleFloat, asArray(1f, 1.1f));
        properties.put(singleDouble, 1d);
        properties.put(multipleDouble, asArray(1d, 1.1d));
        properties.put(singleInteger, 1);
        properties.put(multipleInteger, asArray(1, 2));
        properties.put(singleLong, 1l);
        properties.put(multipleLong, asArray(1l, 2l));
        date1 = new Date();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        date0 = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        date2 = cal.getTime();
        properties.put(singleDate, date1);
        properties.put(multipleDate, asArray(date1, date2));
        properties.put(singleDatetime, date1);
        properties.put(multipleDatetime, asArray(date1, date2));
        properties.put(singleBoolean, true);
        properties.put(multipleBoolean, asArray(true, false));
        NodeRef c0 = nodeService.createNode(f0, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Test One", namespaceService), extendedContent, properties).getChildRef();
        return c0;
    }

    private static String[] mlOrderable_en = new String[] { "AAAA BBBB", "EEEE FFFF", "II", "KK", "MM", "OO", "QQ", "SS", "UU", "AA", "CC" };

    private static String[] mlOrderable_fr = new String[] { "CCCC DDDD", "GGGG HHHH", "JJ", "LL", "NN", "PP", "RR", "TT", "VV", "BB", "DD" };

    private MLText makeMLText()
    {
        return makeMLText(0);
    }

    private MLText makeMLText(int position)
    {
        MLText ml = new MLText();
        ml.addValue(Locale.ENGLISH, mlOrderable_en[position]);
        ml.addValue(Locale.FRENCH, mlOrderable_fr[position]);
        return ml;
    }

    private ArrayList<MLText> makeMLTextMVP()
    {
        return makeMLTextMVP(0);
    }

    private ArrayList<MLText> makeMLTextMVP(int position)
    {
        MLText m1 = new MLText();
        m1.addValue(Locale.ENGLISH, mlOrderable_en[position]);
        MLText m2 = new MLText();
        m2.addValue(Locale.FRENCH, mlOrderable_fr[position]);
        ArrayList<MLText> answer = new ArrayList<MLText>(2);
        answer.add(m1);
        answer.add(m2);
        return answer;
    }

    private <T> ArrayList<T> asArray(T... ts)
    {
        ArrayList<T> list = new ArrayList<T>(ts.length);
        for (T t : ts)
        {
            list.add(t);
        }
        return list;
    }
}
