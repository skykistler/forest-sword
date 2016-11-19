package com.teamlucidic.hax.render;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;

public class Model {
	public HashMap<Integer, Part> parts = new HashMap<Integer, Part>();
	public ArrayList<Part> partList = new ArrayList<Part>();
	public Part currentPart;
	public Texture texture;
	public int scale = 32;
	public int displayList = -1;

	public void render(double x, double y, double z) {
		if (displayList == -1) {
			if (texture != null)
				texture.bind();
			else
				Texture.unbind();

			Part datBod = getPart("body");

			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);
			if (datBod != null) {
				if (datBod.rotation[0] != 0)
					GL11.glRotated(datBod.rotation[0], 1, 0, 0);
				if (datBod.rotation[1] != 0)
					GL11.glRotated(datBod.rotation[1], 0, 1, 0);
				if (datBod.rotation[2] != 0)
					GL11.glRotated(datBod.rotation[2], 0, 0, 1);
			}

			Main.m.modeler.start();
			for (int i = 0; i < partList.size(); i++)
				partList.get(i).render(0, 0, 0);
			Main.m.modeler.finish();
			GL11.glPopMatrix();
		}
	}

	public void addPart(Part p) {
		if (p.vertexes.size() % 4 != 0) {
			Main.m.error("Weird part: " + p.name);
			return;
		}
		p.update();
		parts.put(p.name.hashCode(), p);
		partList.add(p);
	}

	public void addCurrentPart() {
		if (currentPart == null)
			return;
		addPart(currentPart);
		currentPart = null;
	}

	public void newPart(String name) {
		if (currentPart != null) {
			addPart(currentPart);
		}
		currentPart = new Part(name);
	}

	public void addCuboid(String name, String parent, int[] tex, int[] size, double[] offset, double[] attach) {
		newPart(name);
		currentPart.parent = parent;
		int width = size[0];
		int height = size[1];
		int depth = size[2];
		double worldWidth = width / (double) scale;
		double worldHeight = height / (double) scale;
		double worldDepth = depth / (double) scale;

		//bottom
		if (width > 0 && depth > 0) {
			currentPart.addVertex(0, 0, 0, tex[0] + depth, tex[1] + depth + height);
			currentPart.addVertex(worldWidth, 0, 0, tex[0] + width + depth, tex[1] + depth + height);
			currentPart.addVertex(worldWidth, 0, worldDepth, tex[0] + width + depth, tex[1] + 2 * depth + height);
			currentPart.addVertex(0, 0, worldDepth, tex[0] + depth, tex[1] + 2 * depth + height);
		}

		//top
		if (width > 0 && depth > 0) {
			currentPart.addVertex(0, worldHeight, 0, tex[0] + depth, tex[1] + depth);
			currentPart.addVertex(0, worldHeight, worldDepth, tex[0] + depth, tex[1]);
			currentPart.addVertex(worldWidth, worldHeight, worldDepth, tex[0] + width + depth, tex[1]);
			currentPart.addVertex(worldWidth, worldHeight, 0, tex[0] + width + depth, tex[1] + depth);
		}

		//left
		if (depth > 0 && height > 0) {
			currentPart.addVertex(0, 0, 0, tex[0] + depth + width, tex[1] + depth + height);
			currentPart.addVertex(0, 0, worldDepth, tex[0] + 2 * depth + width, tex[1] + depth + height);
			currentPart.addVertex(0, worldHeight, worldDepth, tex[0] + 2 * depth + width, tex[1] + depth);
			currentPart.addVertex(0, worldHeight, 0, tex[0] + depth + width, tex[1] + depth);
		}

		//right
		if (depth > 0 && height > 0) {
			currentPart.addVertex(worldWidth, 0, 0, tex[0] + depth, tex[1] + depth + height);
			currentPart.addVertex(worldWidth, worldHeight, 0, tex[0] + depth, tex[1] + depth);
			currentPart.addVertex(worldWidth, worldHeight, worldDepth, tex[0], tex[1] + depth);
			currentPart.addVertex(worldWidth, 0, worldDepth, tex[0], tex[1] + depth + height);
		}

		//front
		if (width > 0 && height > 0) {
			currentPart.addVertex(0, 0, 0, tex[0] + depth + width, tex[1] + depth + height);
			currentPart.addVertex(0, worldHeight, 0, tex[0] + depth + width, tex[1] + depth);
			currentPart.addVertex(worldWidth, worldHeight, 0, tex[0] + depth, tex[1] + depth);
			currentPart.addVertex(worldWidth, 0, 0, tex[0] + depth, tex[1] + depth + height);
		}

		//back
		if (width > 0 && height > 0) {
			currentPart.addVertex(0, 0, worldDepth, tex[0] + 2 * depth + width, tex[1] + depth + height);
			currentPart.addVertex(worldWidth, 0, worldDepth, tex[0] + 2 * depth + 2 * width, tex[1] + depth + height);
			currentPart.addVertex(worldWidth, worldHeight, worldDepth, tex[0] + 2 * depth + 2 * width, tex[1] + depth);
			currentPart.addVertex(0, worldHeight, worldDepth, tex[0] + 2 * depth + width, tex[1] + depth);
		}

		currentPart.translate(offset[0] * 32d / scale, offset[1] * 32d / scale, offset[2] * 32d / scale);
		if (attach == null)
			currentPart.setAttachmentToMidpoint();
		else
			currentPart.setAttachmentPoint(attach[0] * 32d / scale, attach[1] * 32d / scale, attach[2] * 32d / scale);

		addCurrentPart();
	}

	public void setTexture(String tex) {
		texture = Texture.getTexture(tex);
	}

	public Part getPart(String name) {
		return parts.get(name.hashCode());
	}

	public Model copy() {
		Model copy = new Model();
		copy.setTexture(texture.path);
		copy.scale = scale;
		copy.displayList = this.displayList;
		for (Part p : partList) {
			copy.currentPart = p;
			copy.addCurrentPart();
		}
		return copy;
	}

	public class Part {
		public String name;
		public String parent;
		public Part parentP;
		public ArrayList<double[]> vertexes = new ArrayList<double[]>();
		public double[] attachPoint = new double[3];
		public double[] rotation = new double[3];
		public ArrayList<Part> children = new ArrayList<Part>();

		public Part(String s) {
			name = s;
		}

		public void update() {
			if (parent != null && (parentP == null || !parentP.name.equals(parent))) {
				parentP = getPart(parent);
				parentP.children.add(this);
			} else
				parentP = null;
		}

		public void render(double x, double y, double z) {
			//			GL11.glPushMatrix();
			//			GL11.glTranslated(x + attachPoint[0], y + attachPoint[1], z + attachPoint[2]);
			//			if (rotation[0] != 0)
			//				GL11.glRotated(rotation[0], 1, 0, 0);
			//			if (rotation[1] != 0)
			//				GL11.glRotated(rotation[1], 0, 1, 0);
			//			if (rotation[2] != 0)
			//				GL11.glRotated(rotation[2], 0, 0, 1);
			//			GL11.glTranslated(-attachPoint[0], -attachPoint[1], -attachPoint[2]);

			for (double[] vertex : vertexes)
				Main.m.modeler.addVertexTex(vertex[0] + x, vertex[1] + y, vertex[2] + z, vertex[3], vertex[4]);

			//			GL11.glPopMatrix();
		}

		public void addVertex(double x, double y, double z, double u, double v) {
			double[] vertex = new double[5];
			vertex[0] = x;
			vertex[1] = y;
			vertex[2] = z;
			vertex[3] = u / texture.width;
			vertex[4] = v / texture.height;
			vertexes.add(vertex);
		}

		public void setAttachmentPoint(double x, double y, double z) {
			attachPoint[0] = x;
			attachPoint[1] = y;
			attachPoint[2] = z;
		}

		public void setAttachmentToMidpoint() {
			double[] midPoint = new double[3];
			for (double[] vertex : vertexes)
				for (int i = 0; i < 3; i++)
					midPoint[i] += vertex[i];

			for (int i = 0; i < 3; i++)
				midPoint[i] /= vertexes.size();
			attachPoint = midPoint;
			//			if (name.equals("body")) {
			//				for (int i  = 0; i < attachPoint.length; i++)
			//					System.out.println(attachPoint[i]);
			//				System.out.println(" ");
			//			}
		}

		public void setRotation(double angX, double angY, double angZ) {
			rotation[0] = angX;
			rotation[1] = angY;
			rotation[2] = angZ;
			for (int i = 0; i < children.size(); i++) {
				Part p = children.get(i);
				if (p != null)
					p.setRotation(angX, angY, angZ);
			}
		}

		public void rotate(double angX, double angY, double angZ) {
			rotation[0] += angX;
			rotation[1] += angY;
			rotation[2] += angZ;
			for (Part p : children)
				p.rotate(angX, angY, angZ);
		}

		public void translate(double offsetX, double offsetY, double offsetZ) {
			for (double[] vertex : vertexes) {
				vertex[0] += offsetX;
				vertex[1] += offsetY;
				vertex[2] += offsetZ;
			}
		}

		public Part copy() {
			Part copy = new Part(new String(name));
			copy.parent = new String(parent);
			for (double[] d : vertexes) {
				double[] dCopy = new double[6];
				System.arraycopy(d, 0, dCopy, 0, d.length);
				copy.vertexes.add(dCopy);
			}
			System.arraycopy(attachPoint, 0, copy.attachPoint, 0, attachPoint.length);
			System.arraycopy(rotation, 0, copy.rotation, 0, rotation.length);
			return copy;
		}
	}
}
