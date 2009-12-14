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
package org.alfresco.repo.domain;

import java.util.Locale;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.hibernate.DirtySessionAnnotation;
import org.springframework.extensions.surf.util.Pair;

/**
 * Data abstraction layer for Locale entities.
 * 
 * @author Derek Hulley
 * @since 2.2.1
 */
public interface LocaleDAO
{
    /**
     * @param id            the unique ID of the entity
     * @return              the locale (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    @DirtySessionAnnotation(markDirty=false)
    Pair<Long, Locale> getLocalePair(Long id);
    
    /**
     * @param id            the locale to fetch or <tt>null</tt> to get the default locale
     * @return              the locale or <tt>null</tt> if no such locale exists
     */
    @DirtySessionAnnotation(markDirty=false)
    Pair<Long, Locale> getLocalePair(Locale locale);

    /**
     * @return              the locale pair for the default locale.  Although the <tt>Locale</tt>
     *                      object will be populated, the ID will point to an instance that generically
     *                      refers to the system's default locale i.e. the value returned can vary
     *                      depending on the executing thread's default locale.
     */
    @DirtySessionAnnotation(markDirty=false)
    Pair<Long, Locale> getDefaultLocalePair();
     
    /**
     * Gets the locale ID for an existing instance or creates a new entity if
     * one doesn't exist.
     * 
     * @param id            the locale to fetch or <tt>null</tt> to get or create the default
     *                      locale.
     * @return              the locale - never <tt>null</tt>
     */
    @DirtySessionAnnotation(markDirty=true)
    Pair<Long, Locale> getOrCreateLocalePair(Locale locale);

    /**
     * Find or create the details representing the default locale.
     * 
     * @return              the locale pair for the default locale.  Although the <tt>Locale</tt>
     *                      object will be populated, the ID will point to an instance that generically
     *                      refers to the system's default locale i.e. the value returned can vary
     *                      depending on the executing thread's default locale.
     */
    @DirtySessionAnnotation(markDirty=true)
    Pair<Long, Locale> getOrCreateDefaultLocalePair();
}
