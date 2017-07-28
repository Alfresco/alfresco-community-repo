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
 * An {@link Extensible} object exposes a set of {@link Trait}s as
 * {@link ExtendedTrait}s objects.<br>
 * An {@link ExtendedTrait} is an association between a {@link Trait} exposing
 * object and several extension objects.<br>
 * The actual {@link Trait}s and associated extensions provided by an
 * {@link Extensible} object are given by its {@link ExtensionPoint} handling
 * strategy and by the current set of registered extensions (see
 * {@link Extender}).<br>
 * The exposed {@link Trait}s can be thought of as parts of an object's
 * interface that will be exposed to an extension. Upon the extension invocation
 * the given trait instances will be made available to their corresponding
 * extensions.
 *
 * @author Bogdan Horje
 */
public interface Extensible
{
    <T extends Trait> ExtendedTrait<T> getTrait(Class<? extends T> traitAPI);
}
