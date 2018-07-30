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
package org.alfresco.repo.content.transform.tika;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Tika transformation options
 * 
 * @author Andreea Nechifor
 *
 */
@AlfrescoPublicApi
public class TikaTransformationOptions extends TransformationOptions
{

    public static final String OPT_NOT_EXTRACT_BOOKMARKS_TEXT = "notExtractBookmarksText";

    // False if bookmarks content should be extracted
    private boolean notExtractBookmarksText = false;

    public boolean isNotExtractBookmarksText()
    {
        return notExtractBookmarksText;
    }

    /**
     * If false, extract bookmarks (document outline) text.
     * <p/>
     * The default is <code>false</code>
     * 
     * @param notExtractBookmarksText
     */
    public void setNotExtractBookmarksText(boolean notExtractBookmarksText)
    {
        this.notExtractBookmarksText = notExtractBookmarksText;
    }

}
