/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.rest;

import java.util.List;

import org.alfresco.cmis.CMISAccessControlFormatEnum;
import org.alfresco.cmis.CMISAccessControlService;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Custom FreeMarker Template language method.
 * <p>
 * Gets the ACL of a TemplateNode
 * <p>
 * Usage: cmisacl(TemplateNode node)
 *        cmisacl(TemplateNode node, String format)
 * 
 * @author dward
 */
public class CMISAclMethod implements TemplateMethodModelEx
{
    private CMISAccessControlService accessControlService;

    /**
     * Construct
     */
    public CMISAclMethod(CMISAccessControlService accessControlService)
    {
        this.accessControlService = accessControlService;
    }

    @SuppressWarnings("unchecked")
    public Object exec(List args) throws TemplateModelException
    {
        NodeRef nodeRef = null;
        CMISAccessControlFormatEnum format = CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS;
        try
        {
            int i = 0;
            // extract node ref
            Object arg = args.get(i++);
            if (arg instanceof BeanModel)
            {
                Object wrapped = ((BeanModel) arg).getWrappedObject();
                if (wrapped != null)
                {
                    if (wrapped instanceof TemplateNode)
                    {
                        nodeRef = ((TemplateNode) wrapped).getNodeRef();
                    }
                }
            }
            // extract format if specified
            arg = args.get(i++);
            if (arg instanceof TemplateScalarModel)
            {
                format = CMISAccessControlFormatEnum.FACTORY.toEnum(((TemplateScalarModel) arg).getAsString());                
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore optional arguments
        }

        // query renditions
        if (nodeRef != null)
        {
            return accessControlService.getAcl(nodeRef, format);
        }

        return null;
    }

}
