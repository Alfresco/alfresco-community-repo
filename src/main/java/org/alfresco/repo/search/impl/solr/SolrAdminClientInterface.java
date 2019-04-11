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
package org.alfresco.repo.search.impl.solr;

import java.util.Collections;
import java.util.Map;

import org.alfresco.repo.search.impl.lucene.JSONAPIResult;
import org.alfresco.repo.search.impl.lucene.JSONAPIResultFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Solr Admin client interface
 * Provides access to Actions and Commands in SOLR API
 * @author aborroy
 * @since 6.2
 */
public interface SolrAdminClientInterface extends BeanFactoryAware, InitializingBean
{
    
    /**
     * Default parameters to request a JSON Response (default is XML)
     */
    public static Map<String, String> JSON_PARAM = Collections.singletonMap("wt","json");
    
    /**
     * Execute an ACTION from the SOLR CoreAdmin API
     * @param core Name of the core to execute the command, when null the action is executed for all existing cores
     * @param action SOLR CoreAdmin API Action name
     * @param parameters Action parameters in pairs of key, value
     * @return
     */
    public JSONAPIResult executeAction(String core, JSONAPIResultFactory.ACTION action, Map<String, String> parameters);
    
    /**
     * Execute a COMMAND from the SOLR API
     * @param core Name of the core to execute the command
     * @param handler Name of the handler for the SOLR REST API
     * @param command Name of the command to be invoked
     * @param parameters Action parameters in pairs of key, value
     * @return
     */
    public JSONAPIResult executeCommand(String core, JSONAPIResultFactory.HANDLER handler, JSONAPIResultFactory.COMMAND command, Map<String, String> parameters);
    
    
}
