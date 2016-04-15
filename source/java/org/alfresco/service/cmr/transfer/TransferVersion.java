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
package org.alfresco.service.cmr.transfer;

public interface TransferVersion
{
   /**
    * Gets the major version number, e.g. <u>1</u>.2.3
    *
    * @return  major version number
    */
   public String getVersionMajor();

   /**
    * Gets the minor version number, e.g. 1.<u>2</u>.3
    *
    * @return  minor version number
    */
   public String getVersionMinor();

   /**
    * Gets the version revision number, e.g. 1.2.<u>3</u>
    *
    * @return  revision number
    */
   public String getVersionRevision();

   /**
    * Gets the edition
    *
    * @return  the edition
    */
   public String getEdition();

}
