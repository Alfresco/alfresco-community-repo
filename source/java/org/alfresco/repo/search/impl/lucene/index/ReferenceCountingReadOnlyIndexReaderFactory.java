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
package org.alfresco.repo.search.impl.lucene.index;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;

public class ReferenceCountingReadOnlyIndexReaderFactory
{
    private static Log s_logger = LogFactory.getLog(ReferenceCountingReadOnlyIndexReaderFactory.class);

    private static HashMap<String, ReferenceCountingReadOnlyIndexReader> log = new HashMap<String, ReferenceCountingReadOnlyIndexReader>();

    public static IndexReader createReader(String id, IndexReader indexReader, boolean enableCaching)
    {
        ReferenceCountingReadOnlyIndexReader rc = new ReferenceCountingReadOnlyIndexReader(id, indexReader, enableCaching);
        if (s_logger.isDebugEnabled())
        {
            if (log.containsKey(id))
            {
                s_logger.debug("Replacing ref counting reader for " + id);
            }
            s_logger.debug("Created ref counting reader for " + id + " " + rc.toString());
            log.put(id, rc);
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

        String id;

        int refCount = 0;

        boolean invalidForReuse = false;

        boolean allowsDeletions;

        boolean closed = false;

        // CacheManager cacheManager;

        ConcurrentMap<Integer, DocumentWithInstanceCount> documentCache = concurrentMap(new LinkedHashMap<Integer, DocumentWithInstanceCount>()); // new

        ConcurrentSet<Integer> documentKeys = (ConcurrentSet<Integer>) documentCache.keySet();

        ConcurrentMap<Integer, IdWithInstanceCount> idCache = concurrentMap(new LinkedHashMap<Integer, IdWithInstanceCount>()); // new

        ConcurrentSet<Integer> idKeys = (ConcurrentSet<Integer>) idCache.keySet();

        ConcurrentHashMap<Integer, Boolean> isCategory = new ConcurrentHashMap<Integer, Boolean>();

        boolean enableCaching;

        ReferenceCountingReadOnlyIndexReader(String id, IndexReader indexReader, boolean enableCaching)
        {
            super(indexReader);
            this.id = id;
            this.enableCaching = enableCaching;

        }

        public synchronized void incrementReferenceCount()
        {
            if (closed)
            {
                throw new IllegalStateException(Thread.currentThread().getName() + "Indexer is closed " + id);
            }
            refCount++;
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " - increment - ref count is " + refCount + "        ... " + super.toString());
            }
        }

