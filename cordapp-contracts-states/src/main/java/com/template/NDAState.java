package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;

import java.util.Arrays;
import java.util.List;
import java.util.Date;

/**
 * The state object recording IOU agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 */
public class NDAState implements LinearState, QueryableState {
    private final Party crmDept;
    private final Party legalDept;
    private final String isLegalApproved;
    private final UniqueIdentifier linearId;
    /*private final Party advisoryDept;
    private final Party creditDept;
    private final String typeOfTransaction;
    private final String clientName;
    private final String clientIndustary;
    private final Integer dealSize;
    private final Integer fee;
    private final String clientType;
    private final String clientRegion;
    private final String csEntityInvolved;
    private final UniqueIdentifier stateId;
    private final String isAdvisoryApproved;
    private final String isCreditApproved;
    private final Date expiryDate;
    private final String issuerBank;
    private final UniqueIdentifier linearId;
    */

    public NDAState(Party crmDept, Party legalDept,  String isLegalApproved) {
        this.crmDept = crmDept;
        this.legalDept = legalDept;
        this.isLegalApproved = isLegalApproved;
        this.linearId = new UniqueIdentifier();
    }

    public Party getCrmDept() {
        return crmDept;
    }

    public Party getLegalDept() {
        return legalDept;
    }

    public String getIsLegalApproved() {
        return isLegalApproved;
    }


    @Override public UniqueIdentifier getLinearId() { return linearId; }
    @Override public List<AbstractParty> getParticipants() {
        return Arrays.asList(crmDept, legalDept);
    }

    @Override public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof NDASchemaV1) {
            return new NDASchemaV1.PersistentNDA(
                    this.crmDept.toString(),
                    this.legalDept.toString(),
                    this.isLegalApproved,
                    this.linearId.getId()
            );
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @Override public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new NDASchemaV1());
    }

    @Override
    public String toString() {
        return "IOUState{" +
                "crmDept=" + crmDept +
                ", legalDept=" + legalDept +
                ", stateId=" + linearId +
                ", isLegalApproved=" + isLegalApproved +
                '}';
    }
}