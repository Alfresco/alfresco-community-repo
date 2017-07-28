/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Bootstrap unserializer validator: a bootstrap bean that checks that the
 * classes that would favor Java unserialize remote code execution are not
 * available. Check is needed because libs could be introduced by the
 * application server.
 * 
 * </p>See MNT-15170 for details.
 * 
 * </p> Checked conditions: <br>
 * org.apache.xalan.xsltc.trax.TemplatesImpl and
 * org.springframework.core.SerializableTypeWrapper;<br>
 * org.apache.commons.collections.functors.InvokerTransformer
 * org.apache.commons.collections.functors.InstantiateFactory
 * org.apache.commons.collections.functors.InstantiateTransformer
 * org.apache.commons.collections.functors.PrototypeCloneFactory
 * org.apache.commons.collections.functors.PrototypeSerializationFactory
 * org.apache.commons.collections.functors.WhileClosure
 * org.apache.commons.collections.functors.CloneTransformer
 * org.apache.commons.collections.functors.ForClosure
 */
public class UnserializerValidatorBootstrap extends AbstractLifecycleBean
{

    private static Log logger = LogFactory.getLog(UnserializerValidatorBootstrap.class);
    
    /** The name of the global enablement property. */
    public static final String PROPERTY_UNSERIALIZER_VALIDATOR_ENABLED = "unserializer.validator.enabled";
    
    private static final String ERR_UNEXPECTED_ERROR = "unserializer.validator.err.unexpectederror";
    
    // Bootstrap performed?
    private boolean bootstrapPerformed = false;

    private Properties properties = null;
    /**
     * @deprecated Was never used
     */
    public void setLog(boolean logEnabled)
    {
        // Ignore
    }

    /**
     * Determine if bootstrap was performed?
     * 
     * @return true => bootstrap was performed
     */
    public boolean hasPerformedBootstrap()
    {
        return bootstrapPerformed;
    }

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    private boolean classInPath(String className)
    {
        try
        {
            Class.forName(className, false, this.getClass().getClassLoader());

            // it exists on the classpath
            return true;
        }
        catch (ClassNotFoundException e)
        {

            // it does not exist on the classpath
            return false;
        }
    }

    /**
     * Check if Java unserialize remote code execution is already fixed on this
     * <b>commons collections</b> version.
     * 
     * @return
     */
    private boolean isCommonsCollectionsDeserializerFixed()
    {

        try
        {
            Class<?> invokerTransformerClass = Class.forName(
                    "org.apache.commons.collections.functors.InvokerTransformer", true, this
                            .getClass().getClassLoader());

            if (invokerTransformerClass != null)
            {
                Constructor<?> invokerTransformerConstructor = invokerTransformerClass
                        .getConstructor(String.class, Class[].class, Object[].class);

                Object invokerTransformerInstance = invokerTransformerConstructor.newInstance(null,
                        null, null);

                ObjectOutputStream objectOut = null;
                ByteArrayOutputStream byteOut = null;
                try
                {
                    // Write the object out to a byte array
                    byteOut = new ByteArrayOutputStream();
                    objectOut = new ObjectOutputStream(byteOut);
                    objectOut.writeObject(invokerTransformerInstance);
                    objectOut.flush();
                }
                catch (UnsupportedOperationException e)
                {
                    // Expected: Serialization support is disabled for security
                    // reasons.
                    return true;
                }
                catch (IOException e)
                {
                    throw new AlfrescoRuntimeException(ERR_UNEXPECTED_ERROR, e);
                }
                finally
                {
                    if (objectOut != null)
                    {
                        try
                        {
                            objectOut.close();
                        }
                        catch (Throwable e)
                        {
                        }
                    }
                    if (byteOut != null)
                    {
                        try
                        {
                            byteOut.close();
                        }
                        catch (Throwable e)
                        {
                        }
                    }
                }
            }
        }
        catch (SecurityException e)
        {
            // This is and expected, acceptable exception that we can ignore.
        }
        catch (ClassNotFoundException e)
        {
            // This is and expected, acceptable exception that we can ignore.
        }
        catch (InstantiationException e)
        {
            // This is and expected, acceptable exception that we can ignore.
        }
        catch (IllegalAccessException e)
        {
            // This is and expected, acceptable exception that we can ignore.
        }
        catch (NoSuchMethodException e)
        {
            throw new AlfrescoRuntimeException(ERR_UNEXPECTED_ERROR, e);
        }
        catch (IllegalArgumentException e)
        {
            throw new AlfrescoRuntimeException(ERR_UNEXPECTED_ERROR, e);
        }
        catch (InvocationTargetException e)
        {
            // This is and expected, acceptable exception that we can ignore.
        }

        return false;
    }

    private void validate()
    {
        if (classInPath("org.apache.xalan.xsltc.trax.TemplatesImpl") && classInPath("org.springframework.core.SerializableTypeWrapper"))
        {
            throw new AlfrescoRuntimeException(
                    "Bootstrap failed: both org.apache.xalan.xsltc.trax.TemplatesImpl and org.springframework.core.SerializableTypeWrapper appear at the same time in classpath ");
        }

        // Check if Java unserialize remote code execution is available and not
        // fixed on this <b>commons collections</b>
        if ((classInPath("org.apache.commons.collections.functors.InvokerTransformer")
                || classInPath("org.apache.commons.collections.functors.InstantiateFactory")
                || classInPath("org.apache.commons.collections.functors.InstantiateTransformer")
                || classInPath("org.apache.commons.collections.functors.PrototypeCloneFactory")
                || classInPath("org.apache.commons.collections.functors.PrototypeSerializationFactory")
                || classInPath("org.apache.commons.collections.functors.WhileClosure")
                || classInPath("org.apache.commons.collections.functors.CloneTransformer") || classInPath("org.apache.commons.collections.functors.ForClosure"))
                && !isCommonsCollectionsDeserializerFixed())
        {
            throw new AlfrescoRuntimeException(
                    "Bootstrap failed: org.apache.commons.collections.functors.* unsafe serialization classes found in classpath.");
        }
    }

    private boolean isUnserializerValidatorEnabled()
    {
        return getBooleanProperty(PROPERTY_UNSERIALIZER_VALIDATOR_ENABLED, true);
    }

    private boolean getBooleanProperty(String name, boolean defaultValue)
    {
        boolean value = defaultValue;
        if (properties != null)
        {
            String property = properties.getProperty(name);
            if (property != null)
            {
                value = !property.trim().equalsIgnoreCase("false");
            }
        }
        return value;
    }

    /**
     * Bootstrap unserializer validator.
     */
    public void bootstrap()
    {
        if (isUnserializerValidatorEnabled())
        {
            validate();
        }
        else
        {
            logger.warn("Unserializer validator is disabled");
        }

        // a bootstrap was performed
        bootstrapPerformed = true;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        bootstrap();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }

}
