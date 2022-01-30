package node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import block.Block;
import block.BlockChain;
import transaction.Transaction;
import utils.*;

/**
 * This class represents a Client, that is used to send message to another Node by this Node. It
 * contains a local address and a port which is the same as the corresponding Server in another
 * PeerNode.
 */
public class NodeClient {
  protected Socket clientSocket;
  protected ObjectOutputStream clientOut;
  protected ObjectInputStream clientIn;
  protected ServerInfo serverEnd;

  /**
   * Construct a NodeClient with the given address and port.
   *
   * @param address an address string
   * @param port    a port integer
   */
  public NodeClient(String address, int port) {
    this.serverEnd = new ServerInfo(address, port);
  }

  /**
   * Connect this NodeClient to the corresponding Server, including initialize a new Socket,
   * an OutputStream and an InputStream.
   *
   * @throws IOException if the initialization fails
   */
  private void connect() throws IOException {
    clientSocket = new Socket(serverEnd.getAddress(), serverEnd.getPort());
    clientOut = new ObjectOutputStream(clientSocket.getOutputStream());
    clientIn = new ObjectInputStream(clientSocket.getInputStream());
  }

  /**
   * Send a new Transaction to another Node.
   *
   * @param newTransaction a given Transaction
   * @return Message.SUCCEED  if the new Transaction is updated properly by the receiving Node
   * @throws IOException            if the socket and stream failed
   * @throws ClassNotFoundException if the class of the IO object does not exist
   */
  public synchronized Message sendMsg(Transaction newTransaction) throws
          IOException, ClassNotFoundException {
    connect();
    clientOut.writeObject(Message.TRANSACTION);
    clientOut.writeObject(newTransaction);
    Message feedBack = (Message) clientIn.readObject();
    stopConnection();
    return feedBack;
  }

  /**
   * Send a new Block to another Node.
   *
   * @param newBlock a given Block
   * @return Message.SUCCEED  if the new Block is updated properly by the receiving Node
   * @throws IOException            if the socket and stream failed
   * @throws ClassNotFoundException if the class of the IO object does not exist
   */
  public synchronized Message sendMsg(Block newBlock) throws
          IOException, ClassNotFoundException {
    connect();
    clientOut.writeObject(Message.BLOCK);
    clientOut.writeObject(newBlock);
    Message feedBack = (Message) clientIn.readObject();
    stopConnection();
    return feedBack;
  }

  /**
   * Send a new BlockChain to another Node.
   *
   * @param newBlockChain a given BlockChain
   * @return Message.SUCCEED if the new BlockChain is updated properly by the receiving Node
   * @throws IOException            if the socket and stream failed
   * @throws ClassNotFoundException if the class of the IO object does not exist
   */
  public synchronized Message sendMsg(BlockChain newBlockChain) throws
          IOException, ClassNotFoundException {
    connect();
    clientOut.writeObject(Message.BLOCKCHAIN);
    clientOut.writeObject(newBlockChain);
    Message feedBack = (Message) clientIn.readObject();
    stopConnection();
    return feedBack;
  }

  /**
   * Send a contact request to another Node.
   *
   * @return a string array with the contact name and the contact address(Wallet's PublicKey string)
   * @throws IOException            if the socket and stream failed
   * @throws ClassNotFoundException if the class of the IO object does not exist
   */
  public String[] sendContactRequest() throws IOException, ClassNotFoundException {
    connect();
    clientOut.writeObject(Message.CONTACT_REQUEST);
    String name = (String) clientIn.readObject();
    String publicKeyStr = (String) clientIn.readObject();
    System.out.println("Received new contact: " + name + ", " + publicKeyStr);
    stopConnection();
    return new String[]{name, publicKeyStr};
  }

  /**
   * Send a ContactMap to another Node.
   *
   * @param contactMap a given ContactMap
   * @return Message.SUCCEED  if the ContactMap is updated properly by the receiving Node
   * @throws IOException            if the socket and stream failed
   * @throws ClassNotFoundException if the class of the IO object does not exist
   */
  public Message sendContactMap(ContactMap contactMap) throws IOException, ClassNotFoundException {
    connect();
    clientOut.writeObject(Message.CONTACT_MAP);
    clientOut.writeObject(contactMap);
    Message feedBack = (Message) clientIn.readObject();
    stopConnection();
    return feedBack;
  }

  /**
   * Send a BlockChain request to another Node.
   *
   * @return a BlockChain replied from another Node
   * @throws IOException            if the socket and stream failed
   * @throws ClassNotFoundException if the class of the IO object does not exist
   */
  public BlockChain sendBlockChainRequest() throws IOException, ClassNotFoundException {
    connect();
    clientOut.writeObject(Message.BLOCKCHAIN_REQUEST);
    BlockChain newBlockChain = (BlockChain) clientIn.readObject();
    stopConnection();
    return newBlockChain;
  }

  /**
   * Send a list of Server ports to another Node.
   *
   * @param serverPorts a given Server ports list
   * @return Message.SUCCEED if the Server ports is updated properly by the receiving Node
   * @throws IOException            if the socket and stream failed
   * @throws ClassNotFoundException if the class of the IO object does not exist
   */
  public Message sendServerPort(List<Integer> serverPorts) throws
          IOException, ClassNotFoundException {
    connect();
    clientOut.writeObject(Message.SERVER_PORT);
    clientOut.writeObject(serverPorts);
    Message feedBack = (Message) clientIn.readObject();
    stopConnection();
    return feedBack;
  }

  /**
   * Send a list of Client ports to another Node.
   *
   * @param clientPorts a given Client ports list
   * @return Message.SUCCEED if the Client ports is updated properly by the receiving Node
   * @throws IOException            if the socket and stream failed
   * @throws ClassNotFoundException if the class of the IO object does not exist
   */
  public Message sendClientPort(List<Integer> clientPorts) throws
          IOException, ClassNotFoundException {
    connect();
    clientOut.writeObject(Message.CLIENT_PORT);
    clientOut.writeObject(clientPorts);
    Message feedBack = (Message) clientIn.readObject();
    stopConnection();
    return feedBack;
  }

  /**
   * Close all IO stream and Socket.
   *
   * @throws IOException if the close fails
   */
  private void stopConnection() throws IOException {
    clientOut.flush();
    clientOut.close();
    clientIn.close();
    clientSocket.close();
  }

  /**
   * Get the ServerInfo string of this NodeClient.
   *
   * @return a formatted string
   */
  public String getServerEndStr() {
    return "[" + serverEnd.getAddress() + ", " + serverEnd.getPort() + "]";
  }

  /**
   * Get the ServerInfo of this NodeClient.
   *
   * @return a ServerInfo object
   */
  public ServerInfo getServerEnd() {
    return serverEnd;
  }

  /**
   * Check if two NodeClients are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two NodeClients have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NodeClient other = (NodeClient) o;
    return serverEnd.equals(other.serverEnd);
  }

  /**
   * Generate the hashCode of this NodeClient.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(serverEnd);
  }

  /**
   * Get the string representation of this NodeClient.
   *
   * @return a formatted string
   */
  @Override
  public String toString() {
    return "NodeClient: " + getServerEndStr();
  }
}
