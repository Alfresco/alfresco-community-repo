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
package org.alfresco.repo.template;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class DocumentationURLMethod extends I18NMessageMethod implements TemplateMethodModelEx {

    private String documentationBaseUrl;
    private String version;
    //private String uid;

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDocumentationBaseUrl(String documentationBaseUrl) {
        this.documentationBaseUrl =  documentationBaseUrl;
    }

//    public void setUid(String uid) {
//        this.uid = uid;
//    }

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
        String topicPath = getTopicUid(args);
        String urlComponent = getUrlComponent(args);
        String propertyValue = getPropertyValue(args) ;
        if(!StringUtils.isEmpty(propertyValue)) {
            String docUrl = getDocumentationUrl(topicPath, urlComponent);
            return propertyValue.replace("{0}", docUrl);
        }
        return getDocumentationUrl(topicPath, urlComponent);
    }



    /**
     * Returns documentation URL for this edition and version of ACS.
     * @return documentation URL
     */
    public String getDocumentationUrl()
    {

//        return getDocumentationUrl(null, null);
        return "https://support.hyland.com/p/alfresco";
    }

    /**
     * Returns documentation URL for documentation topic path segment.
     * @param topicUid path segment, e.g. /Administer/Overview
     * @return documentation URL
     */
    public String getDocumentationUrl(String topicUid, String urlComponent)
    {
        return documentationBaseUrl+topicUid+version+urlComponent;
    }

    private String getStringArg(List<?> args, int index) throws TemplateModelException
    {
        if (args.size() > index) {
            Object arg = args.get(index);
            if (arg instanceof TemplateScalarModel) {
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

    private String getPropertyValue(List<?> args) throws TemplateModelException
    {
        return getStringArg(args, 2);
    }
}
