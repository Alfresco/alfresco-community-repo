/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.publishing;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.xml.AbstractXmlHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class JaxbHttpMessageConverter extends AbstractXmlHttpMessageConverter<Object>
{
    private static Log log = LogFactory.getLog(JaxbHttpMessageConverter.class);
    private JAXBContext defaultJaxbContext = null;
    private final ConcurrentMap<Class<?>, JAXBContext> jaxbContexts = new ConcurrentHashMap<Class<?>, JAXBContext>();

    public JaxbHttpMessageConverter()
    {
        super();
    }

    /**
     * Create a JAXB message converter, specifying the Java packages it should use to find JAXB classes
     * @param packagesToInclude A colon-separated list of package names.
     * @see JAXBContext#newInstance(String) 
     */
    public JaxbHttpMessageConverter(String packagesToInclude)
    {
        super();
        try
        {
            defaultJaxbContext = JAXBContext.newInstance(packagesToInclude);
        }
        catch (JAXBException e)
        {
            log.error("Failed to instantiate JAXB context with supplied context path " + packagesToInclude, e);
        }
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType)
    {
        return (clazz.isAnnotationPresent(XmlRootElement.class) || clazz.isAnnotationPresent(XmlType.class))
                && canRead(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType)
    {
        return AnnotationUtils.findAnnotation(clazz, XmlRootElement.class) != null && canWrite(mediaType);
    }

    @Override
    protected boolean supports(Class<?> clazz)
    {
        // should not be called, since we override canRead/Write
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object readFromSource(Class<?> clazz, HttpHeaders headers, Source source) throws IOException
    {
        try
        {
            Unmarshaller unmarshaller = createUnmarshaller(clazz);
            if (clazz.isAnnotationPresent(XmlRootElement.class))
            {
                return unmarshaller.unmarshal(source);
            }
            else
            {
                JAXBElement<?> jaxbElement = unmarshaller.unmarshal(source, clazz);
                return jaxbElement.getValue();
            }
        }
        catch (UnmarshalException ex)
        {
            throw new HttpMessageNotReadableException("Could not unmarshal to [" + clazz + "]: " + ex.getMessage(), ex);

        }
        catch (JAXBException ex)
        {
            throw new HttpMessageConversionException("Could not instantiate JAXBContext: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void writeToResult(Object o, HttpHeaders headers, Result result) throws IOException
    {
        try
        {
            Class<?> clazz = ClassUtils.getUserClass(o);
            Marshaller marshaller = createMarshaller(clazz);
            setCharset(headers.getContentType(), marshaller);
            marshaller.marshal(o, result);
        }
        catch (MarshalException ex)
        {
            throw new HttpMessageNotWritableException("Could not marshal [" + o + "]: " + ex.getMessage(), ex);
        }
        catch (JAXBException ex)
        {
            throw new HttpMessageConversionException("Could not instantiate JAXBContext: " + ex.getMessage(), ex);
        }
    }

    private void setCharset(MediaType contentType, Marshaller marshaller) throws PropertyException
    {
        if (contentType != null && contentType.getCharSet() != null)
        {
            marshaller.setProperty(Marshaller.JAXB_ENCODING, contentType.getCharSet().name());
        }
    }

    /**
     * Creates a new {@link Marshaller} for the given class.
     * 
     * @param clazz
     *            the class to create the marshaller for
     * @return the {@code Marshaller}
     * @throws HttpMessageConversionException
     *             in case of JAXB errors
     */
    protected final Marshaller createMarshaller(Class<?> clazz)
    {
        try
        {
            JAXBContext jaxbContext = getJaxbContext(clazz);
            return jaxbContext.createMarshaller();
        }
        catch (JAXBException ex)
        {
            throw new HttpMessageConversionException("Could not create Marshaller for class [" + clazz + "]: "
                    + ex.getMessage(), ex);
        }
    }

    /**
     * Creates a new {@link Unmarshaller} for the given class.
     * 
     * @param clazz
     *            the class to create the unmarshaller for
     * @return the {@code Unmarshaller}
     * @throws HttpMessageConversionException
     *             in case of JAXB errors
     */
    protected final Unmarshaller createUnmarshaller(Class<?> clazz) throws JAXBException
    {
        try
        {
            JAXBContext jaxbContext = getJaxbContext(clazz);
            return jaxbContext.createUnmarshaller();
        }
        catch (JAXBException ex)
        {
            throw new HttpMessageConversionException("Could not create Unmarshaller for class [" + clazz + "]: "
                    + ex.getMessage(), ex);
        }
    }

    /**
     * Returns a {@link JAXBContext} for the given class.
     * 
     * @param clazz
     *            the class to return the context for
     * @return the {@code JAXBContext}
     * @throws HttpMessageConversionException
     *             in case of JAXB errors
     */
    protected final JAXBContext getJaxbContext(Class<?> clazz)
    {
        Assert.notNull(clazz, "'clazz' must not be null");
        JAXBContext result = null;
        if (defaultJaxbContext != null)
        {
            result = defaultJaxbContext;
        }
        else
        {
            result = jaxbContexts.get(clazz);
            if (result == null)
            {
                try
                {
                    result = JAXBContext.newInstance(clazz);
                    jaxbContexts.putIfAbsent(clazz, result);
                }
                catch (JAXBException ex)
                {
                    throw new HttpMessageConversionException("Could not instantiate JAXBContext for class [" + clazz
                            + "]: " + ex.getMessage(), ex);
                }
            }
        }
        return result;
    }

}
