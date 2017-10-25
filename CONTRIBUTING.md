#How to contribute
Alfresco's maturity comes from the contributions of its community. Contributing back to Alfresco can be a bit confusing as Alfresco is a collection of separate open source projects. Each open source project officially sponsored by Alfresco has a page in this space detailing its governance policy and the method for reporting issues and submitting contributions.
 
For the details on the Records Management project see[here.](https://community.alfresco.com/docs/DOC-6387-project-overview-records-management)

##Getting started
What you need to get started:

* A [Jira](https://issues.alfresco.com/jira/projects/ALF/issues/?filter=allopenissues) account,

* A [GitHub](https://github.com/) account

Useful but not necessary:

* An [Alfresco community](https://community.alfresco.com/) account - this is a good place to ask questions and find answers. 
##Making changes

We use Jira to track issues. If you are committing a fix for a raised issue please include the ticket number in both the merge requests and the git commit messages.
For example a fix for the ticket [ALF-21953](https://issues.alfresco.com/jira/browse/ALF-21953) will have the following git commit message: "ALF-21953: updated to use super pom v9"

If you are adding in a new feature or bug fix please do so [here.](https://issues.alfresco.com/jira/projects/ALF/issues/?filter=allopenissues) By raising a ticket in this project you will be agreeing to the Alfresco Contribution Agreement which can be found at the bottom of the 'Create Issue' form or alternatively attached to[this](https://community.alfresco.com/docs/DOC-7070-alfresco-contribution-agreement)page.

When you are ready to make a change you just need to fork the [records-management](https://github.com/Alfresco/records-management) repository and then make your changes into your copy of the code.

We have a set of standards we follow when writing code. These can be found [here.](https://community.alfresco.com/docs/DOC-4658-coding-standards)

When formatting your change please make sure not to change the format of any other code as this can make the changes difficult to spot and please make sure to use the correct line ending (we use LF)

We ask that when adding/changing code you also add/change the appropriate unit tests and that these tests all run before creating the pull request (these will be run as part of the request it just saves time if you know they will pass before hand). 

###Writing translatable code
TODO

##Submitting changes
When you create a pull request we will run the unit tests using Travis. If these pass a member of the team will look over the change and then assuming there are no issues will accept your change. If there are any issue we will discuss this with you using the pull request.

It really helps speed up the review process if you include a description of the change in the pull request.

After your change has been accepted you can add it to the page[here](https://community.alfresco.com/docs/DOC-5279-featured-contributions)to let other members of the community know (A community account is required to update this page). 

Occasionally we may have to revert a change after it has been accepted. This will usually be cause by failing integration or UI tests. If this happens to you feel free to get in touch for more information.

##Additional info and links

[Alfresco coding standards](https://community.alfresco.com/docs/DOC-4658-coding-standards)

[Alfresco community contributions page](https://community.alfresco.com/docs/DOC-5279-featured-contributions)

[Alfresco community site](https://community.alfresco.com/)

[Alfresco contribution agreement](https://community.alfresco.com/docs/DOC-7070-alfresco-contribution-agreement)

[GitHub "Records Management" code repository](https://github.com/Alfresco/records-management)

[Jira project for raising issues/features](https://issues.alfresco.com/jira/projects/ALF/issues/?filter=allopenissues)

[Records Management project details](https://community.alfresco.com/docs/DOC-6387-project-overview-records-management)

