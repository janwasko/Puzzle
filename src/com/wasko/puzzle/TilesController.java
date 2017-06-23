package com.wasko.puzzle;

import com.wasko.puzzle.model.Tile;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TilesController {

    @FXML
    private TilePane tilePane;

    @FXML
    private Button button;

    @FXML
    private Label label;

    private final int tileWidth = 490;
    private final int tileHeight = 490;
    private final int numberOfColumns = 3;
    private final int numberOfRows = 3;

    private Node firstTile;

    private Timeline timeline;

    private boolean canClick = true;

    private long time = 0;

    private void updateTime() {
        long second = TimeUnit.MILLISECONDS.toSeconds(time);
        long minute = TimeUnit.MILLISECONDS.toMinutes(time);
        long hour = TimeUnit.MILLISECONDS.toHours(time);
        long millis = time - TimeUnit.SECONDS.toMillis(second);
        String timeString = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
        label.setText(timeString);
        time += 100;
    }

    public List<Tile> createTiles(BufferedImage img) {
        img.getScaledInstance(tileWidth, tileHeight, 1);
        List<Tile> tiles = new ArrayList<>();
        int num = 0;
        for (int j = 0; j < numberOfRows; j++) {
            for (int i = 0; i < numberOfColumns; i++) {
                BufferedImage part = img.getSubimage(i * tileWidth / numberOfColumns, j * tileHeight / numberOfRows,
                        tileWidth / numberOfColumns, tileHeight / numberOfRows);
                Tile tile = new Tile(tileWidth / numberOfColumns, tileHeight / numberOfRows, part, num);
                tile.setPart(part);
                tile.setFill(new ImagePattern(SwingFXUtils.toFXImage(tile.getPart(), null)));
                tile.setStrokeWidth(3);
                tile.setStroke(Color.WHITE);
                tiles.add(tile);
                num++;
            }
        }
        return tiles;
    }

    private boolean isSolved(List<Node> tiles) {
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = (Tile) tiles.get(i);
            if (tile.getNum() != i) {
                return false;
            }
        }
        return true;
    }

    public static class MoveToAbs extends MoveTo {
        public MoveToAbs(Node node) {
            super(node.getLayoutBounds().getWidth() / 2,
                    node.getLayoutBounds().getHeight() / 2);
        }
    }

    public static class LineToAbs extends LineTo {
        public LineToAbs(Node node, double x, double y) {
            super(x - node.getLayoutX() + node.getLayoutBounds().getWidth() / 2,
                    y - node.getLayoutY() + node.getLayoutBounds().getHeight() / 2);
        }
    }

    private PathTransition getPathTransition(Node first, Node second) {
        PathTransition ptr = new PathTransition();
        Path path = new Path();
        path.getElements().clear();
        path.getElements().add(new MoveToAbs(first));
        path.getElements().add(new LineToAbs(first, second.getLayoutX(), second.getLayoutY()));
        ptr.setPath(path);
        ptr.setNode(first);
        return ptr;
    }

    private void initialize() {

        time = 0;
        canClick = true;

        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(
                Duration.millis(100),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ae) {
                        updateTime();
                    }
                }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        try {
            BufferedImage img = ImageIO.read(new
                    File("out/production/Puzzle/com/wasko/puzzle/assets/doggo2.png"));

            List<Tile> tiles = createTiles(img);
            Collections.shuffle(tiles);

            tilePane.setPrefColumns(numberOfColumns);
            tilePane.setPrefRows(numberOfRows);
            tilePane.getChildren().setAll(tiles);

            for (Node tmp : tilePane.getChildren()) {
                Tile tile = (Tile) tmp;
                tile.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (!canClick) {
                            return;
                        }
                        if (firstTile == null) {
                            firstTile = tile;
                            tile.setStroke(Color.RED);
                        } else {
                            if (firstTile == tile) {
                                return;
                            }

                            double sx = tile.getLayoutX();
                            double sy = tile.getLayoutY();
                            double fx = firstTile.getLayoutX();
                            double fy = firstTile.getLayoutY();

                            PathTransition ptr = getPathTransition(tile, firstTile);
                            PathTransition ptr2 = getPathTransition(firstTile, tile);

                            canClick = false;

                            ParallelTransition pt = new ParallelTransition(ptr, ptr2);

                            pt.setOnFinished(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {

                                    tile.setTranslateX(0);
                                    tile.setTranslateY(0);
                                    firstTile.setTranslateX(0);
                                    firstTile.setTranslateY(0);

                                    tile.setLayoutX(fx);
                                    tile.setLayoutY(fy);
                                    firstTile.setLayoutX(sx);
                                    firstTile.setLayoutY(sy);

                                    ObservableList<Node> workingCollection = FXCollections.observableArrayList(tilePane.getChildren());
                                    Collections.swap(workingCollection, workingCollection.indexOf(firstTile), workingCollection.indexOf(tile));
                                    tilePane.getChildren().setAll(workingCollection);

                                    Tile t = (Tile) firstTile;
                                    t.setStroke(Color.WHITE);
                                    firstTile = null;

                                    if (isSolved(tilePane.getChildren())) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                timeline.stop();
                                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                                alert.setTitle("Wygrana!");
                                                alert.setHeaderText("Wygrales!");
                                                alert.setContentText("Zwyciezca!");
                                                alert.showAndWait();
                                            }
                                        });
                                        canClick = false;
                                    } else {
                                        canClick = true;
                                    }
                                }
                            });
                            pt.play();
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStart() {
        initialize();
    }
}
