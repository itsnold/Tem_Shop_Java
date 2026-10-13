package com.temshop;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
final class TemShopPanel extends JPanel {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private static final Color BLACK = Color.BLACK;
    private static final Color WHITE = new Color(248, 248, 248);
    private static final Color GOLD = new Color(255, 229, 0);
    private static final Color HEART = new Color(255, 20, 54);
    private static final Font DIALOG_FONT = loadGameFont("assets/fonts/8bitoperator_jvec.ttf", 24f);
    private static final Font MENU_FONT = loadGameFont("assets/fonts/8bitoperator_jvec.ttf", 27f);
    private static final Font ITEM_FONT = loadGameFont("assets/fonts/8bitoperator_jvec.ttf", 15f);
    private static final Font STATUS_FONT = loadGameFont("assets/fonts/8bitoperator_jvec.ttf", 26f);
    private static final Font SIGN_FONT = loadGameFont("assets/fonts/8bitoperator_jvec.ttf", 32f);

    private final List<ShopItem> items = new ArrayList<>(List.of(
            new ShopItem("tem flake", 3, "Heals 2HP\nfood of tem"),
            new ShopItem("tem flake (SALE)", 1, "Heals 2HP\nDISCOUNT FOOD OF TEM!!!"),
            new ShopItem("tem flake (EXP)", 20, "Heals 2HP\nfood of tem (expensiv)"),
            new ShopItem("tem pay 4 colleg", 1000, "COLLEGE\ntem pursu higher education")
    ));
    private final Map<String, Integer> inventory = new LinkedHashMap<>();
    private final MusicPlayer music = new MusicPlayer();
    private final SfxPlayer sfx = new SfxPlayer();
    private final Temmie temmie = new Temmie();
    private final BufferedImage shopImage = loadImage("assets/shop-background.png");
    private final BufferedImage heartImage = loadImage("assets/heart.png");

    private Screen screen = Screen.HOME;
    private int selection;
    private int balance = 145;
    private int talkCount;
    private boolean graduated;
    private String message = "";
    private int revealedCharacters;
    private final javax.swing.Timer typewriter = new javax.swing.Timer(30, event -> revealNextCharacter());
    private final javax.swing.Timer animator = new javax.swing.Timer(100, event -> {
        temmie.tick();
        repaint();
    });

