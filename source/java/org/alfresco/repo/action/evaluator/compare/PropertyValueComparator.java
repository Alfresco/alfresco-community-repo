package org.alfresco.repo.action.evaluator.compare;

import java.io.Serializable;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;

/**
 * Property value comparator interface
 * 
 * @author Roy Wetherall
 */
public interface PropertyValueComparator
{
    /**
     * Callback method to register this comparator with the evaluator.
     * 
     * @param evaluator     the compare property value evaluator
     */
    void registerComparator(ComparePropertyValueEvaluator evaluator);
    
    /**
     * Compares the value of a property with the compare value, using the operator passed.
     * 
     * @param propertyValue     the property value
     * @param compareValue      the compare value
     * @param operation         the operation used to compare the two values
     * @return                  the result of the comparison, true if successful false otherwise
     */
    boolean compare(
            Serializable propertyValue,             
            Serializable compareValue, 
            ComparePropertyValueOperation operation);
}
