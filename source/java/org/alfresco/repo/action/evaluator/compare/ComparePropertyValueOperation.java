package org.alfresco.repo.action.evaluator.compare;

/**
 * ComparePropertyValueOperation enum.
 * <p>
 * Contains the operations that can be used when evaluating whether the value of a property
 * matches the value set.
 * <p>
 * Some operations can only be used with specific types.  If a mismatch is encountered an error will
 * be raised.
 */
public enum ComparePropertyValueOperation 
{
    EQUALS,                 // All property types 
    CONTAINS,               // String properties only
    BEGINS,                 // String properties only   
    ENDS,                   // String properties only
    GREATER_THAN,           // Numeric and date properties only
    GREATER_THAN_EQUAL,     // Numeric and date properties only
    LESS_THAN,              // Numeric and date properties only
    LESS_THAN_EQUAL         // Numeric and date properties only
}