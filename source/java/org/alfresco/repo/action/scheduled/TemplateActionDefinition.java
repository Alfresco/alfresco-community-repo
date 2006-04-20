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

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A template action definition is used to generate an action from a template style 
 * definition. 
 * 
 * @author Andy Hind
 */
public interface TemplateActionDefinition
{   
    /**
     * Generate an action definition for the action defined by this template.
     * 
     * @param nodeRef
     * @return
     */
    public Action getAction(NodeRef nodeRef);
}
