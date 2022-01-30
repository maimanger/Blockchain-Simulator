package controller;

import java.io.IOException;
import block.BlockChain;
import block.Block;
import node.NodeClient;
import utils.Message;

/**
 * This class represents a Runnable controller to create a new Block.
 */
public class CreateBlockRunnable implements Runnable {
  private Controller runnerController;

  /**
   * Construct a CreateBlockRunnable object with the given Controller.
   *
   * @param runnerController the Controller of this CreateBlockRunnable
   */
  public CreateBlockRunnable(Controller runnerController) {
    this.runnerController = runnerController;
  }

  /**
   * Run this object to start creating a new Block.
   */
  @Override
  public void run() {
    Message response;
    Block newBlock = runnerController.getNode().createBlock();
    String sendableText;
    String delimiter = "-".repeat(20);

    // if mining is interrupted, newBlock === null
    if (newBlock != null) {

/*      System.out.println(delimiter + "SENDING NEW BLOCK" + delimiter + newBlock + "\n");*/
      sendableText = delimiter + "SENDING NEW BLOCK" + delimiter + newBlock + "\n";
      runnerController.getNodeView().printSendingLog(sendableText);

      for (NodeClient sender : runnerController.getNode().getMsgSenders()) {
        try {
          response = sender.sendMsg(newBlock);
          // if the response Message is blockchain request, send the whole chain
          if (response == Message.BLOCKCHAIN_REQUEST) {
            BlockChain newBlockChain = runnerController.getNode().getBlockChain();

/*            System.out.println(delimiter + "SENDING NEW BLOCKCHAIN" + delimiter + "\n" + newBlockChain);*/
            sendableText = delimiter + "SENDING NEW BLOCKCHAIN" + delimiter + "\n" + newBlockChain + "\n";
            runnerController.getNodeView().printSendingLog(sendableText);

            response = sender.sendMsg(newBlockChain);
          }
/*          System.out.println("Block sending: " + response + "\n");*/

        } catch (IOException | ClassNotFoundException e) {
          System.out.println("Connection error. Failed sending block to "
                  + sender.getServerEndStr() + "\n");
        }
      }
    }
  }
}
