package org.rscemulation.client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;

import org.rscemulation.client.mudclient;

public class Inputs implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
	private boolean translate = true;
	private GameWindow aGameWindow = null;

	public Inputs(GameWindow gw, boolean trans) {
		this.aGameWindow = gw;
		this.translate = trans;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		aGameWindow.keyDown(e.isShiftDown(), e.isControlDown(), e.isActionKey(), e.getKeyCode(), e.getKeyChar(), e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		aGameWindow.keyUp(e.isControlDown(), e.getKeyCode(), e.getKeyChar());
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		aGameWindow.mouseMove(e.getX(), e.getY() - (translate ? 24 : 0));

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		aGameWindow.mouseDrag(e.isMetaDown(), e.getX(), e.getY() - (translate ? 24 : 0));
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		aGameWindow.mouseDown(e.isMetaDown(), e.getX(), e.getY() - (translate ? 24 : 0));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		aGameWindow.mouseUp(e.getX(), e.getY() - (translate ? 24 : 0));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		aGameWindow.handleScroll(e.getWheelRotation());
	}
}