/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.repo.model.filefolder;

/**
 * Represents configurable hidden file information, based on which some filename
 * patterns will be hidden or not depending the client
 * 
 * @author Andreea Dragoi
 * @since 4.2.5
 */

public interface ConfigurableHiddenFileInfo extends HiddenFileInfo
{
    public boolean isCmisDisableHideConfig();
    public void setCmisDisableHideConfig(boolean cmisDisableHideConfig);

}
