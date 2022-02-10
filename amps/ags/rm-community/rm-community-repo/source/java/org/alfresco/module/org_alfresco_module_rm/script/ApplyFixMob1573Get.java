/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2ClassAssociation;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This webscript patches the RM custom model as fix for MOB-1573. It is only necessary for databases
 * that had their RM amps initialised before the fix went in.
 * There is no side-effect if it is called when it is not needed or if it is called multiple times.
 *
 * TODO This webscript should be removed after DOD certification.
 *
 * @author neilm
 */
@Deprecated
public class ApplyFixMob1573Get extends DeclarativeWebScript
                                  implements RecordsManagementModel
{
    private static final NodeRef RM_CUSTOM_MODEL_NODE_REF = new NodeRef("workspace://SpacesStore/records_management_custom_model");

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
        M2Model customModel = readCustomContentModel();
        if (customModel == null)
        {
            throw new AlfrescoRuntimeException("Custom content model could not be read");
        }

        // Go through every custom reference defined in the custom model and make sure that it
        // has many-to-many multiplicity
        String aspectName = ASPECT_CUSTOM_ASSOCIATIONS.toPrefixString(namespaceService);
        M2Aspect customAssocsAspect = customModel.getAspect(aspectName);

        if (customAssocsAspect == null)
        {
            throw new AlfrescoRuntimeException("Unknown aspect: "+aspectName);
        }

        for (M2ClassAssociation classAssoc : customAssocsAspect.getAssociations())
        {
            classAssoc.setSourceMany(true);
            classAssoc.setTargetMany(true);
        }

        writeCustomContentModel(customModel);

        Map<String, Object> model = new HashMap<>(1, 1.0f);
    	model.put("success", true);

        return model;
    }

    private M2Model readCustomContentModel()
    {
        ContentReader reader = contentService.getReader(RM_CUSTOM_MODEL_NODE_REF,
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
                // Intentionally empty.
            }
        }
        return deserializedModel;
    }

    private void writeCustomContentModel(M2Model deserializedModel)
    {
        ContentWriter writer = contentService.getWriter(RM_CUSTOM_MODEL_NODE_REF,
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