        public synchronized void decrementReferenceCount() throws IOException
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
            if ((refCount == 0) && invalidForReuse)
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " closed." + "        ... " + super.toString());
                }
                if (enableCaching)
                {
                    // No tidy up
                }
                in.close();
                closed = true;
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
            return closed;
        }

        public synchronized void setInvalidForReuse() throws IOException
        {
            if (closed)
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
        protected void doClose() throws IOException
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " closing" + "        ... " + super.toString());
            }
            if (closed)
            {
                throw new IllegalStateException(Thread.currentThread().getName() + "Indexer is closed " + id);
            }
            decrementReferenceCount();
        }

        @Override
        protected void doDelete(int n) throws IOException
        {
            throw new UnsupportedOperationException("Delete is not supported by read only index readers");
        }

        public Document document(int n, FieldSelector fieldSelector) throws IOException
        {
            if ((fieldSelector == null) && enableCaching)
            {
                Integer key = Integer.valueOf(n);
                DocumentWithInstanceCount document = documentCache.get(key);
                if (document == null)
                {

                    document = new DocumentWithInstanceCount(super.document(n, fieldSelector));
                    if (document.instance < 0)
                    {
                        DocumentWithInstanceCount.instanceCount = 0;
                        new DocumentWithInstanceCount(document.document);
                        documentCache.clear();
                    }
                    documentCache.put(key, document);

                    if (documentCache.size() > 100)
                    {
                        documentCache.resize(50);
                    }
                }
                else
                {
                    if (document.instance < DocumentWithInstanceCount.instanceCount - 50)
                    {
                        document = new DocumentWithInstanceCount(document.document);
                        if (document.instance < 0)
                        {
                            DocumentWithInstanceCount.instanceCount = 0;
                            new DocumentWithInstanceCount(document.document);
                            documentCache.clear();
                        }
                        documentCache.replace(key, new DocumentWithInstanceCount(document.document));
                    }
                }
                return document.document;
            }
            else
            {
                if (enableCaching && (fieldSelector instanceof SingleFieldSelector))
                {
                    SingleFieldSelector sfs = (SingleFieldSelector) fieldSelector;

                    if (sfs.field.equals("ID") && sfs.last)
                    {
                        Integer key = Integer.valueOf(n);
                        IdWithInstanceCount id = idCache.get(key);
                        if (id == null)
                        {

                            id = new IdWithInstanceCount(getLastStringValue(n, "ID"));
                            if (id.instance < 0)
                            {
                                IdWithInstanceCount.instanceCount = 0;
                                new IdWithInstanceCount(id.id);
                                idCache.clear();
                            }
                            idCache.put(key, id);

                            if (idCache.size() > 10000)
                            {
                                idCache.resize(5000);
                            }
                        }
                        else
                        {
                            if (id.instance < IdWithInstanceCount.instanceCount - 5000)
                            {
                                id = new IdWithInstanceCount(id.id);
                                if (id.instance < 0)
                                {
                                    IdWithInstanceCount.instanceCount = 0;
                                    new IdWithInstanceCount(id.id);
                                    idCache.clear();
                                }
                                idCache.replace(key, new IdWithInstanceCount(id.id));
                            }
                        }
                        Document d = new Document();
                        d.add(new Field("ID", id.id, Store.NO, Index.UN_TOKENIZED));
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

        public String[] getIds(int n) throws IOException
        {
            return getStringValues(n, "ID");
        }

        public String[] getLinkAspects(int n) throws IOException
        {
            return getStringValues(n, "LINKASPECT");
        }

        public String[] getParents(int n) throws IOException
        {
            return getStringValues(n, "PARENT");
        }

        public String getPath(int n) throws IOException
        {
            return getStringValue(n, "PATH");
        }

        public String getType(int n) throws IOException
        {
            return getStringValue(n, "TYPE");
        }

        public String getIsCategory(int n) throws IOException
        {
            Document d = document(n, new SingleFieldSelector("ISCATEGORY", true));
            Field f = d.getField("ISCATEGORY");
            return f == null ? null : f.stringValue();
        }

        private String getLastStringValue(int n, String fieldName) throws IOException
        {
            Document document = document(n);
            Field[] fields = document.getFields(fieldName);
            Field field = fields[fields.length - 1];
            return (field == null) ? null : field.stringValue();
        }

        private String getStringValue(int n, String fieldName) throws IOException
        {
            Document document = document(n);
            Field field = document.getField(fieldName);
            return (field == null) ? null : field.stringValue();
        }

        private String[] getStringValues(int n, String fieldName) throws IOException
        {
            Document document = document(n);
            Field[] fields = document.getFields(fieldName);
            if (fields != null)
            {
                String[] answer = new String[fields.length];
                int i = 0;
                for (Field field : fields)
                {
                    answer[i++] = (field == null) ? null : field.stringValue();
                }
                return answer;
            }
            else
            {
                return null;
            }
        }
    }

    public static <K, V> ConcurrentMap<K, V> concurrentMap(Map<K, V> m)
    {
        return new ConcurrentMap<K, V>(m);
    }

    private static class ConcurrentMap<K, V> implements Map<K, V>, Serializable
    {
        private final Map<K, V> m; // Backing Map

        final private ReadWriteLock readWriteLock;

        ConcurrentMap(Map<K, V> m)
        {
            if (m == null)
                throw new NullPointerException();
            this.m = m;
            this.readWriteLock = new ReentrantReadWriteLock();
        }

        ConcurrentMap(Map<K, V> m, ReadWriteLock readWriteLock)
        {
            this.m = m;
            this.readWriteLock = readWriteLock;
        }

        public int size()
        {
            readWriteLock.readLock().lock();
            try
            {
                return m.size();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public boolean isEmpty()
        {
            readWriteLock.readLock().lock();
            try
            {
                return m.isEmpty();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public boolean containsKey(Object key)
        {
            readWriteLock.readLock().lock();
            try
            {
                return m.containsKey(key);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public boolean containsValue(Object value)
        {
            readWriteLock.readLock().lock();
            try
            {
                return m.containsValue(value);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public V get(Object key)
        {
            readWriteLock.readLock().lock();
            try
            {
                return m.get(key);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public V put(K key, V value)
        {

            readWriteLock.writeLock().lock();
            try
            {
                return m.put(key, value);
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }

        public V remove(Object key)
        {
            readWriteLock.writeLock().lock();
            try
            {
                return m.remove(key);
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }

        public void putAll(Map<? extends K, ? extends V> map)
        {
            readWriteLock.writeLock().lock();
            try
            {
                m.putAll(map);
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }

        public void clear()
        {
            readWriteLock.writeLock().lock();
            try
            {
                m.clear();
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }

        private transient Set<K> keySet = null;

        private transient Set<Map.Entry<K, V>> entrySet = null;

        private transient Collection<V> values = null;

        public Set<K> keySet()
        {

            readWriteLock.readLock().lock();
            try
            {
                if (keySet == null)
                    keySet = new ConcurrentSet<K>(m.keySet(), readWriteLock);
                return keySet;
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }

        }

        public Set<Map.Entry<K, V>> entrySet()
        {
            readWriteLock.readLock().lock();
            try
            {
                if (entrySet == null)
                    entrySet = new ConcurrentSet<Map.Entry<K, V>>(m.entrySet(), readWriteLock);
                return entrySet;
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public Collection<V> values()
        {
            readWriteLock.readLock().lock();
            try
            {
                if (values == null)
                    values = new ConcurrentCollection<V>(m.values(), readWriteLock);
                return values;
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public boolean equals(Object o)
        {
            readWriteLock.readLock().lock();
            try
            {
                return m.equals(o);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }

        }

        public int hashCode()
        {
            readWriteLock.readLock().lock();
            try
            {
                return m.hashCode();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public String toString()
        {
            readWriteLock.readLock().lock();
            try
            {
                return m.toString();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        private void writeObject(ObjectOutputStream s) throws IOException
        {
            readWriteLock.readLock().lock();
            try
            {
                s.defaultWriteObject();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public void resize(int size)
        {

            ArrayList<K> toRemove = null;
            readWriteLock.writeLock().lock();
            try
            {
                int excess = m.size() - size;
                toRemove = new ArrayList<K>(excess);
                if (excess > 0)
                {

                    Iterator<K> it = keySet.iterator();
                    while (toRemove.size() < excess)
                    {
                        K key = it.next();
                        toRemove.add(key);
                    }
                }

            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }

            if ((toRemove != null) && (toRemove.size() > 0))
            {
                readWriteLock.writeLock().lock();
                try
                {
                    for (K key : toRemove)
                    {
                        m.remove(key);
                    }
                }
                finally
                {
                    readWriteLock.writeLock().unlock();
                }
            }
        }

        public void replace(K key, V value)
        {
            readWriteLock.writeLock().lock();
            try
            {
                m.remove(key);
                m.put(key, value);
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    /**
     * @serial include
     */
    static class ConcurrentCollection<E> implements Collection<E>, Serializable
    {
        final Collection<E> c; // Backing Collection

        final ReadWriteLock readWriteLock;

        ConcurrentCollection(Collection<E> c)
        {
            if (c == null)
                throw new NullPointerException();
            this.c = c;
            this.readWriteLock = new ReentrantReadWriteLock();
        }

        ConcurrentCollection(Collection<E> c, ReadWriteLock readWriteLock)
        {
            this.c = c;
            this.readWriteLock = readWriteLock;
        }

        public int size()
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.size();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public boolean isEmpty()
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.isEmpty();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public boolean contains(Object o)
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.contains(o);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public Object[] toArray()
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.toArray();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public <T> T[] toArray(T[] a)
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.toArray(a);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public Iterator<E> iterator()
        {
            return c.iterator(); // Must be manually synched by user!
        }

        public boolean add(E e)
        {
            readWriteLock.writeLock().lock();
            try
            {
                return c.add(e);
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }

        }

        public boolean remove(Object o)
        {
            readWriteLock.writeLock().lock();
            try
            {
                return c.remove(o);
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }

        public boolean containsAll(Collection<?> coll)
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.containsAll(coll);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public boolean addAll(Collection<? extends E> coll)
        {
            readWriteLock.writeLock().lock();
            try
            {
                return c.addAll(coll);
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }

        public boolean removeAll(Collection<?> coll)
        {
            readWriteLock.writeLock().lock();
            try
            {
                return c.removeAll(coll);
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }

        public boolean retainAll(Collection<?> coll)
        {
            readWriteLock.writeLock().lock();
            try
            {
                return c.retainAll(coll);
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }

        }

        public void clear()
        {
            readWriteLock.writeLock().lock();
            try
            {
                c.clear();
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }

        public String toString()
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.toString();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        private void writeObject(ObjectOutputStream s) throws IOException
        {
            readWriteLock.readLock().lock();
            try
            {
                s.defaultWriteObject();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }
    }

    /**
     * @serial include
     */
    static class ConcurrentSet<E> extends ConcurrentCollection<E> implements Set<E>
    {
        ConcurrentSet(Set<E> s)
        {
            super(s);
        }

        ConcurrentSet(Set<E> s, ReadWriteLock readWriteLock)
        {
            super(s, readWriteLock);
        }

        public boolean equals(Object o)
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.equals(o);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }

        }

        public int hashCode()
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.hashCode();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }
    }

    static class ConcurrentList<E> extends ConcurrentCollection<E> implements List<E>
    {
        static final long serialVersionUID = -7754090372962971524L;

        final List<E> list;

        ConcurrentList(List<E> list)
        {
            super(list);
            this.list = list;
        }

        ConcurrentList(List<E> list, ReadWriteLock readWriteLock)
        {
            super(list, readWriteLock);
            this.list = list;
        }

        public boolean equals(Object o)
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.equals(o);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public int hashCode()
        {
            readWriteLock.readLock().lock();
            try
            {
                return c.hashCode();
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public E get(int index)
        {
            readWriteLock.readLock().lock();
            try
            {
                return list.get(index);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public E set(int index, E element)
        {
            readWriteLock.readLock().lock();
            try
            {
                return list.set(index, element);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public void add(int index, E element)
        {
            readWriteLock.readLock().lock();
            try
            {
                list.add(index, element);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public E remove(int index)
        {
            readWriteLock.readLock().lock();
            try
            {
                return list.remove(index);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public int indexOf(Object o)
        {
            readWriteLock.readLock().lock();
            try
            {
                return list.indexOf(o);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public int lastIndexOf(Object o)
        {
            readWriteLock.readLock().lock();
            try
            {
                return list.lastIndexOf(o);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public boolean addAll(int index, Collection<? extends E> c)
        {
            readWriteLock.readLock().lock();
            try
            {
                return list.addAll(index, c);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        public ListIterator<E> listIterator()
        {
            return list.listIterator(); // Must be manually synched by user
        }

        public ListIterator<E> listIterator(int index)
        {
            return list.listIterator(index); // Must be manually synched by user
        }

        public List<E> subList(int fromIndex, int toIndex)
        {
            readWriteLock.readLock().lock();
            try
            {
                return new ConcurrentList<E>(list.subList(fromIndex, toIndex), readWriteLock);
            }
            finally
            {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * SynchronizedRandomAccessList instances are serialized as SynchronizedList instances to allow them to be
         * deserialized in pre-1.4 JREs (which do not have SynchronizedRandomAccessList). This method inverts the
         * transformation. As a beneficial side-effect, it also grafts the RandomAccess marker onto SynchronizedList
         * instances that were serialized in pre-1.4 JREs. Note: Unfortunately, SynchronizedRandomAccessList instances
         * serialized in 1.4.1 and deserialized in 1.4 will become SynchronizedList instances, as this method was
         * missing in 1.4.
         */
        // private Object readResolve()
        // {
        // return (list instanceof RandomAccess ? new SynchronizedRandomAccessList<E>(list) : this);
        // }
    }

    static class DocumentWithInstanceCount
    {
        volatile static int instanceCount = 0;

        int instance;

        Document document;

        DocumentWithInstanceCount(Document document)
        {
            this.document = document;
            instance = instanceCount++;
        }
    }

    static class IdWithInstanceCount
    {
        volatile static int instanceCount = 0;

        int instance;

        String id;

        IdWithInstanceCount(String id)
        {
            this.id = id;
            instance = instanceCount++;
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
}
