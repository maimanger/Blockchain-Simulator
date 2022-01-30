package driver;

import controller.Controller;
import node.Node;
import node.PeerNode;
import view.NodeView;

public class NodeDriver2 {
  public static void main(String[] args) {
    Node n2 = new PeerNode("n2");
    n2.setSelfClientAddress("localhost");
    Controller n1Controller = new Controller(n2, 7778);
    n1Controller.setView(new NodeView());
  }
}
