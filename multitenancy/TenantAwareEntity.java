package com.ncr.sv.authenticationservice.multitenancy;

import com.ncr.sv.authenticationservice.constants.AuthConstants;
import lombok.Data;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * This class used for configure filtering data based upon organization name
 * and mapped to supper class for partitioned objects.
 */

@MappedSuperclass
@Component
@Data
@FilterDef(name = AuthConstants.TENANT_FILTER, parameters = @ParamDef(name = AuthConstants.ORGANIZATION_NAME,
        type = "string"))
@Filter(name = AuthConstants.TENANT_FILTER, condition = "ORGANIZATION_NAME = :organizationName")
public class TenantAwareEntity implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The organization name into which this data has been partitioned
     */
    @Column(name = "ORGANIZATION_NAME")
    private String organizationName;

}