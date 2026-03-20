package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CustomButton extends JButton {

    //roundness of button
    private final int radius;

    //mouse event state toggle variables
    private boolean hover = false;
    private boolean pressed = false;

    public CustomButton(String text, Color base, int radius) {
        super(text);
        this.radius = radius;
        
        //colour and font
        setBackground(base);
        setForeground(Color.WHITE);
        setFont(getFont().deriveFont(Font.BOLD, 26f));

        //disable defaults 
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        //mouse listener events
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                hover = true;
                repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                pressed = true;
                repaint();
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        Graphics2D graphics2D = (Graphics2D) graphics.create();

        //smother buttons
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        //offsets the button back down when pressed
        int offset;
        if (pressed) {
            offset = 3;
        } else {
            offset = 0;
        }
        // Move text together down when pressed
        if (pressed) {
            graphics.translate(0, 3);
        }

        // Draw shadow only when the button is not pressed 
        if (!pressed) {
            graphics2D.setColor(new Color(0, 0, 0, 60));
            graphics2D.fillRoundRect(4, 6, w - 8, h - 6, radius, radius);
        }

        // Base button shape and positions
        Shape shape = new RoundRectangle2D.Float(2, 2 + offset, w - 4, h - 6, radius, radius);

        Color base = getBackground();

        //brightness contril of gradients based on button status 
        Color top;
        Color bottom;
        if (hover) {
            top = brighten(base, 1.25f);
            bottom = brighten(base, 1.05f);
        } else {
            top = brighten(base, 1.07f);
            if (pressed) {
                bottom = darken(base, 0.75f);
            } else {
                bottom = darken(base, 0.85f);
            }
        }
        GradientPaint gradientPaint = new GradientPaint(0, 0, top, 0, h, bottom);
        graphics2D.setPaint(gradientPaint);
        graphics2D.fill(shape);

        //lighten button when hovered over
        if (hover && !pressed) {
            graphics2D.setComposite(AlphaComposite.SrcOver.derive(0.18f));
            graphics2D.setPaint(new Color(255, 255, 255, 200));
            graphics2D.fill(shape);
        }

        super.paintComponent(graphics);
    }

    //lightens rgb components by set factor
    private Color brighten(Color colorInput, float brightnessFactor) {
        return new Color(
            Math.min(255, (int)(colorInput.getRed() * brightnessFactor)),
            Math.min(255, (int)(colorInput.getGreen() * brightnessFactor)),
            Math.min(255, (int)(colorInput.getBlue() * brightnessFactor))
        );
    }
    //darken
    private Color darken(Color colorInput, float darknessFactor) {
        return new Color(
            (int)(colorInput.getRed() * darknessFactor),
            (int)(colorInput.getGreen() * darknessFactor),
            (int)(colorInput.getBlue() * darknessFactor)
        );
    }
}