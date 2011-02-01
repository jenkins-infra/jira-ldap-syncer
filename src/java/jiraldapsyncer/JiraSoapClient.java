package jiraldapsyncer;

import _soapclient.JiraSoapService;
import _soapclient.JiraSoapServiceService;
import _soapclient.JiraSoapServiceServiceLocator;
import _soapclient.beans.RemoteGroup;
import _soapclient.beans.RemoteUser;
import _soapclient.exceptions.RemoteAuthenticationException;
import _soapclient.exceptions.RemotePermissionException;
import _soapclient.exceptions.RemoteValidationException;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/** JIRA SOAP client */
public class JiraSoapClient
{
	public static final String ROLE = "jiraSoapClient";
	
	private static Log log = LogFactory.getLog(JiraSoapClient.class);

	// jira credentials
	private String adminLogin;
	private String adminPassword;
	
	private String defaultGroup;
	private String inactiveGroup;
    
	// internal implementation details
	private JiraSoapServiceService jiraSoapServiceGetter;
    private JiraSoapService jiraSoapService;
    private String token;

    public static void main(String[] args) throws Exception {
    	ServiceLocator locator = ServiceLocator.getInstance();
    	JiraSoapClient soapClient = (JiraSoapClient) locator.lookupService(JiraSoapClient.ROLE);
    	
    	Map<String, RemoteUser> users = soapClient.getAllUsers();
        log.info("Jira database contains " + users.size() + " users");
    }
    
    public Map<String, RemoteUser> getAllUsers() throws RemoteException {
    	return getGroupUsers(defaultGroup);
    }

    public Map<String, RemoteUser> getInactiveUsers() throws RemoteException {
    	return getGroupUsers(inactiveGroup);
    }
    
    private Map<String, RemoteUser> getGroupUsers(String groupname) throws RemoteException {
        RemoteGroup jiraGroup = getGroup(groupname);
        RemoteUser[] users = jiraGroup.getUsers();
        
        // put them into hashmap
        Map<String, RemoteUser> usersMap = new HashMap<String, RemoteUser>(); 
        for (RemoteUser user : users) {
        	user.setName( user.getName().toLowerCase() );
        	usersMap.put(user.getName(), user);
        }
        
        return usersMap;
    }
    
    public RemoteGroup getAllUsersGroup() throws RemoteException {
    	return getGroup(defaultGroup);
    }
    
    public RemoteGroup getInactiveUsersGroup() throws RemoteException {
    	return getGroup(inactiveGroup);
    }

    public RemoteUser getUser(String id) throws RemoteException {
        return jiraSoapService.getUser(token,id);
    }

    private RemoteGroup getGroup(String groupname) throws RemoteException {
    	RemoteGroup group = null;
    	try {
    		group = jiraSoapService.getGroup(token, groupname);
    	} catch (RemoteValidationException e) {
			// no such group
		}
    	if ( group == null ) {
    		group = createGroup(groupname);
    	}
    	return group;
    }
    
    public boolean createUser(RemoteUser user) throws RemoteException {
    	if ( 
    			user.getEmail() != null && !"".equals( user.getEmail() ) 
    			&&
    			user.getName() != null && !"".equals( user.getName() ) 
    			&&
    			user.getFullname() != null && !"".equals( user.getFullname() ) 
    	) {
    		jiraSoapService.createUser(token, user.getName(), user.getName(), user.getFullname(), user.getEmail());
    		return true;
    	} else {
			log.debug("Could not add (" + user.getName() + ", " + user.getEmail() + ", " + user.getFullname() + ") into Jira - bad data.");
    	}
    	return false;
    }

    public RemoteGroup createGroup(String name) throws RemoteException {
    	return jiraSoapService.createGroup(token, name, null);
    }
    
    public void addUserToGroup(RemoteUser user, RemoteGroup group) throws RemoteException {
    	jiraSoapService.addUserToGroup(token, group, user);
    }
    
    public void removeUserFromGroup(RemoteUser user, RemoteGroup group) throws RemoteException {
    	jiraSoapService.removeUserFromGroup(token, group, user);
    }
    
    public JiraSoapClient(String adminLogin, String adminPassword) throws ServiceException, java.rmi.RemoteException
    {
    	this.adminLogin = adminLogin;
    	this.adminPassword = adminPassword;
        jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
        jiraSoapService = jiraSoapServiceGetter.getJirasoapserviceV2();
        login();
    }

    public void login() throws RemoteException
    {
        token = jiraSoapService.login(adminLogin, adminPassword);
    }

	public void setAdminLogin(String adminLogin) {
		this.adminLogin = adminLogin;
	}
	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}
	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
	}
	public void setInactiveGroup(String inactiveGroup) {
		this.inactiveGroup = inactiveGroup;
	}
}
