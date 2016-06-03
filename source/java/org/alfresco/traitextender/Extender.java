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

/**
 * Trait-extender central extension registry and life cycle handler.<br>
 * Implementors must handle:
 * <ol>
 * <li>  
 * {@link ExtensionBundle} life cycle operations that start and stop bundles
 * of extension points implementations at runtime. <br>
 * See also: {@link Extender#start(ExtensionBundle)} ,
 * {@link Extender#stop(ExtensionBundle)} and {@link Extender#stopAll()} ).</li>
 * <li>The management of the extension point factory registry. At runtime
 * extension point factories can be registered or unregistered. The registered
 * factories will later be used for creating extensions of extensible-traits
 * (see {@link Extender#getExtension(Extensible, ExtensionPoint))}.<br>
 * See also:{@link #register(ExtensionPoint, ExtensionFactory)} and
 * {@link Extender#unregister(ExtensionPoint)}</li>
 * <li>The creation of extension for a given extensible trait. <br>
 * See: {@link Extender#getExtension(Extensible, ExtensionPoint)}</li>
 * </ol>
 * 
 * @author Bogdan Horje
 */
public abstract class Extender
{
    private static Extender instance;

    /**
     * @return singleton {@link ExtenderImpl} instance
     */
    public static synchronized Extender getInstance()
    {
        if (instance == null)
        {
            instance = new ExtenderImpl();
        }
        return instance;
    }

    /**
     * Start life-cycle phase trigger method.<br>
     * Upon successful execution the bundle will have all its extension points
     * registered using {@link #register(ExtensionPoint, ExtensionFactory)} and
     * ready to be.
     * 
     * @param bundle to be started
     */
    public abstract void start(ExtensionBundle bundle);

    /**
     * Start life-cycle phase trigger method.<br>
     * Upon successful execution the bundle will have all its extension points
     * unregistered using {@link #unregister(ExtensionPoint)}.
     * 
     * @param bundle to be stopped
     */
    public abstract void stop(ExtensionBundle bundle);

    /**
     * Stops all previously registered {@link ExtensionBundle}s.
     */
    public abstract void stopAll();

    /**
     * Creates and returns a unique per {@link ExtensibleTrait} object or
     * <code>null</code> if no extension-point factory is registered for the
     * given {@link ExtensionPoint}.<br>
     * Given that {@link Extensible#getTrait(Class)} is used to obtain the
     * {@link ExtendedTrait} that the returned extension is uniquely associated
     * with, the uniqueness and garbage collection of the returned extension is
     * dependent on how the given {@link Extensible} handles its
     * {@link ExtendedTrait}s.
     * 
     * @param anExtensible
     * @param point
     * @return a unique per {@link ExtensibleTrait} extension object or
     *         <code>null</code> if no extension-point factory is registered for
     *         the given {@link ExtensionPoint}
     */
    public abstract <E, M extends Trait> E getExtension(Extensible anExtensible, ExtensionPoint<E, M> point);

    /**
     * Registers an extension-point to factory association to be used in
     * extension creation. The presence of an association for a given extension
     * point guarantees that, when requested, a unique extension object per
     * {@link ExtensibleTrait} extension is returned by
     * {@link #getExtension(Extensible, ExtensionPoint)}.
     * 
     * @param point the extension point to be associated with the given
     *            extension factory during extension retrieval using
     *            {@link Extender#getExtension(Extensible, ExtensionPoint)}
     * @param factory
     */
    public abstract void register(ExtensionPoint<?, ?> point, ExtensionFactory<?> factory);

    /**
     * Unregisters an extension-point to factory association of the given
     * extension point. The absence of an association for a given extension
     * point guarantees that, when requested, a null is returned by
     * {@link #getExtension(Extensible, ExtensionPoint)}.<br>
     * Unregistering extension points does not force the garbage collection of
     * the already created extensions.
     * 
     * @param point
     */
    public abstract void unregister(ExtensionPoint<?, ?> point);

}
