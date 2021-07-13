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

import java.util.List;

import org.springframework.extensions.webscripts.ui.common.StringUtils;
import org.springframework.web.util.HtmlUtils;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Render a sanitized HTML string<br>
 * <p>
 * Usage: sanitizeHTML(String html)
 * 
 * @author Tiago Salvado
 */
public class SanitizeHTMLMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
{

    /*
     * Characters that can not be concatenated with HTML attributes starting with "on" (e.g, onerror, onblur, etc),
     * otherwise, these attributes won't be stripped out from HTML properly making possible XSS attacks to happen
     */
    private final String SEPARATE_CHARS_FROM_ON_ATTRIBUTES_LIST = "/";

    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        Object result = null;

        if (args.size() == 1)
        {
            Object arg0 = args.get(0);

            if (arg0 instanceof TemplateScalarModel)
            {
                result = sanitize(((TemplateScalarModel) arg0).getAsString());
            }
        }

        return result != null ? result : "";
    }

    /**
     * @param html
     *            a string containing HTML
     * 
     * @return a sanitized HTML string
     */
    private String sanitize(String html)
    {
        // Unescapes the HTML so the strip HTML operation can act properly
        String unescapedHtml = HtmlUtils.htmlUnescape(html);
        boolean hasBeenUnescaped = !html.equals(unescapedHtml);

        // Prevents certain characters from being concatenated with an HTML attribute starting with "on"
        unescapedHtml = unescapedHtml.replaceAll("([" + SEPARATE_CHARS_FROM_ON_ATTRIBUTES_LIST + "])(on)", "$1 $2");

        // Strips the unsafe HTML tags to prevent XSS attacks from happening
        String result = StringUtils.stripUnsafeHTMLTags(unescapedHtml, false);

        // Escapes the HTML again if it was unescaped before, otherwise will return as it is
        result = hasBeenUnescaped ? HtmlUtils.htmlEscape(result) : result;

        return result;
    }
}