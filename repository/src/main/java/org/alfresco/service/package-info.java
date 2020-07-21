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
/**
 * Provides the public facing interfaces of the Services of the Alfresco Repository.
 * <p>
 * The PublicService is a marker interface for those services which are intended to be 
 * public entry points to the Alfresco Repository.   Those interfaces marked as PublicService 
 * are audited.
 * <p>
 * The ServiceRegistry provides access to the Alfresco Repository Services for the cases where the spring context is not available.
 */
@PackageMarker
package org.alfresco.service;
import org.alfresco.util.PackageMarker;
