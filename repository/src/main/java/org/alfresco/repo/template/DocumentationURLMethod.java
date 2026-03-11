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
import freemarker.template.TemplateScalarModel;
import org.apache.commons.lang3.StringUtils;

public class DocumentationURLMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
{

    private static final String COMPONENT_SEARCHENTERPRISE = "elasticsearch";
    private static final String COMPONENT_SEARCH = "solr";

    private String documentationBaseUrl;
    private String acsVersion;
    private String alfrescoSearchVersion;
    private String alfrescoSearchEnterpriseVersion;
    private String defaultDocumentationUrl;

    public void setDefaultDocumentationUrl(String defaultDocumentationUrl)
    {
        this.defaultDocumentationUrl = defaultDocumentationUrl;
    }

    public void setAcsVersion(String acsVersion)
    {
        this.acsVersion = acsVersion;
    }

    public void setAlfrescoSearchVersion(String alfrescoSearchVersion)
    {
        this.alfrescoSearchVersion = alfrescoSearchVersion;
    }

    public void setAlfrescoSearchEnterpriseVersion(String alfrescoSearchEnterpriseVersion)
    {
        this.alfrescoSearchEnterpriseVersion = alfrescoSearchEnterpriseVersion;
    }

    public void setDocumentationBaseUrl(String documentationBaseUrl)
    {
        this.documentationBaseUrl = documentationBaseUrl;
    }

    /**
     * Returns documentation URL. You can specify property key which should hold value of topic uid and url component(if required). * a) If no arguments are provided, the default documentation URL is returned.
     * 
     * <pre>
     *      ${documentationUrl() -> https://support.hyland.com/p/alfresco
     * </pre>
     * 
     * b) First argument is interpreted as Topic UID of the URL. The value is retrieved and treated as Topic UID appended after baseURL and before version.
     * 
     * <pre>
     *      ${documentationURL("eet567890373737")} -> https://support.hyland.com/access?dita:id=eet567890373737&vrm_version=26.1
     * </pre>
     * 
     * c) Second argument(if required) is interpreted as an additional URL component, which will be appended to the URL to denote a specific component of Alfresco.
     * 
     * <pre>
     *     ${documentationUrl("eeu1720075126296", "&component=Alfresco%20Content%20Services%20Community%20Edition"} -> https://support.hyland.com/access?dita:id=eeu1720075126296&vrm_version=26.1&component=Alfresco%20Content%20Services%20Community%20Edition
     * </pre>
     *
     * d) Third argument (if required) is interpreted as the Alfresco component (e.g., "solr", "elasticsearch", or empty) to determine which version to use in the URL.
     *
     * <pre>
     *     ${documentationUrl("eeu1720075126296", "", "solr")} -> https://support.hyland.com/access?dita:id=eeu1720075126296&vrm_version=2.0
     * </pre>
     *
     * @param args
     *            arguments passed to Freemarker template method invocation first argument is interpreted as Topic UID of the URL, second argument is interpreted as an additional URL component, third argument is interpreted as the Alfresco component ("solr", "elasticsearch", or empty) to select the version.
     * @return the documentation URL
     * @throws TemplateModelException
     *             if an error occurs
     */
    @Override
    public Object exec(List args) throws TemplateModelException
    {
        String topicUid = getTopicUid(args);
        String urlComponent = getUrlComponent(args);
        String alfrescoComponent = getAlfrescoComponent(args);
        return getDocumentationUrl(topicUid, urlComponent, alfrescoComponent);
    }

    /**
     * Returns default landing documentation URL.
     * 
     * @return default documentation URL
     */
    public String getDocumentationUrl()
    {
        return defaultDocumentationUrl;
    }

    /**
     * Constructs the documentation URL using the base URL, topic UID, version, and additional component.
     * 
     * @param topicUid
     *            path segment
     * @param urlComponent
     *            additional URL component (may be empty)
     * @param alfrescoComponent
     *            additional Alfresco component (may be empty), to determine the version
     * @return full documentation URL
     */
    public String getDocumentationUrl(String topicUid, String urlComponent, String alfrescoComponent)
    {
        if (StringUtils.isEmpty(topicUid) && StringUtils.isEmpty(urlComponent))
        {
            return getDocumentationUrl();
        }
        String version = selectVersion(alfrescoComponent);
        return documentationBaseUrl + topicUid + version + urlComponent;
    }

    private String selectVersion(String alfrescoComponent)
    {
        if (COMPONENT_SEARCHENTERPRISE.equalsIgnoreCase(alfrescoComponent))
        {
            return alfrescoSearchEnterpriseVersion;
        }
        if (COMPONENT_SEARCH.equalsIgnoreCase(alfrescoComponent))
        {
            return alfrescoSearchVersion;
        }
        return acsVersion;
    }

    /**
     * Extracts a string argument from the list at the given index.
     * 
     * @param args
     *            argument list
     * @param index
     *            index to extract
     * @return string value or empty string if not present
     * @throws TemplateModelException
     *             if argument is not a scalar
     */
    private String getStringArg(List<?> args, int index) throws TemplateModelException
    {
        if (args.size() > index)
        {
            Object arg = args.get(index);
            if (arg instanceof TemplateScalarModel)
            {
                String value = ((TemplateScalarModel) arg).getAsString();
                return value != null ? value : "";
            }
        }
        return "";
    }

    private String getTopicUid(List<?> args) throws TemplateModelException
    {
        return getStringArg(args, 0);
    }

    private String getUrlComponent(List<?> args) throws TemplateModelException
    {
        return getStringArg(args, 1);
    }

    private String getAlfrescoComponent(List<?> args) throws TemplateModelException
    {
        return getStringArg(args, 2);
    }
}
