import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import block.Block;
import block.BlockChain;
import transaction.Transaction;
import utils.UTXOMap;
import wallet.Wallet;

import static org.junit.Assert.*;

public class BlockTest {
  public static final int DIFFICULTY = 4;

  private Wallet w1, w2, w3, w4;
  private Transaction tx1, tx2, tx3;
  private UTXOMap utxoMap;
  private Block b1, b2, b3, b4, b5;
  private BlockChain bc;
  String newLine = System.lineSeparator();

  @Before
  public void setUp() {
    utxoMap = new UTXOMap();
    w1 = new Wallet();
    w2 = new Wallet();
    w3 = new Wallet();
    w4 = new Wallet();

    // create genesis block
    b1 = new Block("0", w1.getPublicKeyStr());
    bc = new BlockChain();
  }

  // To simulate a block creation processed in node.PeerNode class
  private static void mineBlockHelper(Block newBlock, int difficulty, UTXOMap utxoMap) {
    boolean isMined;
    Random rand = new Random();
    // add transactions
    // update transaction merkle root
    newBlock.setTransactionsMerkleRoot();
    // keep mining in a while loop
    do {
      isMined = newBlock.oneMining(difficulty, rand);
    } while (!isMined);
    // if mining succeeds, confirm all transactions in this new block (update TX state and UTXO)
    newBlock.processBlockTransactions(utxoMap);
  }




  @Test
  public void testBlockchainBuilding() {
    // create block1, w1 get 10 coins reward
    mineBlockHelper(b1, DIFFICULTY, utxoMap);
    bc.addBlock(b1);

    // create block2, w2 get 10 coins reward
    b2 = new Block(b1.getHash(), w2.getPublicKeyStr());
    mineBlockHelper(b2, DIFFICULTY, utxoMap);
    bc.addBlock(b2);

    // create block3, w3 get 10 coins reward
    b3 = new Block(b2.getHash(), w3.getPublicKeyStr());
    mineBlockHelper(b3, DIFFICULTY, utxoMap);
    bc.addBlock(b3);

    // after creating the first 3 blocks,
    // UTXO Map updated as expected, thus each wallet could get the correct balance
    w1.updateBalance(utxoMap);
    w2.updateBalance(utxoMap);
    w3.updateBalance(utxoMap);
    assertEquals(10.0, w1.getBalance(), 0.001);
    assertEquals(10.0, w2.getBalance(), 0.001);
    assertEquals(10.0, w3.getBalance(), 0.001);

    // w1 send 5 coins to w2
    Transaction tx12 = w1.send(w2.getPublicKeyStr(), 5, "Hello w2", utxoMap);
    // w2 send 9.5 coins to w3
    Transaction tx23 = w2.send(w3.getPublicKeyStr(), 9.5, "Hello w3", utxoMap);
    // w3 send 0.5 coins to w1
    Transaction tx31 = w3.send(w1.getPublicKeyStr(), 0.5, "Hello w1", utxoMap);

    // create block4 that contains three transactions above, w1 get 10 coins reward
    b4 = new Block(b3.getHash(), w1.getPublicKeyStr());
    b4.addTransaction(tx12, utxoMap);
    b4.addTransaction(tx23, utxoMap);
    b4.addTransaction(tx31, utxoMap);

    // w2, w3 local utxoMap remain previous version, thus creating a copy map of the previous map
    UTXOMap utxoMapBefore = utxoMap.copy();
    // after creating the new block b4, w1's local utxoMap has been updated,
    // as well as the global map in this test case
    mineBlockHelper(b4, DIFFICULTY, utxoMap);

    // Test the new block verification
    // w2, w3 should verify the new block b4 based on previous utxoMap
    assertTrue(bc.verifyNewBlock(b4, utxoMapBefore, DIFFICULTY));

    // after verifying the new block b4, w2, w3 could finally add it to their local blockchain
    bc.addBlock(b4);

    // after creating b4, global UTXO Map updated as expected, each wallet get the correct balance
    w1.updateBalance(utxoMap);
    w2.updateBalance(utxoMap);
    w3.updateBalance(utxoMap);
    assertEquals(15.5, w1.getBalance(), 0.001);
    assertEquals(5.5, w2.getBalance(), 0.001);
    assertEquals(19, w3.getBalance(), 0.001);

    // Test the size of current blockchain
    assertEquals(4, bc.size());

    // Test block and blockChain toString
    String[] lines = bc.getLastBlock().toString().strip().split(newLine);
    assertEquals(30, lines.length);

    String delimiter = "*".repeat(120);
    String b4Str = newLine + "{" + newLine + "Hash: " + b4.getHash() + "," + newLine
            + "Previous Hash: " + b4.getPreviousHash() + "," + newLine
            + "Time Stamp: " + b4.getTimeStamp() + "," + newLine
            + "Nonce: " + b4.getNonce() + "," + newLine
            + "Transactions: " + b4.getTransactions() + newLine + delimiter + "}" ;
    assertEquals(b4Str, b4.toString());

    // Test blockchain verification
    assertTrue(bc.verifyChain(4));

    // Test util.UTXOMap generation
    assertEquals(utxoMap, bc.generateUTXOMap());

/*    System.out.println(bc);*/
  }

