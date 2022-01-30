import org.junit.Before;
import org.junit.Test;

import java.security.PublicKey;

import utils.BlockchainUtil;
import transaction.RewardTransaction;
import transaction.Transaction;
import utils.UTXOMap;
import wallet.Wallet;

import static org.junit.Assert.assertEquals;


public class WalletAndTransactionTest {

  private Wallet w1, w2, w3;
  private Transaction tx1, tx2, tx3;
  private UTXOMap utxoMap;
  String newLine = System.lineSeparator();

  @Before
  public void setUp() {
    utxoMap = new UTXOMap();
    w1 = new Wallet();
    w2 = new Wallet();
    w3 = new Wallet();
    // create 3 reward transactions for each wallet
    tx1 = new RewardTransaction(w1.getPublicKeyStr());
    tx2 = new RewardTransaction(w2.getPublicKeyStr());
    tx3 = new RewardTransaction(w3.getPublicKeyStr());
    // update 3 reward outputs in UTXO map
    tx1.updateUTXO(utxoMap);
    tx2.updateUTXO(utxoMap);
    tx3.updateUTXO(utxoMap);
    // update balance of each wallet
    w1.updateBalance(utxoMap);
    w2.updateBalance(utxoMap);
    w3.updateBalance(utxoMap);
  }

  @Test
  public void testSend() {
    // w1 send 5 coins to w2
    Transaction tx12 = w1.send(w2.getPublicKeyStr(), 5, "Hello w2", utxoMap);
    tx12.updateUTXO(utxoMap);
    w1.updateBalance(utxoMap);
    // w2 send 9.5 coins to w3
    Transaction tx23 = w2.send(w3.getPublicKeyStr(), 9.5, "Hello w3", utxoMap);
    tx23.updateUTXO(utxoMap);
    w2.updateBalance(utxoMap);
    // w3 send 0.5 coins to w1
    Transaction tx31 = w3.send(w1.getPublicKeyStr(), 0.5, "Hello w1", utxoMap);
    tx31.updateUTXO(utxoMap);
    w3.updateBalance(utxoMap);

    String tx12Str = newLine + "    Sender Address: " + w1.getPublicKeyStr()
            + newLine + "    Recipient Address: " + w2.getPublicKeyStr()
            + newLine + "    Value: 5.00"
            + newLine + "    Memo: \"Hello w2\""
            + newLine + "    Status: UNCONFIRMED" + newLine;
    assertEquals(tx12Str, tx12.toString());
    String w1Str =  "wallet.Wallet: " + w1.getPublicKeyStr() + newLine
            + "Current Balance: 5.00" + newLine
            + "transaction.Transaction History: [" + tx12Str + "]";
    assertEquals(w1Str, w1.toString());

    String tx23Str = newLine + "    Sender Address: " + w2.getPublicKeyStr()
            + newLine + "    Recipient Address: " + w3.getPublicKeyStr()
            + newLine + "    Value: 9.50"
            + newLine + "    Memo: \"Hello w3\""
            + newLine + "    Status: UNCONFIRMED" + newLine;
    assertEquals(tx23Str, tx23.toString());
    String w2Str =  "wallet.Wallet: " + w2.getPublicKeyStr() + newLine
            + "Current Balance: 5.50" + newLine
            + "transaction.Transaction History: [" + tx23Str + "]";
    assertEquals(w2Str, w2.toString());

    String tx31Str = newLine + "    Sender Address: " + w3.getPublicKeyStr()
            + newLine + "    Recipient Address: " + w1.getPublicKeyStr()
            + newLine + "    Value: 0.50"
            + newLine + "    Memo: \"Hello w1\""
            + newLine + "    Status: UNCONFIRMED" + newLine;
    assertEquals(tx31Str, tx31.toString());
    String w3Str =  "wallet.Wallet: " + w3.getPublicKeyStr() + newLine
            + "Current Balance: 19.00" + newLine
            + "transaction.Transaction History: [" + tx31Str + "]";
    assertEquals(w3Str, w3.toString());

    System.out.println(w3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTransaction() {
    w1.send(w2.getPublicKeyStr(), 11, "", utxoMap);
    w1.send(w2.getPublicKeyStr(), 9.5, "", utxoMap);
    w1.send(w3.getPublicKeyStr(), 0.5, "", utxoMap);
  }


  @Test
  public void testToString() {
    String w1Str = "wallet.Wallet: " + w1.getPublicKeyStr() + newLine
            + "Current Balance: 10.00" + newLine + "transaction.Transaction History: []";
    assertEquals(w1Str, w1.toString());
    String w2Str = "wallet.Wallet: " + w2.getPublicKeyStr() + newLine
            + "Current Balance: 10.00" + newLine + "transaction.Transaction History: []";
    assertEquals(w2Str, w2.toString());
    String w3Str = "wallet.Wallet: " + w3.getPublicKeyStr() + newLine
            + "Current Balance: 10.00" + newLine + "transaction.Transaction History: []";
    assertEquals(w3Str, w3.toString());
    }

    @Test
  public void testKeyConversion() {
      PublicKey k1 = w1.getPublicKey();
      String k1Str = BlockchainUtil.keyToString(k1);
      PublicKey k1Recover = BlockchainUtil.stringToPublicKey(k1Str);
      assertEquals(k1, k1Recover);
    }
  }


