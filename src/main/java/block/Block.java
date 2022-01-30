package block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import transaction.RewardTransaction;
import transaction.Transaction;
import utils.UTXOMap;
import utils.BlockchainUtil;
import wallet.Wallet;

/**
 * This class represents a block that contains hash, previous block's hash, list of transactions,
 * timeStamp, the merkle root of transactions hash codes, and a nonce.
 */
public class Block implements Serializable, Cloneable {

  public static final int BLOCK_MAX_CAPACITY = 5;

  private String hash;
  private String previousHash;
  private List<Transaction> transactions = new ArrayList<>();
  private long timeStamp; //as number of milliseconds since 1/1/1970.
  private String transactionsMerkleRoot;
  private int nonce;

  /**
   * Construct a Block with the given previous block's hash and the PublicKey string of the creator.
   *
   * @param previousHash a hexadecimal string represents the previous block's hash
   * @param creator the PublicKey string of this block's creator
   */
  public Block(String previousHash, String creator) {
    this.previousHash = previousHash;
    // the first transaction of this Block is the RewardTransaction to the creator
    transactions.add(new RewardTransaction(creator));
    hash = "";
    transactionsMerkleRoot = "";
    timeStamp = 0; // set up when start one mining of this block
    nonce = 0; // keep updating while mining, until find the POW solution
  }

  /**
   * Add a new Transaction to this Block.
   *
   * @param transaction a given Transaction
   * @param utxoMap an UTXOMap that is used to validate the new Transaction
   * @return a boolean value, which is true if the adding succeeds
   */
  public boolean addTransaction(Transaction transaction, UTXOMap utxoMap) {
    if (!transaction.outsideValidate(utxoMap)) {
      return false;
    } else {
      return transactions.add(transaction);
    }
  }

  /**
   * Collect new Transactions into this Block from a given Transaction pool.
   * Once a new Transaction is processed, no matter the adding succeeds or not, it will be removed
   * from the given pool.
   *
   * @param transactionPool a given list of Transactions
   * @param utxoMap an UTXOMap that is used to validate the new Transactions
   */
  public void collectTransactionsFromPool(List<Transaction> transactionPool, UTXOMap utxoMap) {
    // use the copy of transactionPool and UTXOMap, because the original might change during the iteration
    List<Transaction> transactionPoolCopy = new ArrayList<>();
    transactionPoolCopy.addAll(transactionPool);
    UTXOMap tempUTXO = utxoMap.copy();

    List<Transaction> processedPool = new ArrayList<>();

    for (Transaction transaction : transactionPoolCopy) {
      // before adding the new TX to the block, this TX must be valid under current tempUTXO
      boolean isAdded = this.addTransaction(transaction, tempUTXO);
      // after adding new TX, tempUTXO will update with this TX to avoid the double-spent
      transaction.updateUTXO(tempUTXO);
      processedPool.add(transaction);

/*      if (isAdded) {
        System.out.println("Transaction adding succeeded! " + transaction);
      } else {
        System.out.println("Transaction adding failed! " + transaction);
      }*/

      // if block has more than 5 transactions, stop adding
      if (transactions.size() >= BLOCK_MAX_CAPACITY) {
        break;
      }
    }
    // no matter adding succeeds or fails, remove all processed TXs from pool
    transactionPool.removeAll(processedPool);
  }

  /**
   * Update the timeStamp of this Block.
   */
  private void updateTimeStamp() {
    timeStamp = new Date().getTime();
  }

  /**
   * Produce the hash of this Block using Sha256 hash function.
   *
   * @return a resulting hexadecimal string
   */
  private String calculateHash() {
    String data = previousHash + timeStamp + nonce + transactionsMerkleRoot;
    return BlockchainUtil.applySha256(data);
  }

  /**
   * Set the merkle root of Transactions in this Block.
   */
  public void setTransactionsMerkleRoot() {
    transactionsMerkleRoot = BlockchainUtil.calculateMerkleRoot(transactions);
  }

  /**
   * Complete one mining of this Block. Before starting a mining, this Block should have collected
   * enough Transactions and updated the merkle root of these Transactions.
   * One mining means using a randomly generated nonce with other data in this Block
   * to generate a hash string. If this hash starts with a string of zeros with the size of a given
   * difficulty integer, the mining would succeed.
   *
   * @param difficulty a given integer that represents the number of the required prefix zeros
   * @param rand a Random object used to generate a random nonce
   * @return a boolean value, which is true if the mining succeeds
   */
  public boolean oneMining(int difficulty, Random rand) {
    updateTimeStamp(); // final timestamp will be the time when the block is mined successfully
    String target = "0".repeat(difficulty);
    nonce = rand.nextInt(Integer.MAX_VALUE);
    hash = calculateHash();
    return hash.substring(0, difficulty).equals(target);
  }

  /**
   * Process all Transactions in this Block, including confirm Trasactions and update a given UTXOMap.
   *
   * @param utxoMap a given UTXOMap to be updated with Transactions in this Block
   */
  public void processBlockTransactions(UTXOMap utxoMap) {
    for (Transaction t : transactions) {
      t.confirm();
      t.updateUTXO(utxoMap); // once a new block successfully mined, update local UTXO map;
    }
  }

  // update given wallet's transaction history,
  // if there is any TX in this block is sent to the wallet

