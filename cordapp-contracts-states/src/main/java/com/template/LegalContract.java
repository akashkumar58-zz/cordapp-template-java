package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class LegalContract implements Contract{
    public static final String LEGAL_CONRACT_ID = "com.example.contract.LegalContract";

    // Our create command
    public static class Approve implements CommandData {
    }

    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Approve> command = requireSingleCommand(tx.getCommands(),
                Approve.class);
        requireThat(check -> {
            check.using("No inputs should be consumed when issuing an IOU.", tx.getInputs().size() == 1);
            check.using("There should be one output state of type IOUState.", tx.getOutputs().size() == 1);

            return null;
        });
    }
}
