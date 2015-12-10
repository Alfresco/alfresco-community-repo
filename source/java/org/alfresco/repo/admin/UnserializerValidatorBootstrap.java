/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.admin;

import java.lang.reflect.Field;

import org.alfresco.error.AlfrescoRuntimeException;
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
 * org.apache.commons.collections.functors.InvokerTransformer.
 */
public class UnserializerValidatorBootstrap extends AbstractLifecycleBean
{

    // Bootstrap performed?
    private boolean bootstrapPerformed = false;

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
     * <b>commons collections</b> version of <b>InvokerTransformer</b>.
     * 
     * @return
     */
    private boolean isCommonsCollectionsDeserializerFixed()
    {
        try
        {
            Class<?> invokerTransformerClass = Class.forName("org.apache.commons.collections.functors.InvokerTransformer", true, this
                    .getClass().getClassLoader());

            if (invokerTransformerClass != null)
            {
                Field deserialize = invokerTransformerClass.getField("DESERIALIZE");
                if (deserialize != null)
                {
                    return true;
                }
            }
        }
        catch (NoSuchFieldException e)
        {
        }
        catch (SecurityException e)
        {
        }
        catch (ClassNotFoundException e)
        {
        }

        return false;
    }

    /**
     * Bootstrap unserializer validator.
     */
    public void bootstrap()
    {
        if (classInPath("org.apache.xalan.xsltc.trax.TemplatesImpl") && classInPath("org.springframework.core.SerializableTypeWrapper"))
        {
            throw new AlfrescoRuntimeException(
                    "Bootstrap failed: both org.apache.xalan.xsltc.trax.TemplatesImpl and org.springframework.core.SerializableTypeWrapper appear at the same time in classpath ");
        }

        // Check if Java unserialize remote code execution is available and not
        // fixed on this <b>commons collections</b> version of
        // <b>InvokerTransformer</b>.
        if (classInPath("org.apache.commons.collections.functors.InvokerTransformer") && !isCommonsCollectionsDeserializerFixed())
        {
            throw new AlfrescoRuntimeException(
                    "Bootstrap failed: org.apache.commons.collections.functors.InvokerTransformer was found in classpath.");
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
