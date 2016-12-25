/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.utils;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;

import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentModel;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;

/**
 * FIXME!!!
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class FilePlanComponentsUtil
{
    private FilePlanComponentsUtil()
    {
        // Intentionally blank
    }

    /** image resource file to be used for records body */
    public static final String IMAGE_FILE = "money.JPG";

    /**
     *  FIXME!!!
     *
     * @param nodeType FIXME!!!
     * @return FIXME!!!
     */
    private static FilePlanComponentModel createRecordModel(String nodeType)
    {
        return FilePlanComponentModel.builder()
                .name("Record " + getRandomAlphanumeric())
                .nodeType(nodeType)
                .build();
    }

    /**
     * FIXME!!!
     *
     * @return FIXME!!!
     */
    public static FilePlanComponentModel createElectronicRecordModel()
    {
        return createRecordModel(CONTENT_TYPE);
    }

    /**
     * FIXME!!!
     *
     * @return FIXME!!!
     */
    public static FilePlanComponentModel createNonElectronicRecordModel()
    {
        return createRecordModel(NON_ELECTRONIC_RECORD_TYPE);
    }

    /**
     * FIXME!!!
     *
     * @param name FIXME!!!
     * @param type FIXME!!!
     * @param title FIXME!!!
     * @return
     */
    public static FilePlanComponentModel createFilePlanComponentModel(String name, String type, String title)
    {
        return FilePlanComponentModel.builder()
                .name(name)
                .nodeType(type)
                .properties(FilePlanComponentProperties.builder()
                                .title(title)
                                .build())
                .build();
    }
}
