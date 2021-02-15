/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

package org.alfresco.repo.rendition2;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.*;

public class RenditionService2NewImpl implements RenditionService2New, InitializingBean
{
    private static Log logger = LogFactory.getLog(RenditionService2New.class);
    private RenditionService2Impl renditionService2;
    private boolean storeRenditionAsPropertyEnabled;
    private NodeService nodeService;

    @Override public RenditionDefinitionRegistry2 getRenditionDefinitionRegistry2()
    {
        return renditionService2.getRenditionDefinitionRegistry2();
    }

    public void setRenditionService2(RenditionService2Impl renditionService2)
    {
        this.renditionService2 = renditionService2;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override public void transform(NodeRef sourceNodeRef, TransformDefinition transformDefinition)
    {

        renditionService2.transform(sourceNodeRef, transformDefinition);
    }

    @Override public void render(NodeRef sourceNodeRef, String renditionName)
    {
        renditionService2.render(sourceNodeRef, renditionName);

    }

    @Override public List<RenditionContentData> getRenditions(NodeRef sourceNodeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("get RenditionContentData for sourceNodeRef:"+ sourceNodeRef.getId());
        }

        List<ChildAssociationRef> childAssociationRefList = renditionService2.getRenditions(sourceNodeRef);
        List<RenditionContentData> renditionContentDataList = new ArrayList<>();
        renditionContentDataList.addAll(convertToRenditionContentDataList(childAssociationRefList));
        if (getRenditionContentDataList(sourceNodeRef).isPresent())
            renditionContentDataList.addAll(getRenditionContentDataList(sourceNodeRef).get());

        return renditionContentDataList;
    }

    private List<RenditionContentData> convertToRenditionContentDataList(
                List<ChildAssociationRef> childAssociationRefList)
    {
        List<RenditionContentData> renditionContentDataList = new ArrayList<>();
        childAssociationRefList.forEach(childAssocRef -> renditionContentDataList
                    .add(convertToRenditionContentData(childAssocRef)));

        return renditionContentDataList;
    }

    private RenditionContentData convertToRenditionContentData(ChildAssociationRef childAssociationRef)
    {
        NodeRef renditionNodeRef = childAssociationRef.getChildRef();
        return getRenditionContentData(renditionNodeRef);
    }

    private RenditionContentData getRenditionContentData(NodeRef renditionNodeRef)
    {
        Map<QName, Serializable> nodeProps = nodeService.getProperties(renditionNodeRef);
        ContentData contentData = (ContentData) nodeProps.get(ContentModel.PROP_CONTENT);
        if (!ContentData.hasContent(contentData))
        {
            throw new IllegalArgumentException("Node id '" + renditionNodeRef.getId() + "' has no content.");
        }
        String renditionName = (String) nodeProps.get(ContentModel.PROP_NAME);
        RenditionContentData renditionContentData = RenditionContentData
                    .getRenditionContentData(contentData, renditionName);
        renditionContentData.setLastModified(((Date) nodeProps.get(ContentModel.PROP_MODIFIED)).getTime());

        return renditionContentData;
    }

    @Override public RenditionContentData getRenditionByName(NodeRef sourceNodeRef, String renditionName)
    {

        if (logger.isDebugEnabled())
        {
            logger.debug("get RenditionContentData for rendition:" + renditionName);
        }
        ChildAssociationRef childAssociationRef = renditionService2.getRenditionByName(sourceNodeRef, renditionName);

        if (childAssociationRef != null)
            return convertToRenditionContentData(childAssociationRef);

        return getRenditionContentData(sourceNodeRef, renditionName);
    }

    @Override public boolean isEnabled()
    {
        return renditionService2.isEnabled();
    }

    @Override public boolean isStoreRenditionAsPropertyEnabled()
    {
        return storeRenditionAsPropertyEnabled;
    }

    public void setStoreRenditionAsPropertyEnabled(boolean booleanValue)
    {
        this.storeRenditionAsPropertyEnabled = booleanValue;
    }

    private RenditionContentData getRenditionContentData(NodeRef sourceNodeRef, String renditionName)
    {
        // todo - there might be scenarios where a single node has renditions stored in both places, maybe even for the same renditionName

        return renditionService2.getRenditionProperty(sourceNodeRef, renditionName);
    }

    private Optional<List<RenditionContentData>> getRenditionContentDataList(NodeRef sourceNodeRef)
    {
        // todo - there might be scenarios where a single node has renditions stored in both places, maybe even for the same renditionName

        List<RenditionContentData> list = renditionService2.getRenditionPropertyList(sourceNodeRef);
        return Optional.ofNullable(list);

    }

    @Override public void afterPropertiesSet() throws Exception
    {

        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "renditionService2", renditionService2);

    }
}
