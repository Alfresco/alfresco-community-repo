/**
 * Filesystem Rule Evaluator to support Scenarios.
 * <p>
 * Low level operations (create, update, delete etc) are passed into the RuleEvaluator.
 * <p>
 * The RuleEvaluator is configured with a list of Scenarios which process the operations as and 
 * when their patterns match.
 * The RuleEvaluator evaluates the stream of operations and returns commands to execute.
 * <p>
 * The Command Executor executes the commands returned from the RuleEvaluator.
 * <p>
 * Each Scenario is a Factory for A ScenarioInstance.  The RuleEvaluator contains a set of active scenario instances.
 * <p>
 * @since 4.0
 */
@PackageMarker
package org.alfresco.filesys.repo.rules;
import org.alfresco.util.PackageMarker;

