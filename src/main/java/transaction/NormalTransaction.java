package transaction;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import utils.BlockchainUtil;
import utils.UTXOMap;

/**
 * This class represents a normal transaction.
 * A NormalTransaction contains ID, sender address, recipient address, value, timeStamp, memo,
 * inputs, outputs, state, and a signature.
 */
public class NormalTransaction extends AbstractTransaction {

  private String sender;
  private UTXOMap inputs;
  private byte[] signature;

  /**
   * Consruct a NormalTransaction with the given sender, recipient, value, memo and inputs.
   *
   * @param sender a Wallet's PublicKey string of the sender
   * @param recipient a Wallet's PublicKey string of the recipient
   * @param value a double transaction value
   * @param memo a memo string
   * @param inputs a UTXOMap contains the UTXOs of the sender
   */
  public NormalTransaction(String sender, String recipient, double value, String memo,
                           UTXOMap inputs) {
    super(recipient, value, memo);
    this.sender = sender;
    this.inputs = inputs;
    this.ID = calculateID();
    // outputs rely on Transaction Id to calculate TransactionFlow ID, thus must be the last to initialize
    setOutputs();
  }

  @Override
  protected String calculateID() {
    String data = sender + recipient + value + memo + timeStamp;
    return BlockchainUtil.applySha256(data);
  }

  @Override
  protected void setOutputs() {
    super.setOutputs();
    double leftOver = inputs.sum() - getOutputSum();
    if (leftOver > 0) {
      // the remaining unspent input become a new output to its original owner
      this.outputs.add(new TransactionFlow(sender, leftOver, ID));
    }
  }

  /**
   * Generate a digital signature of this Transaction.
   *
   * @param privateKey a Wallet's PrivateKey of the sender
   */
  public void sign(PrivateKey privateKey) {
    String transactionData = sender + recipient + value + memo;
    signature = BlockchainUtil.encryptByECDSA(privateKey, transactionData);
  }

  /**
   * Calculate the outputs sum of this Transaction.
   *
   * @return a double value of the outputs sum
   */
  private double getOutputSum() {
    return outputs.stream().mapToDouble(TransactionFlow:: getValue).sum();
  }

  /**
   * Check if all inputs in this Transaction exist in the given UTXOMap.
   *
   * @param utxoMap an UTXOMap represents the UTXOs of the sender in this Transaction
   * @return a boolean value, which is true if the inputs in this TRasaction are valid
   */
  private boolean isLegalInput(UTXOMap utxoMap) {
    return utxoMap.getMap().keySet().containsAll(inputs.getMap().keySet());
  }

  /**
   * Verify the signature of this Transaction, based on the sender's PublicKey, signature and the
   * data in this Transaction.
   *
   * @return a boolean value, which is true if the signature is valid
   */
  private boolean isLegalSignature() {
    String transactionData = sender + recipient + value + memo;
    PublicKey senderKey = BlockchainUtil.stringToPublicKey(sender);
    return BlockchainUtil.verifyByECDSA(senderKey, signature, transactionData);
  }

  @Override
  public boolean outsideValidate(UTXOMap utxoMap) {
    return insideValidate() & isLegalInput(utxoMap) && isLegalSignature();
  }

  @Override
  public boolean insideValidate() {
    double inputsSum = inputs.sum();
    double outputsSum = getOutputSum();
    // check minimum inputs and overspent
    return inputsSum >= MINIMUM_INPUT && Math.abs(outputsSum - inputsSum) < 0.001;
  }


  @Override
  public void updateUTXO(UTXOMap utxoMap) {
    // add new outputs in UTXOMap
    super.updateUTXO(utxoMap);
    // remove old inputs in UTXOMap
    inputs.getMap().forEach((k, v) -> utxoMap.remove(k));
  }

  @Override
  public NormalTransaction copy() {
    NormalTransaction copy = new NormalTransaction(
            this.sender, this.recipient, this.value, this.memo, this.inputs.copy());
    copy.timeStamp = this.timeStamp;
    copy.ID = this.ID;
    copy.outputs = new ArrayList<>();
    copy.outputs.addAll(outputs);
    copy.state = this.state;
    copy.signature = this.signature;
    return copy;
  }

  @Override
  public boolean isSentBy(String sender) {
    return this.sender.equals(sender);
  }


  @Override
  public String toString() {
    return String.format("%n    Sender Address: %s", sender) + super.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    NormalTransaction that = (NormalTransaction) o;
    return sender.equals(that.sender)
            && inputs.equals(that.inputs)
            && Arrays.equals(signature, that.signature);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(super.hashCode(), sender, inputs);
    result = 31 * result + Arrays.hashCode(signature);
    return result;
  }
}
