package serverController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import controller.Controller;

/**
 * This class represents a NodeServer containing a server port, ServerSocket, an ExecutorService object,
 * and a ownerController.
 */
public abstract class AbstractServer implements NodeServer {
  protected int serverPort;
  protected ServerSocket serverSocket;
  protected ExecutorService executor;
  protected Controller ownerController;

  /**
   * Initialize the serverPort, serverSocket, executor and ownerController of this Server.
   *
   * @param serverPort a given port integer
   * @param ownerController a given Controller
   */
  public AbstractServer(int serverPort, Controller ownerController) {
    this.serverPort = serverPort;
    setServerSocket();
    executor = Executors.newCachedThreadPool();
    this.ownerController = ownerController;
  }

  /**
   * Set the ServerSocket of this Server.
   */
  private void setServerSocket() {
    try {
      serverSocket = new ServerSocket(serverPort);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Check if two Servers are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two Servers have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractServer that = (AbstractServer) o;
    return serverPort == that.serverPort
            && ownerController.equals(that.ownerController);
  }

  /**
   * Generate the hashCode of this Server.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(serverPort, ownerController);
  }

  /**
   * Get the string representation of this Server.
   *
   * @return a formatted string
   */
  @Override
  public String toString() {
    return "NodeServer port: " + serverPort;
  }

  /**
   * This class represents a ClientHandler that is used to process a new connected Client.
   */
  protected abstract class AbstractClientHandler implements Runnable {
    protected Socket clientSocket;
    protected ObjectInputStream serverIn;
    protected ObjectOutputStream serverOut;

    /**
     * Initialize the new client Socket, OutputStream and InputStream.
     *
     * @param clientSocket a given client Socket
     * @throws IOException if the stream initialization fails
     */
    public AbstractClientHandler(Socket clientSocket) throws IOException {
      this.clientSocket = clientSocket;
      serverIn = new ObjectInputStream(clientSocket.getInputStream());
      serverOut = new ObjectOutputStream(clientSocket.getOutputStream());
/*      String delimiter = "-".repeat(60);
      System.out.println(delimiter + "NEW MESSAGE!" + delimiter);*/
    }

    /**
     * Close the InputStream, OutputStream and the client Socket.
     *
     * @throws IOException if the close fails
     */
    protected void stopConnection() throws IOException {
      serverIn.close();
      serverOut.flush();
      serverOut.close();
      clientSocket.close();
    }

  }
}