  /**
   * Update a given Wallet's received transaction history with all Transactions in this Block.
   *
   * @param recipientWallet a given Wallet to be updated
   */
  public void updateReceivedTransactionHistoryOf(Wallet recipientWallet) {
    transactions.forEach(recipientWallet::updateReceivedTransactions);
  }

  // update given wallet's transaction history,
  // if there is any TX in this block is sent by or sent to the wallet

  /**
   * Update a given Wallet's transaction history with all Transactions in this Block, including
   * the sent by this Wallet and the received to this Wallet.
   *
   * @param wallet a given Wallet to be updated
   */
  public void updateTransactionHistoryOf(Wallet wallet) {
    transactions.forEach(wallet::updateSentAndRecievedTransactions);
  }

  /**
   * Validate the merkle root and the hash of this Block.
   *
   * @return a boolean value, which is true if this Block has valid hash
   */
  public boolean validateHashCalculation() {
    return transactionsMerkleRoot.equals(BlockchainUtil.calculateMerkleRoot(transactions))
            && hash.equals(calculateHash());
  }


  // if previous hash validation fails, there could be in cases as follows:
  // 1. fraud block, discard it
  // 2. local blockchain is too old to catch up with the new block, the latest block in local chain
  // might be one of the ancestors of the new block; Then we need to check the entire new blockchain
  // 3. new block is a fork of current blockchain, put it in the branch chain list of the node.PeerNode
  // 4. new block is not valid to current mainChain, but it is valid to one of the branch chains,
  //    thus we can use branchChain.verifyNewBlock to check

  /**
   * Self verification of this Block, including verifying the Transactions, hash calculation, and POW.
   *
   * @param utxoMap a given UTXOMap used to verify Transactions
   * @param difficulty a given integer to verify if the hash satisfies POW rule
   * @return a boolean value, which is true if this Block is valid
   */
  public boolean verifySelf(UTXOMap utxoMap, int difficulty) {
    return verifySelfTransactions(utxoMap) // transactions in this block are valid
            && validateHashCalculation()  // self hash in this block is valid
            && validatePOW(difficulty); // POW in this block is valid
  }

  /**
   * Self hash verification of this Block, including the hash calculation and POW.
   *
   * @param difficulty a given integer to verify if the hash satisfies POW rule
   * @return a boolean value, which is true if this Block is valid
   */
  public boolean verifySelfHash(int difficulty) {
    return validateHashCalculation() && validatePOW(difficulty);
  }


  /**
   * Validate the POW solution in this Block.
   *
   * @param difficulty a given integer to verify if the hash satisfies POW rule
   * @return true if the hash of this Block matches the target required by POW
   */
  private boolean validatePOW(int difficulty) {
    String target = "0".repeat(difficulty);
    return hash.substring(0, difficulty).equals(target);
  }


  /**
   * Verify the Transactions of this Block.
   *
   * @param utxoMap a given UTXOMap used to verify Transactions
   * @return a boolean value, which is true if all Transactions in this Block are valid
   */
  private boolean verifySelfTransactions(UTXOMap utxoMap) {
    for (Transaction transaction : this.getTransactions()) {
      if (!transaction.outsideValidate(utxoMap)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Get the hash string of this Block.
   *
   * @return a hash string
   */
  public String getHash() {
    return hash;
  }

  /**
   * Get the hash string of the previous Block.
   *
   * @return a hash string
   */
  public String getPreviousHash() {
    return previousHash;
  }

  /**
   * Get the list of Transactions of this Block.
   *
   * @return a list of Transactions
   */
  public List<Transaction> getTransactions() {
    List<Transaction> copy = new ArrayList<>();
    transactions.forEach(transaction -> copy.add(transaction.copy()));
    return copy;
  }

  /**
   * Get the timeStamp of this Block.
   *
   * @return a long value represents the timeStamp
   */
  public long getTimeStamp() {
    return timeStamp;
  }

  /**
   * Get the nonce of this Block.
   *
   * @return an integer nonce
   */
  public int getNonce() {
    return nonce;
  }

  /**
   * Get a string representation of this Block.
   *
   * @return a formatted string
   */
  @Override
  public String toString() {
    String delimiter = "*".repeat(120);
    return String.format("%n{%nHash: %s,%n", hash)
            + String.format("Previous Hash: %s,%n", previousHash)
            + String.format("Time Stamp: %s,%n", timeStamp)
            + String.format("Nonce: %s,%n", nonce)
            + String.format("Transactions: %s%n%s}", transactions, delimiter);
  }

  /**
   * Get a clone of this Block.
   *
   * @return a Block copy
   */
  @Override
  public Block clone() {
    Block copy = null;
    try {
      copy = (Block) super.clone();
      List<Transaction> transactionsCopy = new ArrayList<>();
      transactions.forEach(transaction -> transactionsCopy.add(transaction.copy()));
      copy.transactions = transactionsCopy;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return copy;
  }

  /**
   * Check if two Blocks are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two Blocks have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Block block = (Block) o;
    return timeStamp == block.timeStamp
            && nonce == block.nonce
            && hash.equals(block.hash)
            && previousHash.equals(block.previousHash)
            && transactions.equals(block.transactions)
            && transactionsMerkleRoot.equals(block.transactionsMerkleRoot);
  }

  /**
   * Generate the hashCode of this Block.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(hash, previousHash, transactions, timeStamp, transactionsMerkleRoot, nonce);
  }
}
