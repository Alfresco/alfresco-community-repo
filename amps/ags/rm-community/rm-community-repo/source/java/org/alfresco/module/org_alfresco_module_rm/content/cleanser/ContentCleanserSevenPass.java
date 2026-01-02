/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.content.cleanser;

import java.io.File;

/**
 * DoD 5220-22M Seven Pass data cleansing implementation.
 *
 */
public class ContentCleanserSevenPass extends ContentCleanser522022M
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.content.cleanser.ContentCleanser#cleanse(java.io.File)
     */
    @Override
    public void cleanse(File file)
    {
        super.cleanse(file);
        overwrite(file, overwriteZeros);
        overwrite(file, overwriteZeros);
        overwrite(file, overwriteOnes);
        overwrite(file, overwriteRandom);

    }
}