package fr.openwide.core.test.jpa.security.acl.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.NotFoundException;

import fr.openwide.core.jpa.business.generic.model.GenericEntity;
import fr.openwide.core.jpa.security.acl.domain.CoreAcl;
import fr.openwide.core.jpa.security.acl.service.AbstractCoreAclServiceImpl;

public class MockAclServiceImpl extends AbstractCoreAclServiceImpl {

	public MockAclServiceImpl() {
	}

	@Override
	protected List<AccessControlEntry> getAccessControlEntriesForEntity(
			CoreAcl acl, GenericEntity<?, ?> objectIdentityEntity)
			throws NotFoundException {
		return new ArrayList<AccessControlEntry>();
	}

	@Override
	protected boolean isCacheEnabled() {
		return false;
	}
}