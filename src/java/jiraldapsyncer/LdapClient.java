package jiraldapsyncer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import _soapclient.beans.RemoteUser;

public class LdapClient {
	public static final String ROLE = "ldapClient";

	private static Log log = LogFactory.getLog(LdapClient.class);
	
	private LdapTemplate ldapTemplate;

	private class EmployeeAttributesMapper implements AttributesMapper {
		public Object mapFromAttributes(Attributes attrs) throws NamingException {
			RemoteUser user = new RemoteUser();
            user.setName((String)getOne(attrs,"cn"));

            String givenName = (String)getOne(attrs,"givenName");
            String sn = (String)getOne(attrs,"sn");

            if (sn.equals("-"))     user.setFullname(givenName);
            else                    user.setFullname(givenName+' '+sn);

            user.setEmail((String)getOne(attrs,"mail"));

			return user;
		}

        private Object getOne(Attributes attrs, String attributeName) throws NamingException {
            Attribute a = attrs.get(attributeName);
            return a!=null ? a.get() : null;
        }
    }

    public RemoteUser getUser(String cn) {
        List results =  ldapTemplate.search( "", "(cn="+cn+")", new EmployeeAttributesMapper());
        return results.isEmpty() ? null : (RemoteUser)results.get(0);
    }

	public Map<String, RemoteUser> getAllUsers() {
		Map<String, RemoteUser> ldapUsers = new HashMap<String, RemoteUser>();
		
		// retrieve users from LDAP
		List results =  ldapTemplate.search(
				"", 
				"(objectclass=person)",
				new EmployeeAttributesMapper()
		);
		
		// put them into hashmap
		for (Object result : results) {
			RemoteUser user = (RemoteUser) result;
			ldapUsers.put(user.getName(), user);
		}
		
		return ldapUsers;
	}
	
	public static void main(String[] args) {
    	ServiceLocator locator = new ServiceLocator();
    	LdapClient ldapClient = (LdapClient) locator.lookupService(LdapClient.ROLE);
    	Map<String, RemoteUser> ldapUsers = ldapClient.getAllUsers();
    	log.info("LDAP directory contains " + ldapUsers.size() + " users");
	}

	public LdapTemplate getLdapTemplate() {
		return ldapTemplate;
	}
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

}

