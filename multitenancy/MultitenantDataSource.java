package com.ncr.sv.authenticationservice.multitenancy;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultitenantDataSource extends AbstractRoutingDataSource {

	/**
	 * Description: Method that returns current tenant from TenantContext as lookup
	 * key, if it returns lookup key value as empty then tenant value will be
	 * default tenant
	 */
	@Override
	protected String determineCurrentLookupKey() {
		super.setLenientFallback(false);
		return TenantContext.getCurrentTenant();
	}
}
