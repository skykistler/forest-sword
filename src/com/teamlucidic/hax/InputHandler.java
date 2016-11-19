package com.teamlucidic.hax;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.render.gui.menu.Menu;

public class InputHandler {

	public MouseHelper mouse;
	public Key forward;
	public Key back;
	public Key right;
	public Key left;
	public Key jump;
	public Key pause;
	public Key inventory;
	public Key fullscreen;

	public Key num1, num2, num3, num4;
	public Key shift, z, x, c;

	public boolean[] keyStates = new boolean[256];
	public boolean[] keyStateTick = new boolean[256];

	public InputHandler() {
		mouse = new MouseHelper();
		forward = new Key(Keyboard.KEY_W);
		back = new Key(Keyboard.KEY_S);
		right = new Key(Keyboard.KEY_D);
		left = new Key(Keyboard.KEY_A);
		jump = new Key(Keyboard.KEY_SPACE);
		shift = new Key(Keyboard.KEY_LSHIFT);
		pause = new Key(Keyboard.KEY_ESCAPE);
		inventory = new Key(Keyboard.KEY_E);
		fullscreen = new Key(Keyboard.KEY_F1);

		num1 = new Key(Keyboard.KEY_1);
		num2 = new Key(Keyboard.KEY_2);
		num3 = new Key(Keyboard.KEY_3);
		num4 = new Key(Keyboard.KEY_4);
		shift = new Key(Keyboard.KEY_LSHIFT);
		z = new Key(Keyboard.KEY_Z);
		x = new Key(Keyboard.KEY_X);
		c = new Key(Keyboard.KEY_C);
	}

	public void update() {
		for (int i = 0; i < 256; i++)
			keyStateTick[i] = false;
		mouse.update();
		
		while (Keyboard.next()) {
			char c = Keyboard.getEventCharacter();
			if (c != Keyboard.CHAR_NONE)
				Menu.keyEvent(c);
		}
	}

	public boolean isDown(int keycode) {
		return Keyboard.isKeyDown(keycode);
	}

	public boolean checkKey(Key key) {
		return checkKey(key.keycode);
	}

	public boolean checkKey(int keycode) {
		if (keyStateTick[keycode])
			return keyStates[keycode];
		if (isDown(keycode) != keyStates[keycode]) {
			keyStateTick[keycode] = true;
			return keyStates[keycode] = !keyStates[keycode];
		}
		return false;
	}

	public class MouseHelper {
		public double sensitivity = .18;
		public double drag = .4;
		public double velX, velY;
		public boolean[] mouseStates = new boolean[10];
		public boolean[] mouseStateTick = new boolean[10];

		public void update() {
			for (int i = 0; i < 10; i++)
				mouseStateTick[i] = false;
			if (Mouse.isGrabbed()) {
				velX += getDX() * sensitivity;
				velY += getDY() * sensitivity;
				velX *= drag;
				velY *= drag;
				velX = Math.abs(velX) < .05 ? 0 : velX;
				velY = Math.abs(velY) < .05 ? 0 : velY;
				Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
			}
		}

		public double getDX() {
			if (Display.isActive())
				return Mouse.getDX();
			return 0;
		}

		public double getDY() {
			if (Display.isActive())
				return Mouse.getDY();
			return 0;
		}

		public boolean isDown(int k) {
			return Display.isActive() && Mouse.isButtonDown(k);
		}

		public boolean checkButton(int k) {
			if (mouseStateTick[k])
				return mouseStates[k];
			if (isDown(k) != mouseStates[k]) {
				mouseStateTick[k] = true;
				return mouseStates[k] = !mouseStates[k];
			}
			return false;
		}

		public int getX() {
			return Mouse.getX();
		}

		public int getY() {
			return Mouse.getY();
		}

		public boolean isDown() {
			return isDown(0) || isDown(1);
		}

		public boolean checkButton() {
			return checkButton(0) || checkButton(1);
		}

	}

	public class Key {
		public Key(int kc) {
			keycode = kc;
		}

		public boolean isDown() {
			return Main.m.input.isDown(keycode);
		}

		public boolean checkKey() {
			return Main.m.input.checkKey(this);
		}

		public int keycode = 0;
	}

	public class Trigger {
		public Key key;
		public MouseHelper mouse;
		public int button;

		public Trigger(Key k) {
			key = k;
		}

		public Trigger(int b) {
			mouse = Main.m.input.mouse;
			button = b;
		}

		public boolean checkTrigger() {
			if (mouse != null)
				return mouse.checkButton(button);
			else
				return key.checkKey();
		}

		public boolean isDown() {
			if (mouse != null)
				return mouse.isDown(button);
			else
				return key.isDown();
		}
	}
}
