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

    @Override
    public Object exec(List args) throws TemplateModelException
    {
        String topicPath = getTopicPath(args);
        String editionSegment = edition.equals("Community") ? "/Alfresco-Content-Services-Community-Edition" : "/Alfresco-Content-Services";
        return documentationBaseUrl
                + editionSegment
                + "/"
                + version
                // edition appears twice in official documentation
                + editionSegment
                + topicPath;
    }

    private String getTopicPath(List<?> args) throws TemplateModelException {
        Object topicPath = super.exec(args);

        return (topicPath instanceof String) ? (String) topicPath : "";
    }
}
