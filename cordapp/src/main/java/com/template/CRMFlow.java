package com.template;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.confidential.IdentitySyncFlow;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;
import static com.template.NDAContract.NDA_CONRACT_ID;

@InitiatingFlow
@StartableByRPC
public class CRMFlow extends FlowLogic<Void>{
    private final Party legalParty;
    private final String isLegalApproved;

    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public CRMFlow(Party legalParty) {
        this.legalParty = legalParty;
        this.isLegalApproved = "PENDING";
    }

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

        // We create the transaction components.
        NDAState outputState = new NDAState(getOurIdentity(), legalParty, isLegalApproved);
        StateAndContract outputStateAndContract = new StateAndContract(outputState, NDA_CONRACT_ID);
        List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), legalParty.getOwningKey());
        Command cmd = new Command<>(new NDAContract.Create(), requiredSigners);

        // We create a transaction builder and add the components.
        final TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.setNotary(notary);

        txBuilder.withItems(outputStateAndContract, cmd);

        // Signing the transaction.
        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        // Creating a session with the other party.
        FlowSession otherpartySession = initiateFlow(legalParty);
        // Obtaining the counterparty's signature.
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, ImmutableList.of(otherpartySession), CollectSignaturesFlow.tracker()));
        // Finalising the transaction.
        subFlow(new FinalityFlow(fullySignedTx));
        return null;
    }
}