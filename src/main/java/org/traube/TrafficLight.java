package org.traube;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.apache.commons.lang3.time.StopWatch;

class LightPanel extends JPanel {
    private Color color;
    private Boolean phase = false;

    public LightPanel(Color color) {
        this(color, false);
    }

    public LightPanel(Color color, Boolean defaultPhase) {
        super();
        this.color = color;
        this.setForeground(this.color);
        this.setOpaque(false);
        this.phase ^= defaultPhase;
        this.toggle();
    }

    public void toggle() {
        if (this.phase == true) {
            this.setForeground(this.color);
        } else {
            this.setForeground(new Color(8, 8, 8));
        }
        this.phase ^= true;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = g2d.getClipBounds().width, height = g2d.getClipBounds().height;
        g2d.setColor(new Color(48, 48, 48));
        g2d.fillOval(0, 0, width - 1, height - 1);
        g2d.setColor(this.getForeground());
        g2d.fillOval((int) (width * 0.05), (int) (height * 0.05), (int) (width * 0.9), (int) (height * 0.9));
        g2d.dispose();
    }
}

public class TrafficLight extends JFrame {
    private String appTitle = "Traffic Light";
    private Boolean phase = false;

    private LightPanel greenLight, yellowLight, redLight;
    private JButton button;

    interface CompUtil extends Consumer<JComponent> {
        static void setPanelSize(JComponent component) {
            component.setPreferredSize(new Dimension(200, 200));
        }
    }

    private <T extends JComponent> void addComponent(T component) {
        this.addComponent(component, null);
    }

    private <T extends JComponent> void addComponent(T component, Consumer<T> function) {
        component.setFont(new Font("FiraCode Nerd Font", Font.PLAIN, 32));
        if (function != null)
            function.accept(component);
        this.add(component);
    }

    private void setTimeout(int delay, ActionListener listener) {
        Timer timer = new Timer(delay, listener);
        timer.setRepeats(false);
        timer.start();
    }

    private StopWatch watch = new StopWatch();
    private boolean notCalledYet = true;

    private void switchLight(ActionEvent event) {
        if (notCalledYet || (phase && watch.getTime() > 2000) || (!phase &&
                watch.getTime() > 3000)) {
            if (phase) {
                greenLight.toggle();
                yellowLight.toggle();
                setTimeout(3000, e -> {
                    yellowLight.toggle();
                    redLight.toggle();
                });

            } else {
                yellowLight.toggle();
                setTimeout(2000, e -> {
                    redLight.toggle();
                    yellowLight.toggle();
                    greenLight.toggle();
                });
            }
            phase ^= true;
            notCalledYet = false;
            watch.reset();
            watch.start();
        }
    }

    public TrafficLight() {
        this.setTitle(appTitle);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(4, 1, 10, 10));
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        Border border = BorderFactory.createLineBorder(new Color(48, 48, 48), 8);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(border, padding));
        contentPanel.setBackground(new Color(96, 96, 96));
        this.setContentPane(contentPanel);

        redLight = new LightPanel(Color.red, true);
        yellowLight = new LightPanel(Color.yellow);
        greenLight = new LightPanel(Color.green);
        button = new JButton("Switch");

        addComponent(redLight, CompUtil::setPanelSize);
        addComponent(yellowLight, CompUtil::setPanelSize);
        addComponent(greenLight, CompUtil::setPanelSize);
        addComponent(button, element -> element.addActionListener(this::switchLight));

        this.pack();
    }

    public static void main(String[] args) {
        new TrafficLight();
    }
}