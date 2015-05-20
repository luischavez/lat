/*
 * Copyright (C) 2015 Luis Chávez
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.luischavez.lat.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class Splash extends JFrame {

    private String[] authors;
    private Image logo;

    public Splash(String[] authors) {
        this.authors = authors;
        try {
            this.logo = ImageIO.read(this.getClass().getResource("/images/logo.png"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void configure() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);
        this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        this.add(new LogoPanel());
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
    }

    public void show(Runnable onFinish) {
        this.configure();
        this.setVisible(true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> Splash.this.setVisible(false));
                onFinish.run();
            }
        }, 1000 * 5);
    }

    private class LogoPanel extends JPanel {

        @Override
        public Dimension getPreferredSize() {
            int width = Splash.this.logo.getWidth(Splash.this);
            int height = Splash.this.logo.getHeight(Splash.this);
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphics2D = (Graphics2D) g;
            graphics2D.drawImage(Splash.this.logo, 0, 0, Splash.this);
            graphics2D.setColor(Color.black);
            graphics2D.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics font = graphics2D.getFontMetrics();
            for (int i = 0; i < Splash.this.authors.length; i++) {
                String author = Splash.this.authors[i];
                int px = (int) (getWidth() / 2 - font.getStringBounds(author, graphics2D).getWidth() / 2);
                int py = 30 + (15 * i);
                graphics2D.drawString(author, px, py);
            }
        }
    }
}
