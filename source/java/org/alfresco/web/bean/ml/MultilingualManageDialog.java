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

import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.DocumentDetailsBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;

public class MultilingualManageDialog extends BaseDialogBean
{
    private final String MSG_MANAGE_DETAILS_FOR="manage_multilingual_details_for";
    
    //TODO: load the bean
    private DocumentDetailsBean docDetails;
    
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getContainerTitle()
    {
        // TODO Auto-generated method stub
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_MANAGE_DETAILS_FOR) + " '" + docDetails.getName() + "'";
    }
    
    @Override
    public List<DialogButtonConfig> getAdditionalButtons()
    {
        // TODO Auto-generated method stub
        return super.getAdditionalButtons();
    }

}
