package org.iglooproject.jpa.security.business.person.service;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.iglooproject.jpa.business.generic.service.GenericEntityServiceImpl;
import org.iglooproject.jpa.exception.SecurityServiceException;
import org.iglooproject.jpa.exception.ServiceException;
import org.iglooproject.jpa.search.service.IHibernateSearchService;
import org.iglooproject.jpa.security.business.authority.model.Authority;
import org.iglooproject.jpa.security.business.authority.service.IAuthorityService;
import org.iglooproject.jpa.security.business.authority.util.CoreAuthorityConstants;
import org.iglooproject.jpa.security.business.person.dao.IGenericUserDao;
import org.iglooproject.jpa.security.business.person.model.GenericUser;
import org.iglooproject.jpa.security.business.person.model.IUserBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class GenericUserServiceImpl<U extends GenericUser<U, ?>>
		extends GenericEntityServiceImpl<Long, U>
		implements IGenericUserService<U> {
	
	private static final IUserBinding BINDING = new IUserBinding();
	
	private static final String[] SEARCH_FIELDS = new String[] { BINDING.username().getPath() };
	
	private static final String[] AUTOCOMPLETE_SEARCH_FIELDS = new String[] { BINDING.username().getPath() };
	
	private static final Sort AUTOCOMPLETE_SORT = new Sort(new SortField(GenericUser.USERNAME_SORT, SortField.Type.STRING));

	@Autowired
	private IAuthorityService authorityService;
	
	@Autowired
	private IHibernateSearchService hibernateSearchService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	private IGenericUserDao<U> personDao;
	
	@Autowired
	public GenericUserServiceImpl(IGenericUserDao<U> personDao) {
		super(personDao);
		this.personDao = personDao;
	}
	
	@Override
	public U getByUsername(String username) {
		return getByNaturalId(username);
	}
	
	@Override
	public U getByUsernameCaseInsensitive(String username) {
		return personDao.getByUsernameCaseInsensitive(username);
	}
	
	/**
	 * @deprecated use the ISearchQuery pattern instead.
	 */
	@Deprecated
	@Override
	public List<U> search(String searchPattern) throws ServiceException, SecurityServiceException {
		return hibernateSearchService.search(getObjectClass(), SEARCH_FIELDS, searchPattern);
	}
	
	/**
	 * @deprecated use the ISearchQuery pattern instead.
	 */
	@Deprecated
	@Override
	public List<U> searchAutocomplete(String searchPattern) throws ServiceException, SecurityServiceException {
		return searchAutocomplete(searchPattern, null, null);
	}
	
	/**
	 * @deprecated use the ISearchQuery pattern instead.
	 */
	@Deprecated
	@Override
	public <U2 extends U> List<U2> searchAutocomplete(Class<U2> clazz, String searchPattern) throws ServiceException, SecurityServiceException {
		return searchAutocomplete(clazz, searchPattern, null, null);
	}
	
	/**
	 * @deprecated use the ISearchQuery pattern instead.
	 */
	@Deprecated
	@Override
	public List<U> searchAutocomplete(String searchPattern, Integer limit, Integer offset) throws ServiceException, SecurityServiceException {
		return searchAutocomplete(getObjectClass(), searchPattern, limit, offset);
	}
	
	/**
	 * @deprecated use the ISearchQuery pattern instead.
	 */
	@Deprecated
	@Override
	public <U2 extends U> List<U2> searchAutocomplete(Class<U2> clazz, String searchPattern, Integer limit, Integer offset) throws ServiceException, SecurityServiceException {
		return hibernateSearchService.searchAutocomplete(clazz, AUTOCOMPLETE_SEARCH_FIELDS, searchPattern,
				limit, offset, AUTOCOMPLETE_SORT);
	}
	
	@Override
	protected void createEntity(U person) throws ServiceException, SecurityServiceException {
		super.createEntity(person);
		
		Date date = new Date();
		person.setCreationDate(date);
		person.setLastUpdateDate(date);
		
		if (person.getAuthorities().size() == 0) {
			Authority defaultAuthority = authorityService.getByName(CoreAuthorityConstants.ROLE_AUTHENTICATED);
			if (defaultAuthority != null) {
				person.addAuthority(defaultAuthority);
				
				super.save(person);
			} else {
				throw new ServiceException("Default authority ROLE_AUTHENTICATED has not been created yet");
			}
		}
	}
	
	@Override
	protected void updateEntity(U person) throws ServiceException, SecurityServiceException {
		person.setLastUpdateDate(new Date());
		super.updateEntity(person);
	}
	
	@Override
	public void updateProfileInformation(U person) throws ServiceException, SecurityServiceException {
		super.update(person);
	}
	
	@Override
	public void addAuthority(U person, Authority authority) throws ServiceException, SecurityServiceException {
		person.addAuthority(authority);
		super.update(person);
	}
	
	@Override
	public void addAuthority(U person, String authorityName) throws ServiceException, SecurityServiceException {
		addAuthority(person, authorityService.getByName(authorityName));
	}
	
	@Override
	public void setActive(U person, boolean active) throws ServiceException, SecurityServiceException {
		person.setActive(active);
		super.update(person);
	}
	
	@Override
	public Long countActive() {
		return personDao.countActive();
	}
	
	@Override
	public void setPasswords(U person, String clearTextPassword) throws ServiceException, SecurityServiceException {
		person.setPasswordHash(passwordEncoder.encode(clearTextPassword));
		super.update(person);
	}

	@Override
	public void updateLastLoginDate(U person) throws ServiceException, SecurityServiceException {
		person.setLastLoginDate(new Date());
		super.updateEntity(person);
	}
	
	@Override
	public void updateLocale(U person, Locale locale) throws ServiceException, SecurityServiceException {
		person.setLocale(locale);
		super.updateEntity(person);
	}

}