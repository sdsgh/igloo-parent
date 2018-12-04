package org.iglooproject.jpa.search.bridge;

import java.util.Collection;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.mapper.pojo.bridge.PropertyBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.PropertyBridgeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.PropertyBridgeWriteContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import org.hibernate.search.mapper.pojo.model.PojoElement;
import org.iglooproject.jpa.business.generic.model.GenericEntity;

public class GenericEntityCollectionIdFieldBridge<A extends GenericEntity<?, A>> implements PropertyBridge {

	@Override
	public void bind(PropertyBridgeBindingContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(DocumentElement target, PojoElement source, PropertyBridgeWriteContext context) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if (value == null) {
			return;
		}
		if (!(value instanceof Collection)) {
			throw new IllegalArgumentException("This FieldBridge only supports Collection of GenericEntity properties.");
		}
		Collection<?> objects = (Collection<?>) value;
		
		for (Object object : objects) {
			luceneOptions.addFieldToDocument(name, objectToString(object), document);
		}
	}

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