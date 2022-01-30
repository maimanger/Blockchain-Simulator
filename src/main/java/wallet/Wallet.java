package wallet;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import transaction.NormalTransaction;
import transaction.Transaction;
import transaction.TransactionFlow;
import utils.UTXOMap;
import utils.BlockchainUtil;


/**
 * A class represents a Wallet, containing PublicKey, PrivateKey, transaction history and a balance.
 */
public class Wallet implements Cloneable {
  private PrivateKey privateKey;
  private PublicKey publicKey;
  // contains both sending and received TXs
  // sending TX updated in Wallet.sent() method
  // received TX updated in PeerNode.updateBlockChain(BLock, int) method
  private List<Transaction> transactionHistory;
  private double balance;

  static {
    try {
      Security.addProvider(new BouncyCastleProvider());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Construct a Wallet object, initialize its PublicKey, PrivateKey, transaction history and balance.
   */
  public Wallet() {
    try {
      setKeys();
    } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
            | NoSuchProviderException e) {
      e.printStackTrace();
    }
    transactionHistory = new ArrayList<>();
    balance = 0.0;
  }

  /**
   * Set PublicKey and PrivateKey of this Wallet.
   *
   * @throws NoSuchAlgorithmException if the given cryptography algorithm dose not exist
   * @throws NoSuchProviderException if the given cryptography provider dose not exist
   * @throws InvalidAlgorithmParameterException if the given parameters to the cryptography algorithm is invalid
   */
  private void setKeys() throws NoSuchAlgorithmException, NoSuchProviderException,
          InvalidAlgorithmParameterException {
    Security.addProvider(new BouncyCastleProvider());
    // Initialize the key generator and generate a KeyPair, by using Elliptic-curve cryptography
    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
    keyGenerator.initialize(new ECGenParameterSpec("prime192v1"),
            SecureRandom.getInstance("SHA1PRNG"));
    KeyPair keys = keyGenerator.generateKeyPair();
    privateKey = keys.getPrivate();
    publicKey = keys.getPublic();
  }

  /**
   * Update the balance of this Wallet by a given UTXOMap.
   *
   * @param utxoMap an UTXOMap contains all UTXOs belong to this Wallet
   */
  public void updateBalance(UTXOMap utxoMap) {
    balance = utxoMap.getMap().values().stream()
            .filter(transactionFlow -> transactionFlow.isOwnedBy(getPublicKeyStr()))
            .mapToDouble(TransactionFlow::getValue).sum();
  }

  /**
   * Send funds from this Wallet to given recipient.
   *
   * @param recipient a Wallet's PublicKey string of the recipient
   * @param value a double value
   * @param memo a memo string
   * @param utxoMap an UTXOMap that contains all UTXOs belong to this Wallet
   * @return a Transaction started by this Wallet owner
   * @throws IllegalArgumentException if the generated Transaction is invalid
   */
  public Transaction send(String recipient, double value, String memo,
                          UTXOMap utxoMap) throws IllegalArgumentException {
    // create a new Transaction
    Transaction newTX = new NormalTransaction(getPublicKeyStr(), recipient, value, memo,
            generateInputs(utxoMap, value));

    // sign the Transaction
    ((NormalTransaction) newTX).sign(privateKey);

    // self-Validate the Transaction
    if (!newTX.insideValidate()) {
      throw new IllegalArgumentException("Invalid transaction!");
    }

    // add new TX to wallet transaction history
    transactionHistory.add(newTX);
    return newTX;
  }

  /**
   * Update the transaction history of this Wallet by a given new Transaction,
   * if it is sent to this Wallet.
   *
   * @param newTransaction a Transaction to be updated in the transaction history of this Wallet
   */
  public void updateReceivedTransactions(Transaction newTransaction) {
    if (newTransaction.isSentTo(getPublicKeyStr())) {
      transactionHistory.add(newTransaction);
    }
  }

  /**
   * Reset the transaction history of this Wallet to an empty list.
   */
  public void resetTransactionHistory() {
    transactionHistory = new ArrayList<>();
  }

  /**
   * Update the transaction history of this Wallet by a given new Transaction, if it is sent to
   * or sent by this Wallet.
   *
   * @param newTransaction a Transaction to be updated in the transaction history of this Wallet
   */
  public void updateSentAndRecievedTransactions(Transaction newTransaction) {
    String keyStr = getPublicKeyStr();
    if (newTransaction.isSentBy(keyStr) || newTransaction.isSentTo(keyStr)) {
      transactionHistory.add(newTransaction);
    }
  }

  /**
   * Generate inputs from a given UTXOMap.
   *
   * @param utxoMap an UTXOMap that contains all UTXOs belong to this Wallet
   * @param transactionValue a given double value of a new Transaction
   * @return an UTXOMap represents the necessary inputs to a new Transaction
   */
  private UTXOMap generateInputs(UTXOMap utxoMap, double transactionValue) {
    UTXOMap inputs = new UTXOMap();
    double inputsSum;

    for (Map.Entry<String, TransactionFlow> each : utxoMap.getMap().entrySet()) {
      String eachKey = each.getKey();
      TransactionFlow eachFlow = each.getValue();
      if (eachFlow.isOwnedBy(getPublicKeyStr())) {
        inputs.put(eachKey, eachFlow);
      }
      // if the inputs sum is larger than the new Transaction's value, stop adding UTXO to inputs
      inputsSum = inputs.sum();
      if (inputsSum > transactionValue) {
        break;
      }
    }

    return inputs;
  }

  /**
   * Get the PublicKey of this Wallet.
   *
   * @return a PublicKey object
   */
  public PublicKey getPublicKey() {
    return publicKey;
  }

  /**
   * Get the PublicKey string of this Wallet.
   *
   * @return a hexadecimal strign represents the PublicKey
   */
  public String getPublicKeyStr() {
    return BlockchainUtil.keyToString(publicKey);
  }

  /**
   * Get the balance of this Wallet.
   *
   * @return a double value of the balance
   */
  public double getBalance() {
    return balance;
  }

  /**
   * Get the transaction history of this Wallet.
   *
   * @return a list of Transactions
   */
  public List<Transaction> getTransactionHistory() {
    List<Transaction> historyCopy = new ArrayList<>();
    transactionHistory.forEach(transaction -> historyCopy.add(transaction.copy()));
    return historyCopy;
  }

  /**
   * Get the string representation of this Wallet.
   *
   * @return a formatted string
   */
  @Override
  public String toString() {
    return String.format("wallet.Wallet: %s%n", getPublicKeyStr())
            + String.format("Current Balance: %.2f%n", balance)
            + String.format("transaction.Transaction History: %s", transactionHistory);
  }

  /**
   * Get the clone of this Wallet.
   *
   * @return a Wallet copy
   */
  @Override
  public Wallet clone() {
    Wallet copy = null;
    try {
      copy = (Wallet) super.clone();
      List<Transaction> transactionHistoryCopy = new ArrayList<>();
      transactionHistory.forEach(transaction -> transactionHistoryCopy.add(transaction.copy()));
      copy.transactionHistory = transactionHistoryCopy;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return copy;
  }

  /**
   * Check if two Wallets are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two Wallets have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Wallet other = (Wallet) o;
    return Math.abs(other.balance - balance) < 0.001
            && privateKey.equals(other.privateKey)
            && publicKey.equals(other.publicKey)
            && transactionHistory.equals(other.transactionHistory);
  }

  /**
   * Generate the hashCode of this Wallet.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(privateKey, publicKey, transactionHistory, balance);
  }


}
