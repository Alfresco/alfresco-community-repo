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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A runtime retained annotation that marks AJ-trait-extended methods of
 * {@link Extensible} objects.<br>
 * It defines the actual circumstances in which the {@link ExtensionPoint}
 * defined using {@link #extensionAPI()} and {@link #traitAPI()} has its
 * extension invoked.<br>
 * Methods marked by this aspect are advised by an extension-routing around
 * advice in {@link RouteExtensions}. Consequently the call will be routed to a
 * method of an extension object having the same signature as the marked method.<br>
 * 
 * @author Bogdan Horje
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Extend
{
    Class<?> extensionAPI();

    Class<? extends Trait> traitAPI();
}
