/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.jcr.dictionary;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Encapsulate Versionable Mixin behaviour mapping to Alfresco
 * 
 * @author davidc
 */
public class VersionableMixin implements ClassMap.AddMixin, ClassMap.RemoveMixin
{

    /*
     *  (non-Javadoc)
     * @see org.alfresco.jcr.dictionary.ClassMap.AddMixin#preAddMixin(org.alfresco.jcr.session.SessionImpl, org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<QName, Serializable> preAddMixin(SessionImpl session, NodeRef nodeRef)
    {
        // switch off auto-versioning
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_INITIAL_VERSION, false);
        properties.put(ContentModel.PROP_AUTO_VERSION, false);
        return properties;
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.jcr.dictionary.ClassMap.AddMixin#postAddMixin(org.alfresco.jcr.session.SessionImpl, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void postAddMixin(SessionImpl session, NodeRef nodeRef)
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.jcr.dictionary.ClassMap.RemoveMixin#preRemoveMixin(org.alfresco.jcr.session.SessionImpl, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void preRemoveMixin(SessionImpl session, NodeRef nodeRef)
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.jcr.dictionary.ClassMap.RemoveMixin#postRemoveMixin(org.alfresco.jcr.session.SessionImpl, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void postRemoveMixin(SessionImpl session, NodeRef nodeRef)
    {
    }

}
