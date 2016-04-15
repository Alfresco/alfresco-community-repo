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
package org.alfresco.repo.bulkimport.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.bulkimport.ContentDataFactory;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * Factory that creates {@link ContentData} out of : 
 * <ul>
 *   <li> a {@link ContentStore}
 *   <li> a {@link File} located within that store's root
 * </ul><br>
 * 
 * The mimetype will be guessed from the file extension, or fall back to binary.
 * The encoding will be guessed from the file itself, or fall back to {@link #defaultEncoding}.
 * 
 * @since 4.0
 */
public class FilesystemContentDataFactory implements ContentDataFactory, InitializingBean
{
    private static final Log logger   = LogFactory.getLog(FilesystemContentDataFactory.class);
    
    private static final String PROTOCOL_DELIMITER = ContentStore.PROTOCOL_DELIMITER;
    
    private MimetypeService mimetypeService;
    private String defaultEncoding;
    private String storeProtocol;
    
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    public void setDefaultEncoding(String defaultEncoding)
    {
        this.defaultEncoding = defaultEncoding;
    }
    
    public void setStoreProtocol(String storeProtocol)
    {
        this.storeProtocol = storeProtocol;
    }

    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "mimetypeService", mimetypeService);
        PropertyCheck.mandatory(this, "defaultEncoding", defaultEncoding);
        PropertyCheck.mandatory(this, "storeProtocol",   storeProtocol);
    }
    
    /**
     * Create a {@link ContentData} by combining the given {@link ContentStore}'s root location and the {@link File}'s path within that store.
     * The given file must therefore be accessible within the content store's configured root location.
     * The encoding and mimetype will be guessed from the given file. 
     * 
     * @param store            The {@link ContentStore} in which the file should be
     * @param contentFile    The {@link File} to check
     * @return the constructed {@link ContentData}
     */
    public ContentData createContentData(ContentStore store, File contentFile)
    {
        try
        {
            String rootLocation = new File(store.getRootLocation()).getCanonicalPath();
            String contentLocation = contentFile.getCanonicalPath();
            if (!contentLocation.startsWith(rootLocation + File.separator))
            {
                throw new IllegalArgumentException("Can't create content URL : file '" + contentLocation
                        + "' is not located within the store's tree ! The store's root is :'" + rootLocation);
            }
            String relativeFilePath = contentLocation.substring(rootLocation.length() + File.separator.length());
            String mimetype = mimetypeService.guessMimetype(contentFile.getName());
            String encoding = defaultEncoding;
            if (!contentFile.isDirectory())
            {
                encoding = guessEncoding(contentFile, mimetype);
            }

            ContentData contentData = new ContentData(storeProtocol + PROTOCOL_DELIMITER + relativeFilePath, mimetype,
                    contentFile.length(), encoding);

            Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
            contentProps.put(ContentModel.PROP_NAME, contentFile.getName());
            contentProps.put(ContentModel.PROP_CONTENT, contentData);

            return contentData;
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * Attempt to guess file encoding. fall back to {@link #defaultEncoding} otherwise.
     * 
     * @param file        the {@link java.io.File} to test
     * @param mimetype    the file mimetype. used to first distinguish between binary and text files
     * @return the encoding as a {@link String}
     */
    private String guessEncoding(File file,String mimetype)
    {
        String encoding = defaultEncoding; // fallback default
        if(file.isDirectory())
            return defaultEncoding; // not necessary to guess folder encoding
        InputStream is = null;
        ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();

        try
        {
           is = new BufferedInputStream(new FileInputStream(file));
           encoding = charsetFinder.getCharset(is, mimetype).name();
        }
        catch (Throwable e)
        {
            if(logger.isWarnEnabled())
                logger.warn("Failed to guess character encoding of file: '" + file.getName() + "'. Falling back to configured default encoding (" + defaultEncoding + ")");
        }
        finally
        {
           if (is != null)
           {
              try { is.close(); } catch (Throwable e) {}
           }
        }
        
        return encoding;
    }

}
