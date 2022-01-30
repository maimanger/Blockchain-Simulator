package node;

import java.util.List;
import utils.ContactMap;

/**
 * This interface represents a HostNode.
 */
public interface HostNode {

  /**
   * Add a new Contact to this Node.
   *
   * @param name name string of the new Contact
   * @param publicKeyStr PublicKey string of the new Contact
   */
  void addContact(String name, String publicKeyStr);

  /**
   * Set the Client address of the msgSender in this Node.
   *
   * @param address an address string
   */
  void setSelfClientAddress(String address);

  /**
   * Add a new msgSender to this Node with a given address and a port.
   *
   * @param address an address string of the new msgSender
   * @param port an integer port of the new msgSender
   */
  void addMsgSender(String address, int port);

  /**
   * Add a new msgSender to this Node with a given port, using the default Client address.
   *
   * @param port an integer port of the new msgSender
   */
  void addMsgSender(int port);

  /**
   * Get the msgSenders of this Node.
   *
   * @return a list of NodeClients
   */
  List<NodeClient> getMsgSenders();

  /**
   * Get the contacts of this Node.
   *
   * @return a ContactMap containing all contacts
   */
  ContactMap getContacts();
}
