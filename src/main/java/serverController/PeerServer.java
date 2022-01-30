package serverController;

import java.io.IOException;
import java.net.Socket;
import controller.Controller;
import transaction.Transaction;
import block.Block;
import block.BlockChain;
import utils.Message;

/**
 * This class represents a PeerServer that is used to receive message from another PeerNode.
 */
public class PeerServer extends AbstractServer {

  public PeerServer(int toPeerServerPort, Controller ownerController) {
    super(toPeerServerPort, ownerController);
  }


  @Override
  public void run() {
    while (true) {
      try {
        executor.execute(new ClientHandler(serverSocket.accept()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  private class ClientHandler extends AbstractClientHandler {
    private String delimiter = "-".repeat(20);

    public ClientHandler(Socket clientSocket) throws IOException {
      super(clientSocket);
    }

    /**
     * Process the received Transaction.
     *
     * @throws IOException if the InputStream/ OutputStream fails
     * @throws ClassNotFoundException if the class of the IO object does not exist
     */
    private void receiveTransaction() throws IOException, ClassNotFoundException {
      String receivedText;
      Transaction newTransaction = (Transaction) serverIn.readObject();
      boolean isUpdated = ownerController.getNode().updateTransactionPool(newTransaction);
      serverOut.writeObject(isUpdated ? Message.SUCCESS : Message.FAIL);

/*      System.out.println("New transaction received from Port " + serverPort
              + ":\n" + newTransaction);*/
/*      System.out.println("Updated: " + isUpdated + "\n");*/

      receivedText = delimiter + "RECEIVED NEW TRANSACTION" + delimiter + "\n"
              + "New transaction received from Port " + serverPort + ":\n" + newTransaction;
      ownerController.getNodeView().printReceivedLog(receivedText);
    }

    /**
     * Process the received Block.
     *
     * @throws IOException if the InputStream/ OutputStream fails
     * @throws ClassNotFoundException if the class of the IO object does not exist
     */
    private void receiveBlock() throws IOException, ClassNotFoundException {
      String receivedText;
      Block newBlock = (Block) serverIn.readObject();
      boolean isUpdated = ownerController.getNode().updateBlockChain(newBlock);
      if (isUpdated) {
        serverOut.writeObject(Message.SUCCESS);
      } else if (ownerController.getNode().verifyNewBlockSelfHash(newBlock)) {
        serverOut.writeObject(Message.BLOCKCHAIN_REQUEST);
      } else {
        serverOut.writeObject(Message.FAIL);
      }

/*      System.out.println("New block(height = " + (ownerController.getNode().getBlockChain().size())
              + ") received from Port " + serverPort
              + ":" + newBlock + "\n");*/

      receivedText = delimiter + "RECEIVED NEW BLOCK" + delimiter + "\n"
              + "New block(height = " + (ownerController.getNode().getBlockChain().size())
              + ") received from Port " + serverPort + ":" + newBlock + "\n";
      ownerController.getNodeView().printReceivedLog(receivedText);
    }

    /**
     * Process the received BlockChain.
     *
     * @throws IOException if the InputStream/ OutputStream fails
     * @throws ClassNotFoundException if the class of the IO object does not exist
     */
    private void receiveBlockChain() throws IOException, ClassNotFoundException {
      String receivedText;
      BlockChain newBlockChain = (BlockChain) serverIn.readObject();
      boolean isUpdated = ownerController.getNode().updateBlockChain(newBlockChain);
      serverOut.writeObject(isUpdated ? Message.SUCCESS : Message.FAIL);

/*      System.out.println("New blockchain received from Port " + serverPort
              + ":\n" + newBlockChain + "\n");*/
/*      System.out.println("Updated: " + isUpdated + "\n");*/

      receivedText = delimiter + "RECEIVED NEW BLOCKCHAIN" + delimiter + "\n"
              + "New blockchain received from Port " + serverPort + ":\n" + newBlockChain + "\n";
      ownerController.getNodeView().printReceivedLog(receivedText);
    }

    /**
     * Process the received BlockChain request.
     *
     * @throws IOException if the InputStream/ OutputStream fails
     */
    private void processBlockChainRequest() throws IOException {
      serverOut.writeObject(ownerController.getNode().getBlockChain());
    }

    /**
     * Process the received Message.
     *
     * @param receivedMsg the received Message value
     * @throws IOException if the InputStream/ OutputStream fails
     * @throws ClassNotFoundException if the class of the IO object does not exist
     */
    private void processMsg(Message receivedMsg) throws IOException, ClassNotFoundException {
      switch (receivedMsg) {
        case TRANSACTION:
          receiveTransaction();
          break;
        case BLOCK:
          receiveBlock();
          break;
        case BLOCKCHAIN:
          receiveBlockChain();
          break;
        case BLOCKCHAIN_REQUEST:
          processBlockChainRequest();
          break;
        default:
          System.out.println("Receiving error...");
          break;
      }
    }

    /**
     * Run the ClientHandler.
     */
    @Override
    public void run() {
      try {
        Message receivedMsg = (Message) serverIn.readObject();
/*        System.out.println("Received: " + receivedMsg);*/
        processMsg(receivedMsg);
        stopConnection();
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }


}
