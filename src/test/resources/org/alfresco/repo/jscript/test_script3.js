// create mail action
var mail = actions.create("mail");
mail.parameters.to = "davidc@alfresco.com";
mail.parameters.subject = "Hello from JavaScript";
mail.parameters.from = "david.caruana@alfresco.org";
mail.parameters.template = root.childByNamePath("Company Home/Data Dictionary/Email Templates/notify_user_email.ftl");
mail.parameters.text = "some text, in case template is not found";

// execute action against passed in node    
mail.execute(doc);

// return
true;
