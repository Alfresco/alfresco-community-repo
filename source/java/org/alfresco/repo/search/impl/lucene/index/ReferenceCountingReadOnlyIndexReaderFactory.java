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
package org.alfresco.repo.search.impl.lucene.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.lucene.LuceneConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.OpenBitSet;

public class ReferenceCountingReadOnlyIndexReaderFactory
{
    private static Log s_logger = LogFactory.getLog(ReferenceCountingReadOnlyIndexReaderFactory.class);

    private static WeakHashMap<String, ReferenceCountingReadOnlyIndexReader> log = new WeakHashMap<String, ReferenceCountingReadOnlyIndexReader>();

    public static IndexReader createReader(String id, IndexReader indexReader, boolean enableCaching, LuceneConfig config)
    {
        ReferenceCountingReadOnlyIndexReader rc = new ReferenceCountingReadOnlyIndexReader(id, indexReader, enableCaching, config);
        if (s_logger.isDebugEnabled())
        {
            if (log.containsKey(id))
            {
                s_logger.debug("Replacing ref counting reader for " + id);
            }
            s_logger.debug("Created ref counting reader for " + id + " " + rc.toString());
            log.put(new String(id), rc);            // Copy the key because the RCROIR references the ID
        }
        return rc;
    }

    public static String getState(String id)
    {
        if (s_logger.isDebugEnabled())
        {
            ReferenceCountingReadOnlyIndexReader rc = log.get(id);
            if (rc != null)
            {
                StringBuilder builder = new StringBuilder();
                builder.append("Id = " + rc.getId() + " Invalid = " + rc.getReferenceCount() + " invalid = " + rc.getInvalidForReuse() + " closed=" + rc.getClosed());
                return builder.toString();
            }

        }
        return ("<UNKNOWN>");

    }

    public static class ReferenceCountingReadOnlyIndexReader extends FilterIndexReader implements ReferenceCounting, CachingIndexReader
    {
        private static Log s_logger = LogFactory.getLog(ReferenceCountingReadOnlyIndexReader.class);

        private static final long serialVersionUID = 7693185658022810428L;

        private static java.lang.reflect.Field s_field;

        String id;

        int refCount = 0;

        boolean invalidForReuse = false;

        boolean allowsDeletions;

        boolean wrapper_closed = false;

        ConcurrentHashMap<Integer, Boolean> isCategory = new ConcurrentHashMap<Integer, Boolean>();

        ConcurrentHashMap<Integer, WithUseCount<List<Field>>> documentCache = new ConcurrentHashMap<Integer, WithUseCount<List<Field>>>();

        ConcurrentHashMap<Integer, WithUseCount<List<Field>>> idCache = new ConcurrentHashMap<Integer, WithUseCount<List<Field>>>();

        ConcurrentHashMap<Integer, WithUseCount<Field>> pathCache = new ConcurrentHashMap<Integer, WithUseCount<Field>>();

        ConcurrentHashMap<Integer, WithUseCount<Field>> typeCache = new ConcurrentHashMap<Integer, WithUseCount<Field>>();

        ConcurrentHashMap<Integer, WithUseCount<List<Field>>> parentCache = new ConcurrentHashMap<Integer, WithUseCount<List<Field>>>();

        ConcurrentHashMap<Integer, WithUseCount<List<Field>>> linkAspectCache = new ConcurrentHashMap<Integer, WithUseCount<List<Field>>>();

        private boolean enableCaching;

        private LuceneConfig config;

        static
        {
            Class<IndexReader> c = IndexReader.class;
            try
            {
                s_field = c.getDeclaredField("closed");
                s_field.setAccessible(true);
            }
            catch (SecurityException e)
            {
                throw new AlfrescoRuntimeException("Reference counting index reader needs access to org.apache.lucene.index.IndexReader.closed to work correctly", e);
            }
            catch (NoSuchFieldException e)
            {
                throw new AlfrescoRuntimeException(
                        "Reference counting index reader needs access to org.apache.lucene.index.IndexReader.closed to work correctly (incompatible version of lucene)", e);
            }
        }

        ReferenceCountingReadOnlyIndexReader(String id, IndexReader indexReader, boolean enableCaching, LuceneConfig config)
        {
            super(indexReader);
            this.id = id;
            if (enableCaching && (config != null))
            {
                this.enableCaching = config.isCacheEnabled();
            }
            this.config = config;
        }
        
