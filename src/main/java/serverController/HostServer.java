package serverController;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import controller.Controller;
import utils.*;

/**
 * This class represents a HostServer that is used to receive message from a HostNode.
 * It contains a list of peerServers, which are used to receive message from PeerNodes.
 */
public class HostServer extends AbstractServer {
  private List<NodeServer> peerServers;

  /**
   * Construct a Host Server with the given port and ownerController.
   *
   * @param toHostServerPort an integer port, which is used to connect the HostNode Client
   * @param ownerController a given Controller
   */
  public HostServer(int toHostServerPort, Controller ownerController) {
    super(toHostServerPort, ownerController);
    peerServers = new ArrayList<>();
  }

  /**
   * Add a given PeerServer to this HostServer, then execute this Server in a new thread.
   *
   * @param newPeerServer a given NodeServer
   */
  public synchronized void addPeerServer(NodeServer newPeerServer) {
    if (peerServers.isEmpty() || !peerServers.contains(newPeerServer)) {
      peerServers.add(newPeerServer);
    }
    // start a new thread of peerServer running
    executor.execute(newPeerServer);
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

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
            && peerServers.equals(((HostServer) o).peerServers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serverPort, ownerController, peerServers);
  }


  private class ClientHandler extends AbstractClientHandler {

    public ClientHandler(Socket clientSocket) throws IOException {
      super(clientSocket);
    }

    /**
     * Process the received Contact request, reply with the contact information
     * got from the ownerController.
     *
     * @throws IOException if the OutPutStream fails
     */
    private void processContactRequest() throws IOException {
      serverOut.writeObject(ownerController.getNode().getOwnerName());
      serverOut.writeObject(ownerController.getNode().getWallet().getPublicKeyStr());
    }

    /**
     * Process the received ContactMap.
     *
     * @throws IOException if the InputStream/ OutputStream fails
     * @throws ClassNotFoundException if the class of the IO object does not exist
     */
    private void receiveContactMap() throws IOException, ClassNotFoundException {
      ContactMap newContactMap = (ContactMap) serverIn.readObject();
      // remove self contact in the ownerNode
      newContactMap.remove(ownerController.getNode().getOwnerName());
      // remove exist contact of ownerNode from new contactMap
      ownerController.getNode().getContacts().getMap().forEach((k, v) -> newContactMap.remove(k));
      // add each contact in new contactMap to this ownerNode
      newContactMap.getMap().forEach((k, v) -> ownerController.getNode().addContact(k, v));
      // check if ownerNode's contactMap contains all contacts in the new Map
      boolean isAdded = ownerController.getNode().getContacts().getMap().entrySet()
              .containsAll(newContactMap.getMap().entrySet());
      serverOut.writeObject(isAdded ? Message.SUCCESS : Message.FAIL);

      System.out.println("ContactMap updated:");
      System.out.println(newContactMap);
    }

    /**
     * Cast the received object as a list.
     *
     * @param received a received object
     * @return a list casted from the received object
     */
    private List<?> castReceivedPort(Object received) {
      List<?> newServerPorts = new ArrayList<>();
      if (received instanceof List<?>) {
        newServerPorts = (List<?>) received;
        newServerPorts.forEach(Integer.class::cast);
      }
      return newServerPorts;
    }

    /**
     * Process the received Server ports.
     *
     * @throws IOException if the InputStream/ OutputStream fails
     * @throws ClassNotFoundException if the class of the IO object does not exist
     */
    private void receiveServerPort() throws IOException, ClassNotFoundException {
      Object received = serverIn.readObject();
      List<?> newServerPorts = castReceivedPort(received);
/*      List<Integer> newServerPorts = (List<Integer>) serverIn.readObject();*/

      newServerPorts.forEach(port -> addPeerServer(new PeerServer((int) port, ownerController)));
      serverOut.writeObject(Message.SUCCESS);
      System.out.println("ToPeerServers updated: " + peerServers);
    }

    private synchronized void  receiveClientPort() throws IOException, ClassNotFoundException {
      Object received = serverIn.readObject();
      List<?> newClientPorts = castReceivedPort(received);
/*      List<Integer> newClientPorts = (List<Integer>) serverIn.readObject();*/

      newClientPorts.forEach(port -> ownerController.getNode().addMsgSender((int) port));
      serverOut.writeObject(Message.SUCCESS);
      System.out.println("ToPeerClients updated: " + ownerController.getNode().getMsgSenders());
    }

    /**
     * Process the received Message.
     *
     * @param receivedMsg the received Message value
     * @throws IOException if the InputStream/ OutputStream fails
     * @throws ClassNotFoundException if the class of the IO object does not exist
     */
    private synchronized void processMsg(Message receivedMsg) throws
            IOException, ClassNotFoundException {
      switch (receivedMsg) {
        case SERVER_PORT:
          receiveServerPort();
          break;
        case CLIENT_PORT:
          receiveClientPort();
          break;
        case CONTACT_REQUEST:
          processContactRequest();
          break;
        case CONTACT_MAP:
          receiveContactMap();
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
        System.out.println("Received: " + receivedMsg);
        processMsg(receivedMsg);
        stopConnection();
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

}
