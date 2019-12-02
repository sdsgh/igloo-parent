package org.iglooproject.basicapp.core.business.user.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.bindgen.Bindable;

@Bindable
@Cacheable
@Entity
public class TechnicalUser extends User {

	private static final long serialVersionUID = 1796077904296031306L;

}
