/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.encryption;

/**
 * A fallback encryptor provides a fallback mechanism for decryption, first using the default
 * encryption keys and, if they fail (perhaps because they have been changed), falling back
 * to a backup set of keys.
 * 
 * Note that encryption will be performed only using the default encryption keys.
 * 
 * @since 4.0
 */
public interface FallbackEncryptor extends Encryptor
{
    /**
     * Is the backup key available in order to fall back to?
     * 
     * @return boolean
     */
    boolean backupKeyAvailable(String keyAlias);
}
