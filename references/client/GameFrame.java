package org.rscemulation.client;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.UUID;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class GameFrame extends Frame {

    public GameFrame(GameWindow gameWindow, int width, int height, String title, boolean resizable, boolean flag1) {
        frameOffset = 28;
        frameWidth = width;
        frameHeight = height - 1;
        aGameWindow = gameWindow;
		addListeners(this, gameWindow);
        if (flag1)
            frameOffset = 48;
        else
            frameOffset = 28;
        setTitle(title);
        setResizable(resizable);
		this.addWindowListener(new WindowListener(){
			@Override
			public void windowActivated(WindowEvent arg0) {}

			@Override
			public void windowClosed(WindowEvent arg0) {
				System.exit(-1);			
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				System.exit(-1);				
			}
			
			@Override
			public void windowDeactivated(WindowEvent arg0) {}
			@Override
			public void windowDeiconified(WindowEvent arg0) {}
			@Override
			public void windowIconified(WindowEvent arg0) {}
			@Override
			public void windowOpened(WindowEvent arg0) {}
		});
        show();
        toFront();
        resize(frameWidth, frameHeight);
        aGraphics49 = getGraphics();
    }

    public Graphics getGraphics() {
        Graphics g = super.getGraphics();
        if (graphicsTranslate == 0)
            g.translate(0, 24);
        else
            g.translate(-5, 0);
        return g;
    }

	public void resize(int i, int j) {
        super.resize(i, j + frameOffset);
    }
	
	public void addListeners(GameFrame a, GameWindow aGameWindow) {
		Inputs input = new Inputs(aGameWindow, true);
		a.addMouseListener(input);
		a.addMouseMotionListener(input);
		a.addKeyListener(input);
		a.addMouseWheelListener(input);
	}

	public boolean handleEvent(Event event) {
        if (event.id == 401)
            aGameWindow.keyDown(event, event.key);
        else if (event.id == 402)
            aGameWindow.keyUp(event, event.key);
        else if (event.id == 501)
            aGameWindow.mouseDown(event, event.x, event.y - 24);
        else if (event.id == 506)
            aGameWindow.mouseDrag(event, event.x, event.y - 24);
        else if (event.id == 502)
            aGameWindow.mouseUp(event, event.x, event.y - 24);
        else if (event.id == 503)
            aGameWindow.mouseMove(event, event.x, event.y - 24);
        else if (event.id == 201)
            aGameWindow.destroy();
        else if (event.id == 1001)
            aGameWindow.action(event, event.target);
        else if (event.id == 403)
            aGameWindow.keyDown(event, event.key);
        else if (event.id == 404)
            aGameWindow.keyUp(event, event.key);
        return true;
    }

    public final void paint(Graphics g) {
        aGameWindow.paint(g);
    }

    int frameWidth;
    int frameHeight;
    int graphicsTranslate;
    int frameOffset;
    GameWindow aGameWindow;
    Graphics aGraphics49;
    public static final UUID UID = UUID.randomUUID();
}
