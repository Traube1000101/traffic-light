package org.traube;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.apache.commons.lang3.time.StopWatch;

class LightPanel extends JPanel {
    private Color color;
    private Boolean phase = false;
    private static boolean useAntialiasing = true;

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

    public static void setAntialiasing(boolean useAntialiasing) {
        LightPanel.useAntialiasing = useAntialiasing;
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
        if (useAntialiasing == true) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        int width = g2d.getClipBounds().width, height = g2d.getClipBounds().height;
        g2d.setColor(new Color(48, 48, 48));
        g2d.fillOval(0, 0, width - 1, height - 1);
        g2d.setColor(this.getForeground());
        g2d.fillOval((int) (width * 0.05), (int) (height * 0.05), (int) (width * 0.9), (int) (height * 0.9));
        g2d.dispose();
    }
}

class ComboboxToolTipRenderer extends DefaultListCellRenderer {
    List<String> tooltips;

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        if (-1 < index && null != value && null != tooltips) {
            list.setToolTipText(tooltips.get(index));
        }

        JLabel label = (JLabel) super.getListCellRendererComponent(list,
                value, index, isSelected, cellHasFocus);
        label.setHorizontalAlignment(JLabel.CENTER);

        return label;
    }

    public void setTooltips(List<String> tooltips) {
        this.tooltips = tooltips;
    }
}

public class TrafficLight extends JFrame {
    private String appTitle = "Traffic Light";
    private Boolean phase = false;
    private short fontSize = 20;

    private LightPanel greenLight, yellowLight, redLight;
    private JPanel controlPanel;
    private JPanel comboBoxPanel;
    private JComboBox<String> comboBox;
    private JButton button;
    private JLabel comboBoxLabel;

    interface CompUtil extends Consumer<JComponent> {
        static void setPanelSize(JComponent component) {
            component.setPreferredSize(new Dimension(200, 200));
        }
    }

    private <T extends JComponent> void addComponent(T component) {
        this.addComponent(component, null);
    }

    private <T extends JComponent> void addComponent(T component, Consumer<T> function) {
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
            LightPanel.setAntialiasing(comboBox.getSelectedIndex() == 0);
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
        this.setLocationRelativeTo(null);

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

        List<String> tooltips = new ArrayList();
        comboBox = new JComboBox<>();
        comboBox.setFont(new Font("FiraCode Nerd Font", Font.PLAIN, fontSize));
        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        comboBox.setRenderer(renderer);
        comboBox.addItem("Enable");
        tooltips.add("Sometimes delays light switching");
        comboBox.addItem("Disable");
        tooltips.add("Fast rendering but jagged edges");
        renderer.setTooltips(tooltips);
        comboBoxLabel = new JLabel("Antialiasing");
        comboBoxLabel.setFont(new Font("FiraCode Nerd Font", Font.PLAIN, fontSize));
        comboBoxLabel.setHorizontalAlignment(JLabel.CENTER);
        comboBoxPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        comboBoxPanel.add(comboBoxLabel);
        comboBoxPanel.add(comboBox);

        button = new JButton("Change");
        button.setFont(new Font("FiraCode Nerd Font", Font.PLAIN, fontSize));
        button.addActionListener(this::switchLight);

        controlPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        controlPanel.setPreferredSize(new Dimension(200, 200));
        controlPanel.setOpaque(false);
        controlPanel.add(comboBoxPanel);
        controlPanel.add(button);

        addComponent(redLight, CompUtil::setPanelSize);
        addComponent(yellowLight, CompUtil::setPanelSize);
        addComponent(greenLight, CompUtil::setPanelSize);
        addComponent(controlPanel);

        this.pack();
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new TrafficLight();
    }
}