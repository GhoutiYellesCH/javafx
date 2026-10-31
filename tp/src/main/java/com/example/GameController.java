package com.example;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.URL;

public class GameController {

    private final Pane root;
    private ImageView mario;
    private ImageView backgroundView;
    private Label coinCounterLabel;

    private ImageView star;
    private ImageView mushroom;
    private ImageView bird;
    private ImageView coin;

    private double marioBaseSpeed = 5;
    private double marioCurrentSpeed = marioBaseSpeed;
    private final double MARIO_JUMP_VELOCITY = -15;
    private final double GRAVITY = 0.8;
    private final double FLOOR_Y = 480;
    private Point2D velocity = new Point2D(0, 0);

    private boolean isPoweredUp = false; 
    private boolean isStarPowered = false;
    private int coinsCollected = 0;

    // Audio Clips (Step 5.2) - NOTE: Update paths if you renamed them to theme.mp3/death.mp3
    private AudioClip coinSound;
    private AudioClip themeSong;
    private AudioClip deathSound;

    public GameController(Pane root) {
        this.root = root;
        loadAudio();
    }

    private void loadAudio() {
        // Paths for audio files
        final String THEME_PATH = "/Nintendo_Super_Mario_Brothers_Theme_Song_Ringtone_(by Fringster.com).mp3";
        final String DEATH_PATH = "/Nintendo_Player_Down_Ringtone_(by Fringster.com).mp3";

        try {
            // Coin sound will be the same as the theme song for simplicity
            coinSound = new AudioClip(getSafeResourcePath(THEME_PATH)); 
            themeSong = new AudioClip(getSafeResourcePath(THEME_PATH));
            deathSound = new AudioClip(getSafeResourcePath(DEATH_PATH));
            System.out.println("Audio files loaded successfully.");
        } catch (Exception e) {
            System.err.println("AUDIO ERROR: Could not load one or more audio files. Check file names and capitalization in tp/resources.");
            System.err.println("Details: " + e.getMessage());
            coinSound = themeSong = deathSound = null; 
        }
    }
    
