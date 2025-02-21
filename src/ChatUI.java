import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ChatUI extends JFrame {
    private JPanel messagesPanel; // Panel to hold messages
    private JTextField inputField; // Text field for message input
    private JButton sendButton; // Send button
    private JScrollPane scrollPane; // Scroll pane for messages
    private PrintWriter serverWriter; // Server writer to send messages

    public ChatUI(PrintWriter serverWriter) {
        this.serverWriter = serverWriter;

        // Window settings
        setTitle("Chat Client");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel for messages
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(new Color(131, 161, 211)); // Light blue background

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Input field with placeholder
        inputField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    g.setColor(new Color(184, 184, 184)); // Placeholder color
                    g.drawString("Type here something", 10, 20); // Placeholder text
                }
            }
        };
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.setForeground(Color.BLACK); // Text color
        inputField.setBackground(Color.WHITE); // Background color
        inputField.setBorder(BorderFactory.createEmptyBorder()); // Remove border
        inputField.addActionListener(e -> sendMessage()); // Send on pressing Enter

        // Setting an icon to the button
        sendButton = new JButton();
        ImageIcon arrowIcon = new ImageIcon("C:\\Users\\Dimka\\Documents\\IdeaProjects\\utpProject02\\src\\SendArrow.png");
        Image scaledImage = arrowIcon.getImage().getScaledInstance(55, 35, Image.SCALE_SMOOTH); // Scale the image
        sendButton.setIcon(new ImageIcon(scaledImage));
        sendButton.setBorder(BorderFactory.createEmptyBorder());
        sendButton.setContentAreaFilled(false); // Remove button background
        sendButton.addActionListener(e -> sendMessage()); // Send on button click

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Add components to the frame
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // Set frame background color
        getContentPane().setBackground(new Color(173, 216, 230)); // Light blue

        setVisible(true); // Show the window
    }

    // Append messages to the chat
    private void appendStyledMessage(String message, boolean isServerMessage, Color backgroundColor) {
        // Add the prefix "You: \n" only for the client messages
        String fullMessage = message;

        JTextArea messageArea = new JTextArea(fullMessage);


        try {
            // Load custom font from file
            File fontFile = new File("C:\\Users\\Dimka\\Documents\\IdeaProjects\\utpProject02\\src\\VCR_OSD_MONO_1.001.ttf");
            Font vcrOsdFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            messageArea.setFont(vcrOsdFont.deriveFont(14f)); // Set the font size
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        messageArea.setLineWrap(true);  // Enable line wrapping
        messageArea.setWrapStyleWord(true);  // Wrap at word boundaries
        messageArea.setEditable(false);
        messageArea.setBackground(backgroundColor);
        messageArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Calculate the width based on the message length (limit width)
        int messageWidth = Math.min(message.length() * 8 + 50, 400);  // 50 for margin adjustment, max width 400px

        // Set a fixed height to prevent shrinking, using a fixed line height of 25 pixels
        int messageHeight = Math.max(50, messageArea.getPreferredSize().height);  // Ensure a minimum height

        messageArea.setPreferredSize(new Dimension(messageWidth, messageHeight));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(messageArea, isServerMessage ? BorderLayout.WEST : BorderLayout.EAST);
        wrapper.setOpaque(false);  // Transparent background

        messagesPanel.add(wrapper);  // Add message to the panel
        messagesPanel.revalidate();
        messagesPanel.repaint();

        // Auto-scroll to the bottom
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }


    // Append a user message
    public void appendUserMessage(String message) {
        appendStyledMessage("You: " + message, false, new Color(173, 216, 230)); // Light blue
    }

    // Append a server message
    public void appendServerMessage(String message) {
        appendStyledMessage(message, true, new Color(220, 220, 220)); // Gray
    }

    // Send a message
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            serverWriter.println(message); // Send to server
            serverWriter.flush();
            appendUserMessage(message); // Show user message in UI
            inputField.setText(""); // Clear input field
        }
    }

}
