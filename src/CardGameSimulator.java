import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

// --- Generic Deck Class ---
class Deck<T> {
    List<T> cards;

    public Deck(List<T> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public T dealCard() {
        return cards.isEmpty() ? null : cards.remove(0);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public void reset(List<T> newCards) {
        cards.clear();
        cards.addAll(newCards);
    }
}

// --- Card Class ---
class StandardCard {
    private final String suit;
    private final String rank;

    public StandardCard(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public String getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}

// --- Generic Player Class ---
class Player<T> {
    private final String name;
    private final List<T> hand;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public void addCard(T card) {
        hand.add(card);
    }

    public List<T> getHand() {
        return hand;
    }

    public String getName() {
        return name;
    }

    public void clearHand() {
        hand.clear();
    }

    public String showHand() {
        StringBuilder sb = new StringBuilder();
        for (T card : hand) {
            sb.append(card.toString()).append("\n");
        }
        return sb.toString();
    }
}

// --- Animated Button Panel ---
class AnimatedPanel extends JPanel {
    private float alpha = 0f;
    private final Timer fadeTimer;
    
    public AnimatedPanel() {
        setOpaque(false);
        fadeTimer = new Timer(50, e -> {
            alpha = Math.min(1f, alpha + 0.1f);
            repaint();
            if (alpha >= 1f) {
                ((Timer)e.getSource()).stop();
            }
        });
        fadeTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paintComponent(g2d);
        g2d.dispose();
    }
}

// --- Custom Button ---
class GameButton extends JButton {
    private Color hoverColor;
    private Color normalColor;
    private Timer pulseTimer;
    private float pulseScale = 1.0f;
    private boolean growing = true;

    public GameButton(String text, Color color) {
        super(text);
        this.normalColor = color;
        this.hoverColor = color.darker();
        setupButton();
        setupPulseAnimation();
    }

    private void setupButton() {
        setBackground(normalColor);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("Arial", Font.BOLD, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(200, 60));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                setBackground(normalColor);
            }
        });
    }

    private void setupPulseAnimation() {
        pulseTimer = new Timer(50, e -> {
            if (growing) {
                pulseScale += 0.01f;
                if (pulseScale >= 1.05f) growing = false;
            } else {
                pulseScale -= 0.01f;
                if (pulseScale <= 0.95f) growing = true;
            }
            repaint();
        });
        pulseTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        // Calculate scaled dimensions
        int sw = (int)(w * pulseScale);
        int sh = (int)(h * pulseScale);
        int x = (w - sw) / 2;
        int y = (h - sh) / 2;
        
        g2d.setColor(getBackground());
        g2d.fillRoundRect(x, y, sw, sh, 20, 20);
        
        g2d.setColor(getForeground());
        FontMetrics fm = g2d.getFontMetrics();
        String text = getText();
        int textX = (w - fm.stringWidth(text)) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);
        
        g2d.dispose();
    }
}

// --- Main UI Class ---
public class CardGameSimulator extends JFrame {
    private final JTextArea displayArea = new JTextArea(10, 30);
    private final GameButton blackjackButton = new GameButton("Play Blackjack", new Color(50, 205, 50));
    private final GameButton highCardButton = new GameButton("Play High Card", new Color(255, 69, 0));
    private final GameButton guessCardButton = new GameButton("Play Guess the Card", new Color(147, 112, 219));
    private final GameButton slapjackButton = new GameButton("Play Slapjack", new Color(255, 215, 0));

