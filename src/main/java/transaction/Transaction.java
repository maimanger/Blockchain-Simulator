package transaction;

import java.io.Serializable;
import java.util.List;
import utils.UTXOMap;

/**
 * This interface represents a Transaction in the blockchain.
 */
public interface Transaction extends Serializable {
  /**
   * An enum type represents the Transaction's state.
   */
  enum TransactionState { UNCONFIRMED, CONFIRMED
  }

  double MINIMUM_INPUT = 1.0;
  double BLOCK_REWARD = 10.0;
  double MINIMUM_VALUE = 0.1;

  /**
   * Validate this Transaction by clients except the sender, including the verification of the
   * signature, legal inputs and outputs.
   *
   * @param utxoMap a given UTXOMap to check if inputs in this Transaction is not spent
   * @return a boolean value, which is true if this Transaction is valid
   */
  boolean outsideValidate(UTXOMap utxoMap);

  /**
   * Check if this Transaction has minimum inputs and is not overspent.
   *
   * @return a boolean value, which is true if this Transaction is valid
   */
  boolean insideValidate();

  /**
   * Set the state of this Transaction to confirmed.
   */
  void confirm();

  /**
   * Update a given UTXOMap based on all inputs and outputs in this Transaction.
   *
   * @param utxoMap a given UTXOMap to be updated
   */
  void updateUTXO(UTXOMap utxoMap);

  /**
   * Get a copy of this Transaction.
   *
   * @return a copy of this Transaction
   */
  Transaction copy();

  /**
   * Check if this Transaction has the same recipient as the given one.
   *
   * @param recipient a Wallet's PublicKey string belongs to the recipient
   * @return a boolean value, which is true if this Transaction is sent to the given recipient
   */
  boolean isSentTo(String recipient);

  /**
   * Check if this Transaction has the same sender as the given one.
   *
   * @param sender a Wallet's PublicKey string belongs to the sender
   * @return a boolean value, which is true if this Transaction is sent by the given sender
   */
  boolean isSentBy(String sender);

  /**
   * Get all outputs in this Transaction.
   *
   * @return a list of TransactionFlow represents the outputs
   */
  List<TransactionFlow> getOutputs();

  /**
   * Get the ID string of this Transaction.
   *
   * @return an ID string
   */
  String getID();

  /**
   * Get the state of this Transaction.
   *
   * @return a TransactionState value
   */
  TransactionState getState();

  /**
   * Get the recipient address of this Transaction.
   *
   * @return a Wallet's PublicKey string represents the address of the given recipient
   */
  String getRecipient();

  /**
   * Get the value of this Transaction.
   *
   * @return a double value
   */
  double getValue();
}

