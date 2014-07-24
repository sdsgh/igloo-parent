package fr.openwide.core.jpa.hibernate.usertype;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.SingleColumnType;
import org.hibernate.usertype.UserType;

abstract class AbstractImmutableMaterializedPrimitiveValueUserType<P, T extends AbstractMaterializedPrimitiveValue<P, T>>
		implements UserType {
	
	private final SingleColumnType<P> delegateType;

	public AbstractImmutableMaterializedPrimitiveValueUserType(SingleColumnType<P> delegateType) {
		super();
		this.delegateType = delegateType;
	}

	@Override
	public int[] sqlTypes() {
		return new int[] { delegateType.sqlType() };
	}
	
	@Override
	public abstract Class<T> returnedClass();

	protected abstract T instantiate(P value);

	@Override
	public boolean equals(Object x, Object y) throws HibernateException { // NOSONAR
		return (x == y) || (x != null && y != null && x.equals(y)); // NOSONAR
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		@SuppressWarnings("unchecked")
		P value = (P) delegateType.nullSafeGet(rs, names, session, owner);
		if (value == null) {
			return null;
		} else {
			return instantiate(value);
		}
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) 
			throws HibernateException, SQLException {
		if (value == null) {
			delegateType.nullSafeSet(st, null, index, session);
		} else {
			delegateType.nullSafeSet(st, ((AbstractMaterializedPrimitiveValue<?, ?>)value).getValue(), index, session);
		}
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value; // type is immutable
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value; // type is immutable
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached; // type is immutable
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original; // type is immutable
	}
}