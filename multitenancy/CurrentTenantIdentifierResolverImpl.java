package com.ncr.sv.authenticationservice.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.microsoft.sqlserver.jdbc.StringUtils;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

	@Value("${com.ncr.sep.multitenancy.defaultTenant}")
	private String defaultTenant;

	/**
	 * Description: Method that returns current tenant which stored in TenantContext
	 * else returns default tenant
	 */
	@Override
	public String resolveCurrentTenantIdentifier() {
		String currentTenant = TenantContext.getCurrentTenant();
		return !StringUtils.isEmpty(currentTenant) ? currentTenant : defaultTenant;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}

}
