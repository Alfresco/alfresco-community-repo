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

package org.alfresco.traitextender;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Standard-singleton {@link Extender} implementation.
 *
 * @author Bogdan Horje
 */
public class ExtenderImpl extends Extender
{
    private static Log logger = LogFactory.getLog(Extender.class);

    private List<ExtensionBundle> bundles = new LinkedList<ExtensionBundle>();

    private Map<ExtensionPoint<?, ?>, ExtensionFactory<?>> pointFactories = new ConcurrentHashMap<ExtensionPoint<?, ?>, ExtensionFactory<?>>();

    public synchronized void start(ExtensionBundle bundle)
    {
        bundles.add(bundle);
        bundle.start(this);
    }

    public void stop(ExtensionBundle bundle)
    {
        bundle.stop(this);
        bundles.remove(bundle);
    }

    public synchronized void stopAll()
    {
        List<ExtensionBundle> bundlesCopy = new LinkedList<>(bundles);
        for (ExtensionBundle bundle : bundlesCopy)
        {
            stop(bundle);
        }
    }

    public synchronized <E, M extends Trait> E getExtension(Extensible anExtensible, ExtensionPoint<E, M> point)
    {
        E extension = null;

        // consistency is checked at registration time
        @SuppressWarnings("unchecked")
        ExtensionFactory<E> factory = (ExtensionFactory<E>) pointFactories.get(point);

        if (factory != null)
        {
            ExtendedTrait<M> exTrait = anExtensible.getTrait(point.getTraitAPI());

            extension = exTrait.getExtension(point.getExtensionAPI());

            if (extension == null)
            {
                extension = exTrait.extend(point.getExtensionAPI(),
                                           factory);
                if (logger.isDebugEnabled())
                {
                    logger.debug("trait extension leak trace : " + System.identityHashCode(extension) + " : "
                                + System.identityHashCode(exTrait) + " : " + System.identityHashCode(extension));
                }
            }
        }
        return extension;
    }

    public synchronized void register(ExtensionPoint<?, ?> point, ExtensionFactory<?> factory)
    {
        // point->factory type consistency checks
        if (!factory.canCreateExtensionFor(point))
        {
            throw new InvalidExtension("Invalid extension factory registry entry : " + point + "->" + factory);
        }
        pointFactories.put(point,
                           factory);
    }

    public synchronized void unregister(ExtensionPoint<?, ?> point)
    {
        pointFactories.remove(point);
    }

}
