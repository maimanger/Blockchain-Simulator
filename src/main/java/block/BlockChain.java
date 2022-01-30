package block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import transaction.Transaction;
import utils.UTXOMap;
import wallet.Wallet;

/**
 * This class represents a blockchain, which contains a list of blocks.
 * Each block has a previousHash attribute referring to the hash of the previous block.
 */
public class BlockChain implements Serializable {
  private List<Block> blockChain;

  /**
   * Construct an empty BlockChain.
   */
  public BlockChain() {
    blockChain = new ArrayList<>();
  }

  /**
   * Add a new Block to this BlockChain. The given new Block must be validated.
   *
   * @param newBlock a new Block to be added to this BlockChain
   * @return a boolean value, which is true if the adding succeeds
   */
  public boolean addBlock(Block newBlock) {
    return blockChain.add(newBlock);
  }

  /**
   * Get the last Block of this BlockChain. If the BlockChain is empty, return null.
   *
   * @return the last Block or null
   */
  public Block getLastBlock() {
    return blockChain.isEmpty() ? null : blockChain.get(blockChain.size() - 1);
  }

  /**
   * Remove the last Block of this BlockChain.
   */
  public void removeLastBlock() {
    if (!blockChain.isEmpty()) {
      blockChain.remove(blockChain.size() - 1);
    }
  }

  /**
   * Check if this BlockChain contains a given Block.
   *
   * @param block a given Block
   * @return a boolean value, which is true if the given Block exist
   */
  public boolean contains(Block block) {
    return blockChain.contains(block);
  }

  /**
   * Get the size of this BlockChain.
   *
   * @return an integer value of the size
   */
  public int size() {
    return blockChain.size();
  }

  /**
   * Verify a given new Block to this BlockChain.
   *
   * @param newBlock a given Block
   * @param utxoMap a given UTXOMap used to verify the new Block's Transactions
   * @param difficulty a given integer used to verify if the hash satisfies POW rule
   * @return a boolean value, which is true if the new Block is valid to this BlockChain
   */
  public boolean verifyNewBlock(Block newBlock, UTXOMap utxoMap, int difficulty) {
    return newBlock.verifySelf(utxoMap, difficulty) && verifyPreviousBlockHash(newBlock);
  }

  /**
   * Verify the previousHash in the given new Block.
   *
   * @param newBlock a given Block
   * @return a boolean value, which is true if the previousHash of the new Block
   *         matches the last Block's hash in this BlockChain
   */
  private boolean verifyPreviousBlockHash(Block newBlock) {
    return newBlock.getPreviousHash().equals(
            getLastBlock() == null ? "0" : getLastBlock().getHash());
  }

  /**
   * Verify this whole BlockChain with the given difficulty number.
   * Check if each block in the chain has valid hash and legal Transactions.
   *
   * @param difficulty a given integer used to verify if the block hash satisfies POW rule
   * @return a boolean value, which is true if this BlockChain is valid
   */
  public boolean verifyChain(int difficulty) {
    Block current, next;
    String target = "0".repeat(difficulty);
    UTXOMap tempUTXOMap = new UTXOMap();

    for (int i = 0; i < blockChain.size(); i++) {
      current = blockChain.get(i);
      next = i < blockChain.size() - 1 ? blockChain.get(i + 1) : null;

      // 1. Validate the hash of each Block:
      // previousHash in next block == hash in the current block
      if ((next != null) && (!current.getHash().equals(next.getPreviousHash()))) {
        System.out.println("PreviousHash validation failed!");
        return false;
        // validate self hash
      } else if (!current.validateHashCalculation()) {
        System.out.println("SelfHash validation failed!");
        return false;
        // validate POW
      } else if (!current.getHash().substring(0, difficulty).equals(target)) {
        System.out.println("POW validation failed");
        return false;

        // 2. Validate Transactions of each Block
        // using an updating tempUTXOMap instead of the real map
      } else if (!verifyChainTransactions(i, tempUTXOMap)) {
        System.out.println("TX validation failed");
        return false;
      }

/*      // 3. Confirm all Transactions and Update the temp UTXOMap in each Block iteration
      current.processBlockTransactions(tempUTXOMap);*/
    }
    return true;
  }

  /**
   * Verify the Transactions of a Block in the given position of this BlockChain.
   *
   * @param blockIndex the index number of the target Block in this BlockChain
   * @param tempUTXOMap a given UTXOMap used to verify Transactions
   * @return a boolean value, which is true if all Transactions are valid
   */
  private boolean verifyChainTransactions(int blockIndex, UTXOMap tempUTXOMap) {
    Block currentBlock = blockChain.get(blockIndex);
    for (Transaction transaction : currentBlock.getTransactions()) {
      if (!transaction.outsideValidate(tempUTXOMap)) {
        return false;
      }
      transaction.updateUTXO(tempUTXOMap);
    }
    return true;
  }

  /**
   * Generate an UTXOMap based on this BlockChain.
   *
   * @return a resulting UTXOMap
   */
  public UTXOMap generateUTXOMap() {
    UTXOMap newMap = new UTXOMap();
    for (Block block : blockChain) {
      for (Transaction transaction : block.getTransactions()) {
        transaction.updateUTXO(newMap);
      }
    }
    return newMap;
  }

  /**
   * Reset the Transaction history of a given Wallet based on this BlockChain.
   *
   * @param wallet an updated Wallet
   */
  public void resetTransactionHistoryOf(Wallet wallet) {
    // reset given wallet's TX history to empty
    wallet.resetTransactionHistory();
    // looping through the blockchain, update TX history based on each block's TXs
    for (Block block : blockChain) {
      block.updateTransactionHistoryOf(wallet);
    }
  }

  /**
   * Get the string representation of this BlockChain.
   *
   * @return a formatted string
   */
  @Override
  public String toString() {
    return "HEIGHT = " + blockChain.size() + "\n" + blockChain;
  }

  /**
   * Get the copy of this BlockChain.
   *
   * @return a BlockChain copy
   */
  public BlockChain copy() {
    BlockChain chainCopy = new BlockChain();
    blockChain.forEach(block -> chainCopy.addBlock(block.clone()));
    return chainCopy;
  }

  /**
   * Check if two BlockChains are equal.
   *
   * @param o the object to be compared to
   * @return a boolean value, which is true if two BlockChains have the same members
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BlockChain that = (BlockChain) o;
    return blockChain.equals(that.blockChain);
  }

  /**
   * Generate the hashCode of this BlockChain.
   *
   * @return a hashCode integer
   */
  @Override
  public int hashCode() {
    return Objects.hash(blockChain);
  }
}
