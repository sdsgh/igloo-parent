package org.iglooproject.jpa.search.bridge;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import org.iglooproject.jpa.business.generic.model.GenericEntity;

public abstract class AbstractGenericEntityIdFieldBridge<A extends GenericEntity<?, A>> implements ValueBridge<A, String> {

	@Override
	public String toIndexedValue(A value, ValueBridgeToIndexedValueContext context) {
		if (value == null) {
			return null;
		}
		Object id = value.getId();
		// The ID may be null if the FieldBridge is being used while building a query.
		return id == null ? "" : id.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public A cast(Object value) {
		// TODO hibernate 6 - do we need a cleverer implementation (do we use id we may need to cast object in DSL)
		return (A) value;
	}

	protected String objectToString(A object) {
		if (object == null) {
			return null;
		}
		if (!(object instanceof GenericEntity)) {
			throw new IllegalArgumentException("This FieldBridge only supports GenericEntity properties.");
		}
		GenericEntity<?, ?> entity = (GenericEntity<?, ?>) object;
		Object id = entity.getId();
		// The ID may be null if the FieldBridge is being used while building a query.
		return id == null ? "" : id.toString();
	}
}