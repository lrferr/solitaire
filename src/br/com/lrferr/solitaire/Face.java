package br.com.lrferr.solitaire;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Face extends AnimatedSprite {

	private float xOrigin;
	private float yOrigin;
	private Place place;
	
	public Face(float pX, float pY, float pWidth, float pHeight,
			ITiledTextureRegion pTiledTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pWidth, pHeight, pTiledTextureRegion, pVertexBufferObjectManager);
		xOrigin = pX;
		yOrigin = pY;
	}

	
	public float getxOrigin() {
		return xOrigin;
	}

	public void setxOrigin(float xOrigin) {
		this.xOrigin = xOrigin;
	}

	public float getyOrigin() {
		return yOrigin;
	}

	public void setyOrigin(float yOrigin) {
		this.yOrigin = yOrigin;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

}
