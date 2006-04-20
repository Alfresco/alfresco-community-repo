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
package org.alfresco.repo.action.scheduled;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A factory that builds models to use with a particular template engine for use with scheduled actions built
 * from action templates.
 * 
 * @author Andy Hind
 */
public interface TemplateActionModelFactory
{
    /**
     * Get the name of the template engine for which this factory applies
     * 
     * @return
     */
    public String getTemplateEngine();
    
    /**
     * Build a model with no default node context.
     * 
     * @return
     */
    public Object getModel();
    
    /**
     * Build a model with a default node context.
     * 
     * @param nodeRef
     * @return
     */
    public Object getModel(NodeRef nodeRef);
}
