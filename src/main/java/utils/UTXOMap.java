package utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import transaction.TransactionFlow;

/**
 * This class represents a map of all unspent transaction ouputs(UTXO) in a blockchain.
 * It maps the output TransactionFlow's ID to the output Transaction itself.
 * For each Transaction, the outputs will be added to the UTXO map as a new UTXO. Such output
 * TransactionFlow will become input in the new Transaction, in which the Transaction sender
 * is the same as the outputs recipient.
 * Once an UTXO become a new Transaction input, it is marked as "spent" and will be
 * removed from the UTXO map.
 */
public class UTXOMap implements Serializable {
  private Map<String, TransactionFlow> utxoMap;

  /**
   * Construct an empty UTXOMap object.
   */
  public UTXOMap() {
    this.utxoMap = new HashMap<>();
  }

  /**
   * Get the map from this UTXOMap.
   *
   * @return a Map<String, TransactionFlow> contained in this UTXOMap
   */
  public Map<String, TransactionFlow> getMap() {
    Map<String, TransactionFlow> mapCopy = new HashMap<>();
    this.utxoMap.forEach(mapCopy::put);
    return mapCopy;
  }

  /**
   * Put a new UTXO entry in this UTXOMap.
   *
   * @param ID a given ID string of the new TransactionFlow
   * @param transactionFlow a given TransactionFlow as the new UTXO
   */
  public void put(String ID, TransactionFlow transactionFlow) {
    utxoMap.put(ID, transactionFlow);
  }

  /**
   * Remove an UTXO entry from this UTXOMap.
   *
   * @param ID a given ID string of the removed TransactionFlow
   */
  public void remove(String ID) {
    utxoMap.remove(ID);
  }

  /**
   * Get the copy of this UTXOMap.
   *
   * @return a copy of this UTXOMap
   */
  public UTXOMap copy() {
    UTXOMap utxoMapCopy = new UTXOMap();
    this.utxoMap.forEach(utxoMapCopy::put); // deep copy, because TransactionFlow is immutable
    return utxoMapCopy;
  }

  /**
   * Check if a given UTXO ID is contained in this UTXOMap.
   *
   * @param ID a given UTXO ID string
   * @return a boolean value, which is true if the given ID is contained in this UTXOMap
   */
  public boolean containsKey(String ID) {
    return utxoMap.containsKey(ID);
  }

  /**
   * Calculate the sum of all UTXOs in this UTXOMap.
   *
   * @return a double value, represents the sum of all UTXOs
   */
  public double sum() {
    return utxoMap.values().stream().mapToDouble(TransactionFlow:: getValue).sum();
  }

  /**
   * Get a string representation of this UTXOMap.
   *
   * @return a formatted string representation
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    for (TransactionFlow each : utxoMap.values()) {
      result.append(each.toString());
    }
    return result.toString();
  }

  /**
   * Check if two UTXOMaps are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two UTXOMaps have the same map-members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UTXOMap otherMap = (UTXOMap) o;
    return utxoMap.equals(otherMap.utxoMap);
  }

  /**
   * Generate the hashCode of this UTXOMap.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(utxoMap);
  }
}
