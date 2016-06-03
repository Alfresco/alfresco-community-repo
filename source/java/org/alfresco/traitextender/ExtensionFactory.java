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
 * Creates extension instances for given {@link Trait}s and
 * {@link ExtensionPoint}s.
 *
 * @author Bogdan Horje
 */
public interface ExtensionFactory<E>
{
    <T extends Trait> E createExtension(T trait);

    /**
     * @param point
     * @return <code>true</code> if the given extensio-point API elements are
     *         compatible with the returned extension (i.e. the given extension
     *         API is assignable form the type of the extension created by this
     *         factory and the {@link Trait} accepted as aparameter in
     *         {@link #createExtension(Trait)} is assignable from the type of
     *         the given trait API).
     */
    boolean canCreateExtensionFor(ExtensionPoint<?, ?> point);
}
