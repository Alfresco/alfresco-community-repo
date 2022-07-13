package org.alfresco.email.action.access;

import com.google.gson.Gson;
import org.alfresco.email.action.access.pojo.Action;
import org.alfresco.email.action.access.pojo.ActionCondition;
import org.alfresco.email.action.access.pojo.Rule;
import org.alfresco.utility.model.UserModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessRestrictionUtil {

    public static final String MAIL_ACTION = "mail";
    public static final String EXPECTED_ERROR_MESSAGE =
            "Only admin or system user is allowed to define uses of or directly execute this action";

    public static Map<String, String> createMailParameters(UserModel sender, UserModel recipient) {
        Map<String, String> parameterValues = new HashMap<>();
        parameterValues.put("from", sender.getEmailAddress());
        parameterValues.put("to", recipient.getEmailAddress());
        parameterValues.put("subject", "Test");
        parameterValues.put("text", "<html>content</html>");

        return parameterValues;
    }

    public static Rule createRuleWithAction(String actionName, Map<String, String> parameterValues) {
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

    public static Action createAction(String actionName, Map<String, String> parameterValues) {
        Action action = new Action();
        action.setActionDefinitionName(actionName);
        action.setParameterValues(parameterValues);

        return action;
    }

    public static String mapObjectToJSON(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }
}
