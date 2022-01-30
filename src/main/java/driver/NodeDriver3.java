package driver;

import controller.Controller;
import node.Node;
import node.PeerNode;
import view.NodeView;

public class NodeDriver3 {
  public static void main(String[] args) {
    Node n3 = new PeerNode("n3");
    n3.setSelfClientAddress("localhost");
    Controller n1Controller = new Controller(n3, 7779);
    n1Controller.setView(new NodeView());
  }
}