    public CardGameSimulator() {
        setTitle("Card Game Menu");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));

        // Gradient background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(25, 25, 112),
                    0, h, new Color(72, 61, 139)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };
        setContentPane(backgroundPanel);
        backgroundPanel.setLayout(new BorderLayout(20, 20));

        // Title Panel
        JPanel titlePanel = new AnimatedPanel();
        JLabel titleLabel = new JLabel("Card Game Collection");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel);
        titlePanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        backgroundPanel.add(titlePanel, BorderLayout.NORTH);

        // Display Area with custom styling
        displayArea.setEditable(false);
        displayArea.setBackground(new Color(255, 255, 255, 220));
        displayArea.setForeground(new Color(25, 25, 112));
        displayArea.setFont(new Font("Arial", Font.PLAIN, 16));
        displayArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel with animation
        JPanel menuPanel = new AnimatedPanel();
        menuPanel.setLayout(new GridLayout(2, 2, 20, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        menuPanel.add(blackjackButton);
        menuPanel.add(highCardButton);
        menuPanel.add(guessCardButton);
        menuPanel.add(slapjackButton);
        backgroundPanel.add(menuPanel, BorderLayout.SOUTH);

        // Button Actions
        blackjackButton.addActionListener(e -> {
            fadeOutAndDispose();
            new BlackjackGame().setVisible(true);
        });

        highCardButton.addActionListener(e -> {
            fadeOutAndDispose();
            new HighCardGame().setVisible(true);
        });

        guessCardButton.addActionListener(e -> playGuessTheCard());

        slapjackButton.addActionListener(e -> {
            fadeOutAndDispose();
            new SlapjackGame().setVisible(true);
        });
    }

    private void fadeOutAndDispose() {
        Timer fadeTimer = new Timer(50, new ActionListener() {
            float opacity = 1.0f;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.1f;
                if (opacity <= 0) {
                    ((Timer)e.getSource()).stop();
                    dispose();
                } else {
                    setOpacity(opacity);
                }
            }
        });
        fadeTimer.start();
    }

    private void playGuessTheCard() {
        List<StandardCard> deck = new Deck<>(new BlackjackGame().createStandardDeck()).cards;
        Random rand = new Random();
        StandardCard chosenCard = deck.get(rand.nextInt(deck.size()));
        
        JDialog dialog = new JDialog(this, "Guess the Card", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("Guess the rank of the card (e.g., Ace, 2, King):");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        
        JTextField input = new JTextField(20);
        input.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton submitButton = new GameButton("Submit Guess", new Color(50, 205, 50));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        submitButton.addActionListener(e -> {
            String guess = input.getText();
            if (guess != null && !guess.trim().isEmpty()) {
                dialog.dispose();
                showResult(guess.trim(), chosenCard);
            }
        });
        
        dialog.setVisible(true);
    }

    private void showResult(String guess, StandardCard chosenCard) {
        JDialog resultDialog = new JDialog(this, "Result", true);
        resultDialog.setLayout(new BorderLayout(10, 10));
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String message = guess.equalsIgnoreCase(chosenCard.getRank()) ?
            "Correct! It was: " + chosenCard :
            "Wrong! It was: " + chosenCard;
            
        JLabel label = new JLabel(message);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(label, BorderLayout.CENTER);
        
        resultDialog.add(panel);
        resultDialog.pack();
        resultDialog.setLocationRelativeTo(this);
        resultDialog.setVisible(true);
        
        // Auto-close after 2 seconds
        Timer timer = new Timer(2000, e -> resultDialog.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            CardGameSimulator game = new CardGameSimulator();
            game.setLocationRelativeTo(null);
            game.setVisible(true);
        });
    }
}

