/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.template;

import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class DocumentationURLMethod extends I18NMessageMethod implements TemplateMethodModelEx
{
    public static final String COMMUNITY_EDITION = "Community";
    public static final String ENTERPRISE_EDITION = "Enterprise";
    public static final String COMMUNITY_EDITION_PATH = "/Alfresco-Content-Services-Community-Edition";
    public static final String ENTERPRISE_EDITION_PATH = "/Alfresco-Content-Services";

    private String documentationBaseUrl;
    private String version;
    private String edition;

    public void setDocumentationBaseUrl(String documentationBaseUrl)
    {
        this.documentationBaseUrl = documentationBaseUrl;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public void setEdition(String edition)
    {
        this.edition = edition;
    }

    /**
     * Returns documentation URL. You can specify property key which should hold value of topic path. You can specify more arguments which would be interpolated accordingly to org.springframework.extensions.surf.util.I18NUtil#getMessage(java.lang.String). Examples: a) Without arguments, returns the documentation URL for this edition and version of ACS:
     * <pre>
     *      ${documentationUrl() -> https://support.hyland.com/r/Alfresco/Alfresco-Content-Services/23.4/Alfresco-Content-Services
     *  </pre>
     * b) First argument is interpreted as I18N message property key. The value is retrieved and treated as path segment appended at the end.
     * <pre>
     *      // i18n message property
     *      admin-console.help-link=/Administer/Overview
     *
     *      // some template
     *      ${documentationURL("admin-console.help-link")} -> https://support.hyland.com/r/Alfresco/Alfresco-Content-Services/23.4/Alfresco-Content-Services/Administer/Overview
     *  </pre>
     * c) Second (and more) arguments are interpolated accordingly to org.springframework.extensions.surf.util.I18NUtil#getMessage(java.lang.String).
     * <pre>
     *     // some i18n message property
     *     admin-console.help-link=/Administer/Overview?queryParam={0}
     *
     *     // some template
     *     ${documentationURL("admin-console.help-link", "aValue")} -> https://support.hyland.com/r/Alfresco/Alfresco-Content-Services/23.4/Alfresco-Content-Services/Administer/Overview?queryParam=aValue
     * </pre>
     *
     * @param args
     *         arguments passed to Freemarker template method invocation, first argument is interpreted as I18N message property key, following arguments will be interpolated
     * @return the documentation URL
     * @throws TemplateModelException
     *         if an error occurs
     */
    @Override
    public Object exec(List args) throws TemplateModelException
    {
        String topicPath = getTopicPath(args);
        return getDocumentationUrl(topicPath);
    }

    /**
     * Returns documentation URL for this edition and version of ACS.
     * @return documentation URL
     */
    public String getDocumentationUrl()
    {
        return getDocumentationUrl(null);
    }

    /**
     * Returns documentation URL for documentation topic path segment.
     * @param topicPathSegment path segment, e.g. /Administer/Overview
     * @return documentation URL
     */
    public String getDocumentationUrl(String topicPathSegment)
    {
        String editionSegment;
        if (COMMUNITY_EDITION.equals(edition)) {
            editionSegment = COMMUNITY_EDITION_PATH;
        } else if (ENTERPRISE_EDITION.equals(edition)) {
            editionSegment = ENTERPRISE_EDITION_PATH;
        } else {
            return documentationBaseUrl;
        }

        return documentationBaseUrl
                + editionSegment
                + "/"
                + version
                // edition appears twice in official documentation
                + editionSegment
                + (topicPathSegment == null ? "" : topicPathSegment);
    }

    private String getTopicPath(List<?> args) throws TemplateModelException
    {
        Object topicPath = super.exec(args);

        return (topicPath instanceof String) ? (String) topicPath : "";
    }
}