  @Test
  public void testInvalidAddTransaction() {
    // w1 get 10 coins reward
    mineBlockHelper(b1, 4, utxoMap);
    bc.addBlock(b1);

    // w2 get 10 coins reward
    b2 = new Block(b1.getHash(), w2.getPublicKeyStr());
    mineBlockHelper(b2, 4, utxoMap);
    bc.addBlock(b2);

    // w3 get 10 coins reward
    b3 = new Block(b2.getHash(), w3.getPublicKeyStr());
    mineBlockHelper(b3, 4, utxoMap);
    bc.addBlock(b3);

    // w1 send 5 coins to w2
    Transaction tx12 = w1.send(w2.getPublicKeyStr(), 5, "Hello w2", utxoMap);
    // w1 create b4 contains transaction above, get 10 coins reward
    b4 = new Block(b3.getHash(), w1.getPublicKeyStr());
    b4.addTransaction(tx12, utxoMap);
    mineBlockHelper(b4, 4, utxoMap);
    bc.addBlock(b4);

    // tx12 was added to b4, cannot be added to b5 (double-spent is impossible)
    b5 = new Block(b4.getHash(), w1.getPublicKeyStr());
    assertFalse(b5.addTransaction(tx12, utxoMap));
  }

  @Test
  public void testInvalidPreviousHash() {
    // w1 get 10 coins reward
    mineBlockHelper(b1, DIFFICULTY, utxoMap);
    bc.addBlock(b1);

    // w2 get 10 coins reward
    b2 = new Block(b1.getHash(), w2.getPublicKeyStr());
    mineBlockHelper(b2, DIFFICULTY, utxoMap);
    bc.addBlock(b2);

    // w3 get 10 coins reward
    b3 = new Block(b2.getHash(), w3.getPublicKeyStr());
    mineBlockHelper(b3, DIFFICULTY, utxoMap);
    bc.addBlock(b3);

    // w1 send 5 coins to w2
    Transaction tx12 = w1.send(w2.getPublicKeyStr(), 5, "Hello w2", utxoMap);

    // w1 create b4 contains transaction above, get 10 coins reward
    // BUT b4 use b1's hash as its previous hash, which is invalid
    b4 = new Block(b1.getHash(), w1.getPublicKeyStr());
    b4.addTransaction(tx12, utxoMap);
    UTXOMap utxoMapBefore = utxoMap.copy();
    mineBlockHelper(b4, DIFFICULTY, utxoMap);
    assertFalse(bc.verifyNewBlock(b4, utxoMapBefore, DIFFICULTY));
  }

