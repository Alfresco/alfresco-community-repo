package org.alfresco.rest.actions.access.pojo;

import java.util.List;

public class Rule {
    private String id;
    private String title;
    private String description;
    private List<String> ruleType;
    private boolean executeAsynchronously;
    private boolean disabled;
    private boolean applyToChildren;
    private Action action;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRuleType() {
        return ruleType;
    }

    public void setRuleType(List<String> ruleType) {
        this.ruleType = ruleType;
    }

    public boolean isExecuteAsynchronously() {
        return executeAsynchronously;
    }

    public void setExecuteAsynchronously(boolean executeAsynchronously) {
        this.executeAsynchronously = executeAsynchronously;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isApplyToChildren() {
        return applyToChildren;
    }

    public void setApplyToChildren(boolean applyToChildren) {
        this.applyToChildren = applyToChildren;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
