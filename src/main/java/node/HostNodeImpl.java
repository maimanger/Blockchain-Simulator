package node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import utils.Message;

/**
 * This class represents a HostNode, that contains a peerServerPortsMap and a peerClientPortsMap.
 */
public class HostNodeImpl extends AbstractNode {
  private Map<Integer, List<Integer>> peerServerPortsMap;
  private Map<Integer, List<Integer>> peerClientPortsMap;

  // TODO: peer1-to-peer2 serverPort == peer2-to-peer1 clientPort, try to optimize the map setting
  // key = peerAToHostServerPort,
  // value = {peerBToHostServerPort: peerAToPeerBServerPort (OR peerBToPeerAClientPort)}
  // create peerA servers:
  // peerPortsMap.get(peerAToHostServerPort).forEach((k, v) -> peerAHostServer.addPeerServer(new PeerServer(v, peerAHostServer)))
  // create peerB clients:
  // peerPortsMap.forEach((k, v) -> peerBHostServer.getNode().addMsgSender(v.get(peerBToHostServerPort)))
  private Map<Integer, Map<Integer, Integer>> peerPortsMap;

  /**
   * Construct a HostNode object.
   */
  public HostNodeImpl() {
    super();
    peerServerPortsMap = new HashMap<>();
    peerClientPortsMap = new HashMap<>();
  }

  /**
   * Initialize the keys in peerServerPortsMap and peerClientPortsMap to the ports of peer clients,
   * which are used to send messages from this HostNode to PeerNodes.
   */
  private void setPeerPortsMapKeys() {
    for (NodeClient sender : msgSenders) {
      int peerToHostServerPort = sender.getServerEnd().getPort();
      peerServerPortsMap.putIfAbsent(peerToHostServerPort, new ArrayList<>());
      peerClientPortsMap.putIfAbsent(peerToHostServerPort, new ArrayList<>());
    }
  }

  /**
   * Put a Server port to the peerServerPort and map it to its owner PeerNode.
   *
   * @param peerToHostServerPort the port integer of the Client that this HostNode
   *                             uses to send message to one PeerNode, referring to the map's key
   * @param newPeerServerPort the port integer of the Server that the PeerNode uses to receive
   *                          message from another PeerNode
   */
  private void addPeerServerPort(int peerToHostServerPort, int newPeerServerPort) {
    peerServerPortsMap.get(peerToHostServerPort).add(newPeerServerPort);
  }

  /**
   * Put a Client port to the peerClientPort and map it to its owner PeerNode.
   *
   * @param peerToHostServerPort the port integer of the Client that this HostNode
   *                             uses to send message to one PeerNode, referring to the map's key
   * @param newPeerClientPort the port integer of the Client that the PeerNode uses to send
   *                          message to another PeerNode
   */
  private void addPeerClientPort(int peerToHostServerPort, int newPeerClientPort) {
    peerClientPortsMap.get(peerToHostServerPort).add(newPeerClientPort);
  }

  /**
   * Set the peerServerPortsMap and the peerClientPortsMap with the given ports list.
   *
   * @param peerToHostServerPort the port integer of the Client that this HostNode
   *                             uses to send message to one PeerNode, referring to the map's key
   * @param newPeerServerPortsList a list of ports of the Servers that the PeerNode uses to receive
   *                               message from other PeerNodes
   * @param newPeerClientPortsList a list of ports of the Clients that the PeerNode uses to send
   *                               message to other PeerNodes
   */
  public void setPeerPortsMap(int peerToHostServerPort, List<Integer> newPeerServerPortsList,
                              List<Integer> newPeerClientPortsList) {
    setPeerPortsMapKeys();
    newPeerServerPortsList.forEach(port -> addPeerServerPort(peerToHostServerPort, port));
    newPeerClientPortsList.forEach(port -> addPeerClientPort(peerToHostServerPort, port));
  }

  /**
   * Get the peerServerPortsMap of this HostNode.
   *
   * @return a map contains the peer Server ports
   */
  public Map<Integer, List<Integer>> getPeerServerPortsMap() {
    return peerServerPortsMap;
  }

  /**
   * Get the peerClientPortsMap of this HostNode.
   *
   * @return a map contains the peer Client ports
   */
  public Map<Integer, List<Integer>> getPeerClientPortsMap() {
    return peerClientPortsMap;
  }

  /**
   * Send the peer Server ports to each PeerNode.
   */
  private void sendPeerServerPorts() {
    boolean isDone;
    Message response;
    for (NodeClient sender : msgSenders) {
      isDone = false;
      while(!isDone) {
        try {
          response = sender.sendServerPort(peerServerPortsMap.get(sender.getServerEnd().getPort()));
          isDone = response == Message.SUCCESS;
        } catch (IOException | ClassNotFoundException e) {
  /*        System.out.println("Connection error. Failed connecting to " + sender.getServerEndStr());*/
        }
      }
    }
    System.out.println("PeerServerPorts sent!");
  }

  /**
   * Send the peer Client ports to each PeerNode.
   */
  private void sendPeerClientPorts() {
    boolean isDone;
    Message response;
    for (NodeClient sender : msgSenders) {
      isDone = false;
      while(!isDone) {
        try {
          response = sender.sendClientPort(peerClientPortsMap.get(sender.getServerEnd().getPort()));
          isDone = response == Message.SUCCESS;
        } catch (IOException | ClassNotFoundException e) {
/*          System.out.println("Connection error. Failed connecting to " + sender.getServerEndStr());*/
        }
      }
    }
    System.out.println("PeerClientPorts sent!");
  }

  /**
   * Send a request for contact information to each PeerNode, and add the replied Contact information
   * to the ContactMap of this HostNode.
   */
  private void requestContact() {
    boolean isDone;
    for (NodeClient sender : msgSenders) {
      isDone = false;
      while(!isDone) {
        try {
          String[] contact = sender.sendContactRequest();
          addContact(contact[0], contact[1]);
          isDone = true;
        } catch (IOException | ClassNotFoundException e) {
          System.out.println("Connection error. Failed connecting to " + sender.getServerEndStr());
        }
      }
    }
  }

  /**
   * Send the ContactMap maintained in this HostNode to each PeerNode.
   */
  private void sendContactMap() {
    boolean isDone;
    Message response;
    for (NodeClient sender : msgSenders) {
      isDone = false;
      while(!isDone) {
        try {
          response = sender.sendContactMap(contacts);
          if (response == Message.FAIL) {
            System.out.println("Cannot update ContactMap in " + sender.getServerEndStr());
          }
          isDone = true;
        } catch (IOException | ClassNotFoundException e) {
          System.out.println("Connection error. Failed sending to " + sender.getServerEndStr());
        }
      }
    }
    System.out.println("ContactMap updated!");
  }

  /**
   * HostNode start working, including sending server and client ports to PeerNodes, collecting
   * the Contact information of PeerNodes and sending the updated ContactMap to them.
   */
  public void start() {
    // 1. send peer-to-peer server ports
    sendPeerServerPorts();
    // 2. send peer-to-peer client ports
    sendPeerClientPorts();
    // 3. send contact request
    requestContact();
    // 4. send contactMap to PeerNodes
    sendContactMap();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
            && peerServerPortsMap.equals(((HostNodeImpl) o).peerServerPortsMap)
            && peerClientPortsMap.equals(((HostNodeImpl) o).peerClientPortsMap);
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), peerServerPortsMap, peerClientPortsMap);
  }

  // TODO: a HostNode could have other operations, like adding a new PeerNode to network or removing a PeerNode

}
