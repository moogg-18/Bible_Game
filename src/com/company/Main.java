package com.company;

import com.company.GameView.DisasterView;
import com.company.GameView.GameView;
import com.company.GameView.RedSeaGameView;
import com.company.GameView.TenComandmentsView;
import com.company.Sprite.DisasterViewSprite.Bug;
import com.company.Sprite.DisasterViewSprite.Frog;
import com.company.Sprite.DisasterViewSprite.Ice;
import com.company.Sprite.DisasterViewSprite.Tombstone;
import com.company.Sprite.Moses;
import com.company.Sprite.RedSeaViewSprite.Anubis;
import com.company.Sprite.RedSeaViewSprite.Cat;
import com.company.Sprite.RedSeaViewSprite.Pharoah;
import com.company.Sprite.Sprite;

//開始畫面
import com.company.Sprite.TenCommandmentSprite.StartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Main extends JPanel implements KeyListener {

    public static final int CELL = 50;
    public static final int WIDTH = 500;
    public static final int HEIGHT = 500;
    public static final int ROW = HEIGHT / CELL;
    public static final int COLUMN = WIDTH / CELL;

    Moses moses;
    public static GameView gameView;
    private int level;

    //HP
    private static final int MAX_HP = 3;
    private int hp = MAX_HP;
    private long invincibleUntil = 0;
    private static final int INVINCIBLE_MS = 1000;

    //計時
    private long startTime = System.currentTimeMillis();
    private int gameDurationSeconds = 300;

    //防止重複觸發
    private boolean timeUpTriggered = false;
    private boolean deathTriggered = false;

    //射擊冷卻
    private long shootCooldownUntil = 0;
    private static final int SHOOT_COOLDOWN_MS = 300;


    private JFrame windowRef;

    public Main() {
        level = 1;
        resetGame(new DisasterView());

        setFocusable(true);
        addKeyListener(this);
    }

    public void setWindowRef(JFrame window) {
        this.windowRef = window;
    }

    //StartPanel 呼叫
    public void startGameWithDifficulty(int seconds) {
        gameDurationSeconds = seconds;
        level = 1;
        resetGame(new DisasterView());
        startGame();
    }

    public void startGame() {
        if (windowRef == null) return;
        windowRef.setContentPane(this);
        windowRef.revalidate();
        windowRef.repaint();
        requestFocusInWindow();
    }

    private void backToStartPanel() {
        if (windowRef == null) return;
        StartPanel startPanel = new StartPanel(this);
        windowRef.setContentPane(startPanel);
        windowRef.revalidate();
        windowRef.repaint();
    }

    //整局重置
    public void resetGame(GameView game) {
        moses = new Moses(1, 1);
        gameView = game;

        hp = MAX_HP;
        invincibleUntil = 0;
        startTime = System.currentTimeMillis();
        shootCooldownUntil = 0;

        timeUpTriggered = false;
        deathTriggered = false;

        repaint();
    }

    //換關卡（不重置 HP/時間）
    public void resetLevel(GameView game) {
        moses = new Moses(1, 1);
        gameView = game;
        invincibleUntil = 0;
        shootCooldownUntil = 0;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameView.drawView(g);
        moses.draw(g);
        drawHearts(g);

        long now = System.currentTimeMillis();
        int passed = (int) ((now - startTime) / 1000);
        int remain = Math.max(0, gameDurationSeconds - passed);

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("Time: " + remain + "s", 415, 40);

        //時間到
        if (passed >= gameDurationSeconds && !timeUpTriggered) {
            timeUpTriggered = true;

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "時間到！請重新選擇難度再開始！");
                level = 1;
                resetGame(new DisasterView());
                backToStartPanel();
            });
        }

        requestFocusInWindow();
    }

    private void drawHearts(Graphics g) {
        int x = 455, y = 20;
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(Color.BLACK);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_HP; i++) sb.append(i < hp ? "♥" : "♡");
        g.drawString(sb.toString(), x, y);
    }

    //死亡處理
    private boolean takeDamageIfPossible() {
        long now = System.currentTimeMillis();
        if (now < invincibleUntil) return false;

        hp--;
        invincibleUntil = now + INVINCIBLE_MS;

        if (hp <= 0 && !deathTriggered) {
            deathTriggered = true;
            level = 1;

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "HP=0，你死了，重新開始！");
                resetGame(new DisasterView());
                backToStartPanel();
            });
            return true;
        }
        return false;
    }

    private boolean changeLevel(String result) {
        if (result.equals("Next level")) {
            level++;
            if (level == 2) resetLevel(new RedSeaGameView());
            else if (level == 3) resetLevel(new TenComandmentsView());
            return true;
        }
        return false;
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        Point mosesPoint = moses.getRelativePosition();

        switch (e.getKeyCode()) {

            //移動
            case KeyEvent.VK_UP:
                if (mosesPoint.y > 1) {
                    String result = moses.overlap(mosesPoint.x, mosesPoint.y - 1);
                    if (result.equals("Die")) {
                        takeDamageIfPossible();
                        return;
                    }
                    if (!result.equals("Cannot move")) mosesPoint.y--;
                    if (result.equals("Game over")) {
                        JOptionPane.showMessageDialog(this, "You win the game!!");
                        return;
                    }
                    if (changeLevel(result)) return;
                }
                break;

            case KeyEvent.VK_DOWN:
                if (mosesPoint.y < ROW) {
                    String result = moses.overlap(mosesPoint.x, mosesPoint.y + 1);
                    if (result.equals("Die")) {
                        takeDamageIfPossible();
                        return;
                    }
                    if (!result.equals("Cannot move")) mosesPoint.y++;
                    if (result.equals("Game over")) {
                        JOptionPane.showMessageDialog(this, "You win the game!!");
                        return;
                    }
                    if (changeLevel(result)) return;
                }
                break;

            case KeyEvent.VK_RIGHT:
                if (mosesPoint.x < COLUMN) {
                    String result = moses.overlap(mosesPoint.x + 1, mosesPoint.y);
                    if (result.equals("Die")) {
                        takeDamageIfPossible();
                        return;
                    }
                    if (!result.equals("Cannot move")) mosesPoint.x++;
                    if (result.equals("Game over")) {
                        JOptionPane.showMessageDialog(this, "You win the game!!");
                        return;
                    }
                    if (changeLevel(result)) return;
                }
                break;

            case KeyEvent.VK_LEFT:
                if (mosesPoint.x > 1) {
                    String result = moses.overlap(mosesPoint.x - 1, mosesPoint.y);
                    if (result.equals("Die")) {
                        takeDamageIfPossible();
                        return;
                    }
                    if (!result.equals("Cannot move")) mosesPoint.x--;
                    if (result.equals("Game over")) {
                        JOptionPane.showMessageDialog(this, "You win the game!!");
                        return;
                    }
                    if (changeLevel(result)) return;
                }
                break;

            //射擊
            case KeyEvent.VK_W: { //向上射
                long nowW = System.currentTimeMillis();
                if (nowW < shootCooldownUntil) return;
                shootCooldownUntil = nowW + SHOOT_COOLDOWN_MS;

                for (int i = mosesPoint.y; i > 0; i--) {
                    for (Sprite s : gameView.getElements()) {
                        if (s.getRelativePosition() != null &&
                                s.getRelativePosition().x == mosesPoint.x &&
                                s.getRelativePosition().y == i) {

                            if (s instanceof Cat || s instanceof Ice || s instanceof Tombstone) return;
                            if (s instanceof Anubis || s instanceof Pharoah ||
                                    s instanceof Bug || s instanceof Frog) {
                                s.setNullPosition();
                                repaint();
                                return;
                            }
                        }
                    }
                }
                return;
            }

            case KeyEvent.VK_S: { //向下射
                long nowS = System.currentTimeMillis();
                if (nowS < shootCooldownUntil) return;
                shootCooldownUntil = nowS + SHOOT_COOLDOWN_MS;

                for (int i = mosesPoint.y; i <= ROW; i++) {
                    for (Sprite s : gameView.getElements()) {
                        if (s.getRelativePosition() != null &&
                                s.getRelativePosition().x == mosesPoint.x &&
                                s.getRelativePosition().y == i) {

                            if (s instanceof Cat || s instanceof Ice || s instanceof Tombstone) return;
                            if (s instanceof Anubis || s instanceof Pharoah ||
                                    s instanceof Bug || s instanceof Frog) {
                                s.setNullPosition();
                                repaint();
                                return;
                            }
                        }
                    }
                }
                return;
            }

            case KeyEvent.VK_D: { //向右射
                long nowD = System.currentTimeMillis();
                if (nowD < shootCooldownUntil) return;
                shootCooldownUntil = nowD + SHOOT_COOLDOWN_MS;

                for (int i = mosesPoint.x; i <= COLUMN; i++) {
                    for (Sprite s : gameView.getElements()) {
                        if (s.getRelativePosition() != null &&
                                s.getRelativePosition().x == i &&
                                s.getRelativePosition().y == mosesPoint.y) {

                            if (s instanceof Cat || s instanceof Ice || s instanceof Tombstone) return;
                            if (s instanceof Anubis || s instanceof Pharoah ||
                                    s instanceof Bug || s instanceof Frog) {
                                s.setNullPosition();
                                repaint();
                                return;
                            }
                        }
                    }
                }
                return;
            }

            case KeyEvent.VK_A: { //向左射
                long nowA = System.currentTimeMillis();
                if (nowA < shootCooldownUntil) return;
                shootCooldownUntil = nowA + SHOOT_COOLDOWN_MS;

                for (int i = mosesPoint.x; i > 0; i--) {
                    for (Sprite s : gameView.getElements()) {
                        if (s.getRelativePosition() != null &&
                                s.getRelativePosition().x == i &&
                                s.getRelativePosition().y == mosesPoint.y) {

                            if (s instanceof Cat || s instanceof Ice || s instanceof Tombstone) return;
                            if (s instanceof Anubis || s instanceof Pharoah ||
                                    s instanceof Bug || s instanceof Frog) {
                                s.setNullPosition();
                                repaint();
                                return;
                            }
                        }
                    }
                }
                return;
            }
        }

        //移動後更新位置
        moses.setPosition(mosesPoint);
        repaint();
    }

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Main gamePanel = new Main();
        gamePanel.setWindowRef(window);

        window.setContentPane(new StartPanel(gamePanel));
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setResizable(false);
    }
}
