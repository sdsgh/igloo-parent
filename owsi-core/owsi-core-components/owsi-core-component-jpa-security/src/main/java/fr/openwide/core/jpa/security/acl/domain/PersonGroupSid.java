package fr.openwide.core.jpa.security.acl.domain;

import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.Sid;

import fr.openwide.core.jpa.security.business.person.model.IPersonGroup;

public class PersonGroupSid extends GrantedAuthoritySid {

	private static final long serialVersionUID = -1418624664701634350L;
	
	private static final String SEPARATOR = "-";
	
	private static final String PERSON_GROUP_PREFIX = "group:";

	public PersonGroupSid(IPersonGroup personGroup) {
		super(PERSON_GROUP_PREFIX + personGroup.getId() + SEPARATOR + personGroup.getName());
	}
	
	public static Integer getGroupId(String grantedAuthority) {
		Integer groupId = null;
		try {
			groupId = Integer.parseInt(grantedAuthority.substring(PERSON_GROUP_PREFIX.length() , grantedAuthority.indexOf(SEPARATOR)));
		} catch(NumberFormatException e) {
		}
		return groupId;
	}
	
	public static boolean isPersonGroupSid(Sid sid) {
		if(sid instanceof GrantedAuthoritySid) {
			return ((GrantedAuthoritySid) sid).getGrantedAuthority().startsWith(PERSON_GROUP_PREFIX);
		} else {
			return false;
		}
	}
}