    TemShopPanel() {
        inventory.put("tem flake", 2);
        inventory.put("tem flake (SALE)", 1);
        inventory.put("tem flake (EXP)", 1);

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(BLACK);
        setFocusable(true);
        installKeys();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                requestFocusInWindow();
                double scaleX = WIDTH / (double) getWidth();
                double scaleY = HEIGHT / (double) getHeight();
                handleClick((int) (event.getX() * scaleX), (int) (event.getY() * scaleY));
            }
        });
        setMessage("* hOI!\n* welcom to...\n* da TEM SHOP!!!!");
        animator.start();
    }

    void stopMusic() {
        typewriter.stop();
        animator.stop();
        music.close();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.scale(getWidth() / (double) WIDTH, getHeight() / (double) HEIGHT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        drawShopScene(g);
        drawBottomPanels(g);
        drawDialogue(g);
        drawMenu(g);
        drawStatus(g);
        g.dispose();
    }

    private void drawShopScene(Graphics2D g) {
        if (shopImage != null) {
            g.drawImage(shopImage, 0, 0, WIDTH, 240, null);
        } else {
            drawFallbackRoom(g);
        }
        temmie.draw(g);
        if (!temmie.hasLayered()) {
            drawTemShopSign(g);
        }
    }

    private void drawTemShopSign(Graphics2D g) {
        g.setColor(new Color(67, 48, 43));
        g.fillRect(206, 147, 187, 93);
        g.setColor(new Color(184, 167, 161));
        g.fillRect(210, 151, 179, 89);
        g.setColor(GOLD);
        g.setFont(SIGN_FONT);
        g.drawString("Tem Shop", 218, 220);
    }

    private void drawFallbackRoom(Graphics2D g) {
        g.setColor(new Color(171, 124, 59));
        g.fillRect(0, 0, WIDTH, 240);
        g.setColor(new Color(137, 96, 46));
        for (int y = 0; y < 240; y += 40) {
            g.fillRect(0, y, WIDTH, 3);
        }
    }

    private void drawBottomPanels(Graphics2D g) {
        g.setColor(WHITE);
        g.fillRect(0, 240, WIDTH, HEIGHT - 240);
        g.setColor(BLACK);
        g.fillRect(8, 248, 414, 226);
        g.fillRect(428, 248, 206, 226);
    }

    private void drawDialogue(Graphics2D g) {
        g.setColor(WHITE);
        g.setFont(DIALOG_FONT);
        String visibleMessage = message.substring(0, Math.min(revealedCharacters, message.length()));
        List<String> lines = wrap(visibleMessage, 26);
        for (int i = 0; i < Math.min(lines.size(), 5); i++) {
            g.drawString(lines.get(i), 39, 284 + i * 40);
        }
    }

    private void drawMenu(Graphics2D g) {
        g.setColor(WHITE);
        if (screen == Screen.HOME) {
            String[] options = {"Buy", "Sell", "Talk", "Exit"};
            g.setFont(MENU_FONT);
            for (int i = 0; i < options.length; i++) {
                int baseline = 285 + i * 40;
                if (selection == i) {
                    drawHeart(g, 449, baseline - 17);
                }
                g.setColor(WHITE);
                g.drawString(options[i], 479, baseline);
            }
            return;
        }

        g.setFont(ITEM_FONT);
        if (screen == Screen.BUY) {
            for (int i = 0; i < items.size(); i++) {
                ShopItem item = items.get(i);
                int baseline = 281 + i * 32;
                if (selection == i) {
                    drawHeart(g, 449, baseline - 13);
                }
                g.setColor(WHITE);
                g.drawString(item.name(), 477, baseline);
                g.setColor(new Color(209, 202, 183));
                g.drawString(item.priceLabel(), 477, baseline + 14);
                g.setColor(WHITE);
            }
            int exitBaseline = 281 + items.size() * 32;
            if (selection == items.size()) {
                drawHeart(g, 449, exitBaseline - 13);
            }
            g.drawString("Exit", 477, exitBaseline);
            return;
        }

        List<ShopItem> goods = sellableItems();
        for (int i = 0; i < goods.size(); i++) {
            ShopItem item = goods.get(i);
            int baseline = 281 + i * 32;
            if (selection == i) {
                drawHeart(g, 449, baseline - 13);
            }
            g.setColor(WHITE);
            g.drawString(item.name(), 477, baseline);
            g.setColor(new Color(209, 202, 183));
            g.drawString("x" + inventory.getOrDefault(item.name(), 0) + " " + sellPrice(item) + "G", 477, baseline + 14);
            g.setColor(WHITE);
        }
        int exitBaseline = 281 + goods.size() * 32;
        if (selection == goods.size()) {
            drawHeart(g, 449, exitBaseline - 13);
        }
        g.setColor(WHITE);
        g.drawString("Exit", 477, exitBaseline);
    }

    private void drawStatus(Graphics2D g) {
        g.setFont(STATUS_FONT);
        g.setColor(WHITE);
        g.drawString(balance + "G", 458, 445);
        g.drawString(inventoryCount() + "/8", 559, 445);
    }

    private void installKeys() {
        bind("UP", "up", () -> moveSelection(-1));
        bind("W", "up-w", () -> moveSelection(-1));
        bind("DOWN", "down", () -> moveSelection(1));
        bind("S", "down-s", () -> moveSelection(1));
        bind("ENTER", "confirm-enter", this::confirmSelection);
        bind("SPACE", "confirm-space", this::confirmSelection);
        bind("ESCAPE", "back", this::back);
        bind("M", "music", () -> music.toggle());
        bind("G", "demo-gold", this::demoGold);
    }

    private void bind(String key, String name, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                action.run();
                repaint();
            }
        });
    }

    private void moveSelection(int change) {
        int size = currentMenuSize();
        if (size == 0) {
            return;
        }
        selection = (selection + change + size) % size;
        sfx.playMove();
        if (screen == Screen.BUY) {
            if (selection < items.size()) {
                setItemMessage(items.get(selection), false);
                temmie.setEmotion(Temmie.Emotion.SIDE_EYE);
            } else {
                setMessage("* Exit");
                temmie.setEmotion(Temmie.Emotion.IDLE);
            }
        } else if (screen == Screen.SELL) {
            List<ShopItem> goods = sellableItems();
            if (selection < goods.size() && !goods.isEmpty()) {
                setItemMessage(goods.get(selection), true);
                temmie.setEmotion(Temmie.Emotion.SIDE_EYE);
            } else {
                setMessage("* Exit");
                temmie.setEmotion(Temmie.Emotion.IDLE);
            }
        }
        repaint();
    }

    private int currentMenuSize() {
        return switch (screen) {
            case HOME -> 4;
            case BUY -> items.size() + 1;
            case SELL -> sellableItems().size() + 1;
        };
    }

    private void confirmSelection() {
        sfx.playConfirm();
        if (screen == Screen.HOME) {
            switch (selection) {
                case 0 -> {
                    screen = Screen.BUY;
                    selection = 0;
                    setItemMessage(items.get(0), false);
                    temmie.setEmotion(Temmie.Emotion.SIDE_EYE);
                }
                case 1 -> {
                    screen = Screen.SELL;
                    selection = 0;
                    if (sellableItems().isEmpty()) {
                        setMessage("* no more item...");
                        temmie.setEmotion(Temmie.Emotion.SAD, 30);
                    } else {
                        setItemMessage(sellableItems().get(0), true);
                        temmie.setEmotion(Temmie.Emotion.SIDE_EYE);
                    }
                }
                case 2 -> talk();
                case 3 -> leaveShop();
                default -> throw new IllegalStateException("Unexpected menu item");
            }
        } else if (screen == Screen.BUY) {
            if (selection >= items.size()) {
                back();
            } else {
                buy(items.get(selection));
            }
        } else {
            List<ShopItem> goods = sellableItems();
            if (selection >= goods.size()) {
                back();
            } else if (goods.isEmpty()) {
                setMessage("* nothing to sell...");
                temmie.setEmotion(Temmie.Emotion.SAD, 30);
            } else {
                sell(goods.get(selection));
            }
        }
        repaint();
    }

    private void buy(ShopItem item) {
        if (inventoryCount() >= 8) {
            setMessage("* WAO!!\n* bag FULLS!");
            temmie.setEmotion(Temmie.Emotion.HAPPY, 30);
        } else if (balance < item.price()) {
            setMessage("* you don\n* hav da muns,");
            temmie.setEmotion(Temmie.Emotion.NERVOUS, 40);
        } else if (isCollege(item)) {
            balance -= item.price();
            sfx.playBuy();
            graduate();
        } else {
            balance -= item.price();
            sfx.playBuy();
            inventory.merge(item.name(), 1, Integer::sum);
            setMessage("* thanks\n* PURCHASE!");
            temmie.setEmotion(Temmie.Emotion.JOY, 30);
        }
    }

    private boolean isCollege(ShopItem item) {
        return !graduated && item.name().equals("tem pay 4 colleg");
    }

    private void graduate() {
        graduated = true;
        temmie.setGraduated(true);
        items.set(3, new ShopItem("temy ARMOR", 1000, "ARMOR 20DF\nmakes battles too easy"));
        temmie.setEmotion(Temmie.Emotion.JOY, 50);
        setMessage("* WOA!!\n* tem go to colleg...\n* tem bak! learn MANY THINs!");
    }

    private void sell(ShopItem item) {
        int count = inventory.getOrDefault(item.name(), 0);
        if (count <= 0) {
            setMessage("* tem can't buy what u don't have!");
            temmie.setEmotion(Temmie.Emotion.SAD, 30);
            return;
        }
        int price = sellPrice(item);
        if (count == 1) {
            inventory.remove(item.name());
        } else {
            inventory.put(item.name(), count - 1);
        }
        balance += price;
        temmie.setEmotion(Temmie.Emotion.JOY, 30);
        List<ShopItem> goods = sellableItems();
        if (goods.isEmpty()) {
            screen = Screen.HOME;
            selection = 1;
        } else {
            selection = Math.min(selection, goods.size());
        }
        setMessage("* tem bought " + item.name() + "!\n* u got " + price + "G!");
    }

    private void talk() {
        String[] lines;
        if (graduated) {
            lines = new String[]{
                    "* HOI!!\n* im temmie",
                    "* tem armor so GOOds!\n* any battle becom EASY!",
                    "* yaYA!! tem got degree\n* in TEM STUDIES!",
                    "* us tems hav a DEEP HISTORY!!!"
            };
        } else {
            lines = new String[]{
                    "* HOI!!\n* im temmie",
                    "* hOI!!!\n* i'm temmie",
                    "* us tems hav a DEEP HISTORY!!!",
                    "* yaYA!!!\n* go to TEM SHOP!!!"
            };
        }
        setMessage(lines[talkCount++ % lines.length]);
        temmie.setEmotion(Temmie.Emotion.HAPPY, 25);
    }

    private void demoGold() {
        balance += 500;
        setMessage("* (demo) tem finds 500G!\n* now u hav " + balance + "G");
        temmie.setEmotion(Temmie.Emotion.HAPPY, 25);
        repaint();
    }

    private void back() {
        if (screen != Screen.HOME) {
            screen = Screen.HOME;
            selection = 0;
            temmie.setEmotion(Temmie.Emotion.IDLE);
            setMessage("* hOI!\n* welcom to...\n* da TEM SHOP!!!!");
        } else {
            leaveShop();
        }
        repaint();
    }

    private void leaveShop() {
        temmie.setEmotion(Temmie.Emotion.IDLE);
        setMessage("* bOI!!");
        int goodbyeDelay = Math.max(900, message.length() * 30 + 100);
        javax.swing.Timer timer = new javax.swing.Timer(goodbyeDelay, event -> {
            Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void setItemMessage(ShopItem item, boolean selling) {
        String prompt = selling ? "tem will buy it for " + sellPrice(item) + "G." : item.description();
        setMessage("* " + item.name() + "\n* " + prompt);
    }

    private void setMessage(String value) {
        message = value;
        revealedCharacters = 0;
        typewriter.stop();
        temmie.setTalking(true);
        if (!message.isEmpty()) {
            typewriter.start();
        } else {
            temmie.setTalking(false);
        }
        repaint();
    }

    private void revealNextCharacter() {
        if (revealedCharacters < message.length()) {
            revealedCharacters++;
            repaint();
        }
        if (revealedCharacters >= message.length()) {
            typewriter.stop();
            temmie.setTalking(false);
        }
    }

    private List<ShopItem> sellableItems() {
        List<ShopItem> goods = new ArrayList<>();
        for (ShopItem item : items) {
            if (item.name().equals("tem pay 4 colleg")) {
                continue;
            }
            if (inventory.getOrDefault(item.name(), 0) > 0) {
                goods.add(item);
            }
        }
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            boolean known = goods.stream().anyMatch(item -> item.name().equals(entry.getKey()));
            if (!known && entry.getValue() > 0) {
                goods.add(new ShopItem(entry.getKey(), 10, "tem wants it!"));
            }
        }
        return goods;
    }

    private int sellPrice(ShopItem item) {
        return Math.max(1, item.price() / 2);
    }

    private int inventoryCount() {
        return inventory.values().stream().mapToInt(Integer::intValue).sum();
    }

    private void handleClick(int x, int y) {
        if (x < 428 || x > 634) {
            return;
        }
        int clicked;
        if (screen == Screen.HOME) {
            if (y < 260 || y > 425) {
                return;
            }
            clicked = (y - 265) / 40;
        } else {
            if (y < 260 || y > 440) {
                return;
            }
            clicked = (y - 265) / 32;
        }
        if (clicked < 0 || clicked >= currentMenuSize()) {
            return;
        }
        if (screen == Screen.HOME) {
            selection = clicked;
            confirmSelection();
        } else if (selection == clicked) {
            confirmSelection();
        } else {
            selection = clicked;
            sfx.playMove();
            if (screen == Screen.BUY) {
                if (selection < items.size()) {
                    setItemMessage(items.get(selection), false);
                    temmie.setEmotion(Temmie.Emotion.SIDE_EYE);
                } else {
                    setMessage("* Exit");
                }
            } else if (!sellableItems().isEmpty() && selection < sellableItems().size()) {
                setItemMessage(sellableItems().get(selection), true);
                temmie.setEmotion(Temmie.Emotion.SIDE_EYE);
            } else {
                setMessage("* Exit");
            }
        }
        repaint();
    }

    private static List<String> wrap(String text, int maxCharacters) {
        List<String> lines = new ArrayList<>();
        for (String sourceLine : text.split("\\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : sourceLine.split(" ")) {
                if (line.length() > 0 && line.length() + word.length() + 1 > maxCharacters) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                if (line.length() > 0) {
                    line.append(' ');
                }
                line.append(word);
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }
        return lines;
    }

    private void drawHeart(Graphics2D g, int x, int y) {
        if (heartImage != null) {
            g.drawImage(heartImage, x, y, heartImage.getWidth() * 2, heartImage.getHeight() * 2, null);
            return;
        }
        String[] pixels = {".##.##.", "#######", "#######", ".#####.", "..###..", "...#..."};
        g.setColor(HEART);
        for (int row = 0; row < pixels.length; row++) {
            for (int column = 0; column < pixels[row].length(); column++) {
                if (pixels[row].charAt(column) == '#') {
                    g.fillRect(x + column * 2, y + row * 2, 2, 2);
                }
            }
        }
    }

    /** 8bitoperator JVE: the game's own dialogue typeface (fnt_maintext). */
    private static Font loadGameFont(String path, float size) {
        File file = new File(path);
        if (file.isFile()) {
            try {
                return Font.createFont(Font.TRUETYPE_FONT, file).deriveFont(size);
            } catch (Exception exception) {
                System.err.println("Couldn't load " + path + ": " + exception.getMessage());
            }
        } else {
            System.err.println("Font missing: " + path + ", using fallback.");
        }
        return new Font(Font.MONOSPACED, Font.BOLD, Math.round(size));
    }

    private static BufferedImage loadImage(String path) {
        File file = new File(path);
        if (!file.isFile()) {
            return null;
        }
        try {
            return ImageIO.read(file);
        } catch (Exception exception) {
            System.err.println("Couldn't load " + path + ": " + exception.getMessage());
            return null;
        }
    }

    private enum Screen {
        HOME, BUY, SELL
    }
}
