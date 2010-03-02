/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.web.bean.wcm;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.lang.StringUtils;

/**
 * Helper class to build a HTML description attribute for the <code><a:listItem></code> tag. 
 * 
 * @author Arseny Kovalchuk
 *
 * @see org.alfresco.web.bean.wcm.CreateWebsiteWizard
 * @see org.alfresco.web.bean.wcm.CreateFormWizard
 */
public class DescriptionAttributeHelper
{
    static final String BLANK = "";
    static final String TABLE_BEGIN = "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"> <colgroup><col width=\"25%\"/><col width=\"75%\"/></colgroup><tbody>";
    static final String TABLE_END = "</tbody></table>";
    static final String TRTD_BEGIN = "<tr><td>";
    static final String TD_TD = ":</td><td>";
    static final String TDTR_END = "</td></tr>";
    static final String SPAN_ITALIC_BEGIN = "<span style=\"font-style:italic\">";
    static final String SPAN_END = "</span>";
   
    /**
     * 
     * @return Returns beginning tags of the HTML table
     * @see org.alfresco.web.bean.wcm.DescriptionAttributeConstants
     */
    public static String getTableBegin()
    {
        return TABLE_BEGIN;
    }
    
    /**
     * 
     * @param fc            Current FacesContext
     * @param fieldName     Field name
     * @param fieldValue    Field value
     * @return              Returns a table line <code><tr><td>Localised field name:</td><td>Field value</td></tr></code>
     */
    public static String getTableLine(FacesContext fc, String fieldName, String fieldValue)
    {
        return getTableLine(fc, fieldName, fieldValue, true);
    }
    
    /**
     * 
     * @param fc            Current FacesContext
     * @param fieldName     Field name
     * @param fieldValue    Field value
     * @param encode        Whether to encode the given value or not
     * @return              Returns a table line <code><tr><td>Localised field name:</td><td>Field value</td></tr></code>
     */
    public static String getTableLine(FacesContext fc, String fieldName, String fieldValue, boolean encode)
    {
        StringBuilder line = new StringBuilder(128);
        line.append(TRTD_BEGIN).append(Application.getMessage(fc, fieldName)).append(TD_TD);
        if (encode)
        {
           line.append(Utils.encode(fieldValue));
        }
        else
        {
           line.append(fieldValue);
        }
        line.append(TDTR_END);
        return line.toString();
    }
    
    /**
     * 
     * @return Returns an ending HTML table tags
     */
    public static String getTableEnd()
    {
        return TABLE_END;
    }
    
    /**
     * 
     * @param fc            Current FacesContext
     * @param fieldValue    Field value
     * @return              Returns localised "description_not_set" message if fieldValue is empty.
     */
    public static String getDescriptionNotEmpty(FacesContext fc, String fieldValue)
    {
        return StringUtils.isEmpty(fieldValue) ?
                SPAN_ITALIC_BEGIN + Application.getMessage(fc, "description_not_set") + SPAN_END :
                Utils.encode(fieldValue);
    }

}
