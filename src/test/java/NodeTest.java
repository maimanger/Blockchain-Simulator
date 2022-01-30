import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import block.Block;
import block.BlockChain;
import node.HostNodeImpl;
import node.Node;
import node.NodeClient;
import node.PeerNode;
import transaction.Transaction;
import wallet.Wallet;

import static org.junit.Assert.*;

public class NodeTest {
  public static final int DIFFICULTY = 4;

  private Node n1, n2, n3, n4;
  private Transaction tx1, tx2, tx3;
  private Block b1, b2, b3, b4, b5, b6;
  private Wallet w1, w2, w3, w4;
  private BlockChain bc;

  @Before
  public void setUp() {
    n1 = new PeerNode("n1");
    n2 = new PeerNode("n2");
    n3 = new PeerNode("n3");
    n4 = new PeerNode("n4");
    // add contacts to each node.PeerNode
    n1.addContact("n2", n2.getWallet().getPublicKeyStr());
    n1.addContact("n3", n3.getWallet().getPublicKeyStr());
    n1.addContact("n4", n4.getWallet().getPublicKeyStr());
    n2.addContact("n1", n1.getWallet().getPublicKeyStr());
    n2.addContact("n3", n3.getWallet().getPublicKeyStr());
    n2.addContact("n4", n4.getWallet().getPublicKeyStr());
    n3.addContact("n1", n1.getWallet().getPublicKeyStr());
    n3.addContact("n2", n2.getWallet().getPublicKeyStr());
    n3.addContact("n4", n4.getWallet().getPublicKeyStr());
    n4.addContact("n1", n1.getWallet().getPublicKeyStr());
    n4.addContact("n2", n2.getWallet().getPublicKeyStr());
    n4.addContact("n3", n3.getWallet().getPublicKeyStr());

    bc = new BlockChain();
  }


  @Test
  public void testCreateBlock() {
    // 1. n1 creates first block b1
    b1 = n1.createBlock();
    tx1 = b1.getTransactions().get(0);
    w1 = n1.getWallet();

    // n1 utxoMap updated
    assertTrue(n1.getUTXOMap().containsKey(tx1.getOutputs().get(0).getID()));
    // w1 balance updated
    assertEquals(10, w1.getBalance(), 0.001);
    // w1 TX history updated
    assertTrue(w1.getTransactionHistory().contains(tx1));
    // n1 blockchain updated
    assertEquals(b1, n1.getBlockChain().getLastBlock());
    assertEquals(1, n1.getBlockChain().size());

    // n2 and n3 accept b1
    n2.updateBlockChain(b1);
    n3.updateBlockChain(b1);
    // n2 and n3 blockchain updated
    assertEquals(n1.getBlockChain(), n2.getBlockChain());
    assertEquals(n3.getBlockChain(), n1.getBlockChain());
    // n2 and n3 utxoMap updated
    assertEquals(n1.getUTXOMap(), n2.getUTXOMap());
    assertEquals(n3.getUTXOMap(), n1.getUTXOMap());


    // 2. n2 creates block b2
    b2 = n2.createBlock();
    tx2 = b2.getTransactions().get(0);
    w2 = n2.getWallet();

    // n2 utxoMap updated
    assertTrue(n2.getUTXOMap().containsKey(tx2.getOutputs().get(0).getID()));
    // w2 balance updated
    assertEquals(10, w2.getBalance(), 0.001);
    // w2 TX history updated
    assertTrue(w2.getTransactionHistory().contains(tx2));
    // n2 blockchain updated
    assertEquals(b2, n2.getBlockChain().getLastBlock());
    assertEquals(2, n2.getBlockChain().size());

    // n1 and n3 accept b2
    n1.updateBlockChain(b2);
    n3.updateBlockChain(b2);
    // n1 and n3 blockchains updated
    assertEquals(n2.getBlockChain(), n1.getBlockChain());
    assertEquals(n3.getBlockChain(), n2.getBlockChain());
    // n1 and n3 utxoMap updated
    assertEquals(n2.getUTXOMap(), n1.getUTXOMap());
    assertEquals(n3.getUTXOMap(), n2.getUTXOMap());


    // 3. n3 creates block b3
    b3 = n3.createBlock();
    tx3 = b3.getTransactions().get(0);
    w3 = n3.getWallet();

    // n3 utxoMap updated
    assertTrue(n3.getUTXOMap().containsKey(tx3.getOutputs().get(0).getID()));
    // w3 balance updated
    assertEquals(10, w3.getBalance(), 0.001);
    // w3 TX history updated
    assertTrue(w3.getTransactionHistory().contains(tx3));
    // n3 blockchain updated
    assertEquals(b3, n3.getBlockChain().getLastBlock());
    assertEquals(3, n3.getBlockChain().size());

    // n1 and n2 accept b3
    n1.updateBlockChain(b3);
    n2.updateBlockChain(b3);
    // n1 and n2 blockchains updated
    assertEquals(n3.getBlockChain(), n1.getBlockChain());
    assertEquals(n2.getBlockChain(), n3.getBlockChain());
    // n1 and n2 utxoMap updated
    assertEquals(n2.getUTXOMap(), n1.getUTXOMap());
    assertEquals(n3.getUTXOMap(), n2.getUTXOMap());
  }


