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
package org.alfresco.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * NOTE: This utility class is a copy of org.apache.commons.lang3.SerializationUtils
 * 
 * Please see http://issues.alfresco.com/jira/browse/ALF-5044 for why this is done.
 * 
 * @author Apache Software Foundation
 * @author Daniel L. Rall
 * @author Jeff Varszegi
 * @author Gary Gregory
 * 
 * <p>
 * Assists with the serialization process and performs additional functionality
 * based on serialization.
 * </p>
 * <p>
 * <ul>
 * <li>Deep clone using serialization
 * <li>Serialize managing finally and IOException
 * <li>Deserialize managing finally and IOException
 * </ul>
 * 
 * <p>
 * This class throws exceptions for invalid <code>null</code> inputs. Each
 * method documents its behaviour in more detail.
 * </p>
 * 
 * <p>
 * #ThreadSafe#
 * </p>
 *
 */
public class SerializationUtils
{

    /**
     * <p>
     * SerializationUtils instances should NOT be constructed in standard
     * programming. Instead, the class should be used as
     * <code>SerializationUtils.clone(object)</code>.
     * </p>
     * 
     * <p>
     * This constructor is public to permit tools that require a JavaBean
     * instance to operate.
     * </p>
     */
    public SerializationUtils()
    {
        super();
    }

    // Clone
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Deep clone an <code>Object</code> using serialization.
     * </p>
     * 
     * <p>
     * This is many times slower than writing clone methods by hand on all
     * objects in your object graph. However, for complex object graphs, or for
     * those that don't support deep cloning this can be a simple alternative
     * implementation. Of course all the objects must be
     * <code>Serializable</code>.
     * </p>
     * 
     * @param object
     *            the <code>Serializable</code> object to clone
     * @return the cloned object
     * @throws AlfrescoRuntimeException
     *             (runtime) if the serialization fails
     */
    public static <T extends Serializable> T clone(T object)
    {
        /*
         * when we serialize and deserialize an object, it is reasonable to
         * assume the deserialized object is of the same type as the original
         * serialized object
         */
        @SuppressWarnings("unchecked")
        final T result = (T) deserialize(serialize(object));
        return result;
    }

    // Serialize
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Serializes an <code>Object</code> to the specified stream.
     * </p>
     * 
     * <p>
     * The stream will be closed once the object is written. This avoids the
     * need for a finally clause, and maybe also exception handling, in the
     * application code.
     * </p>
     * 
     * <p>
     * The stream passed in is not buffered internally within this method. This
     * is the responsibility of your application if desired.
     * </p>
     * 
     * @param obj
     *            the object to serialize to bytes, may be null
     * @param outputStream
     *            the stream to write to, must not be null
     * @throws IllegalArgumentException
     *             if <code>outputStream</code> is <code>null</code>
     * @throws AlfrescoRuntimeException
     *             (runtime) if the serialization fails
     */
    public static void serialize(Serializable obj, OutputStream outputStream)
    {
        if (outputStream == null)
        {
            throw new IllegalArgumentException("The OutputStream must not be null");
        }
        ObjectOutputStream out = null;
        try
        {
            // stream closed in the finally
            out = new ObjectOutputStream(outputStream);
            out.writeObject(obj);

        } catch (IOException ex)
        {
            throw new AlfrescoRuntimeException("Failed to serialize", ex);
        } finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
            } catch (IOException ex)
            {
                // ignore close exception
            }
        }
    }

    /**
     * <p>
     * Serializes an <code>Object</code> to a byte array for
     * storage/serialization.
     * </p>
     * 
     * @param obj
     *            the object to serialize to bytes
     * @return a byte[] with the converted Serializable
     * @throws AlfrescoRuntimeException
     *             (runtime) if the serialization fails
     */
    public static byte[] serialize(Serializable obj)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }

    // Deserialize
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Deserializes an <code>Object</code> from the specified stream.
     * </p>
     * 
     * <p>
     * The stream will be closed once the object is written. This avoids the
     * need for a finally clause, and maybe also exception handling, in the
     * application code.
     * </p>
     * 
     * <p>
     * The stream passed in is not buffered internally within this method. This
     * is the responsibility of your application if desired.
     * </p>
     * 
     * @param inputStream
     *            the serialized object input stream, must not be null
     * @return the deserialized object
     * @throws IllegalArgumentException
     *             if <code>inputStream</code> is <code>null</code>
     * @throws AlfrescoRuntimeException
     *             (runtime) if the serialization fails
     */
    public static Object deserialize(InputStream inputStream)
    {
        if (inputStream == null)
        {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        ObjectInputStream in = null;
        try
        {
            // stream closed in the finally
            in = new ObjectInputStream(inputStream);
            return in.readObject();

        } catch (ClassNotFoundException ex)
        {
            throw new AlfrescoRuntimeException("Failed to deserialize", ex);
        } catch (IOException ex)
        {
            throw new AlfrescoRuntimeException("Failed to deserialize", ex);
        } finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            } catch (IOException ex)
            {
                // ignore close exception
            }
        }
    }

    /**
     * <p>
     * Deserializes a single <code>Object</code> from an array of bytes.
     * </p>
     * 
     * @param objectData
     *            the serialized object, must not be null
     * @return the deserialized object
     * @throws IllegalArgumentException
     *             if <code>objectData</code> is <code>null</code>
     * @throws AlfrescoRuntimeException
     *             (runtime) if the serialization fails
     */
    public static Object deserialize(byte[] objectData)
    {
        if (objectData == null)
        {
            throw new IllegalArgumentException("The byte[] must not be null");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
        return deserialize(bais);
    }

}