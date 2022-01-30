package utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents the information of the Client and its corresponding Server, including
 * an address and a port.
 */
public class ServerInfo implements Serializable {
  private String address;
  private int port;

  /**
   * Construct a ServerInfo object with the given address and port.
   *
   * @param address an address string
   * @param port a port integer
   */
  public ServerInfo(String address, int port) {
    this.address = address;
    this.port = port;
  }

  /**
   * Get the address of this ServerInfo
   *
   * @return an address string
   */
  public String getAddress() {
    return address;
  }

  /**
   * Get the port of this ServerInfo
   *
   * @return a port integer
   */
  public int getPort() {
    return port;
  }

  /**
   * Check if two ServerInfos are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two ServerInfos have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ServerInfo that = (ServerInfo) o;
    return port == that.port && address.equals(that.address);
  }

  /**
   * Generate the hashCode of this ServerInfo.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(address, port);
  }
}
