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

/**
 * This package contains the various types required for the 'Classified Records' feature.
 * Nodes within Alfresco can be given a {@link org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel}
 * which then restricts access to them to users having the appropriate clearance.
 * <p/>
 * The {@link org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService} is responsible
 * for the management of those levels and it is the
 * {@link org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService} which deals
 * wth users and their clearances.
 *
 * @since 3.0.a
 */
package org.alfresco.module.org_alfresco_module_rm.classification;
