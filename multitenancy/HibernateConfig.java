package com.ncr.sv.authenticationservice.multitenancy;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HibernateConfig {


    @Value("${com.ncr.sep.multitenancy.packageToScan}")
    private String packageToScan;

    @Value("${com.ncr.sep.multitenancy.strategy}")
    private String multitenancyStrategy;

    @Autowired
    private JpaProperties jpaProperties;

    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider(AbstractRoutingDataSource dataSource) {
        return new MultiTenantConnectionProviderImpl(dataSource);
    }

    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolverImpl();
    }

    @Bean
    JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    /**
     * Desription: Method used to set multi_tenant default environment variable
     * values to entitymanagerfactory bean
     *
     * @param dataSource
     * @param multiTenantConnectionProviderImpl
     * @param currentTenantIdentifierResolverImpl
     * @return LocalContainerEntityManagerFactoryBean
     */
    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                MultiTenantConnectionProvider multiTenantConnectionProviderImpl,
                                                                CurrentTenantIdentifierResolver currentTenantIdentifierResolverImpl) {

        if (StringUtils.isEmpty(multitenancyStrategy)) {
            throw new RuntimeException("Multitenancy strategy should not be empty");
        } else if (StringUtils.isEmpty(packageToScan)) {
            throw new RuntimeException("PackageToScan should not be empty");
        }

        Map<String, Object> jpaPropertiesMap = new HashMap<>(jpaProperties.getProperties());
        if (!multitenancyStrategy.equalsIgnoreCase(MultiTenancyStrategy.NONE.name())) {
            jpaPropertiesMap.put(Environment.MULTI_TENANT, multitenancyStrategy);
            jpaPropertiesMap.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProviderImpl);
            jpaPropertiesMap.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolverImpl);
        }
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setPackagesToScan(packageToScan);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(this.jpaVendorAdapter());
        localContainerEntityManagerFactoryBean.setJpaPropertyMap(jpaPropertiesMap);
        return localContainerEntityManagerFactoryBean;
    }
}
