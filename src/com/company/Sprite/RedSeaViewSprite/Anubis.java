package com.company.Sprite.RedSeaViewSprite;

import com.company.Sprite.Sprite;

import javax.swing.*;

public class Anubis extends Sprite {
    public Anubis(int x, int y) {
        setPosition(x, y);
        img = new ImageIcon("anubis.png");
    }

    @Override
    public String overlap(int x, int y) {
        return null;
    }
}
