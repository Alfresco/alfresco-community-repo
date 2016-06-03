/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.jscript.app;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Tag property decorator class.
 *
 * @author Mike Hatfield
 */
public class TagPropertyDecorator extends BasePropertyDecorator
{
    private static Log logger = LogFactory.getLog(TagPropertyDecorator.class);

    /**
     * @see org.alfresco.repo.jscript.app.PropertyDecorator#decorate(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, java.io.Serializable)
     */
    @SuppressWarnings("unchecked")
    public JSONAware decorate(QName propertyName, NodeRef nodeRef, Serializable value)
    {
        Collection<NodeRef> collection = (Collection<NodeRef>)value;
        JSONArray array = new JSONArray();

        for (NodeRef obj : collection)
        {
            try
            {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("name", this.nodeService.getProperty(obj, ContentModel.PROP_NAME));
                jsonObj.put("nodeRef", obj.toString());
                array.add(jsonObj);
            }
            catch (InvalidNodeRefException e)
            {
                logger.warn("Tag with nodeRef " + obj.toString() + " does not exist.");
            }
        }

        return array;
    }
}
