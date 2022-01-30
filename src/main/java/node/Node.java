package node;

import java.util.List;
import block.Block;
import block.BlockChain;
import transaction.Transaction;
import utils.UTXOMap;
import wallet.Wallet;

/**
 * This interface represents a Node.
 */
public interface Node extends HostNode {


  /**
   * Create a new block by this Node.
   *
   * @return a new Block
   */
  Block createBlock();

  /**
   * Update the Transaction pool in this Node with the given new Transaction.
   *
   * @param newTransaction a new Transaction to be added
   * @return true if the adding succeeds, otherwise false
   */
  boolean updateTransactionPool(Transaction newTransaction);

  /**
   * Update the BlockChain in this Node with the given new Block.
   *
   * @param newBlock a given Block
   * @return true if the updating succeeds, otherwise false
   */
  boolean updateBlockChain(Block newBlock);

  /**
   * Update the BlockChain in this Node with the given new BlockChain. If the new chain is valid
   * and longer than local chain, update local chain to the new one.
   * @param newBlockChain a given BlockChain
   * @return true if the updating succeeds, otherwise false
   */
  boolean updateBlockChain(BlockChain newBlockChain);

  /**
   * Verify the given new Block self-hash, omitting the Transactions and previousHash validation.
   *
   * @param newBlock a given Block
   * @return true if the new Block is valid, otherwise false
   */
  boolean verifyNewBlockSelfHash(Block newBlock);

  /**
   * Start a Transaction by this Node and set all information in the Transaction automatically.
   *
   * @return a new Transaction, or null if the generated new Transaction is invalid
   */
  Transaction startAutoTransaction();

  /**
   * Get the owner name of this Node.
   *
   * @return a name string
   */
  String getOwnerName();

  /**
   * Get the Wallet of this Node.
   *
   * @return a Wallet object
   */
  Wallet getWallet();

  /**
   * Get the BlockChain maintained in this Node.
   *
   * @return a BlockChain object
   */
  BlockChain getBlockChain();

  /**
   * Get the UTXOMap maintained in this Node..
   *
   * @return an UTXOMap object
   */
  UTXOMap getUTXOMap();

  /**
   * Get the Transaction pool of this Node.
   *
   * @return a list of Transactions
   */
  List<Transaction> getTransactionPool();

}
