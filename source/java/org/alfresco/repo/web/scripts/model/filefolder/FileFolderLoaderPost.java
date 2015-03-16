/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.model.filefolder;

import java.io.IOException;
import java.io.OutputStream;

import org.alfresco.repo.model.filefolder.FileFolderLoader;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Link to {@link FileFolderLoader}
 */
public class FileFolderLoaderPost extends AbstractWebScript implements ApplicationContextAware
{
    public static final String KEY_FOLDER_PATH = "folderPath";
    public static final String KEY_FILE_COUNT = "fileCount";
    public static final String KEY_FILES_PER_TXN = "filesPerTxn";
    public static final String KEY_MIN_FILE_SIZE = "minFileSize";
    public static final String KEY_MAX_FILE_SIZE = "maxFileSize";
    public static final String KEY_MAX_UNIQUE_DOCUMENTS = "maxUniqueDocuments";
    public static final String KEY_FORCE_BINARY_STORAGE = "forceBinaryStorage";
    public static final String KEY_DESCRIPTION_COUNT = "descriptionCount";
    public static final String KEY_DESCRIPTION_SIZE = "descriptionSize";
    public static final String KEY_COUNT = "count";
    
    public static final int DEFAULT_FILE_COUNT = 100;
    public static final int DEFAULT_FILES_PER_TXN = 100;
    public static final long DEFAULT_MIN_FILE_SIZE = 80*1024L;
    public static final long DEFAULT_MAX_FILE_SIZE = 120*1024L;
    public static final long DEFAULT_MAX_UNIQUE_DOCUMENTS = Long.MAX_VALUE;
    public static final int DEFAULT_DESCRIPTION_COUNT = 1;
    public static final long DEFAULT_DESCRIPTION_SIZE = 128L;
    public static final boolean DEFAULT_FORCE_BINARY_STORAGE = false;
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        FileFolderLoader loader = (FileFolderLoader) applicationContext.getBean("fileFolderLoader");
        
        int count = 0;
        String folderPath = "";
        try
        {
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            folderPath = json.getString(KEY_FOLDER_PATH);
            if (folderPath == null)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, KEY_FOLDER_PATH + " not supplied.");
            }
            int fileCount = 100;
            if (json.has(KEY_FILE_COUNT))
            {
                fileCount = json.getInt(KEY_FILE_COUNT);
            }
            int filesPerTxn = DEFAULT_FILES_PER_TXN;
            if (json.has(KEY_FILES_PER_TXN))
            {
                filesPerTxn = json.getInt(KEY_FILES_PER_TXN);
            }
            long minFileSize = DEFAULT_MIN_FILE_SIZE;
            if (json.has(KEY_MIN_FILE_SIZE))
            {
                minFileSize = json.getInt(KEY_MIN_FILE_SIZE);
            }
            long maxFileSize = DEFAULT_MAX_FILE_SIZE;
            if (json.has(KEY_MAX_FILE_SIZE))
            {
                maxFileSize = json.getInt(KEY_MAX_FILE_SIZE);
            }
            long maxUniqueDocuments = DEFAULT_MAX_UNIQUE_DOCUMENTS;
            if (json.has(KEY_MAX_UNIQUE_DOCUMENTS))
            {
                maxUniqueDocuments = json.getInt(KEY_MAX_UNIQUE_DOCUMENTS);
            }
            boolean forceBinaryStorage = DEFAULT_FORCE_BINARY_STORAGE;
            if (json.has(KEY_FORCE_BINARY_STORAGE))
            {
                forceBinaryStorage = json.getBoolean(KEY_FORCE_BINARY_STORAGE);
            }
            int descriptionCount = DEFAULT_DESCRIPTION_COUNT;
            if (json.has(KEY_DESCRIPTION_COUNT))
            {
                descriptionCount = json.getInt(KEY_DESCRIPTION_COUNT);
            }
            long descriptionSize = DEFAULT_DESCRIPTION_SIZE;
            if (json.has(KEY_DESCRIPTION_SIZE))
            {
                descriptionSize = json.getLong(KEY_DESCRIPTION_SIZE);
            }
            
            // Perform the load
            count = loader.createFiles(
                    folderPath,
                    fileCount, filesPerTxn,
                    minFileSize, maxFileSize,
                    maxUniqueDocuments,
                    forceBinaryStorage,
                    descriptionCount, descriptionSize);
        }
        catch (FileNotFoundException e)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Folder not found: ", folderPath);
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
        }
        // Write the response
        OutputStream os = res.getOutputStream();
        try
        {
            JSONObject json = new JSONObject();
            json.put(KEY_COUNT, count);
            os.write(json.toString().getBytes("UTF-8"));
        }
        catch (JSONException e)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Failed to write JSON", e);
        }
        finally
        {
            os.close();
        }
    }
}
