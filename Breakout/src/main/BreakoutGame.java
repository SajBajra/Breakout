package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class BreakoutGame extends JPanel implements ActionListener {
    private static final long serialVersionUID = 7613158128582787310L;
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 400;
    static int PADDLE_WIDTH = 100;
    static final int PADDLE_HEIGHT = 10;
    static final int BALL_SIZE = 20;
    static final int ROWS = 5;
    static final int COLS = 12;  
    static final int BRICK_WIDTH = SCREEN_WIDTH / COLS;  
    static final int BRICK_HEIGHT = 20;
   
    static final int POWERUP_SIZE = 20;
    int highScore = 0; 
    int paddleX = SCREEN_WIDTH / 2 - PADDLE_WIDTH / 2;
    int paddleY = SCREEN_HEIGHT - 50;
    int ballX = SCREEN_WIDTH / 2;
    int ballY = SCREEN_HEIGHT / 2;
    int ballDX = -2;
    int ballDY = -3;
    int score = 0;
    int lives = 3;
    int difficulty;
    boolean running = true;
    boolean paused = false; 
    Timer timer;
    boolean[][] bricks;
    boolean win = false; 
    Color[][] brickColors;
    PowerUp powerUp;
    Random rand = new Random();

    Rectangle ballRect = new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE);
    Rectangle paddleRect = new Rectangle(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT);

    public enum PowerType { INCREASE_PADDLE, EXTRA_LIFE, INCREASE_BALL_SPEED }

    PowerType activePowerUp;

    BreakoutGame(int difficulty) {
        this.difficulty = difficulty;
        setDifficulty();
        initializeGame();
    }

    public void initializeGame() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(new Color(20, 20, 30)); 
        this.setFocusable(true);
        this.setDoubleBuffered(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT && paddleX > 0) {
                    paddleX -= 15;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && paddleX < SCREEN_WIDTH - PADDLE_WIDTH) {
                    paddleX += 15;
                }else if (e.getKeyCode() == KeyEvent.VK_P) {
                    paused = !paused; // Toggle pause state
                }else if (e.getKeyCode() == KeyEvent.VK_R) {
                    restartGame(); // Restart the game
                }


            }
        });
        initializeBricks();
        timer = new Timer(1000 / 60, this); // 60 FPS
        timer.start();
    }

    public void setDifficulty() {
        switch (difficulty) {
            case 1 -> { ballDX = -2; ballDY = -2; lives = 5; }
            case 2 -> { ballDX = -3; ballDY = -3; lives = 3; }
            case 3 -> { ballDX = -4; ballDY = -4; lives = 2; }
        }
    }

    public void initializeBricks() {
        bricks = new boolean[ROWS][COLS];
        brickColors = new Color[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                bricks[i][j] = true;
                brickColors[i][j] = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (running && !win) { // Check for win condition
            drawBricks(g);
            drawPaddle(g);
            drawBall(g);
            drawPowerUp(g);
            drawScore(g);
            drawHelp(g);
        } else if (win) {
            youWon(g); // Draw you won screen
        } else {
            gameOver(g);
        }
    }

    public void youWon(Graphics g) {
        g.setColor(Color.green);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("You Won!", (SCREEN_WIDTH - metrics.stringWidth("You Won!")) / 2, SCREEN_HEIGHT / 2 - 20);
        
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Score: " + score, (SCREEN_WIDTH - metrics.stringWidth("Score: " + score)) / 2, SCREEN_HEIGHT / 2 + 20);
        
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Press R to Restart", (SCREEN_WIDTH - metrics.stringWidth("Press R to Restart")) / 2, SCREEN_HEIGHT / 2 + 60);
    }


    public void drawBricks(Graphics g) {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (bricks[i][j]) {
                    g.setColor(brickColors[i][j]);
                    g.fill3DRect(j * BRICK_WIDTH, i * BRICK_HEIGHT, BRICK_WIDTH, BRICK_HEIGHT, true);
                }
            }
        }
    }

    public void drawPaddle(Graphics g) {
        GradientPaint paddleGradient = new GradientPaint(0, 0, Color.blue, PADDLE_WIDTH, PADDLE_HEIGHT, Color.darkGray);
        ((Graphics2D) g).setPaint(paddleGradient);
        g.fillRect(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
    }

    public void drawBall(Graphics g) {
        g.setColor(Color.white);
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
    }

    public void drawPowerUp(Graphics g) {
        if (powerUp != null && powerUp.isVisible) {
            switch (powerUp.type) {
                case INCREASE_PADDLE -> g.setColor(Color.green);
                case EXTRA_LIFE -> g.setColor(Color.red);
                case INCREASE_BALL_SPEED -> g.setColor(Color.yellow);
            }
            g.fillOval(powerUp.x, powerUp.y, POWERUP_SIZE, POWERUP_SIZE);
        }
    }

    public void drawScore(Graphics g) {
        g.setColor(Color.cyan);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Score: " + score, 10, SCREEN_HEIGHT - 10);
        g.drawString("Lives: " + lives, SCREEN_WIDTH - 80, SCREEN_HEIGHT - 10);
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	if (running && !paused) { // Modify this line
    	    moveBall();
    	    checkCollisions();
    	    movePowerUp();
    	    checkPowerUpCollection();
    	}
        repaint();
        if (score > highScore) {
            highScore = score; // Update the high score if current score is higher
        }

    }

    public void moveBall() {
        ballX += ballDX;
        ballY += ballDY;
        ballRect.setLocation(ballX, ballY);

        if (ballX < 0 || ballX > SCREEN_WIDTH - BALL_SIZE) {
            ballDX *= -1;
        }
        if (ballY < 0) {
            ballDY *= -1;
        }
        if (ballY > SCREEN_HEIGHT) {
            lives--;
            if (lives <= 0) {
                running = false;
            } else {
                resetBallPosition();
            }
        }
    }

    public void resetBallPosition() {
        ballX = SCREEN_WIDTH / 2;
        ballY = SCREEN_HEIGHT / 2;
        ballDY = -Math.abs(ballDY); // Start ball moving upwards
    }

    public void movePowerUp() {
        if (powerUp != null && powerUp.isVisible) {
            powerUp.y += 2;
            if (powerUp.y > SCREEN_HEIGHT) {
                powerUp = null; // Nullify when out of bounds
            }
        }
    }

    public void checkCollisions() {
        paddleRect.setLocation(paddleX, paddleY);

        if (ballRect.intersects(paddleRect)) {
            ballDY *= -1;
        }

        boolean allBricksDestroyed = true; // Check if all bricks are destroyed
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (bricks[i][j]) {
                    allBricksDestroyed = false; // If any brick is still there
                    Rectangle brickRect = new Rectangle(j * BRICK_WIDTH, i * BRICK_HEIGHT, BRICK_WIDTH, BRICK_HEIGHT);
                    if (ballRect.intersects(brickRect)) {
                        bricks[i][j] = false;
                        ballDY *= -1;
                        score += 10;
                        if (rand.nextInt(10) < 2) {
                            spawnPowerUp(j * BRICK_WIDTH, i * BRICK_HEIGHT);
                        }
                        return;
                    }
                }
            }
        }
        
        // If all bricks are destroyed, set win to true
        if (allBricksDestroyed) {
            win = true;
        }
    }


    public void checkPowerUpCollection() {
        if (powerUp != null && powerUp.isVisible && powerUp.getBounds().intersects(paddleRect)) {
            activatePowerUp();
            powerUp = null; // Nullify after collection
        }
    }

    public void spawnPowerUp(int x, int y) {
        powerUp = new PowerUp(x, y, PowerType.values()[rand.nextInt(PowerType.values().length)]);
    }

    public void activatePowerUp() {
        switch (powerUp.type) {
            case INCREASE_PADDLE -> {
                PADDLE_WIDTH += 30; // Increase paddle width
                paddleRect.setSize(PADDLE_WIDTH, PADDLE_HEIGHT); // Update the paddle rectangle
            }
            case EXTRA_LIFE -> lives++;
            case INCREASE_BALL_SPEED -> {
                ballDX *= 1.5;
                ballDY *= 1.5;
            }
        }
    }

    public void restartGame() {
        score = 0;
        lives = 3;
        win = false; // Reset win condition
        initializeBricks();
        setDifficulty();
        running = true;
        ballX = SCREEN_WIDTH / 2;
        ballY = SCREEN_HEIGHT / 2;
        ballDX = -2;
        ballDY = -3;
    }

    public void drawHelp(Graphics g) {
        if (paused) { // Check if the game is paused
            g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent background
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT); // Draw background overlay

            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Paused", SCREEN_WIDTH / 2 - 70, SCREEN_HEIGHT / 2 - 20);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Controls: Arrow Keys to Move, P to Pause, R to Restart", 
                         SCREEN_WIDTH / 2 - 200, SCREEN_HEIGHT / 2 + 10);
            g.drawString("Press P to Resume", 
                         SCREEN_WIDTH / 2 - 150, SCREEN_HEIGHT / 2 + 30);
        }
    }




    public static void main(String[] args) {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(null, "Choose difficulty:", "Difficulty Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        int difficulty = choice + 1;
        JFrame frame = new JFrame("Breakout Game");
        BreakoutGame gamePanel = new BreakoutGame(difficulty);
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    class PowerUp {
        int x, y;
        PowerType type;
        boolean isVisible = true;

        PowerUp(int x, int y, PowerType type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, POWERUP_SIZE, POWERUP_SIZE);
        }
    }
}
