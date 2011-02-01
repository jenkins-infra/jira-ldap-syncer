package jiraldapsyncer;

import java.rmi.RemoteException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import _soapclient.beans.RemoteGroup;
import _soapclient.beans.RemoteUser;

public class JiraLdapSyncer {
	public static final String ROLE = "jiraLdapSyncer";
	
	private static Log log = LogFactory.getLog(JiraLdapSyncer.class);

	private JiraSoapClient jiraSoapClient;
	private LdapClient ldapClient;
	
	private boolean addNewLdapUsersToJira;
	
	public static void main(String[] args) throws RemoteException {
        JiraLdapSyncer jiraLdapSyncer = (JiraLdapSyncer) new ServiceLocator().lookupService(JiraLdapSyncer.ROLE);
	    jiraLdapSyncer.sync();
	}

    public void syncOneUserFromLDAPToJIRA(String id) throws RemoteException {
        RemoteUser ldap = getLdapClient().getUser(id);

        RemoteUser jira = getJiraSoapClient().getUser(id);

        if (jira!=null) {
            log.info(id+ " is already in JIRA");
        } else {
            log.info("Adding new ldap user " + id + " into Jira");
            getJiraSoapClient().createUser(ldap);
        }
    }


	public void sync() throws RemoteException {
	    long start = System.currentTimeMillis();

		// get or create JIRA group for inactive staff ( 'inactive-staff' by default )
    	RemoteGroup jiraInactiveStaff = getJiraSoapClient().getInactiveUsersGroup();

    	// retrieve all JIRA inactive users from inactive staff group 
    	Map<String, RemoteUser> jiraInactiveUsers = getJiraSoapClient().getInactiveUsers();
    	
    	// retrieve all JIRA users
    	Map<String, RemoteUser> jiraUsers = getJiraSoapClient().getAllUsers();

    	// retrieve all LDAP users
    	Map<String, RemoteUser> ldapUsers = getLdapClient().getAllUsers();
    	
    	// check if inactive users are still "inactive": if not => delete them from inactive staff group
    	int cntNoMoreInactiveJiraUsers = 0;
    	for ( RemoteUser inactiveUser : jiraInactiveUsers.values() ) {
    		if ( ldapUsers.containsKey(inactiveUser.getName()) ) {
    			log.info("Jira 'inactive user' " + inactiveUser.getName() + " is now back in LDAP - deleting him from 'inactive-staff' group");
    			getJiraSoapClient().removeUserFromGroup(inactiveUser, jiraInactiveStaff);
    			cntNoMoreInactiveJiraUsers++;
    		}
    	}
    	
    	// 1) add new LDAP users to JIRA 2) check how many users have changed their details in LDAP ( nothing can do with this for now )
    	int newLdapUsers = 0;
    	int diffData = 0;
    	for ( RemoteUser ldapUser : ldapUsers.values() ) {
    		if ( ! jiraUsers.containsKey(ldapUser.getName()) ) {
    			log.info("Adding new ldap user " + ldapUser.getName() + " into Jira");
                try {
                    if ( getJiraSoapClient().createUser(ldapUser) ) {
                        newLdapUsers++;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
    			RemoteUser jiraUser = jiraUsers.get(ldapUser.getName());
    			if (
    					!jiraUser.getEmail().equalsIgnoreCase( ldapUser.getEmail() )
    					|| 
    					!jiraUser.getFullname().equalsIgnoreCase( ldapUser.getFullname() )
    			) {
    				log.info(
    						"Some fields different in Jira DB and in LDAP for user " + ldapUser.getName() + " - "
    						+ "ldap:{" + ldapUser.getFullname() + ", " + ldapUser.getEmail() + "}"
    						+ ", jira:{" + jiraUser.getFullname() + ", " + jiraUser.getEmail() + "}"
    				);
    				// TODO: update user details in Jira
    				diffData++;
    			}
    		}
    	}
    	
    	// check JIRA users who are no more in LDAP and move them to inactive group
    	int oldJiraUsers = 0;
    	for ( RemoteUser jiraUser : jiraUsers.values() ) {
    		if ( ! ldapUsers.containsKey(jiraUser.getName()) && !jiraInactiveUsers.containsKey(jiraUser.getName()) ) {
    			log.info("Found old jira user " + jiraUser.getName() + " - moving him to 'inactive-staff' group");
    			jiraSoapClient.addUserToGroup(jiraUser, jiraInactiveStaff);
    			oldJiraUsers++;
    		}
    	}
    	
    	// print stats
	    long duration = System.currentTimeMillis() - start;
	    log.info("Jira database synced with LDAP in " + (int)(duration/1000) + " s");
    	log.info("Total number of new LDAP users is " + newLdapUsers);
    	log.info("Total number of old Jira users is " + oldJiraUsers);
    	log.info("Total number of 'rehired' Jira users is " + cntNoMoreInactiveJiraUsers);
    	log.info("Total number of users with different data in Jira DB and LDAP is " + diffData);
	}
	
	
	public JiraSoapClient getJiraSoapClient() {
		return jiraSoapClient;
	}
	public void setJiraSoapClient(JiraSoapClient jiraSoapClient) {
		this.jiraSoapClient = jiraSoapClient;
	}
	public LdapClient getLdapClient() {
		return ldapClient;
	}
	public void setLdapClient(LdapClient ldapClient) {
		this.ldapClient = ldapClient;
	}
	public boolean isAddNewLdapUsersToJira() {
		return addNewLdapUsersToJira;
	}
	public void setAddNewLdapUsersToJira(boolean addNewLdapUsersToJira) {
		this.addNewLdapUsersToJira = addNewLdapUsersToJira;
	}
	

}
