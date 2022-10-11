package org.alfresco.rest.actions.access;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import org.alfresco.rest.actions.access.pojo.Action;
import org.alfresco.rest.actions.access.pojo.ActionCondition;
import org.alfresco.rest.actions.access.pojo.Rule;
import org.alfresco.utility.model.UserModel;

public class AccessRestrictionUtil {

    public static final String MAIL_ACTION = "mail";

    public static final String ERROR_MESSAGE_FIELD = "message";
    public static final String ERROR_MESSAGE_ACCESS_RESTRICTED =
            "Only admin or system user is allowed to define uses of or directly execute this action";
    private static final String ERROR_MESSAGE_FAILED_TO_SEND_EMAIL = "Failed to send email to:";

    public static Map<String, Serializable> createMailParameters(UserModel sender, UserModel recipient) {
        Map<String, Serializable> parameterValues = new HashMap<>();
        parameterValues.put("from", sender.getEmailAddress());
        parameterValues.put("to", recipient.getEmailAddress());
        parameterValues.put("subject", "Test");
        parameterValues.put("text", "<html>content</html>");

        return parameterValues;
    }

    public static Rule createRuleWithAction(String actionName, Map<String, Serializable> parameterValues) {
        Rule rule = new Rule();
        rule.setId("");
        rule.setTitle("Test rule title");
        rule.setDescription("Test rule description");
        rule.setRuleType(List.of("inbound"));
        rule.setDisabled(false);
        rule.setApplyToChildren(false);
        rule.setExecuteAsynchronously(false);

        Action compositeAction = new Action();
        compositeAction.setActionDefinitionName("composite-action");

        ActionCondition actionCondition = new ActionCondition();
        actionCondition.setConditionDefinitionName("no-condition");
        actionCondition.setParameterValues(new HashMap<>());

        compositeAction.setConditions(List.of(actionCondition));

        Action action = createAction(actionName, parameterValues);

        compositeAction.setActions(List.of(action));

        rule.setAction(compositeAction);

        return rule;
    }

    public static Action createActionWithParameters(String actionName, Map<String, Serializable> parameterValues) {
        Action compositeAction = new Action();
        compositeAction.setActionDefinitionName("composite-action");

        ActionCondition actionCondition = new ActionCondition();
        actionCondition.setConditionDefinitionName("no-condition");
        actionCondition.setParameterValues(new HashMap<>());

        compositeAction.setConditions(List.of(actionCondition));

        Action action = createAction(actionName, parameterValues);
        action.setExecuteAsynchronously(false);

        compositeAction.setActions(List.of(action));

        return action;
    }


    public static Action createAction(String actionName, Map<String, Serializable> parameterValues) {
        Action action = new Action();
        action.setActionDefinitionName(actionName);
        action.setParameterValues(parameterValues);

        return action;
    }

    public static String mapObjectToJSON(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    /**
     * Return error message that in fact means that the action has passed access restriction correctly,
     * but due to non-configured smtp couldn't send email.
     *
     * @param userModel
     * @return
     */
    public static String getExpectedEmailSendFailureMessage(UserModel userModel) {
        return ERROR_MESSAGE_FAILED_TO_SEND_EMAIL + userModel.getEmailAddress();
    }
}
