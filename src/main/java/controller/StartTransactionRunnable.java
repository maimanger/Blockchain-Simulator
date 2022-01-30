package controller;

import java.io.IOException;
import node.NodeClient;
import transaction.Transaction;
import utils.Message;

/**
 * This class represents a Runnable controller to start new Transaction.
 */
public class StartTransactionRunnable implements Runnable {
  private Controller runnerController;

  /**
   * Construct a StartTransactionRunnable object with the given Controller.
   *
   * @param runnerController the Controller of this CreateBlockRunnable
   */
  public StartTransactionRunnable(Controller runnerController) {
    this.runnerController = runnerController;
  }

  /**
   * Run this object to start the new Transaction.
   */
  @Override
  public void run() {
    Message response;
    String sendableText;
    Transaction newTX = runnerController.getNode().startAutoTransaction();
    String delimiter = "-".repeat(20);
    // if the wallet balance is less than MINIMUM_INPUT, newTX == null
    if (newTX != null) {

/*      System.out.println(delimiter + "SENDING NEW TRANSACTION" + delimiter + "\n" + newTX);*/

      sendableText = delimiter + "SENDING NEW TRANSACTION" + delimiter + "\n" + newTX;
      runnerController.getNodeView().printSendingLog(sendableText);

      for (NodeClient sender : runnerController.getNode().getMsgSenders()) {
        try {
          response = sender.sendMsg(newTX);
/*          System.out.println("Transaction sending: " + response + "\n");*/
        } catch (IOException | ClassNotFoundException e) {
          System.out.println("Connection error. Failed sending transaction to " + sender.getServerEndStr());
        }
      }
    }
  }
}
