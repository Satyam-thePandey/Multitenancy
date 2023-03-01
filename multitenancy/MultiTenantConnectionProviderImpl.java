package com.ncr.sv.authenticationservice.multitenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider {

	private static final long serialVersionUID = 1L;

	private DataSource dataSource;

	@Autowired
	public MultiTenantConnectionProviderImpl(final DataSource dataSource) {
		this.setDataSource(dataSource);
	}

	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		return null;
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		return this.dataSource.getConnection();
	}

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connection.close();

	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		final Connection connection = getAnyConnection();
		return connection;
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		connection.close();

	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

}
