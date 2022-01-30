package driver;

import java.util.Arrays;

import node.HostNodeImpl;

public class HostDriver {
  public static void main(String[] args) {
    // HostNode operation steps:
// 1. create an empty HostNode
// 2. add peerNode clients/msgSenders to this HostNode
// 3. add peerNode connection ports to this HostNode
// 4. start: send peer ports, send contact request, send new contactMap

    HostNodeImpl host = new HostNodeImpl();
    host.setSelfClientAddress("localhost");

    // add msgSenders(Clients) to n1, n2, n3
    host.addMsgSender(7777);
    host.addMsgSender(7778);
    host.addMsgSender(7779);

    // add peerNode's server and client ports
    host.setPeerPortsMap(
            7777, Arrays.asList(6666, 6668), Arrays.asList(6667, 6670));
    host.setPeerPortsMap(
            7778, Arrays.asList(6667, 6669), Arrays.asList(6666, 6671));
    host.setPeerPortsMap(
            7779, Arrays.asList(6670, 6671), Arrays.asList(6668, 6669));

    // 1. send peerClientPorts and peerServerPorts to each peerNode's hostServer,
    // let hostServer create and run new peerServers, let peerNode add new peerClients
    // 2. send Contact request to each peerNode's hostServer,
    // then update hostNode's contactMap based on replied contact
    // 3. send new contactMap to each peerNode's hostServer, let peerNode update its contactMap
    host.start();
  }
}
