Howto configure, build and run Jira Ldap Syncer application:

1. Install maven 1 ( http://maven.apache.org/maven-1.x/ )

2. Install maven axis plugin
maven plugin:download -Dmaven.repo.remote=http://repository.atlassian.com,http://www.ibiblio.org/maven -DartifactId=maven-axis-plugin -DgroupId=atlassian -Dversion=0.7AXIS1.3

3. Ensure SOAP is enabled in your JIRA installation, by following the instructions at
http://confluence.atlassian.com/display/JIRA/Creating+a+SOAP+Client+Tutorial

4. Edit project.properties and change the URL to point to your JIRA installation

5. Run 'maven fetch-wsdl', which downloads the v2 WSDL and copies it to src/wsdl/

6. Run 'maven wsdl-to-java', which builds java stubs for WSDL and puts it into src/java/

7. Edit src/etc/serverAppContext.xml and alter LDAP settings.

8. Check source code 
   - in src/java/jiraldapsyncer/JiraLdapSyncer.java and change logic if needed
   - edit src/java/jiraldapsyncer/LdapClient.java and change mapping of LDAP attributes to Jira RemoteUser profile fields

9. Build using 'maven release'

10. Run 'java -Xmx512M -jar release/jira-ldap-syncer-3.12.jar'

11. Check logfile syncer.log
