package transaction;

import java.io.Serializable;
import java.util.Objects;
import utils.BlockchainUtil;

/**
 * This class represents a TransactionFlow, that contains an ID, owner, value and
 * its parent Transaction's ID.
 */
public class TransactionFlow implements Serializable {
  private final String id;
  private final String owner;
  private final double value;
  private final String transactionID;

  /**
   * Construct a TransactionFlow object by the given owner, value and parent Transaction's ID.
   *
   * @param owner a Wallet's PublicKey string represents the owner address
   * @param value a double value
   * @param transactionID an ID string of the parent Transaction
   */
  public TransactionFlow(String owner, double value, String transactionID) {
    this.owner = owner;
    this.value = value;
    this.transactionID = transactionID;
    this.id = calculateID();
  }

  /**
   * Calculate the ID of this TransactionFlow.
   *
   * @return an ID string
   */
  private String calculateID() {
    String data = owner + value + transactionID;
    return BlockchainUtil.applySha256(data);
  }

  /**
   * Check if this TransactionFlow is owned by a given owner.
   *
   * @param owner a Wallet's PublicKey string that represents the given owner address
   * @return a boolean value, which is true if this TransactionFlow is owned by the given owner
   */
  public boolean isOwnedBy(String owner) {
    return this.owner.equals(owner);
  }

  /**
   * Get the value of this TransactionFlow.
   *
   * @return a double value
   */
  public double getValue() {
    return value;
  }

  /**
   * Get the ID of this TransactionFlow.
   *
   * @return an ID string
   */
  public String getID() {
    return id;
  }

  /**
   * Get the string representation of this TransactionFlow.
   *
   * @return a formatted string
   */
  @Override
  public String toString() {
    return String.format("ID: %s%n", id)
            + String.format("Owner: %s%n", owner)
            + String.format("Value: %.2f%n", value)
            + String.format("TransactionID: %s%n", transactionID);
  }

  /**
   * Check if two TransactionFlows are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two TransactionFlows have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TransactionFlow that = (TransactionFlow) o;
    return Math.abs(this.value - that.value) < 0.001
            && this.id.equals(that.id)
            && this.owner.equals(that.owner)
            && this.transactionID.equals(that.transactionID);
  }

  /**
   * Generate the hashCode of this TransactionFlow.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, owner, value, transactionID);
  }
}
