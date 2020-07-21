/*-
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.util;

import freemarker.cache.TemplateLoader;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.ClassPathRepoTemplateLoader;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A helper class to provide email template related utility functions.
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.2.1
 */
public class EmailHelper
{
    private static final Log LOGGER = LogFactory.getLog(EmailHelper.class);

    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private FileFolderService fileFolderService;
    private PersonService personService;
    private PreferenceService preferenceService;
    private Repository repositoryHelper;
    private TemplateLoader templateLoader;
    private String companyHomeChildName;

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setPreferenceService(PreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    public void setTemplateLoader(TemplateLoader templateLoader)
    {
        this.templateLoader = templateLoader;
    }

    public void setCompanyHomeChildName(String companyHomeChildName)
    {
        this.companyHomeChildName = companyHomeChildName;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);
        PropertyCheck.mandatory(this, "preferenceService", preferenceService);
        PropertyCheck.mandatory(this, "repositoryHelper", repositoryHelper);
        PropertyCheck.mandatory(this, "companyHomeChildName", companyHomeChildName);

        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.personService = serviceRegistry.getPersonService();
        // set the template loader
        setTemplateLoader(new ClassPathRepoTemplateLoader(nodeService, serviceRegistry.getContentService(), "utf-8"));
    }

    /**
     * Gets the email template path or the given fallback template path.
     *
     * @param clientName           optional client app name (used only for logging)
     * @param emailTemplatePath    the email template xpath or class path
     * @param fallbackTemplatePath the fallback template
     * @return <ul>
     * <li>If {@code emailTemplatePath} is empty the fallback template is returned.</li>
     * <li>if the given {@code emailTemplatePath} is an xpath (i.e. starts with app:company_home),
     * then an xpath search will be performed to find the {@code NodeRef}. If no nodeRef
     * is found, the fallback template is returned. </li>
     * <li>If {@code emailTemplatePath} is a nodeRef and the node does not exist, the fallback
     * template is returned, otherwise a string representation of the NodeRef is returned.</li>
     * <li>if {@code emailTemplatePath} is a class path which results in a template being found,
     * then the {@code emailTemplatePath} is returned; otherwise, the fallback template is returned.</li>
     * </ul>
     */
    public String getEmailTemplate(String clientName, String emailTemplatePath, String fallbackTemplatePath)
    {
        if (StringUtils.isEmpty(emailTemplatePath))
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("No email template path is set for client [" + clientName + "]. The fallback template will be used: "
                            + fallbackTemplatePath);
            }
            return fallbackTemplatePath;
        }

        // Make sure that the path doesn't start with '/'
        if (emailTemplatePath.startsWith("/"))
        {
            emailTemplatePath = emailTemplatePath.substring(1);
        }

        if (emailTemplatePath.startsWith(companyHomeChildName))
        {
            NodeRef nodeRef = getLocalizedEmailTemplateNodeRef(emailTemplatePath);
            if (nodeRef == null)
            {
                LOGGER.warn("Couldn't find email template with the XPath [" + emailTemplatePath + "] for client [" + clientName
                            + "]. The fallback template will be used: " + fallbackTemplatePath);

                return fallbackTemplatePath;
            }
            return nodeRef.toString();
        }
        else if (NodeRef.isNodeRef(emailTemplatePath))
        {
            // Just check whether the nodeRef exists or not
            NodeRef ref = new NodeRef(emailTemplatePath);
            if (!nodeService.exists(ref))
            {
                LOGGER.warn("Couldn't find email template with the NodeRef [" + ref + "] for client [" + clientName
                            + "]. The fallback template will be used: " + fallbackTemplatePath);

                return fallbackTemplatePath;
            }

            // No need to return the nodeRef, as this will be handled later by the 'ClassPathRepoTemplateLoader' class
            return emailTemplatePath;
        }
        else
        {
            try
            {
                // We just use the template loader to check whether the given
                // template path is valid and the file is found
                Object template = templateLoader.findTemplateSource(emailTemplatePath);
                if (template != null)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Using email template with class path [" + emailTemplatePath + "] for client: " + clientName);
                    }
                    return emailTemplatePath;
                }
                else
                {
                    LOGGER.warn("Couldn't find email template with class path [" + emailTemplatePath + "] for client [" + clientName
                                + "]. The fallback template will be used: " + fallbackTemplatePath);
                }
            }
            catch (IOException ex)
            {
                LOGGER.error("Error occurred while finding the email template with the class path [" + emailTemplatePath + "] for client ["
                            + clientName + "]. The fallback template will be used: " + fallbackTemplatePath, ex);

            }
            return fallbackTemplatePath;
        }
    }

    /**
     * Gets the localized email template nodeRef.
     *
     * @param emailTemplateXPath the xpath of the template
     * @return {@code NodeRef} of the localized template or null if no node is found
     */
    public NodeRef getLocalizedEmailTemplateNodeRef(String emailTemplateXPath)
    {
        if (StringUtils.isEmpty(emailTemplateXPath))
        {
            return null;
        }

        List<NodeRef> nodeRefs = searchService.selectNodes(repositoryHelper.getRootHome(), emailTemplateXPath, null, this.namespaceService, false);
        if (nodeRefs.isEmpty())
        {
            return null;
        }

        NodeRef nodeRef = nodeRefs.get(0);
        if (nodeRefs.size() > 1)
        {
            LOGGER.error("Found too many email templates using XPath [" + emailTemplateXPath + "]. The first element will be used: " + nodeRef);
        }

        // Now localise this
        return fileFolderService.getLocalizedSibling(nodeRef);
    }

    /**
     * Gets the user's locale.
     *
     * @param userId the user id
     * @return the default locale or the user's preferred locale, if available
     */
    public Locale getUserLocaleOrDefault(String userId)
    {
        if (userId != null && personService.personExists(userId))
        {
            String localeString = AuthenticationUtil.runAsSystem(() -> (String) preferenceService.getPreference(userId, "locale"));
            if (localeString != null)
            {
                return I18NUtil.parseLocale(localeString);
            }
        }

        return I18NUtil.getLocale();
    }
}
