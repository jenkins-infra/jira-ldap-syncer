package io.jenkins.backend.jiraldapsyncer;

/**
 * @author Kohsuke Kawaguchi
 */
public class Foo {
    public static void main(String[] args) {
        System.setProperty("jira.url","https://issues.jenkins-ci.org/");
        System.setProperty("jira.username","kohsuke");
        // System.setProperty("jira.password","...");    set this via JVM option
        System.setProperty("url","ldap://localhost:9389");
        System.setProperty("password","xxx");

        JiraLdapSyncer jiraLdapSyncer = (JiraLdapSyncer) new ServiceLocator().lookupService(JiraLdapSyncer.ROLE);
        RemoteUser jira = jiraLdapSyncer.getJiraRestClient().getUser("kohsuke");
        System.out.println(jira);

        jira = jiraLdapSyncer.getJiraRestClient().getUser("no-such-user");
        System.out.println(jira);
    }
}