// --- Blackjack Game Frame ---
class BlackjackGame extends JFrame {
    protected List<StandardCard> createStandardDeck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        List<StandardCard> cards = new ArrayList<>();
        for (String suit : suits) {
            for (String rank : ranks) {
                cards.add(new StandardCard(suit, rank));
            }
        }
        return cards;
    }

    private final JTextArea displayArea = new JTextArea(10, 30);
    private final JButton dealButton = new JButton("Deal (Blackjack)");
    private final JButton shuffleButton = new JButton("Shuffle Deck");
    private final JButton resetDeckButton = new JButton("Reset Deck");
    private final JButton hitButton = new JButton("Hit (Player 1)");
    private final JButton standButton = new JButton("Stand (Player 1)");
    private final JButton backButton = new JButton("← Back to Menu");
    private Deck<StandardCard> deck;
    private final Player<StandardCard> player1 = new Player<>("Player 1");
    private final Player<StandardCard> dealer = new Player<>("Dealer");
    private boolean playerTurn = true;

    public BlackjackGame() {
        setTitle("Blackjack Simulator");
        setSize(500, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(new Color(34, 139, 34)); // Forest green background

        displayArea.setEditable(false);
        displayArea.setBackground(new Color(255, 255, 255));
        displayArea.setForeground(new Color(0, 100, 0)); // Dark green text
        displayArea.setFont(new Font("Arial", Font.PLAIN, 14));
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(34, 139, 34));
        styleBlackjackButton(dealButton, new Color(0, 100, 0));     // Dark green
        styleBlackjackButton(hitButton, new Color(220, 20, 60));    // Crimson
        styleBlackjackButton(standButton, new Color(25, 25, 112));  // Dark blue
        styleBlackjackButton(shuffleButton, new Color(128, 0, 0));  // Maroon
        styleBlackjackButton(resetDeckButton, new Color(85, 107, 47)); // Dark olive
        styleBlackjackButton(backButton, new Color(128, 128, 128)); // Gray
        buttonPanel.add(dealButton);
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);
        buttonPanel.add(shuffleButton);
        buttonPanel.add(resetDeckButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        deck = new Deck<>(createStandardDeck());

        shuffleButton.addActionListener(e -> {
            deck.shuffle();
            display("Deck shuffled!\n");
        });

        resetDeckButton.addActionListener(e -> {
            deck.reset(createStandardDeck());
            display("Deck reset to full 52 cards.\n");
        });

        dealButton.addActionListener(e -> dealBlackjack());
        hitButton.addActionListener(e -> hitPlayer());
        standButton.addActionListener(e -> dealerTurn());
        backButton.addActionListener(e -> {
            dispose();
            new CardGameSimulator().setVisible(true);
        });
    }

    private void styleBlackjackButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }

    private void dealBlackjack() {
        if (deck.size() < 4) {
            display("Not enough cards. Please shuffle or reset.\n");
            return;
        }

        player1.clearHand();
        dealer.clearHand();
        playerTurn = true;

        player1.addCard(deck.dealCard());
        dealer.addCard(deck.dealCard());
        player1.addCard(deck.dealCard());
        dealer.addCard(deck.dealCard());

        updateDisplay();
    }

    private void hitPlayer() {
        if (!playerTurn) return;
        player1.addCard(deck.dealCard());
        updateDisplay();
        if (calculateScore(player1) > 21) {
            display("Player 1 busts! Dealer wins.\n");
            playerTurn = false;
        }
    }

    private void dealerTurn() {
        if (!playerTurn) return;
        playerTurn = false;

        while (calculateScore(dealer) < 17) {
            dealer.addCard(deck.dealCard());
        }
        updateDisplay();
        int pScore = calculateScore(player1);
        int dScore = calculateScore(dealer);
        if (dScore > 21 || pScore > dScore) {
            display("Player 1 wins!\n");
        } else if (pScore < dScore) {
            display("Dealer wins!\n");
        } else {
            display("It's a tie!\n");
        }
    }

    private int calculateScore(Player<StandardCard> player) {
        int score = 0;
        int aceCount = 0;
        for (StandardCard card : player.getHand()) {
            String rank = card.getRank();
            if (rank.equals("Ace")) {
                score += 11;
                aceCount++;
            } else if (rank.equals("King") || rank.equals("Queen") || rank.equals("Jack")) {
                score += 10;
            } else {
                score += Integer.parseInt(rank);
            }
        }
        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }
        return score;
    }

    private void updateDisplay() {
        display("\nPlayer 1 Hand (" + calculateScore(player1) + "):\n" + player1.showHand());
        display("\nDealer Hand (" + calculateScore(dealer) + "):\n" + dealer.showHand());
    }

    private void display(String message) {
        displayArea.append(message);
    }
}

// --- High Card Game Frame ---
class HighCardGame extends JFrame {
    private final JTextArea displayArea = new JTextArea(10, 30);
    private final JButton playerDrawButton = new JButton("Draw for Player");
    private final JButton dealerDrawButton = new JButton("Draw for Dealer");
    private Deck<StandardCard> deck;
    private final Player<StandardCard> player = new Player<>("Player");
    private final Player<StandardCard> dealer = new Player<>("Dealer");
    private boolean gameInProgress = false;

