## Alfresco Governance Services' Extended Permission Service

![Completeness Badge](https://img.shields.io/badge/Document_Level-Complete-green.svg?style=flat-square)

![Version Badge](https://img.shields.io/badge/Version-Current-blue.svg?style=flat-square)

### Purpose

When working on the Records Management module, we needed additional functionality around permissions, and therefore 
introduced the [ExtendedPermissionService](../../rm-community/rm-community-repo/source/java/org/alfresco/repo/security/permissions/impl/ExtendedPermissionServiceImpl.java).

### Overview

The ExtendedPermissionService is wired in, via [Spring config](../../rm-community/rm-community-repo/config/alfresco/module/org_alfresco_module_rm/extended-repository-context.xml), 
to extend Alfresco's core PermissionService, and adds support for: 
* the [RMPermissionModel](../../rm-community/rm-community-repo/source/java/org/alfresco/module/org_alfresco_module_rm/capability/RMPermissionModel.java), which defines the available permissions capabilities.
* the [PermissionProcessorRegistry](../../rm-community/rm-community-repo/source/java/org/alfresco/repo/security/permissions/processor/PermissionProcessorRegistry.java), which introduces pre- and post- processors.
* other minor method extensions (e.g. to setInheritParentPermissions)

### Permission Processor Registry

This was added in RM 2.4 to support the requirements around the additional security classifications and markings.

The registry is simply two array lists, one for pre-processors and one for post-processors, which are iterated around 
before / after (respectively) the wrapped call PermissionService.hasPermission

Out of the box, a system with the RM module installed will have the following permissions processors defined:

#### Community:

##### Pre-processors: 
* None.

##### Post-processors: 
* [RecordsManagementPermissionPostProcessor](../../rm-community/rm-community-repo/source/java/org/alfresco/module/org_alfresco_module_rm/permission/RecordsManagementPermissionPostProcessor.java)
  * If the node is an RM node (i.e. it has the [RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT](../../rm-community/rm-community-repo/source/java/org/alfresco/module/org_alfresco_module_rm/model/RecordsManagementModel.java) marker aspect) and the 
  core permissions evaluates to DENIED, then this post processor allows read/writes if the appropriate read/file 
  permissions are present.

#### Enterprise:
(links only work in clones of Enterprise repos)

##### Pre-processors:
* [SecurityMarksPermissionPreProcessor](../../rm-enterprise/rm-enterprise-repo/src/main/java/org/alfresco/module/org_alfresco_module_rm/securitymarks/permission/SecurityMarksPermissionPreProcessor.java)
  * For all content: denies the result if the required security clearance rules (for classification or marks) are not satisfied. (uses 
[securityClearanceService.isClearedFor](../../rm-enterprise/rm-enterprise-repo/src/main/java/org/alfresco/module/org_alfresco_module_rm/securitymarks/SecurityClearanceServiceImpl.java))

##### Post-processors: 
* None.


### Configuration and Extension points

Additional processors can be defined by extending either [PermissionPreProcessorBaseImpl](../../rm-community/rm-community-repo/source/java/org/alfresco/repo/security/permissions/processor/impl/PermissionPreProcessorBaseImpl.java) 
or [PermissionPostProcessorBaseImpl](../../rm-community/rm-community-repo/source/java/org/alfresco/repo/security/permissions/processor/impl/PermissionPostProcessorBaseImpl.java) 
which call the add method on the appropriate list during init.

### Performance Implications

There is certainly a performance overhead when adding additional processing to permission checks. This is most noticeable
 in the SecurityMarksPermissionPreProcessor where we need to call out to an external service. This has been profiled 
 heavily and optimised during 2.5 and 2.6 development.
 
 ###TODO:
 Not yet documented (in related areas of the code) are:
 * Capabilities (see rm-capabilities-*.xml, declarativeCapability.java and DeclarativeCompositeCapability.java)
 * RM's permission system has an any allow allows policy unlike alfresco which policy is any deny denies
 