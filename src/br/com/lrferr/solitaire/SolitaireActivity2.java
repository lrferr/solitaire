package br.com.lrferr.solitaire;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.audio.sound.Sound;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.SAXUtils;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.IEntityLoader;
import org.andengine.util.level.LevelLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.xml.sax.Attributes;

import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;


public class SolitaireActivity2 extends BasePuzzle implements IOnSceneTouchListener, IOnMenuItemClickListener,  IAccelerationListener, IOnAreaTouchListener, IScrollDetectorListener, IClickDetectorListener {

	private Camera camera;
	private BitmapTextureAtlas ballTextureAtlas;
	private BitmapTextureAtlas emptyPlaceTextureAtlas;
	private BitmapTextureAtlas fontTexture;
	private BitmapTextureAtlas intervalTextureAtlas;	
	private BitmapTextureAtlas starTextureAtlas;
	private BitmapTextureAtlas menuTexture;
	
	private TiledTextureRegion ballTextureRegion;
	private TextureRegion emptyPlaceTextureRegion;
	private TextureRegion intervalTextureRegionV1;
	private TextureRegion intervalTextureRegionH1;
	private TextureRegion intervalTextureRegionV2;
	private TextureRegion intervalTextureRegionH2;
	private TextureRegion starTextureRegion;
	private ITextureRegion menuResetTextureRegion;
	private ITextureRegion menuQuitTextureRegion;
	
	private RepeatingSpriteBackground grassBackground;

	private static ArrayList<Face> faces = new ArrayList<Face>();
	private ArrayList<Body> bodyFaceList = new ArrayList<Body>();

	private static ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Body> bodyPlaceList = new ArrayList<Body>();
	
	private Scene scene;
	private int timePassed = 0;
	private int casaVaga = 0;
	
	public static int remains = 0;
	
	
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private static final String TAG_ENTITY_ATTRIBUTE_WIDTH = "width";
	private static final String TAG_ENTITY_ATTRIBUTE_HEIGHT = "height";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BALL = "ball";

	
	
	public Scene getScene() {
		return scene;
	}

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 320;
	private static final int LAYER_COUNT = 4;
	private static final int Y_SPACE = 45;
	private static final int X_SPACE = 60;
	private  final int LAYER_BACKGROUND = 0;
	private  final int LAYER_PLACE = 1;
	private Sound gameOverSound;
	static final int LAYER_BALL = 2;
	private  final int LAYER_SCORE = 3;
	private int FONT_SIZE;
	protected static final int MENU_RESET = 0;
	protected static final int MENU_QUIT = MENU_RESET + 1;

	protected static final int MENU_OK = MENU_RESET + 1;
	protected static final int MENU_NEXT_LEVEL = MENU_OK + 1;
	protected static final int MENU_SKIP = MENU_NEXT_LEVEL + 1;

	protected static final int LEVEL_COUNT = 20;
	
	protected static int LEVELS = LEVEL_COUNT;
	protected static int LEVEL_COLUMNS_PER_SCREEN = 4;
	protected static int LEVEL_ROWS_PER_SCREEN = 3;
	protected static float LEVEL_PADDING = 50.0f;

	
	private Font font;
	private Text gameOverText;
	private Text scoreText;
	private static Text remainsText;
	private Font fontScore;
	private Font fontRemain;
	private BitmapTextureAtlas fontTextureScore;
	private BitmapTextureAtlas fontTextureRemain;
	protected MenuScene menuScene;
	private BitmapTextureAtlas fontTextureMenu;
	private Font fontMenu;
	private PhysicsWorld physicsWorld;
	
	public Face face;
	public Place place;
	private BitmapTextureAtlas mFontTexture;
	private Font mFont;
	private Scene mLevelSelectScene;
	private SurfaceScrollDetector mScrollDetector;
	private ClickDetector mClickDetector;
	private BitmapTextureAtlas mLevelSelectorTextureAtlas;
	private TextureRegion mLevelSelectRegion;
	
	protected float mMinY = 0;
	protected float mMaxY = 0;
	protected int iLevelClicked = -1;
	protected int mMaxLevelReached = 21;
	private boolean isLevelSelecting;
	private int mCurrentLevel;
	