    public HighCardGame() {
        setTitle("High Card");
        setSize(500, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(new Color(230, 230, 250)); // Lavender background

        displayArea.setEditable(false);
        displayArea.setBackground(new Color(255, 255, 255));
        displayArea.setForeground(new Color(75, 0, 130)); // Indigo text
        displayArea.setFont(new Font("Arial", Font.PLAIN, 14));
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(230, 230, 250));
        styleHighCardButton(playerDrawButton, new Color(0, 128, 128)); // Teal
        styleHighCardButton(dealerDrawButton, new Color(128, 0, 128)); // Purple
        buttonPanel.add(playerDrawButton);
        buttonPanel.add(dealerDrawButton);
        add(buttonPanel, BorderLayout.SOUTH);

        deck = new Deck<>(createStandardDeck());
        deck.shuffle();

        playerDrawButton.addActionListener(e -> drawForPlayer());
        dealerDrawButton.addActionListener(e -> drawForDealer());
    }

    private List<StandardCard> createStandardDeck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        List<StandardCard> cards = new ArrayList<>();
        for (String suit : suits) {
            for (String rank : ranks) {
                cards.add(new StandardCard(suit, rank));
            }
        }
        return cards;
    }

    private void styleHighCardButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }

    private void drawForPlayer() {
        if (!gameInProgress) {
            player.clearHand();
            dealer.clearHand();
            gameInProgress = true;
        }
        
        if (player.getHand().size() > 0) {
            display("Player already drew a card!\n");
            return;
        }

        if (deck.isEmpty()) {
            deck = new Deck<>(createStandardDeck());
            deck.shuffle();
        }

        player.addCard(deck.dealCard());
        updateDisplay();
    }

    private void drawForDealer() {
        if (!gameInProgress) {
            display("Player must draw first!\n");
            return;
        }

        if (dealer.getHand().size() > 0) {
            display("Dealer already drew a card!\n");
            return;
        }

        if (deck.isEmpty()) {
            deck = new Deck<>(createStandardDeck());
            deck.shuffle();
        }

        dealer.addCard(deck.dealCard());
        String result = determineWinner();
        updateDisplay();
        gameInProgress = false;

        // Prompt for new game with winner information
        int choice = JOptionPane.showConfirmDialog(this, 
            result + "\n\nWould you like to play again?", 
            "Game Over", 
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            player.clearHand();
            dealer.clearHand();
            gameInProgress = false;
            updateDisplay();
        } else {
            dispose();
            new CardGameSimulator().setVisible(true);
        }
    }

    private String determineWinner() {
        if (player.getHand().isEmpty() || dealer.getHand().isEmpty()) {
            return "";
        }

        StandardCard playerCard = player.getHand().get(0);
        StandardCard dealerCard = dealer.getHand().get(0);
        
        int playerValue = getCardValue(playerCard);
        int dealerValue = getCardValue(dealerCard);

        if (playerValue > dealerValue) {
            return "Player wins with " + playerCard + " vs " + dealerCard + "!";
        } else if (dealerValue > playerValue) {
            return "Dealer wins with " + dealerCard + " vs " + playerCard + "!";
        } else {
            return "It's a tie! Both have " + playerCard.getRank() + "!";
        }
    }

    private int getCardValue(StandardCard card) {
        String rank = card.getRank();
        switch (rank) {
            case "Ace": return 14;
            case "King": return 13;
            case "Queen": return 12;
            case "Jack": return 11;
            default: return Integer.parseInt(rank);
        }
    }

    private void updateDisplay() {
        displayArea.setText("");
        display("Player's card:\n" + player.showHand() + "\n");
        display("Dealer's card:\n" + dealer.showHand() + "\n");
    }

    private void display(String message) {
        displayArea.append(message);
    }
}

// --- Slapjack Game Frame ---
class SlapjackGame extends JFrame {
    private final JTextArea displayArea = new JTextArea(10, 30);
    private final JButton slapButton = new JButton("SLAP!");
    private final JComboBox<String> timerComboBox;
    private Deck<StandardCard> deck;
    private final Player<StandardCard> player = new Player<>("Player");
    private StandardCard currentCard;
    private boolean isJack = false;
    private Timer flipTimer;
    private Timer slapTimer;
    private int score = 0;
    private String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
    private int currentSuitIndex = 0;

