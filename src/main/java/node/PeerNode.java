package node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import block.Block;
import block.BlockChain;
import transaction.Transaction;
import transaction.NormalTransaction;
import utils.UTXOMap;
import wallet.Wallet;

/**
 * This class represents a PeerNode containing an ownerName, a wallet, a BlockChain, an UTXOMap, and
 * a Transaction pool.
 */
public class PeerNode extends AbstractNode implements Node {
  public static final int DIFFICULTY = 4;

  private String ownerName;
  private Wallet wallet;
  private BlockChain mainChain;
  private UTXOMap utxoMap;
  private List<Transaction> transactionPool;

  /**
   * Construct a PeerNode with the given ownerName.
   *
   * @param ownerName a name string
   */
  public PeerNode(String ownerName) {
    super();
    this.ownerName = ownerName;
    wallet = new Wallet(); // initialize a new Wallet with PublicKey and PrivateKey
    mainChain = new BlockChain();
    utxoMap = new UTXOMap();
    transactionPool = new ArrayList<>();
  }

  /**
   * Reset the UTXOMap maintained in this PeerNode to be consistent with its BlockChain.
   */
  private void resetUTXOMap() {
    utxoMap = mainChain.generateUTXOMap();
  }


  @Override
  public Block createBlock() {
    // 1. create a new empty block
    String previousHash = mainChain.size() == 0 ? "0" : mainChain.getLastBlock().getHash();
    Block newBlock = new Block(previousHash, wallet.getPublicKeyStr());

    //2. add transactions to this new block by using for-each loop,
    // if transactionPool is empty, automatically skip this step
    newBlock.collectTransactionsFromPool(transactionPool, utxoMap);

    // 3. after adding transactions, calculate the merkle root of them
    newBlock.setTransactionsMerkleRoot();

    // 4. start mining this new block
    Random rand = new Random(); // generate random integer nonce in POW
    int startingSize = mainChain.size(); // remember the original mainChain size
    boolean isMined;
    // keep looping while new block hasn't been mined and the mainChain hasn't updated
    do {
      isMined = newBlock.oneMining(DIFFICULTY, rand);
    } while (!isMined && startingSize == mainChain.size());

    // 5. if new block is mined successfully, updating the local mainChain, UTXOMap, PeerNode's wallet,
    // and return this new block
    if (isMined) {
      mainChain.addBlock(newBlock);
      newBlock.processBlockTransactions(utxoMap);
      // update TX history of the wallet in this PeerNode (Miner),
      newBlock.updateReceivedTransactionHistoryOf(wallet);
      // update wallet's balance based on new local UTXOMap
      wallet.updateBalance(utxoMap);
      return newBlock;
    } else {
      System.out.println("Mining was interrupted!");
      return null;
    }
  }

  @Override
  public synchronized boolean updateTransactionPool(Transaction newTransaction) {
    if (newTransaction == null) {
      return false;
    } else if (transactionPool.contains(newTransaction)) {
      return false;
    } else {
      return transactionPool.add(newTransaction);
    }
  }

  @Override
  public synchronized boolean updateBlockChain(Block newBlock) {
    boolean isUpdated = false;
    // 1. a regular valid newBlock added to the mainChain
    // -- add the new Block to the mainChain, then confirm all new TXs and update the local UTXOMap
    if (mainChain.verifyNewBlock(newBlock, utxoMap, DIFFICULTY)) {
      isUpdated = mainChain.addBlock(newBlock);
      newBlock.processBlockTransactions(utxoMap);
      // update wallet's transaction history in this PeerNode,
      newBlock.updateReceivedTransactionHistoryOf(wallet);
      // update wallet's balance based on new local UTXOMap
      wallet.updateBalance(utxoMap);
    }

    // 2. have a local mainChain that is too old to catch up with the new block,
    // this Node needs to ask for the whole chain for further validation)
    // OR a fraud block -- return false
    return isUpdated;
  }

  @Override
  public synchronized boolean updateBlockChain(BlockChain newBlockChain) {
    boolean isLonger = newBlockChain.size() > mainChain.size();
    if (newBlockChain.size() > mainChain.size() && newBlockChain.verifyChain(DIFFICULTY)) {
      mainChain = newBlockChain.copy();
      // reset local UTXOMap based on the new local mainChain
      resetUTXOMap();
      // reset the whole transaction history of the wallet in this PeerNode, based on the given new chain
      mainChain.resetTransactionHistoryOf(wallet);
      // update wallet's balance based on new local UTXOMap
      wallet.updateBalance(utxoMap);
      return true;
    }
    System.out.println("New blockchain is not accepted!");
    return false;
  }

  @Override
  public boolean verifyNewBlockSelfHash(Block newBlock) {
    return newBlock.verifySelfHash(DIFFICULTY);
  }

  // TODO: For further improvement, this method could be replaced by taking user input using a GUI
  @Override
  public Transaction startAutoTransaction() {
    Random rand = new Random();
    Transaction newTransaction = null;
    boolean done = false;

    while (!done && wallet.getBalance() >= NormalTransaction.MINIMUM_INPUT) {
      // generate random transaction value
      double transactionValue = rand.nextFloat() * wallet.getBalance();

      // pick a random recipient from contacts map
      // if no valid recipient was chosen, send funds to itself
      Map<String, String> contactMap = contacts.getMap();
      int randIntBound = contactMap.size() > 0 ? contactMap.size() : 1;
      Map.Entry<String, String> recipient = contactMap.entrySet().stream()
              .skip(rand.nextInt(randIntBound)).findFirst().orElse(null);
      String recipientAddress = recipient == null ? wallet.getPublicKeyStr() : recipient.getValue();
      String recipientName = recipient == null ? ownerName : recipient.getKey();

      // generate a transaction memo
      String memo = String.format(
              "%s send %.2f coins to %s", ownerName, transactionValue, recipientName);

      // start sending
      try {
        newTransaction = wallet.send(recipientAddress, transactionValue, memo, utxoMap);
        done = true;
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
    // add this new transaction to self TX pool
    updateTransactionPool(newTransaction);
    return newTransaction;
  }

  @Override
  public String getOwnerName() {
    return ownerName;
  }

  @Override
  public Wallet getWallet() {
    return wallet.clone();
  }

  @Override
  public BlockChain getBlockChain() {
    return mainChain.copy();
  }

  @Override
  public UTXOMap getUTXOMap() {
    return utxoMap.copy();
  }

  @Override
  public List<Transaction> getTransactionPool() {
    List<Transaction> poolCopy = new ArrayList<>();
    transactionPool.forEach(transaction -> poolCopy.add(transaction.copy()));
    return poolCopy;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
            && ownerName.equals(((PeerNode) o).ownerName)
            && wallet.equals(((PeerNode) o).wallet)
            && mainChain.equals(((PeerNode) o).mainChain)
            && utxoMap.equals(((PeerNode) o).utxoMap)
            && transactionPool.equals(((PeerNode) o).transactionPool);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), ownerName, wallet, mainChain, utxoMap, transactionPool);
  }


}
