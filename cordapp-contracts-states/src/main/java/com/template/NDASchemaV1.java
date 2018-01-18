package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;
import java.util.Date;
/**
 * An NDAState schema.
 */
public class NDASchemaV1 extends MappedSchema {
    public NDASchemaV1() {
        super(NDASchema.class, 1, ImmutableList.of(PersistentNDA.class));
    }

    @Entity
    @Table(name = "nda_states")
    public static class PersistentNDA extends PersistentState {
        @Column(name = "crmDept") private final String crmDept;
        @Column(name = "legalDept") private final String legalDept;
        @Column(name = "isLegalApproved")private final String isLegalApproved;
        @Column(name = "linearId")private final UUID linearId;

        public PersistentNDA(String crmDept, String legalDept, String isLegalApproved, UUID linearId) {
            this.crmDept = crmDept;
            this.legalDept = legalDept;
            this.isLegalApproved = isLegalApproved;
            this.linearId = linearId;
        }
        public String getCrmDept() {
            return crmDept;
        }
        public String getLegalDept() {
            return legalDept;
        }
        public UUID getLinearId() {
            return linearId;
        }
        public String isLegalApproved() {
            return isLegalApproved;
        }
    }
}