package org.iglooproject.jpa.search.bridge;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import org.iglooproject.jpa.business.generic.model.GenericEntity;

public class GenericEntityLongIdFieldBridge implements ValueBridge<GenericEntity<Long, ?>, Long> {

	@Override
	public Long toIndexedValue(GenericEntity<Long, ?> value, ValueBridgeToIndexedValueContext context) {
		if (value == null) {
			return null;
		}
		return value.getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenericEntity<Long, ?> cast(Object value) {
		return (GenericEntity<Long, ?>) value;
	}

}