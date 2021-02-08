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

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RenditionService2NewImpl implements RenditionService2New
{

    private RenditionService2Impl renditionService2;

    private boolean storeRenditionAsPropertyEnabled;

    @Override public RenditionDefinitionRegistry2 getRenditionDefinitionRegistry2()
    {
        return renditionService2.getRenditionDefinitionRegistry2();
    }

    public void setRenditionService2(RenditionService2Impl renditionService2)
    {
        this.renditionService2 = renditionService2;
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
        List<ChildAssociationRef> childAssociationRefList = renditionService2.getRenditions(sourceNodeRef);
        List<RenditionContentData> renditionContentDataList = new ArrayList<>();
        renditionContentDataList.addAll(convertToRenditionContentDataList(childAssociationRefList));
        //TODO - retrieve RenditionCotentData the new way and add it to the list
        return renditionContentDataList;
    }

    private List<RenditionContentData> convertToRenditionContentDataList(List<ChildAssociationRef> childAssociationRefList)
    {
        //TODO - Implement the logic convert ChildAssoc to RenditionCotentData
        List<RenditionContentData> renditionContentDataList=new ArrayList<>();
        return renditionContentDataList;
    }

    private RenditionContentData convertToRenditionContentData(ChildAssociationRef childAssociationRef)
    {
        //TODO - implement the logic to extract RenditionContentData from childAssocicationRef
        return new RenditionContentData("");
    }


    @Override public RenditionContentData getRenditionByName(NodeRef sourceNodeRef, String renditionName)
    {

        ChildAssociationRef childAssociationRef = renditionService2.getRenditionByName(sourceNodeRef, renditionName);
        if(childAssociationRef!=null)
            return convertToRenditionContentData(childAssociationRef);
        //TODO - retrieve the contentData the new way from properties
        return null;
    }

    public void consume(NodeRef sourceNodeRef, InputStream transformInputStream, RenditionDefinition2 renditionDefinition,
                int transformContentHashCode)
    {
        if (renditionDefinition instanceof TransformDefinition || !isStoreRenditionAsPropertyEnabled())
        {
             renditionService2.consume(sourceNodeRef, transformInputStream, renditionDefinition, transformContentHashCode);

        }
        else{
            //TODO- implement the logic to store the renditions as property
        }
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

}
