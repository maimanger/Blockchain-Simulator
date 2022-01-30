package node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import utils.*;

/**
 * This class represents a Node containing contactMap, list of NodeClients
 * and a default client address of this Node.
 */
public abstract class AbstractNode implements HostNode {
  protected ContactMap contacts;
  protected List<NodeClient> msgSenders;
  protected String selfClientAddress;

  /**
   * Initialize an empty ContactMap and a list of msgSenders.
   */
  public AbstractNode() {
    this.contacts = new ContactMap();
    this.msgSenders = new ArrayList<>();
  }

  @Override
  public void setSelfClientAddress(String address) {
    this.selfClientAddress = address;
  }

  @Override
  public void addContact(String name, String publicKeyStr) {
    contacts.add(name, publicKeyStr);
  }

  @Override
  public synchronized void addMsgSender(String address, int port) {
    NodeClient newMsgSender= new NodeClient(address, port);
    if (!msgSenders.contains(newMsgSender)) {
      msgSenders.add(newMsgSender);
    }
  }

  @Override
  public synchronized void addMsgSender(int port) {
    NodeClient newMsgSender= new NodeClient(selfClientAddress, port);
    if (!msgSenders.contains(newMsgSender)) {
      msgSenders.add(newMsgSender);
    }
  }


  @Override
  public List<NodeClient> getMsgSenders() {
    List<NodeClient> sendersCopy = new ArrayList<>(msgSenders);
    return sendersCopy;
  }

  @Override
  public ContactMap getContacts() {
    return contacts.copy();
  }

  /**
   * Check if two Nodes are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two Nodes have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractNode that = (AbstractNode) o;
    return contacts.equals(that.contacts)
            && msgSenders.equals(that.msgSenders)
            && selfClientAddress.equals(that.selfClientAddress);
  }

  /**
   * Generate the hashCode of this Node.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(contacts, msgSenders, selfClientAddress);
  }
}
