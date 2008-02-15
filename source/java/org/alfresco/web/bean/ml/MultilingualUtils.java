/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.ml;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;

/**
 * Util class for the management of multilingual documents on the web client side
 *
 * @author yanipig
 */
public class MultilingualUtils implements Serializable
{

    private static final long serialVersionUID = 2218309432064312000L;

   /**
     * Returns true if the current user has enough right to add a content to the space
     * where the pivot translation is located in.
     *
     * @param multlingualDocument
     * @param fc
     * @return
     */
    public static boolean canAddChildrenToPivotSpace(Node multlingualDocument, FacesContext fc)
    {
        MultilingualContentService mlservice = getMultilingualContentService(fc);
        NodeService nodeService = getNodeService(fc);

        // get the pivot translation and get the space where it's located
        NodeRef pivot = mlservice.getPivotTranslation(multlingualDocument.getNodeRef());
        NodeRef space = nodeService.getPrimaryParent(pivot).getParentRef();

        // return if the current user can add a content to the space of the pivot
        return new Node(space).hasPermission(PermissionService.ADD_CHILDREN);
    }

    /**
     * Returns true if the current user can delete each translation of the mlContainer of the given node
     *
     * @param multlingualDocument
     * @param fc
     * @return
     */
    public static boolean canDeleteEachTranslation(Node multlingualDocument, FacesContext fc)
    {
        boolean can = true;

        MultilingualContentService mlservice = getMultilingualContentService(fc);

        Map<Locale, NodeRef> translations = mlservice.getTranslations(multlingualDocument.getNodeRef());
        for (Map.Entry<Locale, NodeRef> entry : translations.entrySet())
        {
            Node translation = new Node(entry.getValue());

            if(translation.hasPermission(PermissionService.DELETE_NODE) == false
                    || translation.isLocked() == true
                    || translation.hasAspect(ContentModel.ASPECT_WORKING_COPY) == true
                )
            {
                can = false;
                break;
            }
        }

        return can;
    }

    /**
     * Returns true if the current user can move each translation of the mlContainer of the given node
     *
     * @param multlingualDocument
     * @param fc
     * @return
     */
    public static boolean canMoveEachTranslation(Node multlingualDocument, FacesContext fc)
    {
        boolean can = true;

        MultilingualContentService mlservice = getMultilingualContentService(fc);

        Map<Locale, NodeRef> translations = mlservice.getTranslations(multlingualDocument.getNodeRef());
        for (Map.Entry<Locale, NodeRef> entry : translations.entrySet())
        {
            Node translation = new Node(entry.getValue());

            if(translation.hasPermission(PermissionService.DELETE_NODE) == false)
            {
                can = false;
                break;
            }
        }

        return can;
    }

    /**
     * Returns true if the current user can delete each translation and create
 *   * a new content in the space
     *
     * @param multlingualDocument
     * @param fc
     * @return
     */
    public static boolean canStartNewEditon(Node multlingualDocument, FacesContext fc)
    {
        boolean canDelete = MultilingualUtils.canMoveEachTranslation(multlingualDocument, fc);
        boolean canCreate = MultilingualUtils.canAddChildrenToPivotSpace(multlingualDocument, fc);

        return canDelete && canCreate;
    }

    private static MultilingualContentService getMultilingualContentService(FacesContext fc)
    {
        return (MultilingualContentService) FacesHelper.getManagedBean(fc, "MultilingualContentService");
    }

    private static NodeService getNodeService(FacesContext fc)
    {
        return (NodeService) FacesHelper.getManagedBean(fc, "NodeService");
    }

}