  @Test
  public void testStartTransaction() {
    // 1. create first 3 blocks, each of them contains only one reward transaction
    b1 = n1.createBlock();
    n2.updateBlockChain(b1);
    n3.updateBlockChain(b1);
    b2 = n2.createBlock();
    n1.updateBlockChain(b2);
    n3.updateBlockChain(b2);
    b3 = n3.createBlock();
    n1.updateBlockChain(b3);
    n2.updateBlockChain(b3);


    // 2. Create random transactions
    // n1 creates random transaction tx1, n2 and n3 collect tx1 to their TX pool
    tx1 = n1.startAutoTransaction();
    n2.updateTransactionPool(tx1);
    n3.updateTransactionPool(tx1);
    assertTrue(n1.getTransactionPool().contains(tx1));
    assertTrue(n2.getTransactionPool().contains(tx1));
    assertTrue(n3.getTransactionPool().contains(tx1));
    //n2 creates random transaction tx2, n1 and n3 collect tx2 to their TX pool
    tx2 = n2.startAutoTransaction();
    n1.updateTransactionPool(tx2);
    n3.updateTransactionPool(tx2);
    assertTrue(n1.getTransactionPool().contains(tx2));
    assertTrue(n2.getTransactionPool().contains(tx2));
    assertTrue(n3.getTransactionPool().contains(tx2));
    //n3 creates random transaction tx3, n1 and n2 collect tx3 to their TX pool
    tx3 = n3.startAutoTransaction();
    n1.updateTransactionPool(tx3);
    n2.updateTransactionPool(tx3);
    assertTrue(n1.getTransactionPool().contains(tx3));
    assertTrue(n2.getTransactionPool().contains(tx3));
    assertTrue(n3.getTransactionPool().contains(tx3));

    // get each wallet's starting balance (tx1, tx2, tx3 have not updated)
    double w1Balance = n1.getWallet().getBalance();
    assertEquals(10, w1Balance, 0.001);
    double w2Balance = n2.getWallet().getBalance();
    assertEquals(10, w2Balance, 0.001);
    double w3Balance = n3.getWallet().getBalance();
    assertEquals(10, w3Balance, 0.001);


    // 3. n1 creates new block b4, which will contain tx1, tx2 and tx3
    b4 = n1.createBlock();
    assertTrue(b4.getTransactions().contains(tx1));
    assertTrue(b4.getTransactions().contains(tx2));
    assertTrue(b4.getTransactions().contains(tx3));

    // n1 utxoMap updated
    assertTrue(n1.getUTXOMap().getMap().containsKey(
            b4.getTransactions().get(0).getOutputs().get(0).getID()));
    assertTrue(n1.getUTXOMap().getMap().containsKey(
            b4.getTransactions().get(1).getOutputs().get(0).getID()));
    assertTrue(n1.getUTXOMap().getMap().containsKey(
            b4.getTransactions().get(1).getOutputs().get(1).getID()));
    assertTrue(n1.getUTXOMap().getMap().containsKey(
            b4.getTransactions().get(2).getOutputs().get(0).getID()));
    assertTrue(n1.getUTXOMap().getMap().containsKey(
            b4.getTransactions().get(2).getOutputs().get(1).getID()));
    assertTrue(n1.getUTXOMap().getMap().containsKey(
            b4.getTransactions().get(3).getOutputs().get(0).getID()));
    assertTrue(n1.getUTXOMap().getMap().containsKey(
            b4.getTransactions().get(3).getOutputs().get(1).getID()));

    // w1 balance updated
    w1 = n1.getWallet();
    double w1BalanceChange = 10 - tx1.getValue(); // + new reward 10 coins - output of tx1
    w1BalanceChange += tx2.isSentTo(w1.getPublicKeyStr()) ? tx2.getValue() : 0;
    w1BalanceChange += tx3.isSentTo(w1.getPublicKeyStr()) ? tx3.getValue() : 0;
    assertEquals(w1Balance + w1BalanceChange, w1.getBalance(), 0.001);

    // w1 TX history updated
    assertTrue(w1.getTransactionHistory().contains(tx1));
    if (tx2.isSentTo(w1.getPublicKeyStr())) {
      assertTrue(w1.getTransactionHistory().contains(tx2));
    }
    if (tx3.isSentTo(w1.getPublicKeyStr())) {
      assertTrue(w1.getTransactionHistory().contains(tx3));
    }

    // n1 blockchain updated
    assertEquals(b4, n1.getBlockChain().getLastBlock());
    assertEquals(4, n1.getBlockChain().size());


    // 4. n2, n3 accept b4
    n2.updateBlockChain(b4);
    n3.updateBlockChain(b4);

    // n2 and n3 blockchain updated
    assertEquals(n1.getBlockChain(), n2.getBlockChain());
    assertEquals(n3.getBlockChain(), n1.getBlockChain());

    // n2 and n3 utxoMap updated
    assertEquals(n1.getUTXOMap(), n2.getUTXOMap());
    assertEquals(n3.getUTXOMap(), n1.getUTXOMap());

    // w2 balance updated
    w2 = n2.getWallet();
    double w2BalanceChange = - tx2.getValue(); // - output of tx2
    w2BalanceChange += tx1.isSentTo(w2.getPublicKeyStr()) ? tx1.getValue() : 0;
    w2BalanceChange += tx3.isSentTo(w2.getPublicKeyStr()) ? tx3.getValue() : 0;
    assertEquals(w2Balance + w2BalanceChange, w2.getBalance(), 0.001);

    // w2 TX history updated
    assertTrue(w2.getTransactionHistory().contains(tx2));
    if (tx1.isSentTo(w2.getPublicKeyStr())) {
      assertTrue(w2.getTransactionHistory().contains(tx1));
    }
    if (tx3.isSentTo(w2.getPublicKeyStr())) {
      assertTrue(w2.getTransactionHistory().contains(tx3));
    }

    // w3 balance updated
    w3 = n3.getWallet();
    double w3BalanceChange = - tx3.getValue(); // - output of tx3
    w3BalanceChange += tx1.isSentTo(w3.getPublicKeyStr()) ? tx1.getValue() : 0;
    w3BalanceChange += tx2.isSentTo(w3.getPublicKeyStr()) ? tx2.getValue() : 0;
    assertEquals(w3Balance + w3BalanceChange, w3.getBalance(), 0.001);

    // w3 TX history updated
    assertTrue(w3.getTransactionHistory().contains(tx3));
    if (tx1.isSentTo(w3.getPublicKeyStr())) {
      assertTrue(w3.getTransactionHistory().contains(tx1));
    }
    if (tx2.isSentTo(w3.getPublicKeyStr())) {
      assertTrue(w3.getTransactionHistory().contains(tx2));
    }


    // 5. new node n4 update its blockchain based on n1's blockchain
    n4.updateBlockChain(n1.getBlockChain());
    assertEquals(n1.getBlockChain(), n4.getBlockChain());
    assertEquals(n1.getUTXOMap(), n4.getUTXOMap());

    // w4 balance updated
    w4 = n4.getWallet();
    double w4BalanceChange = 0;
    w4BalanceChange += tx1.isSentTo(w4.getPublicKeyStr()) ? tx1.getValue() : 0;
    w4BalanceChange += tx2.isSentTo(w4.getPublicKeyStr()) ? tx2.getValue() : 0;
    w4BalanceChange += tx3.isSentTo(w4.getPublicKeyStr()) ? tx3.getValue() : 0;
    assertEquals(w4BalanceChange, w4.getBalance(), 0.001);

    // w4 TX history updated
    if (tx1.isSentTo(w4.getPublicKeyStr())) {
      assertTrue(w4.getTransactionHistory().contains(tx1));
    }
    if (tx2.isSentTo(w4.getPublicKeyStr())) {
      assertTrue(w4.getTransactionHistory().contains(tx2));
    }
    if (tx3.isSentTo(w4.getPublicKeyStr())) {
      assertTrue(w4.getTransactionHistory().contains(tx3));
    }

    // 6. test large amount of TXs in node's pool
    List<Transaction> txList = new ArrayList<>();
    while (txList.size() <= 50) {
      txList.add(n1.startAutoTransaction());
      txList.add(n2.startAutoTransaction());
      txList.add(n3.startAutoTransaction());
      txList.add(n4.startAutoTransaction());
    }

    for (Transaction t : txList) {
      n1.updateTransactionPool(t);
      n2.updateTransactionPool(t);
      n3.updateTransactionPool(t);
      n4.updateTransactionPool(t);
    }

    b5 = n4.createBlock();
    assertTrue( b5.getTransactions().size() <= 5);
    assertFalse(b5.getTransactions().containsAll(txList));
  }

