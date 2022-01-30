package serverController;

/**
 * This interface represents a NodeServer, which is used to receive messages from other Node.
 */
public interface NodeServer extends Runnable {

  /**
   * Start running this NodeServer.
   */
  void run();
}
