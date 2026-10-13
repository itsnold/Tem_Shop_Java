package com.temshop;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import com.jcraft.jorbis.VorbisDecoder;
import java.io.File;

/** Plays the Tem Shop Ogg track or an optional WAV override. */
final class MusicPlayer {
    private volatile Clip clip;
    private Thread musicThread;
    private volatile boolean running;
    private volatile boolean enabled = true;

    MusicPlayer() {
        running = true;
        musicThread = new Thread(this::loadAndPlay, "tem-shop-music");
        musicThread.setDaemon(true);
        musicThread.start();
    }

    boolean isEnabled() {
        return enabled;
    }

    void toggle() {
        enabled = !enabled;
        Clip currentClip = clip;
        if (currentClip != null) {
            if (enabled) {
                if (currentClip.getFramePosition() >= currentClip.getFrameLength()) {
                    currentClip.setFramePosition(0);
                }
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                currentClip.stop();
            }
        }
    }

    void close() {
        running = false;
        Clip currentClip = clip;
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
        }
        if (musicThread != null) {
            musicThread.interrupt();
        }
    }

    private void loadAndPlay() {
        File wavOverride = resolveAsset("tem-shop-loop.wav");
        File temShopTrack = resolveAsset("mus_temshop.ogg");
        Clip loadedClip = loadClip(wavOverride, false);
        if (loadedClip == null) {
            loadedClip = loadClip(temShopTrack, true);
        }
        if (loadedClip != null) {
            if (!running) {
                loadedClip.close();
                return;
            }
            clip = loadedClip;
            if (enabled && running) {
                loadedClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            while (running) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            loadedClip.stop();
            loadedClip.close();
            return;
        }
        System.err.println("Tem Shop music could not be loaded from " + temShopTrack.getAbsolutePath());
    }

    private File resolveAsset(String name) {
        File local = new File("assets", name);
        if (local.isFile()) {
            return local;
        }
        try {
            File classes = new File(MusicPlayer.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File besideBuild = new File(classes.getParentFile(), "assets/" + name);
            if (besideBuild.isFile()) {
                return besideBuild;
            }
        } catch (Exception exception) {
            System.err.println("Couldn't locate music assets: " + exception.getMessage());
        }
        return local;
    }

    private Clip loadClip(File file, boolean ogg) {
        if (!file.isFile()) {
            return null;
        }
        try (AudioInputStream stream = ogg
                ? VorbisDecoder.decode(file.toPath())
                : AudioSystem.getAudioInputStream(file)) {
            Clip loadedClip = AudioSystem.getClip();
            loadedClip.open(stream);
            return loadedClip;
        } catch (Exception exception) {
            System.err.println("Couldn't play " + file + ": " + exception.getMessage());
            return null;
        }
    }

}
