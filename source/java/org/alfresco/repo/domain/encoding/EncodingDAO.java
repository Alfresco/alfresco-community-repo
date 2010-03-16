/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.encoding;

import org.alfresco.util.Pair;

/**
 * DAO services for <b>alf_encoding</b> and related tables
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface EncodingDAO
{
    /**
     * Get the encoding pair.
     * 
     * @param encoding              the encoding string
     * @return                      the ID-encoding pair or <tt>null</tt> if it doesn't exsit
     */
    Pair<Long, String> getEncoding(String encoding);
    
    Pair<Long, String> getEncoding(Long id);
    
    Pair<Long, String> getOrCreateEncoding(String encoding);
}
