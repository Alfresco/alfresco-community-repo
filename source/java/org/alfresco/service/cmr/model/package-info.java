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

/**
   FileFolderService
   <p/>
   These services give <b>much</b> simpler APIs for manipulating nodes structures
   conforming to traditional file/folder trees within the data dictionary.
   <p/>
   When using these methods please be aware that they only work with File/Folder trees linked
   together by a primary association of type cm:contains.   In particular these methods do not
   work on the association above "company home"
   <p/>
   FileFolderService provides the public service.
   <p>
   FileFolderUtil provides a utility methods.
 */
@PackageMarker
package org.alfresco.service.cmr.model;
import org.alfresco.util.PackageMarker;

