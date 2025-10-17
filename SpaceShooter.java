//SpaceShooter.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SpaceShooter {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🚀 Space Shooter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            panel.startGame();
        });
    }
}

class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private Thread gameThread;
    private boolean running = false;

    private Player player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;
    private ArrayList<Star> stars;

    private int score = 0;
    private boolean gameOver = false;
    private Random rand = new Random();

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        player = new Player(WIDTH / 2 - 20, HEIGHT - 100);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        stars = new ArrayList<>();

        // create background stars
        for (int i = 0; i < 80; i++) stars.add(new Star(rand.nextInt(WIDTH), rand.nextInt(HEIGHT)));
    }

    public void startGame() {
        running = true;
        gameOver = false;
        score = 0;
        enemies.clear();
        bullets.clear();
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastTime;
            if (elapsed > 16) { // ~60 FPS
                updateGame();
                repaint();
                lastTime = now;
            }
        }
    }

    private void updateGame() {
        if (gameOver) return;

        player.update();

        // Update stars for parallax background
        for (Star s : stars) s.update();

        // Update bullets
        Iterator<Bullet> bit = bullets.iterator();
        while (bit.hasNext()) {
            Bullet b = bit.next();
            b.update();
            if (b.getY() < 0) bit.remove();
        }

        // Spawn enemies
        if (rand.nextInt(40) == 0) {
            enemies.add(new Enemy(rand.nextInt(WIDTH - 40), -40, 2 + rand.nextInt(3)));
        }

        // Update enemies
        Iterator<Enemy> eit = enemies.iterator();
        while (eit.hasNext()) {
            Enemy e = eit.next();
            e.update();
            if (e.getY() > HEIGHT) eit.remove();

            // Collision with player
            if (e.getBounds().intersects(player.getBounds())) {
                gameOver = true;
            }

            // Collision with bullets
            for (Iterator<Bullet> b2 = bullets.iterator(); b2.hasNext();) {
                Bullet b = b2.next();
                if (b.getBounds().intersects(e.getBounds())) {
                    b2.remove();
                    eit.remove();
                    score += 10;
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Space gradient background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 10, 30), 0, HEIGHT, new Color(0, 0, 0));
        g2.setPaint(gradient);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Stars
        for (Star s : stars) s.draw(g2);

        // Player
        player.draw(g2);

        // Bullets
        for (Bullet b : bullets) b.draw(g2);

        // Enemies
        for (Enemy e : enemies) e.draw(g2);

        // UI
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 18));
        g2.drawString("Score: " + score, 20, 30);

        if (gameOver) {
            g2.setFont(new Font("Consolas", Font.BOLD, 40));
            String msg = "GAME OVER";
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (WIDTH - w) / 2, HEIGHT / 2 - 20);

            g2.setFont(new Font("Consolas", Font.PLAIN, 20));
            String retry = "Press SPACE to Restart";
            int rw = g2.getFontMetrics().stringWidth(retry);
            g2.drawString(retry, (WIDTH - rw) / 2, HEIGHT / 2 + 20);
        }

        g2.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_LEFT) player.setLeft(true);
        if (k == KeyEvent.VK_RIGHT) player.setRight(true);
        if (k == KeyEvent.VK_SPACE) {
            if (gameOver) {
                startGame();
            } else {
                bullets.add(new Bullet(player.getX() + 18, player.getY() - 10));
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_LEFT) player.setLeft(false);
        if (k == KeyEvent.VK_RIGHT) player.setRight(false);
    }

    @Override public void keyTyped(KeyEvent e) {}
}

// Player spaceship
class Player {
    private int x, y;
    private final int width = 40, height = 40;
    private boolean left = false, right = false;
    private final int speed = 6;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        if (left && x > 0) x -= speed;
        if (right && x < GamePanel.WIDTH - width) x += speed;
    }

    public void draw(Graphics2D g) {
        GradientPaint shipGradient = new GradientPaint(x, y, new Color(0, 255, 255),
                x, y + height, new Color(0, 100, 255));
        g.setPaint(shipGradient);
        int[] xs = {x, x + width / 2, x + width};
        int[] ys = {y + height, y, y + height};
        g.fillPolygon(xs, ys, 3);
        g.setColor(Color.CYAN);
        g.fillOval(x + width / 2 - 5, y + 10, 10, 10);
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setLeft(boolean b) { left = b; }
    public void setRight(boolean b) { right = b; }
}

// Bullets
class Bullet {
    private int x, y, speed = 10;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() { y -= speed; }

    public void draw(Graphics2D g) {
        GradientPaint gp = new GradientPaint(x, y, Color.WHITE, x, y + 10, Color.CYAN);
        g.setPaint(gp);
        g.fillRoundRect(x, y, 4, 10, 3, 3);
    }

    public int getY() { return y; }
    public Rectangle getBounds() { return new Rectangle(x, y, 4, 10); }
}

// Enemies
class Enemy {
    private int x, y, size = 40, speed;
    private Color color;

    public Enemy(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        int r = (int)(100 + Math.random() * 155);
        int g = (int)(50 + Math.random() * 205);
        color = new Color(r, g, 255 - g / 2);
    }

    public void update() { y += speed; }

    public void draw(Graphics2D g) {
        GradientPaint gp = new GradientPaint(x, y, color.brighter(), x, y + size, color.darker());
        g.setPaint(gp);
        g.fillOval(x, y, size, size);
    }

    public int getY() { return y; }
    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
}

// Stars for animated background
class Star {
    private int x, y;
    private int size;
    private double speed;

    public Star(int x, int y) {
        this.x = x;
        this.y = y;
        this.size = 1 + (int)(Math.random() * 3);
        this.speed = 1 + Math.random() * 2;
    }

    public void update() {
        y += speed;
        if (y > GamePanel.HEIGHT) {
            y = 0;
            x = (int)(Math.random() * GamePanel.WIDTH);
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 180));
        g.fillOval(x, y, size, size);
    }
}