	public static final short CATEGORYBIT_PLACE = 1;
	public static final short CATEGORYBIT_BALL = 2;
	public static final short MASKBITS_PLACE = CATEGORYBIT_PLACE;
	public static final short MASKBITS_BALL = MASKBITS_PLACE;
	public static final FixtureDef PLACE_FIXTURE_DEF = PhysicsFactory.createFixtureDef(0.0f, 0.5f, 0.5f, false, CATEGORYBIT_PLACE, MASKBITS_PLACE, (short)0);
	public static final FixtureDef BALL_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1.0f, 0.5f, 0.5f, false, CATEGORYBIT_BALL, MASKBITS_BALL, (short)0);
	
	


	@Override
	public EngineOptions onCreateEngineOptions() {
		Toast.makeText(this, "Touch & Drag the blue balls.\nThen drag and drop on the white hole!", Toast.LENGTH_LONG).show();
		this.camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return 
				new EngineOptions(true, 
						ScreenOrientation.LANDSCAPE_SENSOR, 
						new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), 
						this.camera);		

	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.ballTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 128, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.emptyPlaceTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		this.intervalTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		this.starTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		this.ballTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.ballTextureAtlas, this, "blueballtiled.png", 0, 0, 4, 1);
		this.ballTextureAtlas.load();

		this.emptyPlaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.emptyPlaceTextureAtlas, this, "blueemptyplace.png", 0, 0);
		this.intervalTextureRegionV1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.intervalTextureAtlas, this, "pVertical.png", 0, 0);
		this.intervalTextureRegionV2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.intervalTextureAtlas, this, "pVertical.png", 0, 0);
		this.starTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.starTextureAtlas, this, "star.png", 0, 0);
		this.intervalTextureRegionH1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.intervalTextureAtlas, this, "pHonrizontal.png", 0, 0);
		this.intervalTextureRegionH2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.intervalTextureAtlas, this, "pHonrizontal.png", 0, 0);
		this.emptyPlaceTextureAtlas.load();
		this.intervalTextureAtlas.load();
		this.starTextureAtlas.load();
		this.grassBackground = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.getTextureManager(), AssetBitmapTextureAtlasSource.create(this.getAssets(), "gfx/bluetable.png"), this.getVertexBufferObjectManager()); 

		/* Load the font we are going to use. */
		FontFactory.setAssetBasePath("font/");
		this.fontScore = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getAssets(), "Droid.ttf", 16, true, Color.BLUE);
		this.fontRemain = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getAssets(), "Droid.ttf", 12, true, Color.BLUE);
		this.fontScore.load();
		this.fontRemain.load();
		
		this.menuTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
		this.menuResetTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.menuTexture, this, "menu_reset.png", 0, 0);
		this.menuQuitTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.menuTexture, this, "menu_quit.png", 0, 50);
		this.menuTexture.load();

	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		mCurrentLevel = 1;
		
		this.scene = new Scene();
		for(int i = 0; i < LAYER_COUNT; i++) {
			this.scene.attachChild(new Entity());
		}
		this.scene.setBackground(this.grassBackground);
		this.scene.setOnAreaTouchListener(this);
		this.menuScene = this.createMenuScene();

		this.physicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		this.scene.registerUpdateHandler(this.physicsWorld);

		final LevelLoader levelLoader = new LevelLoader();
		loadLevel(levelLoader);
		
		removeRandomFace(faces);


		
		scene.setTouchAreaBindingOnActionMoveEnabled(true);
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		
		scene.setOnSceneTouchListener(this);

		
		remainsAndScoreHandler();


		return this.scene;
		//isLevelSelecting = true;
    	//return CreateLevelBoxes();

	}

	private void removeRandomFace(ArrayList<Face> faces) {
		int numberFaces = faces.size();
		int posicao = (int)(Math.random() * numberFaces);
		removeFace((Face) faces.get(posicao));
		remains--;
	}

	private void remainsAndScoreHandler(){

		/**/
		remainsText = new Text(5,  50, this.fontRemain, 
				"Remains: " + remains, "Remains: XX".length(),  this.getVertexBufferObjectManager());
		remainsText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		remainsText.setAlpha(1.0f);
		this.scene.getChildByIndex(LAYER_SCORE).attachChild(remainsText);

		/* The ScoreText showing how many points the pEntity scored. */
		this.scoreText = new Text(5, 5, this.fontScore, "Time: 0", "Time: XXXX".length(), this.getVertexBufferObjectManager());
		this.scoreText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.scoreText.setAlpha(1.0f);
		this.scene.getChildByIndex(LAYER_SCORE).attachChild(this.scoreText);
		
		this.scene.registerUpdateHandler(new TimerHandler(1.0f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				remainsText.setText("Remains: " + remains);
        		if ( hasGame() ){
        			//Debug.d("continue\n");
        			timePassed = timePassed + 1;
        			scoreText.setText("Time: " + timePassed);
        			//Log.d("Remains", "remains: " + remains);
        			if (remains == 2)
        				onWin();
        		}
        		else
        			onGameOver();
			}
		}));		
		
	}
	
	private void loadLevel(LevelLoader levelLoader) {

		levelLoader.setAssetBasePath("levels/");

		levelLoader.registerEntityLoader(LevelConstants.TAG_LEVEL, new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
				SolitaireActivity2.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(SolitaireActivity2.this, "Loaded level with width=" + width + " and height=" + height + ".", Toast.LENGTH_LONG).show();
					}
				});

				return SolitaireActivity2.this.scene;
			}
		});

		levelLoader.registerEntityLoader(TAG_ENTITY, new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_HEIGHT);
				final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);

				final VertexBufferObjectManager vertexBufferObjectManager = SolitaireActivity2.this.getVertexBufferObjectManager();

				final Body bodyFace;
				final Body bodyPlace;
				if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BALL)) {
					place = new Place(x, y, width, height, SolitaireActivity2.this.emptyPlaceTextureRegion, vertexBufferObjectManager) {
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) {
							//if (this.collidesWith());
							super.onManagedUpdate(pSecondsElapsed);
						}

					};
					place.setOcupado(true);
					bodyFace = PhysicsFactory.createCircleBody(SolitaireActivity2.this.physicsWorld, place, BodyType.StaticBody, SolitaireActivity2.PLACE_FIXTURE_DEF);
					face = new Face(x, y, width, height, SolitaireActivity2.this.ballTextureRegion, vertexBufferObjectManager);
					bodyPlace = PhysicsFactory.createCircleBody(SolitaireActivity2.this.physicsWorld, face, BodyType.DynamicBody, SolitaireActivity2.BALL_FIXTURE_DEF);
					SolitaireActivity2.remains++;
				} else {
					throw new IllegalArgumentException();
				}

				face.setPlace(place);
				face.animate(100, true);
				
				SolitaireActivity2.this.scene.registerTouchArea(face);
				SolitaireActivity2.this.scene.getChildByIndex(LAYER_PLACE).attachChild(place);
				SolitaireActivity2.this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(place, bodyPlace, true, true));
				SolitaireActivity2.this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(face, bodyFace, true, true));
				places.add(place);
				faces.add(face);
				return face;
			}
		});


		try {
			levelLoader.loadLevelFromAsset(this.getAssets(), "level001.lvl");
		} catch (final IOException e) {
			Debug.e(e);
		}
		
	}

	protected void onGameOver() {
		// TODO Auto-generated method stub
		this.gameOverText = new Text(0, 0, this.fontScore, "Game\nOver", new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
		this.gameOverText.setPosition((CAMERA_WIDTH - this.gameOverText.getWidth()) * 0.5f, (CAMERA_HEIGHT - this.gameOverText.getHeight()) * 0.5f - 50);
		this.gameOverText.registerEntityModifier(new ScaleModifier(3, 0.1f, 2.0f));
		this.gameOverText.registerEntityModifier(new RotationModifier(3, 0, 720));

		Rectangle recBlack = new Rectangle((CAMERA_WIDTH - this.gameOverText.getWidth()) * 0.5f - 50, 
				(CAMERA_HEIGHT - this.gameOverText.getHeight()) * 0.5f - 25,
				150.0f, 100.0f, this.getVertexBufferObjectManager());
		recBlack.setColor(0.0f, 0.0f, 0.0f);
		recBlack.registerEntityModifier(new ScaleModifier(3, 0.1f, 2.0f));
		//recBlack.registerEntityModifier(new RotationModifier(3, 0, 720));
		Sprite starSprite = new Sprite(recBlack.getX() + 5, recBlack.getY() + 5, starTextureRegion, this.getVertexBufferObjectManager());
		
		
		recBlack.attachChild(starSprite);
		this.scene.attachChild(recBlack);
		this.scene.attachChild(this.gameOverText);
		
		//this.mGameRunning = false;
		
	}

	protected void onWin() {
		// TODO Auto-generated method stub
		this.gameOverText = new Text(0, 0, this.fontScore, "Congratulations\nYou Win!!!", new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
		this.gameOverText.setPosition((CAMERA_WIDTH - this.gameOverText.getWidth()) * 0.5f, (CAMERA_HEIGHT - this.gameOverText.getHeight()) * 0.5f);
		this.gameOverText.registerEntityModifier(new ScaleModifier(3, 0.1f, 2.0f));
		this.gameOverText.registerEntityModifier(new RotationModifier(3, 0, 720));

		this.scene.attachChild(this.gameOverText);
		
	}

	protected boolean hasGame() {
		for (Place place : places){
			if (!place.isOcupado())
				continue;
			float distanciaX = place.getWidth();
			float distanciaY = place.getHeight();
			Place cima = getPlace(place.getX(), place.getY() - 2*distanciaY);
			Place baixo = getPlace(place.getX(), place.getY() + 2*distanciaY);
			Place esquerda = getPlace(place.getX() - 2*distanciaX, place.getY());
			Place direita = getPlace(place.getX() + 2*distanciaX, place.getY());
			
			if (cima != null) {
				Place cimaCima = getPlace(cima.getX(), cima.getY() - 2*distanciaY);
				if (cimaCima != null)
					if (cima.isOcupado() && !cimaCima.isOcupado())
						return true;
			}
			
			if (baixo != null) {
				Place baixoBaixo = getPlace(baixo.getX(), baixo.getY() + 2*distanciaY);
				if (baixoBaixo != null)
					if (baixo.isOcupado() && !baixoBaixo.isOcupado())
						return true;
			}
			
			if (esquerda != null) {
				Place esquerdaEsquerda = getPlace(esquerda.getX() - 2*distanciaX, esquerda.getY());
				if (esquerdaEsquerda != null)
					if (esquerda.isOcupado() && !esquerdaEsquerda.isOcupado())
						return true;
			}
			
			if (direita != null) {
				Place direitaDireita = getPlace(direita.getX() + 2*distanciaX, direita.getY());
				if (direitaDireita != null)
					if (direita.isOcupado() && !direitaDireita.isOcupado())
						return true;
			}
			
		}
		return false;
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		// TODO Auto-generated method stub
		return false;
	}

	

	protected MenuScene createMenuScene() {
		
		this.menuScene = new MenuScene(this.camera);

		final SpriteMenuItem resetMenuItem = new SpriteMenuItem(MENU_RESET, this.menuResetTextureRegion, this.getVertexBufferObjectManager());
		resetMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.menuScene.addMenuItem(resetMenuItem);

		final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, this.menuQuitTextureRegion, this.getVertexBufferObjectManager());
		quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.menuScene.addMenuItem(quitMenuItem);

		this.menuScene.buildAnimations();

		this.menuScene.setBackgroundEnabled(false);

		this.menuScene.setOnMenuItemClickListener(this);
		
		return menuScene;
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		Log.d("SOLITAIRE", "Cena pressionada");
		return false;
	}
	
	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			ITouchArea pTouchArea, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		
		if(pSceneTouchEvent.isActionDown()) {
			Log.d("SOLITAIRE", "Entidade pressionada");
			Face face = (Face)pTouchArea;
			//xOrigin = face.getX();
			//yOrigin = face.getY();
			Log.d("MEDIDAS FACE", " " + face.getX() + "," + face.getY());
			face.setScale(1.5f);
			//this.removeFace((Face)pTouchArea);
			return true;
		}
		
		else if (pSceneTouchEvent.isActionMove()){
			Log.d("SOLITAIRE", "Movendo");
			Face face = (Face)pTouchArea;			
			final PhysicsConnector facePhysicsConnector = this.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(face);
			Body body = facePhysicsConnector.getBody();
			body.setTransform((pSceneTouchEvent.getX() - face.getWidth() / 2) / 32, (pSceneTouchEvent.getY() - face.getHeight() / 2) / 32, 0);
			return true;
		}
		
		else if(pSceneTouchEvent.isActionUp()){
			Face face = (Face)pTouchArea;
			face.setScale(1.0f);
			for (Place place : places) {
				Face meio = getMeioSeJogadaPossivel(face, place);
				if (face.collidesWith(place) && meio != null) {
					Log.d("Solitaire", "Colidiu!");
					removeFace(meio);
					getPlace(face.getxOrigin(), face.getyOrigin()).setOcupado(false);
					place.setOcupado(true);
					face.setPlace(place);
					face.setxOrigin(place.getX());
					face.setyOrigin(place.getY());
					face.setX(place.getX());
					face.setY(place.getY());
					final PhysicsConnector facePhysicsConnector = this.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(face);
					Body body = facePhysicsConnector.getBody();
					body.setTransform((face.getxOrigin() + face.getWidth() / 2) / 32, (face.getyOrigin() + face.getHeight() / 2) / 32, 0);
					return true;
				}
			}
			
			//if (face.collidesWith((IShape) pTouchArea)){
			final PhysicsConnector facePhysicsConnector = this.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(face);
			Body body = facePhysicsConnector.getBody();
			body.setTransform((face.getxOrigin() + face.getWidth() / 2) / 32, (face.getyOrigin() + face.getHeight() / 2) / 32, 0);
			Log.d("FACE: ORIGINAIS", " " + face.getxOrigin() + "," + face.getyOrigin());
			//}
			
			return true;
		}
		
		return false;

	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}
	
	

	private void removeFace(final Face face) {
		final PhysicsConnector facePhysicsConnector = this.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(face);
		
		this.physicsWorld.unregisterPhysicsConnector(facePhysicsConnector);
		this.physicsWorld.destroyBody(facePhysicsConnector.getBody());

		this.scene.unregisterTouchArea(face);
		this.scene.detachChild(face);
		remains--;
		face.getPlace().setOcupado(false);
		face.setPlace(null);
		faces.remove(face);
		
		System.gc();
	}

	private void moveFace(final Face face, float x, float y){
	//	face.registerEntityModifier(pEntityModifier);
	}
	
	public static Face getFace(float x, float y) {
		for (Face face : faces)
			if (face.getxOrigin() == x && face.getyOrigin() == y)
				return face;
		return null;
	}
	
	public static Place getPlace(float x, float y) {
		for (Place place : places)
			if (place.getX() == x && place.getY() == y)
				return place;
		return null;
	}
	
	/*
	 * método que pega meio, mas verifica se a jogada é possível
	 */
	public Face getMeioSeJogadaPossivel(Face face, Place place) {
		Face meio;
		float distanciaX = face.getWidth();
		float distanciaY = face.getHeight();
		if (!place.isOcupado()){
			if (face.getxOrigin() == place.getX()){
				if ( (face.getyOrigin() + 4*distanciaY) == place.getY() ) {
					meio = getFace(face.getxOrigin(), face.getyOrigin() + 2*distanciaY);
					if (meio != null)
						return meio;
				}
				if ( (face.getyOrigin() - 4*distanciaY) == place.getY() ) {
					meio = getFace(face.getxOrigin(), face.getyOrigin() - 2*distanciaY);
					if (meio != null)
						return meio;
				}
			}
			else if (face.getyOrigin() == place.getY()){
				if ( (face.getxOrigin() + 4*distanciaX) == place.getX() ) {
					meio = getFace(face.getxOrigin() + 2*distanciaX, face.getyOrigin());
					if (meio != null)
						return meio;
				}
				if ( (face.getxOrigin() - 4*distanciaX) == place.getX() ) {
					meio = getFace(face.getxOrigin() - 2*distanciaX, face.getyOrigin());					
					if (meio != null)
						return meio;
				}		
			}
		}	
		return null;
	}
	
	private Scene CreateLevelBoxes() {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(SolitaireActivity2.this, "Select a level to start!", Toast.LENGTH_SHORT).show();
			}
		});
		
		// Level Selector
		this.mLevelSelectorTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 64);
		this.mLevelSelectRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mLevelSelectorTextureAtlas, this, "levelcircle.png", 0, 0);
		
		//load font texture
		FontFactory.setAssetBasePath("font/");
		this.mFont = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getAssets(), "tahoma.ttf", 48, true, Color.WHITE);
		this.mFont.load();

		
		this.mLevelSelectScene = new Scene();
		this.mLevelSelectScene.setBackground(new Background(0.2f, 0.2f, 0.5f));

		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mClickDetector = new ClickDetector(this);

		this.mLevelSelectScene.setOnSceneTouchListener(this);
		this.mLevelSelectScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.mLevelSelectScene.setOnSceneTouchListenerBindingOnActionDownEnabled(true);

		// calculate the amount of required columns for the level count
		int totalRows = (LEVELS / LEVEL_COLUMNS_PER_SCREEN) + 1;

		// Calculate space between each level square
		float spaceBetweenRows = (CAMERA_HEIGHT / LEVEL_ROWS_PER_SCREEN) - LEVEL_PADDING;
		float spaceBetweenColumns = (CAMERA_WIDTH / LEVEL_COLUMNS_PER_SCREEN) - LEVEL_PADDING;

		//Set the wood Background
		for (int x = 0; x < CAMERA_WIDTH; x += 128) {
			for (int y = 0; y < (totalRows*150); y += 128) {
				//Sprite mBackground = new Sprite(x, y, 128, 128, this.);
				//this.mLevelSelectScene.attachChild(this.grassBackground);
				this.mLevelSelectScene.setBackground(this.grassBackground);
			}
		}
		
 		// Current Level Counter
		int iLevel = 1;

		// Create the Level selectors, one row at a time.
		float boxX = LEVEL_PADDING, boxY = LEVEL_PADDING;
		for (int y = 0; y < totalRows; y++) {
			for (int x = 0; x < LEVEL_COLUMNS_PER_SCREEN; x++) {

				// On Touch, save the clicked level in case it's a click and not
				// a scroll.
				final int levelToLoad = iLevel;

				// Create the rectangle. If the level selected
				// has not been unlocked yet, don't allow loading.
				Sprite box = new Sprite(boxX, boxY, mLevelSelectRegion, this.getVertexBufferObjectManager()) {

					@Override
					public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
						if (levelToLoad >= mMaxLevelReached)
							iLevelClicked = -1;
						else
							iLevelClicked = levelToLoad;
						return false;
					}
				};
				
				box.setScale(1.5f);
 
				this.mLevelSelectScene.attachChild(box);

				// Center for different font size
				if (iLevel < 10) {
					this.mLevelSelectScene.attachChild(new Text(boxX + 17.0f, boxY + 3.0f, this.mFont, String.valueOf(iLevel), this.getVertexBufferObjectManager()));
				} else {
					this.mLevelSelectScene.attachChild(new Text(boxX + 4.0f, boxY + 3.0f, this.mFont, String.valueOf(iLevel), this.getVertexBufferObjectManager()));
				}

				this.mLevelSelectScene.registerTouchArea(box);

				iLevel++;
				boxX += spaceBetweenColumns + LEVEL_PADDING;

				if (iLevel > LEVELS)
					break;
			} 

			if (iLevel > LEVELS)
				break;

			boxY += spaceBetweenRows + LEVEL_PADDING;
			boxX = 50;
		}

		// Set the max scroll possible, so it does not go over the boundaries.
		mMaxY = boxY - CAMERA_HEIGHT + 200;


		
		return this.mLevelSelectScene;
	}

	@Override
	public void onClick(ClickDetector pClickDetector, int pPointerID,
			float pSceneX, float pSceneY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScroll(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}
	
		
}
