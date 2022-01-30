package controller;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import node.Node;
import serverController.*;
import view.NodeView;

/**
 * This class represents a Controller of a Node.
 */
public class Controller implements Starter {

  private Node ownerNode; // this is the model
  private NodeView nodeView;
  private CreateBlockRunnable createBlockRunnable;
  private StartTransactionRunnable startTransactionRunnable;
  private ShowBlockChainRunnable showBlockChainRunnable;
  private NodeServer hostServer;
  private ScheduledExecutorService executor;

  /**
   * Construct a Controller object with the given Node and a HostServer port.
   *
   * @param ownerNode a given Node
   * @param toHostServerPort a port integer
   */
  public Controller(Node ownerNode, int toHostServerPort) {
    this.ownerNode = ownerNode;
    this.createBlockRunnable = new CreateBlockRunnable(this);
    this.startTransactionRunnable = new StartTransactionRunnable(this);
    this.showBlockChainRunnable = new ShowBlockChainRunnable(this);
    this.hostServer = new HostServer(toHostServerPort, this);
    this.executor = Executors.newScheduledThreadPool(4);
  }

  /**
   * Set the view combined with this Controller.
   *
   * @param nodeView a given View object
   */
  public void setView(NodeView nodeView) {
    this.nodeView = nodeView;
    this.nodeView.addStarter(this);
    this.nodeView.addListener();
  }

  /**
   * Start running this Controller.
   */
  public void start() {
    executor.schedule(hostServer, 0, TimeUnit.MILLISECONDS);
    executor.scheduleAtFixedRate(createBlockRunnable,
            6000, 20000, TimeUnit.MILLISECONDS);
    executor.scheduleAtFixedRate(startTransactionRunnable,
            10000, 16000, TimeUnit.MILLISECONDS);
    executor.scheduleAtFixedRate(showBlockChainRunnable,
            7000, 4500, TimeUnit.MILLISECONDS);

  }

  /**
   * Get the ownerNode of this Controller.
   *
   * @return a Node object
   */
  public Node getNode() {
    return ownerNode;
  }

  /**
   * Get the view of this Controller.
   *
   * @return a Vies object
   */
  public NodeView getNodeView() {
    return nodeView;
  }

  /**
   * Check if two Controllers are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two Controllers have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Controller that = (Controller) o;
    return ownerNode.equals(that.ownerNode)
            && nodeView.equals(that.nodeView)
            && createBlockRunnable.equals(that.createBlockRunnable)
            && startTransactionRunnable.equals(that.startTransactionRunnable)
            && showBlockChainRunnable.equals(that.showBlockChainRunnable)
            && hostServer.equals(that.hostServer);
  }

  /**
   * Generate the hashCode of this Controller.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(ownerNode, nodeView, createBlockRunnable, startTransactionRunnable,
            showBlockChainRunnable, hostServer);
  }


}
