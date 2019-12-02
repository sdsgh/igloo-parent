package org.iglooproject.test.search.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.bindgen.Bindable;

@Entity
@Bindable
public class Searchable {
	
	public static final String MULTIPLE_INDEXES = "multipleIndexes";
	public static final String MULTIPLE_INDEXES_AUTOCOMPLETE = MULTIPLE_INDEXES + "Autocomplete";
	
	@Id
	@GeneratedValue
	public Long id;

	public String autocomplete;

	public String keyword;
	
	public String multipleIndexes;
	
	public String notIndexed;

}
