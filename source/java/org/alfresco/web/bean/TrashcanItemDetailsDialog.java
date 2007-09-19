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
package org.alfresco.web.bean;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.repository.Path;
import org.alfresco.web.app.Application;

import org.alfresco.web.bean.repository.Node;

public class TrashcanItemDetailsDialog extends TrashcanDialog
{
    private static final String MSG_DETAILS_OF = "details_of";
    private static final String MSG_ORIGINAL_LOCATION = "original_location";
    private static final String MSG_DELETEDITEM_DETAILS_DESCR = "deleteditem_details_description";

    private static final String MSG_CLOSE = "close";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return null;

    }

    @Override
    public String getContainerDescription()
    {
        Path path = (Path) property.getItem().getProperties().get("locationPath");
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_ORIGINAL_LOCATION) + ": " + path.toDisplayPath(nodeService) + "\n"
                + Application.getMessage(FacesContext.getCurrentInstance(), MSG_DELETEDITEM_DETAILS_DESCR);

    }

    @Override
    public String getContainerTitle()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_DETAILS_OF) + " '" + property.getItem().getName() + "'";
    }

    public Node getItem()
    {
        return property.getItem();
    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
    }

}
