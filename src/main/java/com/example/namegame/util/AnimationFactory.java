package com.example.namegame.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Factory for creating feedback animations.
 */
public class AnimationFactory {
    
    private static final Duration ANIMATION_DURATION = Duration.millis(300);
    private static final Duration SHAKE_DURATION = Duration.millis(400);
    
    /**
     * Plays a "correct answer" animation (scale pulse with green glow).
     */
    public static void playCorrect(Node node) {
        // Save original effect
        var originalEffect = node.getEffect();
        
        // Green glow
        DropShadow glow = new DropShadow();
        glow.setColor(Color.LIMEGREEN);
        glow.setRadius(20);
        glow.setSpread(0.5);
        node.setEffect(glow);
        
        // Scale animation
        ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION, node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.15);
        scale.setToY(1.15);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        
        // Fade the glow
        FadeTransition fade = new FadeTransition(ANIMATION_DURATION.multiply(2), node);
        fade.setFromValue(1.0);
        fade.setToValue(1.0);
        
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.setOnFinished(e -> node.setEffect(originalEffect));
        parallel.play();
    }
    
    /**
     * Plays an "incorrect answer" animation (shake with red glow).
     */
    public static void playIncorrect(Node node) {
        // Save original effect and position
        var originalEffect = node.getEffect();
        double originalX = node.getTranslateX();
        
        // Red glow
        DropShadow glow = new DropShadow();
        glow.setColor(Color.RED);
        glow.setRadius(20);
        glow.setSpread(0.5);
        node.setEffect(glow);
        
        // Shake animation
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), originalX)),
            new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), originalX - 10)),
            new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), originalX + 10)),
            new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), originalX - 10)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), originalX + 10)),
            new KeyFrame(Duration.millis(250), new KeyValue(node.translateXProperty(), originalX - 5)),
            new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), originalX + 5)),
            new KeyFrame(SHAKE_DURATION, new KeyValue(node.translateXProperty(), originalX))
        );
        
        timeline.setOnFinished(e -> node.setEffect(originalEffect));
        timeline.play();
    }
    
    /**
     * Plays a streak bonus animation (rotate + pulse).
     */
    public static void playStreakBonus(Node node) {
        // Save original
        var originalEffect = node.getEffect();
        
        // Golden glow
        DropShadow glow = new DropShadow();
        glow.setColor(Color.GOLD);
        glow.setRadius(30);
        glow.setSpread(0.7);
        node.setEffect(glow);
        
        // Scale pulse
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.3);
        scale.setToY(1.3);
        scale.setCycleCount(4);
        scale.setAutoReverse(true);
        
        // Subtle rotation
        RotateTransition rotate = new RotateTransition(Duration.millis(600), node);
        rotate.setFromAngle(-5);
        rotate.setToAngle(5);
        rotate.setCycleCount(2);
        rotate.setAutoReverse(true);
        
        ParallelTransition parallel = new ParallelTransition(scale, rotate);
        parallel.setOnFinished(e -> {
            node.setEffect(originalEffect);
            node.setRotate(0);
        });
        parallel.play();
    }
    
    /**
     * Plays a fade-in animation.
     */
    public static void fadeIn(Node node) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
    
    /**
     * Plays a fade-out animation.
     */
    public static void fadeOut(Node node, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setFromValue(1);
        fade.setToValue(0);
        if (onFinished != null) {
            fade.setOnFinished(e -> onFinished.run());
        }
        fade.play();
    }
    
    /**
     * Plays a slide-in animation from the right.
     */
    public static void slideInFromRight(Node node) {
        node.setTranslateX(50);
        node.setOpacity(0);
        
        TranslateTransition translate = new TranslateTransition(Duration.millis(300), node);
        translate.setFromX(50);
        translate.setToX(0);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        new ParallelTransition(translate, fade).play();
    }
    
    /**
     * Highlights a node briefly.
     */
    public static void highlight(Node node, Color color) {
        var originalEffect = node.getEffect();
        
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(15);
        glow.setSpread(0.5);
        node.setEffect(glow);
        
        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(e -> node.setEffect(originalEffect));
        pause.play();
    }
}
