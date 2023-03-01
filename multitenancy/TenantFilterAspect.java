package com.ncr.sv.authenticationservice.multitenancy;

import com.ncr.sv.authenticationservice.constants.AuthConstants;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * This class used as java aspect for filtering data access repository methods in order to enable Hibernate filter that
 * handles filtering based on the settings.
 */

@Aspect
@Component
public class TenantFilterAspect {

    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(TenantFilterAspect.class);


    /**
     * Entity manager associated with the current invocation
     */
    @Autowired
    private EntityManager entityManager;


    /**
     * This function enables or disables filter based upon configured expression
     *
     * @param joinPoint current invocation point (method)
     * @throws Exception if there are any other errors
     */
    @Before("execution(* com.ncr.sv.authenticationservice.repository.TenantAwareRepository+.isTable*(..)) " +
            "or execution(* com.ncr.sv.authenticationservice.repository.TenantAwareRepository+.find*(..))" +
            "or execution(* com.ncr.sv.authenticationservice.repository.TenantAwareRepository+.get*(..))" +
            "or execution(* com.ncr.sv.authenticationservice.repository.TenantAwareRepository+.fetch*(..))" +
            "or execution(* com.ncr.sv.authenticationservice.repository.TenantAwareRepository+.update*(..))" +
            "or execution(* com.ncr.sv.authenticationservice.repository.TenantAwareRepository+.exists*(..))" +
            "or execution(* com.ncr.sv.authenticationservice.repository.TenantAwareRepository+.delete*(..))")
    public void applyFilter(final JoinPoint joinPoint) throws Exception {
        LOG.debug("[TenantFilterAspect] [applyFilter()] : Action started to apply filter on joint point ");

        String targetOrganization = TenantContext.getCurrentTenant();
        Session session = this.entityManager.unwrap(Session.class);
        Filter enableFilter = session.enableFilter(AuthConstants.TENANT_FILTER);
        enableFilter.setParameter(AuthConstants.ORGANIZATION_NAME, targetOrganization);

        LOG.debug("[TenantFilterAspect] [applyFilter()] : Action completed to apply filter on joint point ");
    }

    /**
     * This function applies partition to the join point arguments (method arguments) based upon
     * configured expression
     *
     * @param joinPoint instrumented pointed
     * @throws Exception if there are any other errors
     */
    @Before("execution(* com.ncr.sv.authenticationservice.repository.TenantAwareRepository+.save*(..)) ")
    public void applyPartition(final JoinPoint joinPoint) throws Exception {
        LOG.info("[TenantFilterAspect] [applyPartition()] : Action started to apply partition on joint point ");

        Collection<TenantAwareEntity> arguments = extractPartitionArguments(joinPoint.getArgs());

        if (!arguments.isEmpty()) {
            String organizationName = TenantContext.getCurrentTenant();

            for (TenantAwareEntity object : arguments) {
                if (StringUtils.isEmpty(object.getOrganizationName())) {
                    object.setOrganizationName(organizationName);
                }
            }
        }
        LOG.info("[TenantFilterAspect] [applyPartition()] : Action completed to apply partition on joint point ");
    }


    /**
     * This function extracts {@link TenantAwareEntity} arguments from method invocation. Supports Iterable and direct PartitionedObject
     * arguments.
     *
     * @param args arguments of the instrumented method
     * @return collection of resolved PartitionedObject
     */
    protected Collection<TenantAwareEntity> extractPartitionArguments(final Object[] args) {
        LOG.info("[TenantFilterAspect] [extractPartitionArguments()] : Action started to extracting partition arguments");
        if ((args == null) || (args.length == 0)) {
            return Collections.emptyList();
        }

        List<TenantAwareEntity> result = new LinkedList<>();

        for (Object argument : args) {
            if (argument instanceof TenantAwareEntity) {
                result.add((TenantAwareEntity) argument);
            }
            if (argument instanceof Iterable) {
                for (Object element : (Iterable<?>) argument) {
                    if (element instanceof TenantAwareEntity) {
                        result.add((TenantAwareEntity) element);
                    }
                }
            }
        }
        LOG.info("[TenantFilterAspect] [extractPartitionArguments()] : Action completed to extracting partition arguments");
        return result;
    }
}