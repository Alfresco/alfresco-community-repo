# Create alfresco
CREATE USER 'alfresco' IDENTIFIED BY 'alfresco';
GRANT ALL on alfresco.* to 'alfresco'@'%' identified by 'alfresco' with grant option;
FLUSH HOSTS;
FLUSH PRIVILEGES;
# Create DB
CREATE DATABASE IF NOT EXISTS `alfresco` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;