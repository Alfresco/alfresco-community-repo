package org.alfresco.module.org_alfresco_module_rm.relationship;

import static org.alfresco.util.ParameterCheck.mandatoryString;

/**
 * POJO representing the relationship display name
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipDisplayName
{
    /** The source text of the relationship */
    private String sourceText;

    /** The target text of the relationship */
    private String targetText;

    /**
     * Constructor for creating the relationship display name.
     * In case of a bidirectional relationship the source
     * text and target text will be the same.
     *
     * @param sourceText The source text of the relationship
     * @param targetText The target text of the relationship
     */
    public RelationshipDisplayName(String sourceText, String targetText)
    {
        mandatoryString("sourceText", sourceText);
        mandatoryString("targetText", targetText);

        setSourceText(sourceText);
        setTargetText(targetText);
    }

    /**
     * Gets the source text of the relationship
     *
     * @return The source text of the relationship
     */
    public String getSourceText()
    {
        return this.sourceText;
    }

    /**
     * Sets the source text of the relationship
     *
     * @param sourceText The source text of the relationship
     */
    private void setSourceText(String sourceText)
    {
        this.sourceText = sourceText;
    }

    /**
     * Gets the target text of the relationship
     *
     * @return The target text of the relationship
     */
    public String getTargetText()
    {
        return this.targetText;
    }

    /**
     * Sets the target text of the relationship
     *
     * @param targetText The target text of the relationship
     */
    private void setTargetText(String targetText)
    {
        this.targetText = targetText;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(")
          .append("source=").append(sourceText)
          .append(", target=").append(targetText)
          .append(")");
        return sb.toString();
    }
}
