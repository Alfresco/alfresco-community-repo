/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2ClassAssociation;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This webscript applies necessary changes to the RM custom model in the repository. These changes
 * are to 'patch' a deployed RM custom model during the DoD certification process. With that in mind
 * they are safe to apply to a live database i.e. without side-effect to existing data and safe
 * to call multiple times.
 * <P>
 *
 * TODO This webscript should be removed after DOD certification as none of these patches are needed
 * for a newly-installed DoD amp.
 *
 * @author neilm
 */
@Deprecated
public class ApplyDodCertModelFixesGet extends DeclarativeWebScript
                                  implements RecordsManagementModel
{
    private static final NodeRef RM_CUSTOM_MODEL_NODE_REF = new NodeRef("workspace://SpacesStore/records_management_custom_model");
    private static final String RMC_CUSTOM_RECORD_SERIES_PROPERTIES = RecordsManagementCustomModel.RM_CUSTOM_PREFIX + ":customRecordSeriesProperties";
    private static final String RMC_CUSTOM_RECORD_CATEGORY_PROPERTIES = RecordsManagementCustomModel.RM_CUSTOM_PREFIX + ":customRecordCategoryProperties";
    private static final String RMC_CUSTOM_RECORD_FOLDER_PROPERTIES = RecordsManagementCustomModel.RM_CUSTOM_PREFIX + ":customRecordFolderProperties";
    private static final String RMC_CUSTOM_RECORD_PROPERTIES = RecordsManagementCustomModel.RM_CUSTOM_PREFIX + ":customRecordProperties";

    /** Logger */
    private static Log logger = LogFactory.getLog(ApplyDodCertModelFixesGet.class);

    private ContentService contentService;
    private NamespaceService namespaceService;

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Applying webscript-based patches to RM custom model in the repo.");
        }

        M2Model customModel = readCustomContentModel();

        String customAspectName = ASPECT_CUSTOM_ASSOCIATIONS.toPrefixString(namespaceService);
        M2Aspect customAssocsAspect = customModel.getAspect(customAspectName);

        if (customAssocsAspect == null)
        {
            final String msg = "Unknown aspect: " + customAspectName;
            if (logger.isErrorEnabled())
            {
                logger.error(msg);
            }
            throw new AlfrescoRuntimeException(msg);
        }


        // MOB-1573. All custom references should have many-many multiplicity.
        if (logger.isInfoEnabled())
        {
            logger.info("MOB-1573. All custom references should have many-many multiplicity.");
        }

        for (M2ClassAssociation classAssoc : customAssocsAspect.getAssociations())
        {
            classAssoc.setSourceMany(true);
            classAssoc.setTargetMany(true);

        }

        //MOB-1621. Custom fields should be created as untokenized by default.
        if (logger.isInfoEnabled())
        {
            logger.info("MOB-1621. Custom fields should be created as untokenized by default.");
        }

        List<String> allCustomPropertiesAspects = new ArrayList<String>(4);
        allCustomPropertiesAspects.add(RMC_CUSTOM_RECORD_SERIES_PROPERTIES);
        allCustomPropertiesAspects.add(RMC_CUSTOM_RECORD_CATEGORY_PROPERTIES);
        allCustomPropertiesAspects.add(RMC_CUSTOM_RECORD_FOLDER_PROPERTIES);
        allCustomPropertiesAspects.add(RMC_CUSTOM_RECORD_PROPERTIES);
        for (String aspectName : allCustomPropertiesAspects)
        {
            M2Aspect aspectObj = customModel.getAspect(aspectName);
            List<M2Property> customProperties = aspectObj.getProperties();
            for (M2Property propertyObj : customProperties)
            {
                propertyObj.setIndexed(true);
                propertyObj.setIndexedAtomically(true);
                propertyObj.setStoredInIndex(false);
                propertyObj.setIndexTokenisationMode(IndexTokenisationMode.FALSE);
            }
        }


        writeCustomContentModel(customModel);

        if (logger.isInfoEnabled())
        {
            logger.info("Completed application of webscript-based patches to RM custom model in the repo.");
        }

        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
    	model.put("success", true);

        return model;
    }

    private M2Model readCustomContentModel()
    {
        ContentReader reader = this.contentService.getReader(RM_CUSTOM_MODEL_NODE_REF,
                                                             ContentModel.TYPE_CONTENT);

        if (!reader.exists()) {throw new AlfrescoRuntimeException("RM CustomModel has no content.");}

        InputStream contentIn = null;
        M2Model deserializedModel = null;
        try
        {
            contentIn = reader.getContentInputStream();
            deserializedModel = M2Model.createModel(contentIn);
        }
        finally
        {
            try
            {
                if (contentIn != null)
                {
                    contentIn.close();
                }
            }
            catch (IOException ignored)
            {
                // Intentionally empty.`
            }
        }
        return deserializedModel;
    }

    private void writeCustomContentModel(M2Model deserializedModel)
    {
        ContentWriter writer = this.contentService.getWriter(RM_CUSTOM_MODEL_NODE_REF,
                                                             ContentModel.TYPE_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.setEncoding("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        deserializedModel.toXML(baos);

        String updatedModelXml;
        try
        {
            updatedModelXml = baos.toString("UTF-8");
            writer.putContent(updatedModelXml);
            // putContent closes all resources.
            // so we don't have to.
        } catch (UnsupportedEncodingException uex)
        {
            throw new AlfrescoRuntimeException("Exception when writing custom model xml.", uex);
        }
    }
}