package com.wasko.puzzle.model;

import javafx.scene.shape.Rectangle;

import java.awt.image.BufferedImage;

public class Tile extends Rectangle {

    private BufferedImage part;
    private int num;

    public Tile(double width, double height, BufferedImage part, int num) {
        super(width, height);
        this.part = part;
        this.num = num;
    }

    public Tile(BufferedImage part, int id) {
        this.part = part;
        this.num = id;
    }

    public BufferedImage getPart() {
        return part;
    }

    public void setPart(BufferedImage part) {
        this.part = part;
    }

    public int getNum() {
        return num;
    }
}
