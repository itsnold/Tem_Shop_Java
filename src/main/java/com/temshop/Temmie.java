package com.temshop;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Layered Temmie shopkeeper, assembled from the original decompiled sprites
 * like the game does in {@code obj_shop5_body}.
 *
 * <p>Emotion numbers match the original {@code global.faceemotion} values:
 * 0 idle, 1 happy jitter, 2 nervous with sweat, 3 side-eye confirm,
 * 4 sad with sweat, 5 excited with sweat, 6 joy sell-face, 7 angry sell-face.
 */
final class Temmie {
    enum Emotion {
        IDLE, HAPPY, NERVOUS, SIDE_EYE, SAD, EXCITED, JOY, ANGRY
    }

    private final BufferedImage body = load("assets/tem/tembody.png");
    private final BufferedImage box = load("assets/tem/tembox.png");
    private final BufferedImage brows = load("assets/tem/brows.png");
    private final BufferedImage eyesOpen = load("assets/tem/eyes1_open.png");
    private final BufferedImage eyesBlink = load("assets/tem/eyes1_blink.png");
    private final BufferedImage eyes2 = load("assets/tem/eyes2.png");
    private final BufferedImage eyes3 = load("assets/tem/eyes3.png");
    private final BufferedImage eyes4 = load("assets/tem/eyes4.png");
    private final BufferedImage eyes5 = load("assets/tem/eyes5.png");
    private final BufferedImage eyes6 = load("assets/tem/eyes6.png");
    private final BufferedImage mouth1a = load("assets/tem/mouth1_0.png");
    private final BufferedImage mouth1b = load("assets/tem/mouth1_1.png");
    private final BufferedImage mouth2a = load("assets/tem/mouth2_0.png");
    private final BufferedImage mouth2b = load("assets/tem/mouth2_1.png");
    private final BufferedImage mouth3 = load("assets/tem/mouth3.png");
    private final BufferedImage sell0 = load("assets/tem/sellface_0.png");
    private final BufferedImage sell1 = load("assets/tem/sellface_1.png");
    private final BufferedImage sellX0 = load("assets/tem/sellface_x_0.png");
    private final BufferedImage sellX1 = load("assets/tem/sellface_x_1.png");
    private final BufferedImage sweat = load("assets/tem/sweat.png");
    private final BufferedImage coffee0 = load("assets/tem/coffeeline_0.png");
    private final BufferedImage coffee1 = load("assets/tem/coffeeline_1.png");
    private final BufferedImage hat = load("assets/tem/temhat.png");
    private final BufferedImage fallbackComposite = load("assets/temmie.png");

    private final Random random = new Random();
    private int siner;
    private Emotion emotion = Emotion.IDLE;
    private int emotionTimeout = -1;
    private int blinkCountdown = 30;
    private int blinkFrames;
    private boolean talking;
    private boolean graduated;
    private int bodyJitterX;
    private int bodyJitterY;

    void tick() {
        siner++;
        if (emotionTimeout > 0) {
            emotionTimeout--;
            if (emotionTimeout == 0) {
                emotion = Emotion.IDLE;
                emotionTimeout = -1;
            }
        }
        if (blinkFrames > 0) {
            blinkFrames--;
        } else {
            blinkCountdown--;
            if (blinkCountdown <= 0) {
                blinkFrames = 2;
                blinkCountdown = 25 + random.nextInt(25);
            }
        }
        if (emotion == Emotion.EXCITED) {
            bodyJitterX = random.nextInt(3) - 1;
            bodyJitterY = random.nextInt(3) - 1;
        } else {
            bodyJitterX = 0;
            bodyJitterY = 0;
        }
    }

    void setTalking(boolean value) {
        talking = value;
    }

    void setEmotion(Emotion value) {
        emotion = value;
        emotionTimeout = -1;
    }

    void setEmotion(Emotion value, int ticks) {
        emotion = value;
        emotionTimeout = ticks;
    }

    void setGraduated(boolean value) {
        graduated = value;
    }

    boolean isGraduated() {
        return graduated;
    }

    Emotion getEmotion() {
        return emotion;
    }

    boolean hasLayered() {
        return body != null && box != null;
    }