    public SlapjackGame() {
        setTitle("Slapjack");
        setSize(500, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(new Color(255, 240, 245)); // Misty rose background

        displayArea.setEditable(false);
        displayArea.setBackground(new Color(255, 255, 255));
        displayArea.setForeground(new Color(139, 0, 139)); // Dark magenta text
        displayArea.setFont(new Font("Arial", Font.PLAIN, 14));
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        // Create timer selection dropdown
        String[] timerOptions = {"1 second", "1.5 seconds", "2 seconds", "2.5 seconds", "3 seconds"};
        timerComboBox = new JComboBox<>(timerOptions);
        timerComboBox.setSelectedIndex(2); // Default to 2 seconds
        timerComboBox.addActionListener(e -> updateSlapTimer());

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(255, 240, 245));
        controlPanel.add(new JLabel("Slap Timer: "));
        controlPanel.add(timerComboBox);
        add(controlPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 240, 245));
        styleSlapButton(slapButton);
        buttonPanel.add(slapButton);
        add(buttonPanel, BorderLayout.SOUTH);

        deck = new Deck<>(createDeckForCurrentSuit());
        deck.shuffle();

        slapButton.addActionListener(e -> slap());

        // Initialize flip timer (flips card every 1.5 seconds)
        flipTimer = new Timer(1500, e -> flipCard());
        flipTimer.setRepeats(true);

        // Initialize slap timer with default 2 seconds
        slapTimer = new Timer(2000, e -> {
            if (isJack) {
                display("Too slow! You missed the Jack!\n");
                endGame();
            }
        });
        slapTimer.setRepeats(false);

        // Start the game
        display("Game started! Watch for Jacks and SLAP!\n");
        display("Current suit: " + suits[currentSuitIndex] + "\n");
        flipTimer.start();
    }

    private void updateSlapTimer() {
        String selectedTime = (String) timerComboBox.getSelectedItem();
        int milliseconds = (int) (Double.parseDouble(selectedTime.split(" ")[0]) * 1000);
        slapTimer.setDelay(milliseconds);
    }

    private List<StandardCard> createDeckForCurrentSuit() {
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        List<StandardCard> cards = new ArrayList<>();
        for (String rank : ranks) {
            cards.add(new StandardCard(suits[currentSuitIndex], rank));
        }
        return cards;
    }

    private void moveToNextSuit() {
        currentSuitIndex = (currentSuitIndex + 1) % suits.length;
        deck = new Deck<>(createDeckForCurrentSuit());
        deck.shuffle();
        display("\nMoving to " + suits[currentSuitIndex] + " suit!\n");
    }

    private void flipCard() {
        if (deck.isEmpty()) {
            if (currentSuitIndex < suits.length - 1) {
                moveToNextSuit();
                return;
            } else {
                flipTimer.stop();
                display("All suits completed! Game Over!\n");
                endGame();
                return;
            }
        }

        currentCard = deck.dealCard();
        isJack = currentCard.getRank().equals("Jack");
        
        display("Card flipped: " + currentCard + "\n");
        if (isJack) {
            display("JACK! SLAP NOW!\n");
            slapTimer.restart();
        }
    }

    private void slap() {
        if (!isJack) {
            display("No Jack to slap! -1 point penalty\n");
            score = Math.max(0, score -1);
            return;
        }

        slapTimer.stop();
        isJack = false;
        
        // Add the current card to the player's pile
        player.addCard(currentCard);
        score++;
        display("Great slap! +1 point\n");
    }

    private void endGame() {
        flipTimer.stop();
        slapTimer.stop();
        
        display("\nFinal Score:\n");
        display("Your score: " + score + " points\n");
        display("Cards collected: " + player.getHand().size() + "\n");
        
        // Ask to play again
        int choice = JOptionPane.showConfirmDialog(this, 
            "Would you like to play again?", 
            "Game Over", 
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            dispose();
            new CardGameSimulator().setVisible(true);
        }
    }

    private void resetGame() {
        currentSuitIndex = 0;
        deck = new Deck<>(createDeckForCurrentSuit());
        deck.shuffle();
        player.clearHand();
        score = 0;
        isJack = false;
        displayArea.setText("");
        display("New game started! Watch for Jacks and SLAP!\n");
        display("Current suit: " + suits[currentSuitIndex] + "\n");
        flipTimer.start();
    }

    private void styleSlapButton(JButton button) {
        button.setBackground(new Color(255, 0, 0)); // Bright red
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(220, 0, 0));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 0, 0));
            }
        });
    }

    private void display(String message) {
        displayArea.append(message);
    }
}