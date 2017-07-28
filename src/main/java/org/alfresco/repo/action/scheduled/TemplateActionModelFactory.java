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
package org.alfresco.repo.action.scheduled;

import java.util.Map;

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
     * @return - the template engine.
     */
    public String getTemplateEngine();
    
    /**
     * Build a model with no default node context.
     * 
     * @return - the model for the template engine.
     */
    public Map<String, Object> getModel();
    
    /**
     * Build a model with a default node context.
     * 
     * @param nodeRef NodeRef
     * @return - the model (with nodeRef as its context).
     */
    public Map<String, Object> getModel(NodeRef nodeRef);
}
