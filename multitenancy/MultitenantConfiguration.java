package com.ncr.sv.authenticationservice.multitenancy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.MultiTenancyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.ncr.sv.authenticationservice.constants.AuthConstants;

@Configuration
public class MultitenantConfiguration {
	private DataSource dataSource;

	@Value("${com.ncr.sep.multitenancy.strategy}")
	private String multitenancyStrategy;
	@Value("${com.ncr.sep.multitenancy.defaultTenant}")
	private String defaultTenant;

	@Value("${com.ncr.sep.multitenancy.datasource.dir}")
	private String tenantDataSourceDir;


	@Autowired
	private DataSourceProperties properties;

	Logger logger = LoggerFactory.getLogger(MultitenantConfiguration.class);

	/**
	 * Description: Method that loads external datasource files in the specified
	 * directory at runtime
	 */
	@Bean
	@ConfigurationProperties
	public DataSource loadDataSourceFiles() {

		Map<Object, Object> resolvedDataSources = new HashMap<>();
		
		if (StringUtils.isEmpty(defaultTenant)) {
			throw new RuntimeException("defaultTenant should not be empty");
		}

		if (!StringUtils.isEmpty(multitenancyStrategy) &&
				!multitenancyStrategy.equalsIgnoreCase(MultiTenancyStrategy.NONE.name())) {

			if (StringUtils.isEmpty(tenantDataSourceDir)) {
				throw new RuntimeException("tenantDataSourceDir should not be empty, if multitenancy strategy is " + multitenancyStrategy);
			}

			File[] files = Paths.get(tenantDataSourceDir).toFile().listFiles();
			for (File propertyFile : files) {
				Properties tenantProperties = new Properties();
				DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();

				try {

					tenantProperties.load(new FileInputStream(propertyFile));
					String tenantId = tenantProperties.getProperty(AuthConstants.NAME);

					dataSourceBuilder.username(tenantProperties.getProperty(AuthConstants.USERNAME));
					dataSourceBuilder.password(tenantProperties.getProperty(AuthConstants.PASSWORD));
					dataSourceBuilder.url(tenantProperties.getProperty(AuthConstants.URL));
					resolvedDataSources.put(tenantId, dataSourceBuilder.build());
				} catch (IOException exp) {
					throw new RuntimeException("Problem in tenant datasource:" + exp);
				}
			}

		}

		AbstractRoutingDataSource dataSource = new MultitenantDataSource();
		DataSourceBuilder defaultDataSourceBuilder = DataSourceBuilder.create();
		defaultDataSourceBuilder.url(properties.getUrl()).username(properties.getUsername())
				.password(properties.getPassword());
		resolvedDataSources.put(defaultTenant, defaultDataSourceBuilder.build());
		dataSource.setDefaultTargetDataSource(defaultDataSourceBuilder.build());
		dataSource.setTargetDataSources(resolvedDataSources);
		dataSource.afterPropertiesSet();
		resolvedDataSources.keySet().stream()
				.forEach(a -> logger.info(a.toString() + " tenant details loaded successfully"));
		return dataSource;
	}
}
