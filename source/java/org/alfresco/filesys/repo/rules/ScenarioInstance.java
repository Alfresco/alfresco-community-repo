package org.alfresco.filesys.repo.rules;

/**
 * A scenario instance is an active scenario.   It has a ranking, an 
 * evaluate method and knows whether it is complete.
 * <p>
 * The evaluate method is called repeatedly as operations are processed. 
 */
public interface ScenarioInstance
{
    enum Ranking
    {
        LOW,   // Bottom priority
        MEDIUM, 
        HIGH,
        
    };
    
    /**
     * Get the Ranking
     * @return Ranking
     */
    public Ranking getRanking();
    
    /**
     * evaluate the scenario against the current operation
     * 
     * @param operation Operation
     */
    public Command evaluate(Operation operation);
    
    /**
     * Is the scenario complete?
     *
     * @return boolean
     */
    public boolean isComplete();     
    
    
}
