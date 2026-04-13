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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages key resources (key store and key store passwords)
 * 
 * @since 4.0
 * 
 */
public interface KeyResourceLoader
{
    /**
     * Loads and returns an InputStream of the key store at the configured location.
     * If the file cannot be found this method returns null.
     * 
     * @return InputStream
     * @throws FileNotFoundException
     */
    public InputStream getKeyStore(String keyStoreLocation) throws FileNotFoundException;
    
    /**
     * Loads key metadata from the configured passwords file location.
     * 
     * Note that the passwords are not cached locally.
     * If the file cannot be found this method returns null.
     * 
     * @return Properties
     * @throws IOException
     */
    public Properties loadKeyMetaData(String keyMetaDataFileLocation) throws IOException, FileNotFoundException;
}
