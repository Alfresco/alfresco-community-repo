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






