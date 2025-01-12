import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by ashesh on 9/18/2015.
 */
public class JOGLFrame extends JFrame
{
    private View view;
    private TextRenderer textRenderer;
    private GLCanvas canvas;
    int frameno;


    public JOGLFrame(String title)
    {
        //routine JFrame setting stuff
        super(title);
        setSize(500,500); //this opens a 400x400 window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //when X is pressed, close program

        //Our View class is the actual driver of the OpenGL stuff
        view = new View();

        GLProfile glp = GLProfile.getGL2GL3();
        GLCapabilities caps = new GLCapabilities(glp);
        canvas = new GLCanvas(caps);

        add(canvas);

        //capture mouse events
        MyMouseAdapter mouseAdapter = new MyMouseAdapter();

        canvas.addMouseListener(mouseAdapter);
        canvas.addMouseMotionListener(mouseAdapter);
        canvas.addKeyListener(new KeyboardListener());

        canvas.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable glAutoDrawable) { //called the first time this canvas is created. Do your initialization here
                try
                {
                    view.init(canvas);
                    InputStream in = getClass().getClassLoader()
                            .getResourceAsStream
                                    ("scenegraphs/two-bicycles" +
                                    ".xml");
                    view.initScenegraph(canvas,in);
                    textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 18),true,false);
                    glAutoDrawable.getGL().setSwapInterval(1);
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(JOGLFrame.this,e.getMessage(),"Error while loading",JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }

            @Override
            public void dispose(GLAutoDrawable glAutoDrawable) { //called when the canvas is destroyed.
                view.dispose(glAutoDrawable);
            }

            @Override
            public void display(GLAutoDrawable glAutoDrawable) { //called every time this window must be redrawn
                view.draw(canvas);
                /*textRenderer.beginRendering(canvas.getWidth(), canvas.getHeight());
                // optionally set the color
                textRenderer.setColor(1.0f, 1.0f, 0.0f, 1.0f);
                String text = "Frame rate: " + canvas.getAnimator().getLastFPS();
                textRenderer.draw(text, 10, canvas.getHeight()-50);
                textRenderer.endRendering();*/
                if (frameno<10000) {
                    String filename = "output/image"
                      +String.format("%03d.png",frameno);
                    try {
                        view.captureFrame(filename,glAutoDrawable);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                    frameno++;
                }
            }

            @Override
            public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) { //called every time this canvas is resized
                view.reshape(glAutoDrawable,x,y,width,height);
                repaint(); //refresh window
            }
        });

        canvas.setFocusable(true);
        canvas.requestFocus();

        //Add an animator to the canvas
        AnimatorBase animator = new FPSAnimator(canvas,60);
        animator.setUpdateFPSFrames(50,null);
        animator.start();

        frameno = 0;
    }

    private class KeyboardListener implements KeyListener{
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_O:
                    view.updateCameraMode("near-bike");
                    break;
                case KeyEvent.VK_F:
                    view.updateCameraMode("far-bike");
                    break;
                case KeyEvent.VK_G:
                    view.updateCameraMode("global");
                    break;
                case KeyEvent.VK_H:
                    view.updateCameraMode("helicopter");
                    break;
                case KeyEvent.VK_I:
                    view.updateCameraMode("walking");
                    break;
                case KeyEvent.VK_UP :
                    view.updateCamera(1, "t");
                    break;
                case KeyEvent.VK_DOWN:
                    view.updateCamera(-1, "t");
                    break;
                case KeyEvent.VK_LEFT:
                    view.updateCamera(1, "s");
                    break;
                case KeyEvent.VK_RIGHT:
                    view.updateCamera(-1, "s");
                    break;
                case KeyEvent.VK_W :
                    view.updateCamera(1, "w");
                    break;
                case KeyEvent.VK_Z:
                    view.updateCamera(-1, "w");
                    break;
                case KeyEvent.VK_A:
                    view.updateCamera(-1, "a");
                    break;
                case KeyEvent.VK_S:
                    view.updateCamera(1, "a");
                    break;
            }
        }
    }

    private class MyMouseAdapter extends MouseAdapter
    {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1)
                JOGLFrame.this.view.mousePressed(e.getX(),e.getY());

        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (e.getButton() == MouseEvent.BUTTON1)
                JOGLFrame.this.view.mouseReleased(e.getX(),e.getY());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            JOGLFrame.this.view.mouseDragged(e.getX(),e.getY());
            JOGLFrame.this.canvas.repaint();
        }
    }


}
