package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import controller.Starter;

/**
 * This class represents a View of this BlockChain simulator.
 */
public class NodeView extends JFrame {
  private JTextPane sendingLog;
  private JTextPane receivedLog;
  private JTextPane blockchainLog;
  private JButton startButton;
  private Starter starter;

  /**
   * Construct a view object.
   */
  public NodeView() {
    setTitle("Blockchain Simulator Log");
    getContentPane().setLayout(null); // absolute layout
    drawStartButton();
    drawSendingLogArea();
    drawReceivedLogArea();
    drawBlockchainLogArea();
    setSize(1010, 750); // set main frame size
    setResizable(false);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }

  /**
   * Create a start button in this View.
   */
  private void drawStartButton() {
    startButton = new JButton("START");
    startButton.setBounds(433, 678, 117, 23);
    startButton.setFont(new Font("Arial", Font.PLAIN, 20));
    getContentPane().add(startButton);
  }

  /**
   * Create a sendingLog textPane in this View.
   */
  private void drawSendingLogArea() {
    JScrollPane sendingPane = new JScrollPane();
    sendingPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED,
            null, null, null, null));
    sendingPane.setBounds(10, 10, 480, 291);
    getContentPane().add(sendingPane);

    JLabel sendingLabel = new JLabel("SENDING");
    sendingLabel.setHorizontalAlignment(SwingConstants.CENTER);
    sendingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    sendingPane.setColumnHeaderView(sendingLabel);

    sendingLog = new JTextPane();
    sendingLog.setFont(new Font("Consolas", Font.PLAIN, 13));
    sendingLog.setEditable(false);
    sendingPane.setViewportView(sendingLog);
  }

  /**
   * Create a ReceivedLog textPane in this View.
   */
  private void drawReceivedLogArea() {
    JScrollPane receivedPane = new JScrollPane();
    receivedPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED,
            null, null, null, null));
    receivedPane.setBounds(497, 10, 489, 291);
    getContentPane().add(receivedPane);

    JLabel receivedLable = new JLabel("RECEIVED");
    receivedLable.setHorizontalAlignment(SwingConstants.CENTER);
    receivedLable.setFont(new Font("Arial", Font.PLAIN, 16));
    receivedPane.setColumnHeaderView(receivedLable);

    receivedLog = new JTextPane();
    receivedLog.setFont(new Font("Consolas", Font.PLAIN, 13));
    receivedLog.setEditable(false);
    receivedPane.setViewportView(receivedLog);
  }

  /**
   * Create a BlockChain textPane in this View.
   */
  private void drawBlockchainLogArea() {
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED,
            null, null, null, null));
    scrollPane.setBounds(10, 315, 976, 350);
    getContentPane().add(scrollPane);

    JLabel blockchainLable = new JLabel("CURRENT BLOCKCHAIN");
    blockchainLable.setHorizontalAlignment(SwingConstants.CENTER);
    blockchainLable.setFont(new Font("Arial", Font.PLAIN, 16));
    scrollPane.setColumnHeaderView(blockchainLable);

    blockchainLog = new JTextPane();
    blockchainLog.setFont(new Font("Consolas", Font.PLAIN, 13));
    blockchainLog.setEditable(false);
    scrollPane.setViewportView(blockchainLog);
  }

  /**
   * Add a starter object to this View.
   *
   * @param starter a starter object provides start operation of a Controller
   */
  public void addStarter(Starter starter) {
    this.starter = starter;
  }

  /**
   * Print the sending log to the textPane.
   *
   * @param sendingText a text string to be printed
   */
  public synchronized void printSendingLog(String sendingText) {
    sendingLog.setText(sendingText);
  }

  /**
   * Print the received log to the textPane.
   *
   * @param receivedText a text string to be printed
   */
  public synchronized void printReceivedLog(String receivedText) {
    receivedLog.setText(receivedText);
  }

  /**
   * Print the BlockChain to the textPane.
   *
   * @param blockChainText a text string to be printed
   */
  public synchronized void printBlockChainLog(String blockChainText) {
    blockchainLog.setText(blockChainText);
  }

  /**
   * Add an ActionListener to the start button of this View.
   */
  public void addListener() {
    this.startButton.addActionListener(e -> starter.start());
  }
}
