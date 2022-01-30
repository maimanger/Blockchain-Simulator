package utils;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * This class represents a contact information, which maps a contact name to a wallet address (PublicKey).
 */
public class ContactMap implements Serializable {

  private Map<String, String> contactMap;

  /**
   * Construct a ContactMap object, containing a Map as its instance variable.
   */
  public ContactMap() {
    contactMap = new TreeMap<>();
  }

  /**
   * Add a nameString-keyString pair into the contact map.
   *
   * @param name a given name string
   * @param publicKey a given hexadecimal string represents the PublicKey of the contact's wallet
   */
  public void add(String name, String publicKey) {
    contactMap.put(name, publicKey);
  }

  /**
   * Remove the entry mapped with the given key from the contact map.
   *
   * @param name a given name string as the removed key
   */
  public void remove(String name) {
    contactMap.remove(name);
  }


  /**
   * Get the contact map of this object.
   *
   * @return a Map<String, String> object represents the contact map
   */
  public Map<String, String> getMap() {
    Map<String, String> mapCopy = new TreeMap<>();
    contactMap.forEach(mapCopy::put);
    return mapCopy;
  }

  /**
   * Get the copy of this ContactMap.
   *
   * @return a copy of this ContactMap
   */
  public ContactMap copy() {
    ContactMap copy = new ContactMap();
    copy.contactMap = this.getMap();
    return copy;
  }

  /**
   * Get a string representation of this Contact Map.
   *
   * @return a formatted string
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    contactMap.forEach((k, v) -> result.append(k + ": " + v + "\n"));
    return result.toString();
  }

  /**
   * Check if two ContactMaps are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two ContactMaps have the same map-members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ContactMap that = (ContactMap) o;
    return contactMap.equals(that.contactMap);
  }

  /**
   * Generate the hashCode of this ContactMap.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(contactMap);
  }
}
