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
 *
 * The Policy Component.
 *
 * Policy Component for managing Policies and Behaviours.
 * <p>
 * This component provides the ability to:
 * <p>
 * <ul>
 *   <li>a) Register policies</li>
 *   <li>b) Bind behaviours to policies</li>
 *   <li>c) Invoke policy behaviours</li>
 * </ul>
 * <p>
 * A behaviour may be bound to a Policy before the Policy is registered.  In
 * this case, the behaviour is not validated (i.e. checked to determine if it
 * supports the policy interface) until the Policy is registered.  Otherwise,
 * the behaviour is validated at bind-time.
 * <p>
 * Policies may be selectively "turned off" by the Behaviour Filter.
 * @see org.alfresco.repo.policy.PolicyComponent
 * @see org.alfresco.repo.policy.BehaviourFilter
 */
@PackageMarker
package org.alfresco.repo.policy;
import org.alfresco.util.PackageMarker;






