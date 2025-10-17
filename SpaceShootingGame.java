import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

public class SpaceShootingGame extends JPanel implements KeyListener, ActionListener {

    private Timer timer;
    private int spaceshipX = 225;
    private final int spaceshipY = 450;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private final int spaceshipSpeed = 5;

    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();

    private int enemySpeed = 2;
    private int score = 0;
    private boolean gameOver = false;

    public SpaceShootingGame() {
        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.BLACK);

        // Create enemies
        for (int i = 0; i < 5; i++) {
            enemies.add(new Enemy(50 + i * 80, 30));
        }

        timer = new Timer(15, this);
        timer.start();

        addKeyListener(this);
        setFocusable(true);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw spaceship
        g.setColor(Color.CYAN);
        g.fillRect(spaceshipX, spaceshipY, 50, 20);

        // Draw bullets
        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g.fillRect(b.x, b.y, 5, 10);
        }

        // Draw enemies
        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            g.fillOval(e.x, e.y, 40, 40);
        }

        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, 10, 20);

        // Game over message
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER", 120, 250);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // Move spaceship
            if (leftPressed && spaceshipX > 0) {
                spaceshipX -= spaceshipSpeed;
            }
            if (rightPressed && spaceshipX < getWidth() - 50) {
                spaceshipX += spaceshipSpeed;
            }

            // Move bullets
            Iterator<Bullet> bulletIter = bullets.iterator();
            while (bulletIter.hasNext()) {
                Bullet b = bulletIter.next();
                b.y -= 7;
                if (b.y < 0) {
                    bulletIter.remove();
                }
            }

            // Move enemies
            for (Enemy enemy : enemies) {
                enemy.y += enemySpeed;
                if (enemy.y > spaceshipY + 20) {
                    gameOver = true;
                    timer.stop();
                }
            }

            // Check bullet-enemy collisions
            Iterator<Bullet> bIter = bullets.iterator();
            while (bIter.hasNext()) {
                Bullet b = bIter.next();
                Iterator<Enemy> eIter = enemies.iterator();
                while (eIter.hasNext()) {
                    Enemy enemy = eIter.next();
                    Rectangle bulletRect = new Rectangle(b.x, b.y, 5, 10);
                    Rectangle enemyRect = new Rectangle(enemy.x, enemy.y, 40, 40);
                    if (bulletRect.intersects(enemyRect)) {
                        bIter.remove();
                        eIter.remove();
                        score += 10;
                        break;
                    }
                }
            }

            // If all enemies destroyed, respawn
            if (enemies.isEmpty()) {
                for (int i = 0; i < 5; i++) {
                    enemies.add(new Enemy(50 + i * 80, 30));
                }
                // Increase speed a bit each wave
                enemySpeed++;
            }

            repaint();
        }
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (code == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if (code == KeyEvent.VK_SPACE) {
            if (!gameOver) {
                bullets.add(new Bullet(spaceshipX + 22, spaceshipY));
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }

    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Shooting Game");
        SpaceShootingGame gamePanel = new SpaceShootingGame();
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Inner classes for bullets and enemies
    class Bullet {
        int x, y;

        Bullet(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    class Enemy {
        int x, y;

        Enemy(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
