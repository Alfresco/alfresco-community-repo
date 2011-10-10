/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.bulkimport.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
	private static final String OS_FILE_SEPARATOR  = System.getProperty("file.separator");
	
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
	 * @param store			The {@link ContentStore} in which the file should be
	 * @param contentFile	The {@link File} to check
	 * @return the constructed {@link ContentData}
	 */
	public ContentData createContentData(ContentStore store, File contentFile)
    {
		if(!contentIsInStore(contentFile, store))
		{
			throw new IllegalArgumentException("Can't create content URL : file '" + contentFile.getAbsolutePath() + 
					"' is not located within the store's tree ! The store's root is :'" + store.getRootLocation());
		}
			
		String relativeFilePath = contentFile.getAbsolutePath().replace(store.getRootLocation() + OS_FILE_SEPARATOR, "");
		String mimetype = mimetypeService.guessMimetype(contentFile.getName());
    	String encoding = defaultEncoding;
    	if(!contentFile.isDirectory())
    	{
    		encoding = guessEncoding(contentFile, mimetype);
    	}
    	
        ContentData contentData = new ContentData(storeProtocol + PROTOCOL_DELIMITER + relativeFilePath, mimetype, contentFile.length(), encoding);
        
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, contentFile.getName());
        contentProps.put(ContentModel.PROP_CONTENT, contentData);
         
        return contentData;		
    }
	
	/**
	 * Check if file is in the store's tree, by checking if the file path starts 
	 * with the store's configured root location.
	 * 
	 * @param store			The {@link ContentStore} in which the file should be
	 * @param contentFile	The {@link File} to check
	 * @return boolean : whether or not the file is in the expected file tree
	 */
	private boolean contentIsInStore(File contentFile,ContentStore store)
	{
		return contentFile.getAbsolutePath().startsWith(store.getRootLocation());
	}
	
	/**
	 * Attempt to guess file encoding. fall back to {@link #defaultEncoding} otherwise.
	 * 
	 * @param file		the {@link java.io.File} to test
	 * @param mimetype	the file mimetype. used to first distinguish between binary and text files
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
