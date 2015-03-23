/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ClassificationServiceImpl extends ServiceBaseImpl
                                       implements ClassificationService
{
    private static Log logger = LogFactory.getLog(ClassificationServiceImpl.class);

    private static final String[] ATTRIBUTE_KEYS = new String[] { "org.alfresco",
                                                                  "module.org_alfresco_module_rm",
                                                                  "classification.levels" };

    public static final String DEFAULT_CONFIG_LOCATION =
                                 "/alfresco/module/org_alfresco_module_rm/classification/rm-classification-levels.json";

    private AttributeService attributeService; // TODO What about other code (e.g. REST API) accessing the AttrService?

    /** The classification levels currently configured in this server. */
    private List<ClassificationLevel> configuredLevels;

    private Configuration config = new Configuration(DEFAULT_CONFIG_LOCATION);

    public void setAttributeService(AttributeService service) { this.attributeService = service; }

    public void initConfiguredClassificationLevels()
    {
        final List<ClassificationLevel> allPersistedLevels = getPersistedLevels();
        final List<ClassificationLevel> configuredLevels   = getConfiguredLevels();

        if (logger.isDebugEnabled())
        {
            // Note! We cannot log the level names or even the size of these lists for security reasons.
            logger.debug("Persisted classification levels: "  + loggableStatusOf(allPersistedLevels));
            logger.debug("Configured classification levels: " + loggableStatusOf(configuredLevels));
        }

        if (configuredLevels == null || configuredLevels.isEmpty())
        {
            throw new MissingConfiguration("Classification level configuration is missing.");
        }
        else if ( !configuredLevels.equals(allPersistedLevels))
        {
            attributeService.setAttribute((Serializable) configuredLevels, ATTRIBUTE_KEYS);
            this.configuredLevels = configuredLevels;
        }
        else
        {
            this.configuredLevels = allPersistedLevels;
        }
    }

    /** Helper method for debug-logging of sensitive lists. */
    private String loggableStatusOf(List<?> l)
    {
        if      (l == null)   { return "null"; }
        else if (l.isEmpty()) { return "empty"; }
        else                  { return "non-empty"; }
    }

    /**
     * Gets the list (in descending order) of classification levels - as persisted in the system.
     * @return the list of classification levels if they have been persisted, else {@code null}.
     */
    List<ClassificationLevel> getPersistedLevels() {
        return authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<List<ClassificationLevel>>()
        {
            @Override public List<ClassificationLevel> doWork() throws Exception
            {
                return (List<ClassificationLevel>) attributeService.getAttribute(ATTRIBUTE_KEYS);
            }
        });
    }

    /** Gets the list (in descending order) of classification levels - as defined in the system configuration. */
    List<ClassificationLevel> getConfiguredLevels()
    {
        return config.getConfiguredLevels();
    }

    @Override
    public List<ClassificationLevel> getApplicableLevels()
    {
        return configuredLevels == null ? Collections.<ClassificationLevel>emptyList() :
                                          Collections.unmodifiableList(configuredLevels);
    }
}
