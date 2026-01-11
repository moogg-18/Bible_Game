package com.company.Sprite.TenCommandmentSprite;

import com.company.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class StartPanel extends JPanel {

    private final Main main;
    private final Image bgImage;

    //START按鈕區域
    private Rectangle startRect;

    //是否滑鼠移到START
    private boolean hoverStart = false;

    //新增：難度按鈕區域
    private Rectangle easyRect;
    private Rectangle normalRect;
    private Rectangle hardRect;

    //新增：是否滑鼠移到難度按鈕
    private boolean hoverEasy = false;
    private boolean hoverNormal = false;
    private boolean hoverHard = false;

    //新增：選到的難度秒數（-1 代表尚未選擇）
    private int selectedSeconds = -1;

    public StartPanel(Main main) {
        this.main = main;

        //讀取桌面上的red.png當開始畫面背景
        String desktopPath = System.getProperty("user.home")
                + File.separator + "Desktop"
                + File.separator + "red.png";

        bgImage = new ImageIcon(desktopPath).getImage();

        setFocusable(true);

        //滑鼠點擊：先選難度，選完難度才會出現START，點START才進入遊戲
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                //新增：點難度按鈕 → 設定秒數
                if (easyRect != null && easyRect.contains(p)) {
                    selectedSeconds = 300; //簡單(遊戲時間:300秒)
                    repaint();
                    return;
                }
                if (normalRect != null && normalRect.contains(p)) {
                    selectedSeconds = 60;  //中度(遊戲時間:60秒)
                    repaint();
                    return;
                }
                if (hardRect != null && hardRect.contains(p)) {
                    selectedSeconds = 15;  //高難(遊戲時間:15秒)
                    repaint();
                    return;
                }

                //條件一：沒選擇難度就不出現START，所以這裡 startRect 會是 null
                if (startRect != null && startRect.contains(p)) {
                    //條件二：選完難度才可以按START進入遊戲並且重新計時遊戲時間
                    StartPanel.this.main.startGameWithDifficulty(selectedSeconds);
                }
            }
        });

        //滑鼠移動：hover效果 + 游標變手指
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();

                //新增：難度
                boolean nowHoverEasy = easyRect != null && easyRect.contains(p);
                boolean nowHoverNormal = normalRect != null && normalRect.contains(p);
                boolean nowHoverHard = hardRect != null && hardRect.contains(p);

                if (nowHoverEasy != hoverEasy || nowHoverNormal != hoverNormal || nowHoverHard != hoverHard) {
                    hoverEasy = nowHoverEasy;
                    hoverNormal = nowHoverNormal;
                    hoverHard = nowHoverHard;
                    repaint();
                }

                //原本：START hover（但只有選到難度才會有startRect）
                boolean nowHoverStart = startRect != null && startRect.contains(p);
                if (nowHoverStart != hoverStart) {
                    hoverStart = nowHoverStart;
                    repaint();
                }

                //新增：只要滑到可點區域就變手指
                boolean canHand = (easyRect != null && easyRect.contains(p))
                        || (normalRect != null && normalRect.contains(p))
                        || (hardRect != null && hardRect.contains(p))
                        || (startRect != null && startRect.contains(p));

                setCursor(
                        canHand
                                ? new Cursor(Cursor.HAND_CURSOR)
                                : new Cursor(Cursor.DEFAULT_CURSOR)
                );
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 500);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        //背景圖片鋪滿整個畫面
        g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);

        //開啟反鋸齒，畫面更平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //新增：難度按鈕位置加空隙
        int diffW = 220;
        int diffH = 55;
        int diffX = (getWidth() - diffW) / 2;

        //新增：按鈕之間的空隙
        int gap = 12;

        //新增：第一個按鈕的起始 Y
        int startY = (int) (getHeight() * 0.40);

        int easyY = startY;
        int normalY = startY + diffH + gap;
        int hardY = startY + (diffH + gap) * 2;

        easyRect = new Rectangle(diffX, easyY, diffW, diffH);
        normalRect = new Rectangle(diffX, normalY, diffW, diffH);
        hardRect = new Rectangle(diffX, hardY, diffW, diffH);


        //新增：畫難度按鈕
        drawDifficultyButton(g2, easyRect, "簡單", hoverEasy, selectedSeconds == 300);
        drawDifficultyButton(g2, normalRect, "中度", hoverNormal, selectedSeconds == 60);
        drawDifficultyButton(g2, hardRect, "高難", hoverHard, selectedSeconds == 15);

        //條件一：沒選擇難度就不出現START
        if (selectedSeconds == -1) {
            startRect = null;
            hoverStart = false;
            g2.dispose();
            return;
        }

        //START 位置加間距
        int btnW = 220;
        int btnH = 70;
        int btnX = (getWidth() - btnW) / 2;

        //和難度按鈕共用間距，視覺會一致
        int startGap = 18;
        int btnY = hardRect.y + hardRect.height + startGap;
        startRect = new Rectangle(btnX, btnY, btnW, btnH);

        //START白框變色
        Color normal = new Color(255, 255, 255, 230);
        Color hover  = new Color(255, 190, 120, 235);
        g2.setColor(hoverStart ? hover : normal);
        g2.fillRoundRect(btnX, btnY, btnW, btnH, 30, 30);

        //白框外框線
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(btnX, btnY, btnW, btnH, 30, 30);

        //在白框中畫START文字
        String text = "START";
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        int textX = btnX + (btnW - fm.stringWidth(text)) / 2;
        int textY = btnY + (btnH - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(hoverStart ? Color.BLACK : new Color(40, 40, 40));
        g2.drawString(text, textX, textY);

        g2.dispose();
    }

    //新增：難度按鈕
    private void drawDifficultyButton(Graphics2D g2, Rectangle rect, String label,
                                      boolean hover, boolean selected) {
        int x = rect.x, y = rect.y, w = rect.width, h = rect.height;

        Color normal = new Color(255, 255, 255, 200);
        Color hoverC = new Color(255, 190, 120, 220);
        Color selectedC = new Color(180, 255, 200, 220);

        if (selected) g2.setColor(selectedC);
        else g2.setColor(hover ? hoverC : normal);

        g2.fillRoundRect(x, y, w, h, 22, 22);

        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 22, 22);

        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (w - fm.stringWidth(label)) / 2;
        int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(new Color(40, 40, 40));
        g2.drawString(label, tx, ty);

    }
}
