/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.audit.extractor;

import java.io.Serializable;

/**
 * An extractor that merely records a null value.  This enables configuration such
 * that the <i>presence</i> of a data path can be recorded when the actual value
 * is of no interest.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class NullValueDataExtractor extends AbstractDataExtractor
{
    /**
     * @return          Returns <tt>true</tt> always
     */
    public boolean isSupported(Serializable data)
    {
        return true;
    }

    /**
     * @return          Returns <tt>null</tt> always
     */
    public Serializable extractData(Serializable in) throws Throwable
    {
        return null;
    }
}