  @Test
  public void testUpdateChain() {
    b1 = n1.createBlock();
    n2.updateBlockChain(b1);
    n3.updateBlockChain(b1);
    b2 = n2.createBlock();
    n1.updateBlockChain(b2);
    n3.updateBlockChain(b2);
    b3 = n3.createBlock();
    n1.updateBlockChain(b3);
    n2.updateBlockChain(b3);

    tx1 = n1.startAutoTransaction();
    tx2 = n2.startAutoTransaction();
    tx3 = n3.startAutoTransaction();
    n1.updateTransactionPool(tx2);
    n1.updateTransactionPool(tx3);
    assertTrue(n1.getTransactionPool().containsAll(Arrays.asList(tx1, tx2, tx3)));
    n2.updateTransactionPool(tx1);
    n2.updateTransactionPool(tx3);
    assertTrue(n2.getTransactionPool().containsAll(Arrays.asList(tx1, tx2, tx3)));
    n3.updateTransactionPool(tx1);
    n3.updateTransactionPool(tx2);
    assertTrue(n3.getTransactionPool().containsAll(Arrays.asList(tx1, tx2, tx3)));

    b4 = n1.createBlock();

    // n4 updated its mainChain, util.UTXOMap, wallet TX history based on n1's blockchain
    n4.updateBlockChain(n1.getBlockChain());
    assertEquals(n1.getUTXOMap(), n4.getUTXOMap());
    if (tx1.isSentTo(n4.getWallet().getPublicKeyStr())) {
      System.out.println("tx1 is sent to n4");
      assertTrue(n4.getWallet().getTransactionHistory().contains(tx1));
    }
    if (tx2.isSentTo(n4.getWallet().getPublicKeyStr())) {
      System.out.println("tx2 is sent to n4");
      assertTrue(n4.getWallet().getTransactionHistory().contains(tx2));
    }
    if (tx3.isSentTo(n4.getWallet().getPublicKeyStr())) {
      System.out.println("tx3 is sent to n4");
      assertTrue(n4.getWallet().getTransactionHistory().contains(tx3));
    }
  }


