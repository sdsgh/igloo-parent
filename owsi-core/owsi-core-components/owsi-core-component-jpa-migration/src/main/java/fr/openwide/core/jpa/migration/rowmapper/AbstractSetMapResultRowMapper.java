package fr.openwide.core.jpa.migration.rowmapper;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

public abstract class AbstractSetMapResultRowMapper<K,V> extends AbstractResultRowMapper<Map<K, Set<V>>> {
	
	private final SetMultimap<K, V> multimap;
	
	protected AbstractSetMapResultRowMapper(SetMultimap<K, V> results) {
		this(results, Multimaps.asMap(results));
	}

	private AbstractSetMapResultRowMapper(SetMultimap<K, V> multimap, Map<K, Set<V>> mapView) {
		super(mapView);
		this.multimap = multimap;
	}
	
	protected SetMultimap<K, V> getMultimap() {
		return multimap;
	}
}
