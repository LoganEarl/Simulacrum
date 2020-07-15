package main.java.network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientForm {
    private JPanel clientView;
    private JTextField inputText;
    private JTextPane outputText;
    private boolean quit;

    private BufferedInputStream in;
    private BufferedOutputStream out;

    private String sessionLog = "";
    private int selectIndex = -1;

    private static final String CREATE_CHEPE = "register Chepe a;create new character;Chepe;human;add 25 to str;add 25 to dex;add 40 to fit;add 10 to tough;done;yes";
    private static final String CREATE_CART = "register Cart a;create new character;Cart;human;add 25 to str;add 25 to dex;add 40 to fit;add 10 to tough;done;yes";

    private final List<String> waitingMessages = Collections.synchronizedList(new ArrayList<>());

    private final java.util.List<String> prevEntries = new LinkedList<>();

    public static void main(String... args) {
        JFrame frame = new JFrame("Simulacrum");
        ClientForm mForm = new ClientForm();
        frame.setContentPane(mForm.clientView);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        String address;
        int port;
        String defaultAddress = "localhost:5555";
        boolean goodAddress = false;
        while (!goodAddress) {
            address = JOptionPane.showInputDialog("Gib server",defaultAddress);
            if(address != null) {
                String[] addressArgs = address.split(":");
                defaultAddress = address;
                try {
                    if (addressArgs.length == 2) {
                        address = addressArgs[0];
                        port = Integer.parseInt(addressArgs[1]);
                        mForm.setupSocket(address, port);
                        goodAddress = true;
                    }
                } catch (Exception ignored) {
                }
            }else
                break;
        }
        if(goodAddress)
            mForm.init();
        else
            System.exit(0);
    }

    private void appendToDisplayText(final String text) {
        SwingUtilities.invokeLater(() -> {
            String convertedText = text.replace("\n", "<br>");
            convertedText = convertedText.replace("\t", "&emsp;");
            convertedText = convertedText.replace("  ", "&nbsp;<wr>&nbsp;<wr>");
            sessionLog += convertedText + "<br>";

            outputText.setText(sessionLog);

            String curText = outputText.getText();
            if (curText.length() > 10000)
                outputText.setText(curText.substring(curText.length() - 10000));
        });
    }

    private void init() {
        inputText.setForeground(Color.WHITE);
        inputText.setBackground(Color.BLACK);

        outputText.setForeground(Color.WHITE);
        outputText.setBackground(Color.BLACK);
        outputText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        inputText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String entry = inputText.getText();
                    if (prevEntries.size() == 0 || (!prevEntries.get(prevEntries.size() - 1).equals(entry)))
                        prevEntries.add(prevEntries.size(), entry);
                    selectIndex = prevEntries.size();
                    inputText.setText("");

                    if(entry.equals("CARTTIME"))
                        entry = CREATE_CART;
                    if(entry.equals("CHEPETIME"))
                        entry = CREATE_CHEPE;

                    String[] entries = entry.split(";");
                    waitingMessages.addAll(Arrays.asList(entries));

                } else if (e.getKeyCode() == KeyEvent.VK_UP && prevEntries.size() > 0) {
                    selectIndex--;
                    if (selectIndex < 0)
                        selectIndex = 0;
                    inputText.setText(prevEntries.get(selectIndex));
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN && prevEntries.size() > 0) {
                    selectIndex++;
                    if (selectIndex >= prevEntries.size()) {
                        selectIndex = prevEntries.size();
                        inputText.setText("");
                    } else
                        inputText.setText(prevEntries.get(selectIndex));
                }
            }
        });

        inputText.grabFocus();
    }

    private void sendMessage(String entry){
        String[] words = entry.split(" ");
        StringBuilder messageToSend = new StringBuilder();

        boolean firstLine = true;
        for (String word : words) {
            if (firstLine)
                firstLine = false;
            else
                messageToSend.append("\n");
            messageToSend.append(word);
        }

        messageToSend.append(WebServer.MESSAGE_DIVIDER);

        try {
            out.write(messageToSend.toString().getBytes());
            out.flush();
        } catch (Exception ignored) {

        }
    }

    private void setupSocket(String address, int port) throws Exception{
        Socket socket = new Socket(address, port);
        in = new BufferedInputStream(socket.getInputStream());

        out = new BufferedOutputStream(socket.getOutputStream());

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                int available = in.available();
                if (available > 0) {
                    byte[] message = new byte[available];
                    //noinspection ResultOfMethodCallIgnored
                    in.read(message, 0, available);
                    String rawMessages = new String(message);
                    rawMessages = rawMessages.replace("SERVER_PROMPT_MESSAGE\n", "");
                    String[] messages = rawMessages.split("<!EOM!>");

                    for (String msg : messages)
                        appendToDisplayText(msg);
                }
            }catch (IOException e){
                quit = true;
            }
        }, 50,50, TimeUnit.MILLISECONDS);

        executor.scheduleAtFixedRate(() -> {
                try{
                    if(waitingMessages.size() > 0)
                        sendMessage(waitingMessages.remove(0));
                    Thread.sleep(300);
                }catch (Exception e) {
                    quit = true;
                }
            },500,500,TimeUnit.MILLISECONDS);
    }
}