  // n1 balance is 0, cannot send funds
  @Test
  public void testInvalidStartTransaction() {
    assertNull(n1.startAutoTransaction());
  }

  @Test
  public void testInvalidNewBlockChain() {
    b1 = n1.createBlock();
    bc.addBlock(b1);
    bc.addBlock(b1);
    assertFalse(n1.updateBlockChain(bc));
  }


  @Test
  public void testUpdateTransactionPool() {
    b1 = n1.createBlock();
    tx1 = n1.startAutoTransaction();
    assertFalse(n1.updateTransactionPool(null));
    assertFalse(n1.updateTransactionPool(tx1));
    List<Transaction> n2TXpool = n2.getTransactionPool();
    n2TXpool.add(tx1);
    assertTrue(n2.updateTransactionPool(tx1));
    assertEquals(n2TXpool, n2.getTransactionPool());
  }

  @Test
  public void testVerifyNewBlockExceptPreviousHash() {
    b1 = n1.createBlock();
    b2 = n2.createBlock();
    assertTrue(n2.verifyNewBlockSelfHash(b2));
  }



  @Test
  public void testAddMsgSender() {
    n1.addMsgSender("localhost", 6666);
    n2.setSelfClientAddress("localhost");
    n2.addMsgSender(6667);
    assertEquals(new NodeClient("localhost", 6666), n1.getMsgSenders().get(0));
    assertEquals(new NodeClient("localhost", 6667), n2.getMsgSenders().get(0));
  }

  @Test
  public void testAddPeerPortsList() {
    HostNodeImpl n0 = new HostNodeImpl();
    n0.setSelfClientAddress("localhost");
    n0.addMsgSender(6666);
    n0.setPeerPortsMap(6666, Arrays.asList(7777, 7778), Arrays.asList(7779, 7780));
    Map<Integer, List<Integer>> peerServerPortsMap = new HashMap<>();
    Map<Integer, List<Integer>> peerClientPortsMap = new HashMap<>();
    peerServerPortsMap.put(6666, Arrays.asList(7777, 7778));
    peerClientPortsMap.put(6666, Arrays.asList(7779, 7780));
    assertEquals(peerServerPortsMap, n0.getPeerServerPortsMap());
    assertEquals(peerClientPortsMap, n0.getPeerClientPortsMap());
  }




}