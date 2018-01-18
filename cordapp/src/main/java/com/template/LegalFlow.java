package com.template;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.confidential.IdentitySyncFlow;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import java.security.PublicKey;
import java.util.List;

import static com.template.LegalContract.LEGAL_CONRACT_ID;
import static com.template.NDAContract.NDA_CONRACT_ID;
import static net.corda.core.contracts.ContractsDSL.requireThat;

@InitiatingFlow
@StartableByRPC
public class LegalFlow extends FlowLogic<Void>{
    //private NDAState inputState;
    private final UniqueIdentifier linearId;
    private final Party crmParty;
    private final String isLegalApproved;

    public LegalFlow(UniqueIdentifier linearId, Party crmParty) {
        this.linearId = linearId;
        isLegalApproved = "APPROVED";
        this.crmParty = crmParty;
    }
    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    @Override
    public Void call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(this.linearId),
                Vault.StateStatus.UNCONSUMED, null);
        List<StateAndRef<NDAState>> ndaStates = getServiceHub().getVaultService().queryBy(NDAState.class, queryCriteria).getStates();

        // We create the transaction components.
        NDAState outputState = new NDAState(getOurIdentity(), crmParty, "APPROVED");
        StateAndContract outputStateAndContract = new StateAndContract(outputState, LEGAL_CONRACT_ID);
        List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), crmParty.getOwningKey());
        Command cmd = new Command<>(new LegalContract.Approve(), requiredSigners);

        // We create a transaction builder and add the components.
        final TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.addInputState(ndaStates.get(0));
        txBuilder.setNotary(notary);
        txBuilder.withItems(outputStateAndContract, cmd);

        // Signing the transaction.
        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);
        // Creating a session with the other party.
        FlowSession otherpartySession = initiateFlow(crmParty);
        // Obtaining the counterparty's signature.
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, ImmutableList.of(otherpartySession), CollectSignaturesFlow.tracker()));
        // Finalising the transaction.
        subFlow(new FinalityFlow(fullySignedTx));
        return null;
    }
}