        @Override
        public synchronized void incRef()
        {
            if (wrapper_closed)
            {
                throw new IllegalStateException(Thread.currentThread().getName() + "Indexer is closed " + id);
            }
            if (refCount++ > 0)
            {
                super.incRef();
            }
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " - increment - ref count is " + refCount + "        ... " + super.toString());
            }
            if (!wrapper_closed)
            {
                try
                {
                    s_field.set(this, false);
                }
                catch (IllegalArgumentException e)
                {
                    throw new AlfrescoRuntimeException("Failed to mark index as open ..", e);
                }
                catch (IllegalAccessException e)
                {
                    throw new AlfrescoRuntimeException("Failed to mark index as open ..", e);
                }
            }
        }

        private synchronized void decrementReferenceCount() throws IOException
        {
            refCount--;
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " - decrement - ref count is " + refCount + "        ... " + super.toString());
            }
            closeIfRequired();
            if (refCount < 0)
            {
                s_logger.error("Invalid reference count for Reader " + id + " is " + refCount + "        ... " + super.toString());
            }
        }

        private void closeIfRequired() throws IOException
        {
            if ((refCount == 0) && invalidForReuse && !wrapper_closed)
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " closed." + "        ... " + super.toString());
                }
                if (enableCaching)
                {
                    // No tidy up
                }
                // Pass on the last decRef
                super.decRef();

                wrapper_closed = true;
            }
            else
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName()
                            + ": Reader " + id + " still open .... ref = " + refCount + " invalidForReuse = " + invalidForReuse + "        ... " + super.toString());
                }
            }
        }

        public synchronized int getReferenceCount()
        {
            return refCount;
        }

        public synchronized boolean getInvalidForReuse()
        {
            return invalidForReuse;
        }

        public synchronized boolean getClosed()
        {
            return wrapper_closed;
        }

        public synchronized void setInvalidForReuse() throws IOException
        {
            if (wrapper_closed)
            {
                throw new IllegalStateException(Thread.currentThread().getName() + "Indexer is closed " + id);
            }
            invalidForReuse = true;
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " set invalid for reuse" + "        ... " + super.toString());
            }
            closeIfRequired();
        }

        
        @Override
        public synchronized void decRef() throws IOException
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " closing" + "        ... " + super.toString());
            }
            if (wrapper_closed)
            {
                throw new IllegalStateException(Thread.currentThread().getName() + "Indexer is closed " + id);
            }
            decrementReferenceCount();
            if (refCount > 0)
            {
                super.decRef();
            }
        }

        @Override
        protected void doDelete(int n) throws IOException
        {
            throw new UnsupportedOperationException("Delete is not supported by read only index readers");
        }

        private interface Accessor<T>
        {
            T get(int n, FieldSelector fieldSelector) throws IOException;
        }

        private class ListFieldAccessor implements Accessor<List<Field>>
        {

            public List<Field> get(int n, FieldSelector fieldSelector) throws IOException
            {
                Document document = ReferenceCountingReadOnlyIndexReader.super.document(n, fieldSelector);
                List<Field> fields = (List<Field>) document.getFields();
                ArrayList<Field> cacheable = new ArrayList<Field>(fields.size());
                cacheable.addAll(fields);
                return cacheable;
            }

        }

        private class MultipleValueFieldAccessor implements Accessor<List<Field>>
        {
            String fieldName;

            MultipleValueFieldAccessor(String fieldName)
            {
                this.fieldName = fieldName;
            }

            public List<Field> get(int n, FieldSelector fieldSelector) throws IOException
            {
                Document document = ReferenceCountingReadOnlyIndexReader.super.document(n, fieldSelector);
                Field[] fields = document.getFields(fieldName);
                ArrayList<Field> cacheable = new ArrayList<Field>(fields.length);
                for (Field field : fields)
                {
                    cacheable.add(field);
                }
                return cacheable;
            }

        }

        private class SingleValueFieldAccessor implements Accessor<Field>
        {
            String fieldName;

            SingleValueFieldAccessor(String fieldName)
            {
                this.fieldName = fieldName;
            }

            public Field get(int n, FieldSelector fieldSelector) throws IOException
            {
                return new Field(fieldName, getStringValue(n, fieldName), Store.NO, Index.UN_TOKENIZED);
            }

        }

        private final ListFieldAccessor LIST_FIELD_ACCESSOR = new ListFieldAccessor();

        private final MultipleValueFieldAccessor MV_ID_FIELD_ACCESSOR = new MultipleValueFieldAccessor("ID");

        private final SingleValueFieldAccessor SV_TYPE_FIELD_ACCESSOR = new SingleValueFieldAccessor("TYPE");

        private final SingleValueFieldAccessor SV_PATH_FIELD_ACCESSOR = new SingleValueFieldAccessor("PATH");

        private final MultipleValueFieldAccessor MV_PARENT_FIELD_ACCESSOR = new MultipleValueFieldAccessor("PARENT");

        private final MultipleValueFieldAccessor MV_LINKASPECT_FIELD_ACCESSOR = new MultipleValueFieldAccessor("LINKASPECT");

        private OpenBitSet nodes = null;

        private <T> T manageCache(ConcurrentHashMap<Integer, WithUseCount<T>> cache, Accessor<T> accessor, int n, FieldSelector fieldSelector, int limit) throws IOException
        {
            Integer key = Integer.valueOf(n);
            WithUseCount<T> value = cache.get(key);
            if (value == null)
            {
                T made = accessor.get(n, fieldSelector);
                value = new WithUseCount<T>(made, n);
                cache.put(key, value);

                // resize

                if (limit >= 0)
                {
                    if (cache.size() >= limit)
                    {
                        HashMap<Integer, WithUseCount<T>> keep = new HashMap<Integer, WithUseCount<T>>();
                        WithUseCount<T>[] existing = new WithUseCount[0];
                        synchronized (cache)
                        {
                            existing = cache.values().toArray(existing);
                            cache.clear();
                        }
                        Arrays.sort(existing);

                        for (WithUseCount<T> current : existing)
                        {
                            keep.put(Integer.valueOf(current.doc), current);
                            if ((current.count.get() == 0) || (keep.size() > (limit / 4)))
                            {
                                break;
                            }
                        }
                        keep.put(key, value);
                        cache.putAll(keep);
                    }
                }
            }
            else
            {
                value.count.getAndIncrement();
            }
            return value.object;
        }

        @SuppressWarnings("unchecked")
        public Document document(int n, FieldSelector fieldSelector) throws IOException
        {
            if ((fieldSelector == null) && enableCaching)
            {
                List<Field> listOfFields = manageCache(documentCache, LIST_FIELD_ACCESSOR, n, fieldSelector, config.getMaxDocumentCacheSize());
                Document document = new Document();
                document.getFields().addAll(listOfFields);
                return document;

            }
            else
            {
                if (enableCaching && (fieldSelector instanceof SingleFieldSelector))
                {
                    SingleFieldSelector sfs = (SingleFieldSelector) fieldSelector;

                    if (sfs.field.equals("ID") && !sfs.last)
                    {
                        List<Field> idFields = manageCache(idCache, MV_ID_FIELD_ACCESSOR, n, fieldSelector, config.getMaxDocIdCacheSize());
                        Document d = new Document();
                        d.getFields().addAll(idFields);
                        return d;

                    }
                    if (sfs.field.equals("ISCATEGORY") && sfs.last)
                    {
                        Integer key = Integer.valueOf(n);
                        Boolean isCat = isCategory.get(key);
                        if (isCat == null)
                        {
                            isCat = (getStringValue(n, "ISCATEGORY") != null);
                            isCategory.put(key, isCat);
                        }
                        Document d = new Document();
                        if (isCat)
                        {
                            d.add(new Field("ISCATEGORY", "T", Store.NO, Index.UN_TOKENIZED));
                        }
                        return d;
                    }
                    if (sfs.field.equals("PATH") && sfs.last)
                    {
                        Field pathField = manageCache(pathCache, SV_PATH_FIELD_ACCESSOR, n, fieldSelector, config.getMaxPathCacheSize());
                        Document d = new Document();
                        d.add(pathField);
                        return d;

                    }
                    if (sfs.field.equals("TYPE") && sfs.last)
                    {
                        Field typeField = manageCache(typeCache, SV_TYPE_FIELD_ACCESSOR, n, fieldSelector, config.getMaxTypeCacheSize());
                        Document d = new Document();
                        d.add(typeField);
                        return d;

                    }
                    if (sfs.field.equals("PARENT") && !sfs.last)
                    {
                        List<Field> listOfFields = manageCache(parentCache, MV_PARENT_FIELD_ACCESSOR, n, fieldSelector, config.getMaxParentCacheSize());
                        Document document = new Document();
                        document.getFields().addAll(listOfFields);
                        return document;

                    }
                    if (sfs.field.equals("LINKASPECT") && !sfs.last)
                    {
                        List<Field> listOfFields = manageCache(linkAspectCache, MV_LINKASPECT_FIELD_ACCESSOR, n, fieldSelector, config.getMaxLinkAspectCacheSize());
                        Document document = new Document();
                        document.getFields().addAll(listOfFields);
                        return document;

                    }

                }
            }

            return super.document(n, fieldSelector);
        }

        public String getId()
        {
            return id;
        }

        public boolean isInvalidForReuse()
        {
            return invalidForReuse;
        }

        public String getId(int n) throws IOException
        {
            Document d = document(n, new SingleFieldSelector("ID", true));
            return d.getField("ID").stringValue();
        }

        public String getPathLinkId(int n) throws IOException
        {
            Document document = document(n, new SingleFieldSelector("ID", true));
            Field[] fields = document.getFields("ID");
            Field field = fields[fields.length - 1];
            return (field == null) ? null : field.stringValue();
        }

        public String[] getIds(int n) throws IOException
        {
            return getStringValues(n, "ID");
        }

        public String[] getLinkAspects(int n) throws IOException
        {
            // return getStringValues(n, "LINKASPECT");
            Document d = document(n, new SingleFieldSelector("LINKASPECT", false));
            return d.getValues("LINKASPECT");
        }

        public String[] getParents(int n) throws IOException
        {
            // return getStringValues(n, "PARENT");
            Document d = document(n, new SingleFieldSelector("PARENT", false));
            return d.getValues("PARENT");
        }

        public String getPath(int n) throws IOException
        {
            // return getStringValue(n, "PATH");
            Document d = document(n, new SingleFieldSelector("PATH", true));
            Field f = d.getField("PATH");
            return f == null ? null : f.stringValue();
        }

        public String getType(int n) throws IOException
        {
            // return getStringValue(n, "TYPE");
            Document d = document(n, new SingleFieldSelector("TYPE", true));
            Field f = d.getField("TYPE");
            return f == null ? null : f.stringValue();
        }

        public String getIsCategory(int n) throws IOException
        {
            Document d = document(n, new SingleFieldSelector("ISCATEGORY", true));
            Field f = d.getField("ISCATEGORY");
            return f == null ? null : f.stringValue();
        }

        // private String getLastStringValue(int n, String fieldName) throws IOException
        // {
        // Document document = document(n);
        // Field[] fields = document.getFields(fieldName);
        // Field field = fields[fields.length - 1];
        // return (field == null) ? null : field.stringValue();
        // }

        private String getStringValue(int n, String fieldName) throws IOException
        {
            Document document = document(n);
            Field field = document.getField(fieldName);
            return (field == null) ? null : field.stringValue();
        }

        @SuppressWarnings("unchecked")
        private String[] getStringValues(int n, String fieldName) throws IOException
        {
            Document document = document(n);
            ArrayList<Field> fields = new ArrayList<Field>();
            ArrayList<String> answer = new ArrayList<String>(2);
            fields.addAll((List<Field>) document.getFields());

            for (Field field : fields)
            {
                if (field.name().equals(fieldName))
                {
                    answer.add(field.stringValue());
                }
            }

            return answer.toArray(new String[answer.size()]);
        }

        public synchronized TermDocs getNodeDocs() throws IOException
        {
            if (nodes == null)
            {
                TermDocs nodeDocs = termDocs(new Term("ISNODE", "T"));
                nodes = new OpenBitSet();
                while (nodeDocs.next())
                {
                    nodes.set(nodeDocs.doc());
                }
                nodeDocs.close();
            }
            return new TermDocSet(nodes);
        }
    }

    static class WithUseCount<T> implements Comparable<WithUseCount<T>>
    {
        AtomicInteger count = new AtomicInteger(0);

        T object;

        int doc;

        WithUseCount(T object, int doc)
        {
            this.object = object;
            this.doc = doc;
        }

        public int compareTo(WithUseCount<T> other)
        {
            return other.count.get() - this.count.get();
        }
    }

    private static class SingleFieldSelector implements FieldSelector
    {
        String field;

        boolean last;

        SingleFieldSelector(String field, boolean last)
        {
            this.field = field;
            this.last = last;
        }

        public FieldSelectorResult accept(String fieldName)
        {
            if (fieldName.equals(field))
            {
                return FieldSelectorResult.LOAD;
            }
            else
            {
                return FieldSelectorResult.NO_LOAD;
            }
        }

    }

    static class TermDocSet implements TermDocs
    {
        OpenBitSet set;

        int position = -1;

        TermDocSet(OpenBitSet set)
        {
            this.set = set;
        }

        public void close() throws IOException
        {
            // Noop
        }

        public int doc()
        {
            return position;
        }

        public int freq()
        {
            return 1;
        }

        public boolean next() throws IOException
        {
           position++;
           position = set.nextSetBit(position);
           return (position != -1);
        }

        public int read(int[] docs, int[] freqs) throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void seek(Term term) throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void seek(TermEnum termEnum) throws IOException
        {
           throw new UnsupportedOperationException();
        }

        public boolean skipTo(int target) throws IOException
        {
            do
            {
                if (!next())
                {
                    return false;
                }
            }
            while (target > doc());
            return true;

        }

    }
}
