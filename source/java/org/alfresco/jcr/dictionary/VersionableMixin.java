/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
