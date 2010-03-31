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
package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;

/**
 * A class providing basic OOo-related functionality shared by both
 * {@link ContentTransformer}s and {@link ContentTransformerWorker}s.
 */
public class OOoContentTransformerHelper extends ContentTransformerHelper
{
    /**
     * There are some conversions that fail, despite the converter believing them possible.
     * This method can be used by subclasses to check if a targetMimetype or source/target
     * Mimetype pair are blocked.
     * 
     * @param sourceMimetype
     * @param targetMimetype
     * @return <code>true</code> if the mimetypes are blocked, else <code>false</code>
     */
    protected boolean isTransformationBlocked(String sourceMimetype, String targetMimetype)
    {
        if (targetMimetype.equals(MimetypeMap.MIMETYPE_XHTML))
        {
            return false;
        }
        else if (targetMimetype.equals(MimetypeMap.MIMETYPE_WORDPERFECT))
        {
            return false;
        }
        else if (targetMimetype.equals(MimetypeMap.MIMETYPE_FLASH))
        {
            return false;
        }
        // OpenOffice 3.2.x doesn't seem to support all Office 97 to Office 07 conversions
        else if (sourceMimetype.equals(MimetypeMap.MIMETYPE_WORD) && targetMimetype.equals(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING))
        {
            return false;
        }
        else if (sourceMimetype.equals(MimetypeMap.MIMETYPE_EXCEL) && targetMimetype.equals(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET))
        {
            return false;
        }
        else if (sourceMimetype.equals(MimetypeMap.MIMETYPE_HTML) && targetMimetype.equals(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}