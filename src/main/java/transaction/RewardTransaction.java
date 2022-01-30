package transaction;

import java.util.ArrayList;

import utils.UTXOMap;

/**
 * This class represents a reward transaction that is sent to a block creator.
 */
public class RewardTransaction extends AbstractTransaction {

  /**
   * Construct a RewardTransaction object by a given recipient.
   *
   * @param recipient a Wallet's PublicKey string that represents the address of the recipient
   */
  public RewardTransaction(String recipient) {
    super(recipient, Transaction.BLOCK_REWARD, "Block creation reward");
    this.ID = calculateID();
    // outputs rely on Transaction Id to calculate TransactionFlow ID, thus must be the last to initialize
    setOutputs();
  }

  @Override
  public boolean outsideValidate(UTXOMap utxoMap) {
    return true;
  }

  @Override
  public boolean insideValidate() {
    return true;
  }

  @Override
  public Transaction copy() {
    RewardTransaction copy = new RewardTransaction(this.recipient);
    copy.timeStamp = this.timeStamp;
    copy.ID = this.ID;
    copy.outputs = new ArrayList<>();
    copy.outputs.addAll(outputs);
    copy.state = this.state;
    return copy;
  }

}