  @Test
  public void testInvalidDifficulty() {
    // w1 get 10 coins reward
    mineBlockHelper(b1, DIFFICULTY, utxoMap);
    bc.addBlock(b1);

    // w2 get 10 coins reward
    b2 = new Block(b1.getHash(), w2.getPublicKeyStr());

    // for miner w2, its local UTXO Map is updated when the mining successes
    // for w1 and w3, their local UTXO Map reamin previous version
    // w1 and w3 must use verifySelfTransactions method to validate the new block, based on previous UTXO Map
    UTXOMap utxoMapBefore = utxoMap.copy();
    mineBlockHelper(b2, DIFFICULTY, utxoMap);
    assertFalse(bc.verifyNewBlock(b1, utxoMapBefore, 3));

  }

  @Test
  public void testInvalidBlockTransactions() {
    // w1 get 10 coins reward
    mineBlockHelper(b1, DIFFICULTY, utxoMap);
    bc.addBlock(b1);

    // w2 get 10 coins reward
    b2 = new Block(b1.getHash(), w2.getPublicKeyStr());
    mineBlockHelper(b2, DIFFICULTY, utxoMap);
    // utxoMap has been updated by miner w2,
    // thus other users cannot verify b2's transactions by using updated utxoMap
    assertFalse(bc.verifyNewBlock(b1, utxoMap, DIFFICULTY));

  }



  // simulate an interrupted mining, the given newBlock will not be mined
  private static boolean interruptedMiningHelper(BlockChain startingChain, Block newBlock,
                                                 int difficulty) {
    boolean isMined = false;
    Random rand = new Random();
    int stratingSize = startingChain.size();
    int count = 0;
    while (!isMined && stratingSize == startingChain.size()) {
      isMined = newBlock.oneMining(difficulty, rand);
      count++;
      if (count == 5) {
        startingChain.addBlock(startingChain.getLastBlock()); // change the original blockchain's size
      }
    }
    return isMined;
  }


  @Test
  public void testInterruptedMining() {
    mineBlockHelper(b1, DIFFICULTY, utxoMap);
    bc.addBlock(b1);
    b2 = new Block(b1.getHash(), w2.getPublicKeyStr());
    mineBlockHelper(b2, DIFFICULTY, utxoMap);
    bc.addBlock(b2);
    b3 = new Block(b2.getHash(), w3.getPublicKeyStr());
    mineBlockHelper(b3, DIFFICULTY, utxoMap);
    bc.addBlock(b3);

    // w1 send 5 coins to w2
    Transaction tx12 = w1.send(w2.getPublicKeyStr(), 5, "Hello w2", utxoMap);
    // w2 send 9.5 coins to w3
    Transaction tx23 = w2.send(w3.getPublicKeyStr(), 9.5, "Hello w3", utxoMap);
    // w3 send 0.5 coins to w1
    Transaction tx31 = w3.send(w1.getPublicKeyStr(), 0.5, "Hello w1", utxoMap);

    // create block4 that contains three transactions above, w1 get 10 coins reward
    b4 = new Block(b3.getHash(), w1.getPublicKeyStr());
    b4.addTransaction(tx12, utxoMap);
    b4.addTransaction(tx23, utxoMap);
    b4.addTransaction(tx31, utxoMap);

    // b4 is not mined
    assertFalse(interruptedMiningHelper(bc, b4, DIFFICULTY));
    assertEquals(4, bc.size());
    assertFalse(bc.verifyNewBlock(b4, utxoMap,DIFFICULTY));

  }

  @Test
  public void testInvalidChain() {
    mineBlockHelper(b1, DIFFICULTY, utxoMap);
    bc.addBlock(b1);
    bc.addBlock(b1);
    assertFalse(bc.verifyChain(DIFFICULTY)); // invalid previousHash

    bc.removeLastBlock();
    b2 = new Block(b1.getHash(), w2.getPublicKeyStr());
    interruptedMiningHelper(bc, b2, DIFFICULTY);
    bc.removeLastBlock();
    bc.addBlock(b2);
    assertEquals(2, bc.size());
    assertFalse(bc.verifyChain(DIFFICULTY)); // invalid b2 hash

    bc.removeLastBlock();
    mineBlockHelper(b2, DIFFICULTY, utxoMap);
    bc.addBlock(b2);
    assertFalse(bc.verifyChain(10)); // invalid POW


  }


}