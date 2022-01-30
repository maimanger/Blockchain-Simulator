package transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import utils.BlockchainUtil;
import utils.UTXOMap;


/**
 * This class represents a Transaction in the blockchain.
 * A Transaction contains ID, recipient address, value, timeStamp, memo, outputs and state.
 */
public abstract class AbstractTransaction implements Transaction {

  protected String ID;
  protected String recipient;
  protected double value;
  protected long timeStamp; //as number of milliseconds since 1/1/1970.
  protected String memo;
  protected List<TransactionFlow> outputs = new ArrayList<>();
  protected TransactionState state;

  /**
   * Initialize all instance variables of this AbstractTransaction.
   *
   * @param recipient a given Wallet's PublicKey string of the recipient
   * @param value a double Transaction value
   * @param memo a memo string
   */
  public AbstractTransaction(String recipient, double value, String memo) {
    this.recipient = recipient;

    try {
      setValue(value);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      System.out.println("Transaction value is set to the default minimum.");
      this.value = MINIMUM_VALUE;
    }

    this.memo = memo;
    this.timeStamp = new Date().getTime();
    this.state = TransactionState.UNCONFIRMED;
  }


  /**
   * Set the value of this Transaction.
   *
   * @param value a given double Transaction value
   * @throws IllegalArgumentException if the given value is less than the required minimum value
   */
  private void setValue(double value) throws IllegalArgumentException {
    if (value < MINIMUM_VALUE) {
      throw new IllegalArgumentException("Transaction value is too small!");
    }
    this.value = value;
  }

  /**
   * Set the outputs of this Transaction.
   */
  protected void setOutputs() {
    this.outputs.add(new TransactionFlow(recipient, value, ID));
  }

  @Override
  public void confirm() {
    state = TransactionState.CONFIRMED;
  }

  @Override
  public void updateUTXO(UTXOMap utxoMap) {
    // add new outputs to UTXOMap
    for (TransactionFlow output : outputs) {
      utxoMap.put(output.getID(), output);
    }
  }

  @Override
  public boolean isSentTo(String recipient) {
    return this.recipient.equals(recipient);
  }

  @Override
  public boolean isSentBy(String sender) {
    return false;
  }

  // get a copy of outputs list, not the original list
  @Override
  public List<TransactionFlow> getOutputs() {
    // TransactionFlow is immutable, thus we can add it directly to a copy list
    return new ArrayList<>(outputs);
  }

  @Override
  public String getID() {
    return ID;
  }

  @Override
  public TransactionState getState() {
    return state;
  }

  @Override
  public String getRecipient() {
    return recipient;
  }

  @Override
  public double getValue() {
    return value;
  }

  /**
   * Calculate this Transaction's ID using Sha256 hashing algorithm.
   *
   * @return a hexadecimal ID string
   */
  protected String calculateID() {
    String data = recipient + value + memo + timeStamp;
    return BlockchainUtil.applySha256(data);
  }


  /**
   * Get the string representation of this Transaction.
   *
   * @return a formatted string
   */
  @Override
  public String toString() {
    return String.format("%n    Recipient Address: %s", recipient)
            + String.format("%n    Value: %.2f", value)
            + String.format("%n    Memo: \"%s\"", memo)
            + String.format("%n    Status: %s%n", state);
  }

  /**
   * Check if two Transactions are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two Transactions have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractTransaction that = (AbstractTransaction) o;
    return Math.abs(that.value - value) < 0.001
            && timeStamp == that.timeStamp
            && ID.equals(that.ID)
            && recipient.equals(that.recipient)
            && memo.equals(that.memo)
            && outputs.equals(that.outputs);
  }

  /**
   * Generate the hashCode of this Transaction.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(ID, recipient, value, timeStamp, memo, outputs, state);
  }
}