    /** Helper method to get resource URL string or throw exception if null */
    private String getSafeResourcePath(String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new RuntimeException("Resource not found at: " + path);
        }
        return resource.toExternalForm();
    }

    public void setupGame() {
        // Reset properties
        if (themeSong != null) themeSong.stop();
        root.getChildren().clear();
        isPoweredUp = false;
        isStarPowered = false;
        marioCurrentSpeed = marioBaseSpeed;
        velocity = Point2D.ZERO;
        // The coin counter is NOT reset here, it is reset in resetGame()

        // 1. Setup Background (ImageView for Scrolling) (Step 5.1)
        final String BG_PATH = "/3739446.jpg";
        InputStream bgStream = getClass().getResourceAsStream(BG_PATH);

        if (bgStream == null) {
            System.err.println("FATAL IMAGE ERROR: Background image not found at: " + BG_PATH + ". Using colored fallback background.");
            // Create a wide solid-color background image as a fallback so scrolling still works
            int fallbackWidth = 900 * 4; // a reasonable world width
            int fallbackHeight = 600; // match typical scene height
            WritableImage wi = new WritableImage(fallbackWidth, fallbackHeight);
            PixelWriter pw = wi.getPixelWriter();
            for (int yy = 0; yy < fallbackHeight; yy++) {
                for (int xx = 0; xx < fallbackWidth; xx++) {
                    // simple gradient to make movement visible
                    double t = (double) xx / fallbackWidth;
                    pw.setColor(xx, yy, Color.color(0.2 + 0.2 * t, 0.5, 0.9 - 0.2 * t));
                }
            }
            backgroundView = new ImageView(wi);
            backgroundView.setFitWidth(fallbackWidth);
            backgroundView.setFitHeight(fallbackHeight);
        } else {
            Image bgImage = new Image(bgStream);
            backgroundView = new ImageView(bgImage);
            backgroundView.setFitWidth(bgImage.getWidth() * 4); 
            backgroundView.setFitHeight(root.getHeight());
        }
        
        root.getChildren().add(0, backgroundView); 

        // 2. Setup Mario (Step 2.1)
        mario = createObject("/Mario.jpg", 50, FLOOR_Y, 50, 50);
        root.getChildren().add(mario);

        // 3. Setup Objects (Step 4)
        star = createObject("/png-clipart-yellow-star-art-super-mario-bros-super-mario-galaxy-2-5-star-heroes-super-mario-bros-thumbnail.png", 500, FLOOR_Y + 10, 40, 40);
        mushroom = createObject("/il_fullxfull.2983914114_glet.png", 700, FLOOR_Y + 10, 40, 40);
        bird = createObject("/captain-toad-treasure-tracker-super-mario-bros-2-wii-u-png-favpng-Jdkk9wcAbDWWDkzfmYGdnGwNw.jpg", 1500, 400, 50, 50);
        coin = createObject("/png-transparent-new-super-mario-bros-2-coins-gold-coin-super-mario-bros-trademark-thumbnail.png", 850, FLOOR_Y - 50, 30, 30);
        root.getChildren().addAll(star, mushroom, bird, coin);

        // 4. Setup UI and Controls (Step 2.2, 4.1, 6)
        setupUIControls();
        
        // Play theme song (Step 5.2)
        if (themeSong != null) {
            themeSong.setCycleCount(AudioClip.INDEFINITE);
            themeSong.play();
        }
        
        // Game Loop
        Timeline gameLoop = new Timeline(new KeyFrame(Duration.millis(1000/60), e -> updateGame()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }
    
    private void setupUIControls() {
        // Menu Bar (Step 6)
        MenuBar menuBar = new MenuBar();
        Menu gameMenu = new Menu("Game");
        MenuItem newGameItem = new MenuItem("Nouvelle Partie"); // (Step 6.1)
        newGameItem.setOnAction(e -> resetGame());
        gameMenu.getItems().add(newGameItem);
        menuBar.getMenus().add(gameMenu);
        menuBar.setPrefWidth(root.getWidth());
        
        // Coin Counter (Step 4.1)
        coinCounterLabel = new Label("Coins: " + coinsCollected);
        coinCounterLabel.getStyleClass().add("coin-counter");
        coinCounterLabel.setLayoutX(10);
        coinCounterLabel.setLayoutY(30);

        // Control Buttons (Step 2.2)
        HBox controlBox = new HBox(10);
        Button rightBtn = new Button("Right");
        Button leftBtn = new Button("Left");
        Button upBtn = new Button("Up");

        // Event Handling for Buttons (Step 3.1)
        rightBtn.setOnAction(e -> moveMario(marioCurrentSpeed, 0));
        leftBtn.setOnAction(e -> moveMario(-marioCurrentSpeed, 0));
        upBtn.setOnAction(e -> handleJump());

        controlBox.getChildren().addAll(leftBtn, rightBtn, upBtn);
        controlBox.setLayoutX(10);
        controlBox.setLayoutY(root.getHeight() - 50);
        
        root.getChildren().addAll(menuBar, coinCounterLabel, controlBox);
    }
    
    /** Creates an ImageView, safely handling missing images. */
    private ImageView createObject(String imageName, double x, double y, double width, double height) {
        InputStream stream = getClass().getResourceAsStream(imageName);
        
        if (stream == null) {
            System.err.println("IMAGE ERROR: Object image not found at: " + imageName + ". Using red placeholder.");
            int w = Math.max(1, (int) width);
            int h = Math.max(1, (int) height);
            WritableImage placeholder = new WritableImage(w, h);
            PixelWriter pw = placeholder.getPixelWriter();
            for (int yy = 0; yy < h; yy++) {
                for (int xx = 0; xx < w; xx++) {
                    pw.setColor(xx, yy, Color.RED);
                }
            }
            ImageView view = new ImageView(placeholder);
            view.setFitWidth(width);
            view.setFitHeight(height);
            view.setX(x);
            view.setY(y);
            return view;
        }

        Image img = new Image(stream);
        ImageView view = new ImageView(img);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setX(x);
        view.setY(y);
        return view;
    }

    /** Main game loop for physics and collision checking */
    private void updateGame() {
        // Apply gravity
        if (mario.getY() < FLOOR_Y) { 
            velocity = velocity.add(0, GRAVITY);
        } else {
            mario.setY(FLOOR_Y);
            velocity = new Point2D(velocity.getX(), 0);
        }

        // Apply velocity (Y movement is handled here for jumping/falling)
        mario.setY(mario.getY() + velocity.getY());
        
        checkCollisions();
    }

    /** Handles movement via keyboard (Step 3.2) */
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.RIGHT) {
            moveMario(marioCurrentSpeed, 0);
        } else if (event.getCode() == KeyCode.LEFT) {
            moveMario(-marioCurrentSpeed, 0);
        } else if (event.getCode() == KeyCode.UP) { // <-- Handles the Up arrow key
            handleJump();
        }
    }
    
    private void handleJump() {
        if (mario.getY() >= FLOOR_Y) { // Only jump if on the ground
            velocity = new Point2D(velocity.getX(), MARIO_JUMP_VELOCITY);
        }
    }

    /** Moves Mario and handles background scrolling (Step 5.1). */
    private void moveMario(double dx, double dy) {
        
        final double SCROLL_THRESHOLD = root.getWidth() / 2;

        if (dx > 0 && mario.getX() >= SCROLL_THRESHOLD) {
            // Moving Right and Mario is past the center: Scroll the world left
            double scrollDistance = dx;
            
            // Scroll the background (using translateX)
            backgroundView.setTranslateX(backgroundView.getTranslateX() - scrollDistance);
            
            // Scroll objects backward
            star.setX(star.getX() - scrollDistance);
            mushroom.setX(mushroom.getX() - scrollDistance);
            bird.setX(bird.getX() - scrollDistance);
            coin.setX(coin.getX() - scrollDistance);
            
            // Keep Mario anchored near the center
            mario.setX(SCROLL_THRESHOLD);
            
        } else if (dx < 0 && mario.getX() <= 100) { 
            // Moving Left and near the edge: Scroll the world right
            double scrollDistance = dx;
            
            // Stop scrolling when the background returns to 0
            if (backgroundView.getTranslateX() < 0) {
                backgroundView.setTranslateX(backgroundView.getTranslateX() - scrollDistance);
                
                // Scroll objects forward
                star.setX(star.getX() - scrollDistance);
                mushroom.setX(mushroom.getX() - scrollDistance);
                bird.setX(bird.getX() - scrollDistance);
                coin.setX(coin.getX() - scrollDistance);
            } else {
                 // If we've hit the left edge of the map, just move Mario
                 mario.setX(mario.getX() + dx);
            }
        } else {
            // Mario is near the edge, so just move Mario normally
            mario.setX(mario.getX() + dx);
        }

        // Keep Mario within bounds (preventing him from leaving the left edge)
        if (mario.getX() < 0) {
            mario.setX(0);
        }
    }

    /** Checks for collision with special objects. (Step 4) */
    private void checkCollisions() {
        // Check collision with Coin (Step 4.1)
        if (root.getChildren().contains(coin) && mario.getBoundsInParent().intersects(coin.getBoundsInParent())) {
            root.getChildren().remove(coin);
            coinsCollected++;
            coinCounterLabel.setText("Coins: " + coinsCollected);
            if (coinSound != null) coinSound.play(); // Play coin sound (Step 5.2)
        }

        // Check collision with Star (Step 4.2)
        if (root.getChildren().contains(star) && mario.getBoundsInParent().intersects(star.getBoundsInParent()) && !isStarPowered) {
            root.getChildren().remove(star);
            marioCurrentSpeed *= 2; // Increase speed
            isStarPowered = true;
            
            // Rotation animation (Step 4.2)
            mario.setRotate(mario.getRotate() + 360); 
            
            // Set a timer to revert speed and rotation
            PauseTransition resetStar = new PauseTransition(Duration.seconds(8));
            resetStar.setOnFinished(e -> {
                marioCurrentSpeed = marioBaseSpeed;
                mario.setRotate(0);
                isStarPowered = false;
            });
            resetStar.play();
        }
        
        // Check collision with Mushroom (Step 4.3)
        if (root.getChildren().contains(mushroom) && mario.getBoundsInParent().intersects(mushroom.getBoundsInParent()) && !isPoweredUp) {
             root.getChildren().remove(mushroom);
             mario.setFitWidth(70); // Increase size temporarily
             mario.setFitHeight(70);
             isPoweredUp = true;
             
             // Timer to revert size
             PauseTransition sizeTimer = new PauseTransition(Duration.seconds(10));
             sizeTimer.setOnFinished(e -> revertMarioSize());
             sizeTimer.play();
        }

        // Check collision with Bird (Step 4.3)
        if (root.getChildren().contains(bird) && mario.getBoundsInParent().intersects(bird.getBoundsInParent())) {
            if (isPoweredUp) {
                 // Mario is big: lose mushroom power/size reverts
                 revertMarioSize();
                 root.getChildren().remove(bird);
            } else {
                 // Mario is small: Lose game 
                 gameOver();
            }
        }
    }

    /** Reverts Mario's size and mushroom status. */
    private void revertMarioSize() {
        mario.setFitWidth(50);
        mario.setFitHeight(50);
        isPoweredUp = false;
    }
    
    /** Handles game loss scenario. */
    private void gameOver() {
        if (themeSong != null) themeSong.stop();
        if (deathSound != null) deathSound.play();
        
        velocity = Point2D.ZERO; 
        marioCurrentSpeed = 0;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Partie Perdue!");
        alert.setContentText("Vous avez collecté " + coinsCollected + " pièces. Utilisez le menu Nouvelle Partie pour recommencer.");
        alert.showAndWait();
    }
    
    /** Resets the game to its initial state (Step 6.1) */
    public void resetGame() {
        coinsCollected = 0;
        setupGame();
    }
}