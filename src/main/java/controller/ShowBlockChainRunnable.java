package controller;

/**
 * This class represents a Runnable controller to show the BlockChain.
 */
public class ShowBlockChainRunnable implements Runnable {
  private Controller runnerController;

  /**
   * Construct a ShowBlockChainRunnable object with the given Controller.
   *
   * @param runnerController the Controller of this CreateBlockRunnable
   */
  public ShowBlockChainRunnable(Controller runnerController) {
    this.runnerController = runnerController;
  }

  /**
   * Run this object to show a BlockChain.
   */
  @Override
  public void run() {
/*    String delimiter = "-".repeat(60);
    System.out.println(delimiter + "CURRENT BLOCKCHAIN" + delimiter + "\n" + runnerController.getNode().getBlockChain());*/

    String showableText = runnerController.getNode().getBlockChain().toString();
    runnerController.getNodeView().printBlockChainLog(showableText);

  }
}
