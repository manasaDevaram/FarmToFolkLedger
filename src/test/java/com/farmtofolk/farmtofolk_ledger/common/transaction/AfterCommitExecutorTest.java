package com.farmtofolk.farmtofolk_ledger.common.transaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class AfterCommitExecutorTest {

  @Test
  void defersActionUntilTransactionCommit() {
    AtomicBoolean executed = new AtomicBoolean(false);
    TransactionSynchronizationManager.initSynchronization();
    try {
      new AfterCommitExecutor().run(() -> executed.set(true));
      assertFalse(executed.get());

      TransactionSynchronizationManager.getSynchronizations()
          .forEach(synchronization -> synchronization.afterCommit());
      assertTrue(executed.get());
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }
}
