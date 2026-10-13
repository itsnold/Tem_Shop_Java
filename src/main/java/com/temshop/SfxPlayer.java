package com.temshop;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Authentic shop sound effects from the original asset dump.
 *
 * <p>Sound IDs verified against the decompiled shop scripts: cursor move is
 * {@code snd_squeak} ({@code snd_play(114)}), menu confirm is
 * {@code snd_select} ({@code snd_play(111)}), and a completed purchase is
 * {@code snd_buyitem} ({@code snd_play(113)}).
 */
final class SfxPlayer {
    void playMove() {
        play("snd_squeak.wav");
    }

    void playConfirm() {
        play("snd_select.wav");
    }

    void playBuy() {
        play("snd_buyitem.wav");
    }

    private void play(String name) {
        File file = resolveAsset(name);
        if (!file.isFile()) {
            System.err.println("Sound effect missing: " + file.getPath());
            return;
        }
        Thread thread = new Thread(() -> playOnce(file), "tem-sfx-" + name);
        thread.setDaemon(true);
        thread.start();
    }

    private static void playOnce(File file) {
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            CountDownLatch done = new CountDownLatch(1);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    done.countDown();
                }
            });
            clip.open(stream);
            clip.start();
            done.await(Math.max(1, clip.getMicrosecondLength() / 1000 + 500), TimeUnit.MILLISECONDS);
            clip.stop();
            clip.close();
        } catch (Exception exception) {
            System.err.println("Couldn't play " + file + ": " + exception.getMessage());
        } catch (Throwable throwable) {
            System.err.println("Couldn't play " + file + ": " + throwable.getMessage());
        }
    }

    private File resolveAsset(String name) {
        File local = new File("assets/sfx", name);
        if (local.isFile()) {
            return local;
        }
        try {
            File classes = new File(SfxPlayer.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File besideBuild = new File(classes.getParentFile(), "assets/sfx/" + name);
            if (besideBuild.isFile()) {
                return besideBuild;
            }
        } catch (Exception exception) {
            System.err.println("Couldn't locate sound assets: " + exception.getMessage());
        }
        return local;
    }
}
