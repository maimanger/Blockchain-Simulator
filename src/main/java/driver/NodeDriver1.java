package driver;

import controller.Controller;
import node.Node;
import node.PeerNode;
import view.NodeView;

public class NodeDriver1 {
  public static void main(String[] args) {
    Node n1 = new PeerNode("n1");
    n1.setSelfClientAddress("localhost");
    Controller n1Controller = new Controller(n1, 7777);
    n1Controller.setView(new NodeView());
  }
}
