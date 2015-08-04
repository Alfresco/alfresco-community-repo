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

package org.alfresco.repo.web.scripts.custommodel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;

import org.alfresco.repo.dictionary.CustomModelServiceImpl;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.CustomModels;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.XMLUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Custom model upload POST. This class is the controller for the
 * "cmm-upload.post" web scripts.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelUploadPost extends DeclarativeWebScript
{
    private static final String SHARE_EXT_MODULE_ROOT_ELEMENT = "module";
    private static final String TEMP_FILE_PREFIX = "cmmExport";
    private static final String TEMP_FILE_SUFFIX = ".zip";
    private static final int BUFFER_SIZE = 10 * 1024;

    private CustomModels customModels;
    private CustomModelService customModelService;

    public void setCustomModels(CustomModels customModels)
    {
        this.customModels = customModels;
    }

    public void setCustomModelService(CustomModelService customModelService)
    {
        this.customModelService = customModelService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        if (!customModelService.isModelAdmin(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            throw new WebScriptException(Status.STATUS_FORBIDDEN, PermissionDeniedException.DEFAULT_MESSAGE_ID);
        }

        FormData formData = (FormData) req.parseContent();
        if (formData == null || !formData.getIsMultiPart())
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "cmm.rest_api.model.import_not_multi_part_req");
        }

        ImportResult resultData = null;
        boolean processed = false;
        for (FormData.FormField field : formData.getFields())
        {
            if (field.getIsFile())
            {
                final String fileName = field.getFilename();
                File tempFile = createTempFile(field.getInputStream());
                try (ZipFile zipFile = new ZipFile(tempFile, StandardCharsets.UTF_8))
                {
                    resultData = processUpload(zipFile, field.getFilename());
                }
                catch (ZipException ze)
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "cmm.rest_api.model.import_not_zip_format", new Object[] { fileName });
                }
                catch (IOException io)
                {
                    throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "cmm.rest_api.model.import_process_zip_file_failure", io);
                }
                finally
                {
                    // now the import is done, delete the temp file
                    tempFile.delete();
                }
                processed = true;
                break;
            }

        }

        if (!processed)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "cmm.rest_api.model.import_no_zip_file_uploaded");
        }

        // If we get here, then importing the custom model didn't throw any exceptions.
        Map<String, Object> model = new HashMap<>(2);
        model.put("importedModelName", resultData.getImportedModelName());
        model.put("shareExtXMLFragment", resultData.getShareExtXMLFragment());

        return model;
    }

    protected File createTempFile(InputStream inputStream)
    {
        try
        {
            File tempFile = TempFileProvider.createTempFile(inputStream, TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            return tempFile;
        }
        catch (Exception ex)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "cmm.rest_api.model.import_process_zip_file_failure", ex);
        }
    }

    protected ImportResult processUpload(ZipFile zipFile, String filename) throws IOException
    {
        if (zipFile.size() > 2)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "cmm.rest_api.model.import_invalid_zip_package");
        }

        CustomModel customModel = null;
        String shareExtModule = null;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();

            if (!entry.isDirectory())
            {
                final String entryName = entry.getName();
                try (InputStream input = new BufferedInputStream(zipFile.getInputStream(entry), BUFFER_SIZE))
                {
                    if (!(entryName.endsWith(CustomModelServiceImpl.SHARE_EXT_MODULE_SUFFIX)) && customModel == null)
                    {
                        try
                        {
                            M2Model m2Model = M2Model.createModel(input);
                            customModel = importModel(m2Model);
                        }
                        catch (DictionaryException ex)
                        {
                            if (shareExtModule == null)
                            {
                                // Get the input stream again, as the zip file doesn't support reset.
                                try (InputStream moduleInputStream = new BufferedInputStream(zipFile.getInputStream(entry), BUFFER_SIZE))
                                {
                                    shareExtModule = getExtensionModule(moduleInputStream, entryName);
                                }

                                if (shareExtModule == null)
                                {
                                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "cmm.rest_api.model.import_invalid_zip_entry_format", new Object[] { entryName });
                                }
                            }
                            else
                            {
                                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "cmm.rest_api.model.import_invalid_model_entry", new Object[] { entryName });
                            }
                        }
                    }
                    else
                    {
                        shareExtModule = getExtensionModule(input, entryName);
                        if (shareExtModule == null)
                        {
                            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "cmm.rest_api.model.import_invalid_ext_module_entry", new Object[] { entryName });
                        }
                    }
                }
            }
        }

        return new ImportResult(customModel, shareExtModule);
    }

    protected CustomModel importModel(M2Model m2Model)
    {
        CustomModel model = null;
        try
        {
            model = customModels.createCustomModel(m2Model);
        }
        catch (Exception ex)
        {
            int statusCode;
            if (ex instanceof ConstraintViolatedException)
            {
                statusCode = Status.STATUS_CONFLICT;
            }
            else if (ex instanceof InvalidArgumentException)
            {
                statusCode = Status.STATUS_BAD_REQUEST;
            }
            else
            {
                statusCode = Status.STATUS_INTERNAL_SERVER_ERROR;
            }
            String msg = ex.getMessage();
            // remove log numbers. regEx => match 8 or more integers
            msg = (msg != null) ? msg.replaceAll("\\d{8,}", "").trim() : "cmm.rest_api.model.import_failure";

            throw new WebScriptException(statusCode, msg);
        }

        return model;
    }

    protected String getExtensionModule(InputStream inputStream, String fileName)
    {
        Element rootElement = null;
        try
        {
            final DocumentBuilder db = XMLUtil.getDocumentBuilder();
            rootElement = db.parse(inputStream).getDocumentElement();
        }
        catch (IOException io)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "cmm.rest_api.model.import_process_ext_module_file_failure", io);
        }
        catch (SAXException ex)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "cmm.rest_api.model.import_invalid_ext_module_entry", new Object[] { fileName }, ex);
        }

        if (rootElement != null && SHARE_EXT_MODULE_ROOT_ELEMENT.equals(rootElement.getNodeName()))
        {
            StringWriter sw = new StringWriter();
            XMLUtil.print(rootElement, sw, false);

            return sw.toString();
        }

        return null;
    }

    /**
     * Simple POJO for model import result.
     *
     * @author Jamal Kaabi-Mofrad
     */
    public static class ImportResult
    {
        private String importedModelName;
        private String shareExtXMLFragment;

        public ImportResult(CustomModel customModel, String shareExtXMLFragment)
        {
            this.shareExtXMLFragment = shareExtXMLFragment;
            if (customModel != null)
            {
                this.importedModelName = customModel.getName();
            }
        }

        public String getImportedModelName()
        {
            return this.importedModelName;
        }

        public String getShareExtXMLFragment()
        {
            return this.shareExtXMLFragment;
        }
    }
}