    /** Draws in the panel's 640x480 virtual space (2x the original 320x240 room). */
    void draw(Graphics2D g) {
        if (!hasLayered()) {
            drawFallback(g);
            return;
        }
        int bob = (int) (Math.sin(siner / 8.0) * 2.0);
        int bodyX = 198 + bodyJitterX;
        int bodyY = 2 + bob + bodyJitterY;

        BufferedImage coffee = (siner / 8) % 2 == 0 ? coffee0 : coffee1;
        if (coffee != null) {
            int coffeeBob = (int) (Math.sin(siner / 4.0) * 3.0);
            g.drawImage(coffee, 356, 124 + coffeeBob, 16, 14, null);
        }

        g.drawImage(body, bodyX, bodyY, 182, 170, null);

        switch (emotion) {
            case IDLE -> drawIdle(g, bodyX, bodyY);
            case HAPPY -> {
                drawImage(g, brows, 276 + bodyJitterX, 64 + bob);
                BufferedImage jittered = eyes2;
                int jx = random.nextInt(2) - random.nextInt(2);
                int jy = random.nextInt(2) - random.nextInt(2);
                drawImage(g, jittered, 270 + jx, 76 + bob + jy);
                drawImage(g, talking ? mouth1b : mouth1a, 282, 96 + bob);
            }
            case NERVOUS -> {
                drawImage(g, brows, 276, 64 + bob);
                drawImage(g, eyes3, 278, 80 + bob);
                drawImage(g, talking || (siner / 3) % 2 == 0 ? mouth2a : mouth2b, 282, 96 + bob);
                drawSweat(g, bob);
            }
            case SIDE_EYE -> {
                drawImage(g, eyes4, 278, 60 + bob);
                drawImage(g, mouth3, 296, 80 + bob);
            }
            case SAD -> {
                int faceY = (int) (Math.sin(siner / 5.0) * 3.0);
                drawImage(g, eyes5, 274, 64 + bob + faceY);
                drawImage(g, mouth3, 288, 86 + bob + faceY);
                drawSweat(g, bob);
            }
            case EXCITED -> {
                int faceY = (int) (Math.sin(siner / 3.0) * 4.0);
                drawImage(g, eyes6, 274, 62 + bob + faceY);
                drawImage(g, mouth3, 288, 86 + bob + faceY);
                drawSweat(g, bob);
            }
            case JOY -> {
                BufferedImage face = (siner / 2) % 2 == 0 ? sell0 : sell1;
                drawImage(g, face, 278, 50 + bob);
            }
            case ANGRY -> {
                BufferedImage face = (siner / 2) % 2 == 0 ? sellX0 : sellX1;
                drawImage(g, face, 278, 50 + bob);
            }
        }

        g.drawImage(box, 160, 136, 218, 102, null);
        if (graduated && hat != null) {
            g.drawImage(hat, bodyX + 74, bodyY, 46, 28, null);
        }
    }

    private void drawIdle(Graphics2D g, int bodyX, int bodyY) {
        int bob = bodyY - 2;
        drawImage(g, brows, 276, 64 + bob);
        BufferedImage eyes = blinkFrames > 0 ? eyesBlink : eyesOpen;
        if (eyes == null) {
            eyes = eyesOpen != null ? eyesOpen : eyesBlink;
        }
        drawImage(g, eyes, 278, 80 + bob);
        BufferedImage mouth = mouth1a;
        if (talking) {
            mouth = (siner / 2) % 2 == 0 ? mouth1a : mouth1b;
            if (mouth == null) {
                mouth = mouth1a != null ? mouth1a : mouth1b;
            }
        }
        drawImage(g, mouth, 282, 96 + bob);
    }

    private void drawSweat(Graphics2D g, int bob) {
        if (sweat == null) {
            return;
        }
        int sweatBob = (int) (Math.sin(siner / 4.0) * 3.0);
        g.drawImage(sweat, 266, 78 + bob + sweatBob, 60, 34, null);
    }

    private static void drawImage(Graphics2D g, BufferedImage image, int x, int y) {
        if (image == null) {
            return;
        }
        g.drawImage(image, x, y, image.getWidth() * 2, image.getHeight() * 2, null);
    }

    private void drawFallback(Graphics2D g) {
        int bob = (int) (Math.sin(siner / 8.0) * 2.0);
        if (fallbackComposite != null) {
            g.drawImage(fallbackComposite, 207, 5 + bob, 182, 170, null);
            return;
        }
        int x = 207;
        int y = 5 + bob;
        g.setColor(new java.awt.Color(223, 222, 214));
        g.fillOval(x + 5, y + 49, 172, 105);
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(x + 34, y + 43, 113, 65);
        g.setColor(java.awt.Color.BLACK);
        g.fillRect(x + 69, y + 72, 7, 9);
        g.fillRect(x + 112, y + 72, 7, 9);
    }

    private static BufferedImage load(String path) {
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
}
