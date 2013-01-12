package br.com.lrferr.solitaire;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

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
import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.andengine.entity.shape.Shape;
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
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.SAXUtils;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.IEntityLoader;
import org.andengine.util.level.LevelLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.xml.sax.Attributes;

import android.opengl.GLES20;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;



public class SolitaireActivity extends BasePuzzle implements IOnSceneTouchListener, IOnMenuItemClickListener,  IAccelerationListener, IOnAreaTouchListener, IScrollDetectorListener, IClickDetectorListener {

	private Camera camera;
	
	private BitmapTextureAtlas faceTextureAtlas;
	private BitmapTextureAtlas placeTextureAtlas;
	private BitmapTextureAtlas intervalTextureAtlas;	
	private BitmapTextureAtlas starTextureAtlas;
	private BitmapTextureAtlas menuTexture;
	
	private TiledTextureRegion faceTextureRegion;
	private TextureRegion placeTextureRegion;

	private TextureRegion starTextureRegion;
	
	private RepeatingSpriteBackground tableBackground;

	private static ArrayList<Face> faces = new ArrayList<Face>();
	private ArrayList<Body> bodyFaceList = new ArrayList<Body>();

	private static ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Body> bodyPlaceList = new ArrayList<Body>();
	
	private Scene mainScene;
	
	private int timePassed = 0;
	
	public static int remains = 0;
	
	protected float mCurrentY = 0;
	
	
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private static final String TAG_ENTITY_ATTRIBUTE_WIDTH = "width_entity";
	private static final String TAG_ENTITY_ATTRIBUTE_HEIGHT = "height_entity";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FACE = "ball";
	private static final String TAG_ENTITY_ATTRIBUTE_INITEMPTY = "initEmpty";

	
	
	public Scene getScene() {
		return mainScene;
	}

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 320;
	private static final int LAYER_COUNT = 4;
	private  final int LAYER_BACKGROUND = 0;
	private  final int LAYER_PLACE = LAYER_BACKGROUND + 1;
	private	 final int LAYER_FACE = LAYER_PLACE + 1;
	private  final int LAYER_SCORE = LAYER_FACE + 1;
	
	protected static final int MENU_RESET = 0;
	protected static final int MENU_QUIT = MENU_RESET + 1;
	protected static final int MENU_OK = MENU_QUIT + 1;
	protected static final int MENU_NEXT_LEVEL = MENU_OK + 1;
	protected static final int MENU_SKIP = MENU_NEXT_LEVEL + 1;

	protected static final int LEVEL_COUNT = 10;
	protected static int LEVELS = LEVEL_COUNT;
	protected static int LEVEL_COLUMNS_PER_SCREEN = 4;
	protected static int LEVEL_ROWS_PER_SCREEN = 3;
	protected static float LEVEL_PADDING = 40.0f;
	
	protected static String[] COLOR_RANDOM_PLACE = {"red", "blue", "yellow", "green", "orange", "purple" };
	protected static String[] COLOR_RANDOM_FACE = {"red", "blue", "yellow", "green", "orange", "purple"};
	protected static String[] COLOR_RANDOM_TABLE = {"red", "blue", "yellow", "green", "orange", "purple", "sky" };

	
	private Text gameOverText;
	private Text scoreText;
	private static Text remainsText;
	private static Text levelText;
	
	private Font font;
	private Font fontScore;
	private Font fontRemain;
	private Font fontLevel;
	
	protected MenuScene menuScene;
	
	private PhysicsWorld physicsWorld;
	private ArrayList<PhysicsConnector> physConnectList = new ArrayList<PhysicsConnector>();

	public Face face;
	public Place place;
	private Scene levelSelectScene;
	private SurfaceScrollDetector scrollDetector;
	private ClickDetector clickDetector;
	private BitmapTextureAtlas levelSelectorTextureAtlas;
	private TextureRegion levelSelectRegion;
	
	protected float minY = 0;
	protected float maxY = 0;
	protected int iLevelClicked = -1;
	protected int maxLevelReached = 21;
	private boolean isLevelSelecting = false;
	private int currentLevel;
	private TimerHandler timeHandler;
	
	public static final short CATEGORYBIT_PLACE = 1;
	public static final short CATEGORYBIT_FACE = 2;
	public static final short MASKBITS_PLACE = CATEGORYBIT_PLACE;
	public static final short MASKBITS_FACE = MASKBITS_PLACE;
	public static final FixtureDef PLACE_FIXTURE_DEF = PhysicsFactory.createFixtureDef(0.0f, 0.5f, 0.5f, false, CATEGORYBIT_PLACE, MASKBITS_PLACE, (short)0);
	public static final FixtureDef FACE_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1.0f, 0.5f, 0.5f, false, CATEGORYBIT_FACE, MASKBITS_FACE, (short)0);
	
	


	@Override
	public EngineOptions onCreateEngineOptions() {
		Toast.makeText(this, "Touch & Drag the faces.\nThen drag and drop on the white hole!", Toast.LENGTH_LONG).show();
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

		this.faceTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 128, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.placeTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		this.intervalTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		this.starTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		this.faceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.faceTextureAtlas, this, "bluefacetiled.png", 0, 0, 4, 1);
		this.faceTextureAtlas.load();

		this.placeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.placeTextureAtlas, this, "" +
				"place.png", 0, 0);
		this.starTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.starTextureAtlas, this, "star.png", 0, 0);
		this.placeTextureAtlas.load();
		this.intervalTextureAtlas.load();
		this.starTextureAtlas.load();
		this.tableBackground = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.getTextureManager(), AssetBitmapTextureAtlasSource.create(this.getAssets(), "gfx/skytable.png"), this.getVertexBufferObjectManager()); 

		/* Load the font we are going to use. */
		FontFactory.setAssetBasePath("font/");
		this.fontScore = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getAssets(), "Droid.ttf", 20, true, android.graphics.Color.MAGENTA);
		this.fontRemain = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getAssets(), "Droid.ttf", 20, true, android.graphics.Color.GRAY);
		this.fontLevel = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getAssets(), "Droid.ttf", 20, true, android.graphics.Color.GREEN);
		this.fontScore.load();
		this.fontRemain.load();
		this.fontLevel.load();
		
		this.menuTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
		this.menuTexture.load();

	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		currentLevel = 1;
		
		this.mainScene = new Scene();
		for(int i = 0; i < LAYER_COUNT; i++) {
			this.mainScene.attachChild(new Entity());
		}
		this.mainScene.setBackground(this.tableBackground);
		this.mainScene.setOnAreaTouchListener(this);
		this.menuScene = this.createMenuScene();

		this.physicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		this.mainScene.registerUpdateHandler(this.physicsWorld);

		
		//loadLevel();
		
		//removeRandomFace(faces);


		
		mainScene.setTouchAreaBindingOnActionMoveEnabled(true);
		mainScene.setTouchAreaBindingOnActionDownEnabled(true);
		
		mainScene.setOnSceneTouchListener(this);

		
		//remainsAndScoreHandler();


		//return this.scene;
		isLevelSelecting = true;
    	return createLevelBoxes();

	}

	private void removeRandomFace(ArrayList<Face> faces) {
		if (faces != null && faces.size() > 0) {
			int numberFaces = faces.size();
			boolean ok = false;
			while(!ok){
				int posicao = (int)(Math.random() * numberFaces);
				Place p = places.get(posicao);
				if (p.isInitEmpty()){
					ok = true;
					removeFace((Face) faces.get(posicao));
				}
			}
		}
	}

	private void remainsAndScoreHandler(int level){


		timePassed = 0;
		
		Rectangle recBlack = new Rectangle(0.0f, 0.0f, 
				CAMERA_WIDTH, 32, this.getVertexBufferObjectManager());
		recBlack.setColor(0.0f, 0.0f, 0.0f);
		this.mainScene.getChildByIndex(LAYER_SCORE).attachChild(recBlack);

		
		/**/
		levelText = new Text(5, 5, this.fontLevel,
				"Level: " + level, "Level: XX".length(), this.getVertexBufferObjectManager());
		levelText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		levelText.setAlpha(1.0f);		
		this.mainScene.getChildByIndex(LAYER_SCORE).attachChild(levelText);
		
		
		remainsText = new Text(CAMERA_WIDTH - 150,  5, this.fontRemain, 
				"Remains: " + remains, "Remains: XX".length(),  this.getVertexBufferObjectManager());
		remainsText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		remainsText.setAlpha(1.0f);
		this.mainScene.getChildByIndex(LAYER_SCORE).attachChild(remainsText);

		/* The ScoreText showing how many points the pEntity scored. */
		this.scoreText = new Text(CAMERA_WIDTH - 300, 5, this.fontScore, "Time: 0", "Time: XXXX".length(), this.getVertexBufferObjectManager());
		this.scoreText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.scoreText.setAlpha(1.0f);
		this.mainScene.getChildByIndex(LAYER_SCORE).attachChild(this.scoreText);
		
		this.timeHandler = new TimerHandler(1.0f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				remainsText.setText("Remains: " + remains);
        		if ( hasGame() ){
        			//Debug.d("continue\n");
        			timePassed = timePassed + 1;
        			scoreText.setText("Time: " + timePassed);
        			//Log.d("Remains", "remains: " + remains);
        			if (remains == 1)
        				onWin();
        		}
        		else {
        			if (remains == 1)
        				onWin();  
        			else
        				onGameOver();
        		}
			}
		});		
		this.mainScene.registerUpdateHandler(timeHandler);
	}
	
	public void loadLevel(final int iLevel) {
		
		
		Debug.d("Level Clicado: ", String.valueOf(iLevelClicked));
		if (iLevel < 1) return;
		
		remains = 0;
		
		int posicao = (int)(Math.random() * COLOR_RANDOM_PLACE.length);

		this.faceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.faceTextureAtlas, this, COLOR_RANDOM_PLACE[posicao] + "facetiled.png", 0, 0, 4, 1);
		this.faceTextureAtlas.load();
		this.placeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.placeTextureAtlas, this, COLOR_RANDOM_PLACE[posicao] + "place.png", 0, 0);
		this.placeTextureAtlas.load();
		posicao = (int)(Math.random() * COLOR_RANDOM_TABLE.length);
		this.tableBackground = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.getTextureManager(), AssetBitmapTextureAtlasSource.create(this.getAssets(), "gfx/" + COLOR_RANDOM_TABLE[posicao] + "table.png"), this.getVertexBufferObjectManager()); 
		this.mainScene.setBackground(this.tableBackground);
		
		//this.mainScene.getChildByIndex(LAYER_SCORE).detachChild(remainsText);
		//this.mainScene.getChildByIndex(LAYER_SCORE).detachChild(scoreText);
		this.mainScene.getChildByIndex(LAYER_SCORE).detachChildren();
		this.mainScene.unregisterUpdateHandler(timeHandler);
		
		for (PhysicsConnector physConnector : this.physConnectList) {
			this.physicsWorld.unregisterPhysicsConnector(physConnector);
		}
		physConnectList.clear();

		for (Shape sprite : faces) {
			this.mainScene.unregisterTouchArea(sprite);
			this.mainScene.detachChild(sprite);
		}
		faces.clear();
		for (Shape sprite : places) {
			this.mainScene.unregisterTouchArea(sprite);
			this.mainScene.getChildByIndex(LAYER_PLACE).detachChild(sprite);
		}
		places.clear();
		
		for (Body body : bodyFaceList) {
			this.physicsWorld.destroyBody(body);
		}
		for (Body body : bodyPlaceList) {
			this.physicsWorld.destroyBody(body);
		}
		
		this.physicsWorld.clearForces();
		this.physicsWorld.clearPhysicsConnectors();
		bodyFaceList.clear();
		bodyPlaceList.clear();

		
		final LevelLoader levelLoader = new LevelLoader();
		levelLoader.setAssetBasePath("levels/");

		levelLoader.registerEntityLoader(LevelConstants.TAG_LEVEL, new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
				SolitaireActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//Toast.makeText(SolitaireActivity.this, "Loaded level with width=" + width + " and height=" + height + ".", Toast.LENGTH_LONG).show();
					}
				});

				return SolitaireActivity.this.mainScene;
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
				final Boolean initEmpty = Boolean.parseBoolean(SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_INITEMPTY));

				final VertexBufferObjectManager vertexBufferObjectManager = SolitaireActivity.this.getVertexBufferObjectManager();

				final Body bodyFace;
				final Body bodyPlace;
				if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FACE)) {
					place = new Place(x, y, width, height, SolitaireActivity.this.placeTextureRegion, vertexBufferObjectManager) {
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) {
							//if (this.collidesWith());
							super.onManagedUpdate(pSecondsElapsed);
						}

					};
					place.setOcupado(true);
					place.setInitEmpty(initEmpty);
					bodyFace = PhysicsFactory.createCircleBody(SolitaireActivity.this.physicsWorld, place, BodyType.StaticBody, SolitaireActivity.PLACE_FIXTURE_DEF);
					face = new Face(x, y, width, height, SolitaireActivity.this.faceTextureRegion, vertexBufferObjectManager);
					bodyPlace = PhysicsFactory.createCircleBody(SolitaireActivity.this.physicsWorld, face, BodyType.DynamicBody, SolitaireActivity.FACE_FIXTURE_DEF);
					SolitaireActivity.remains++;
				} else {
					throw new IllegalArgumentException();
				}

				face.setPlace(place);
				face.animate(100, true);
				
				SolitaireActivity.this.mainScene.registerTouchArea(face);
				SolitaireActivity.this.mainScene.getChildByIndex(LAYER_PLACE).attachChild(place);
				PhysicsConnector pc = new PhysicsConnector(place, bodyPlace, true, true);
				SolitaireActivity.this.physicsWorld.registerPhysicsConnector(pc);
				physConnectList.add(pc);
				pc = new PhysicsConnector(face, bodyFace, true, true);
				SolitaireActivity.this.physicsWorld.registerPhysicsConnector(pc);
				physConnectList.add(pc);
				places.add(place);
				faces.add(face);
				return face;
			}
		});


		try {
			if (iLevel > 0) {
				
			if (iLevel < 10)
				levelLoader.loadLevelFromAsset(this.getAssets(), "level00" + iLevel + ".xml");
			else if (iLevel < 100)
				levelLoader.loadLevelFromAsset(this.getAssets(), "level0" + iLevel + ".xml");
			}
		} catch (final IOException e) {
			Debug.e(e);
		}
		
		removeRandomFace(faces);
		remainsAndScoreHandler(iLevel);
		isLevelSelecting = false;
		
		
		camera.setCenter(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);
		this.mEngine.setScene(mainScene);
	}

	protected void onGameOver() {
		// TODO Auto-generated method stub
		this.gameOverText = new Text(0, 0, this.fontScore, "Game\nOver", new TextOptions(HorizontalAlign.LEFT), this.getVertexBufferObjectManager());
		//this.gameOverText.setPosition((CAMERA_WIDTH - this.gameOverText.getWidth()) * 0.5f - 25, (CAMERA_HEIGHT - this.gameOverText.getHeight()) * 0.5f - 50);
		this.gameOverText.setPosition(CAMERA_WIDTH/2, CAMERA_HEIGHT/2);
		this.gameOverText.registerEntityModifier(new ScaleModifier(3, 0.1f, 2.0f));
		//this.gameOverText.registerEntityModifier(new RotationModifier(3, 0, 720));
		/*
		Rectangle recBlack = new Rectangle((CAMERA_WIDTH - this.gameOverText.getWidth()) * 0.5f - 25, 
				(CAMERA_HEIGHT - this.gameOverText.getHeight()) * 0.5f - 25,
				180.0f, 100.0f, this.getVertexBufferObjectManager());
		recBlack.setColor(0.0f, 0.0f, 0.0f);
		recBlack.registerEntityModifier(new ScaleModifier(5, 0.1f, 2.0f));


		recBlack.attachChild(this.gameOverText);
		this.mainScene.getChildByIndex(LAYER_SCORE).attachChild(recBlack);
		*/
		this.mainScene.getChildByIndex(LAYER_SCORE).attachChild(this.gameOverText);
		//this.mGameRunning = false;
		
	}

	protected void onWin() {
		// TODO Auto-generated method stub
		this.gameOverText = new Text(0, 0, this.fontScore, "Congratulations\nYou Win!!!", new TextOptions(HorizontalAlign.LEFT), this.getVertexBufferObjectManager());
		//this.gameOverText.setPosition((CAMERA_WIDTH - this.gameOverText.getWidth()) * 0.5f -25, (CAMERA_HEIGHT - this.gameOverText.getHeight()) * 0.5f - 25);
		this.gameOverText.setPosition(CAMERA_WIDTH/2 - 50.0f, CAMERA_HEIGHT/2);
		this.gameOverText.registerEntityModifier(new ScaleModifier(3, 0.1f, 2.0f));
		//this.gameOverText.registerEntityModifier(new RotationModifier(3, 0, 720));

		/*
		Rectangle recBlack = new Rectangle((CAMERA_WIDTH - this.gameOverText.getWidth()) * 0.5f - 25, 
				(CAMERA_HEIGHT - this.gameOverText.getHeight()) * 0.5f - 25,
				180.0f, 100.0f, this.getVertexBufferObjectManager());
		recBlack.setColor(0.0f, 0.0f, 0.0f);
		recBlack.registerEntityModifier(new ScaleModifier(5, 0.1f, 2.0f));
		//recBlack.registerEntityModifier(new RotationModifier(3, 0, 720));
		Sprite starSprite = new Sprite(recBlack.getX(), recBlack.getY(), starTextureRegion, this.getVertexBufferObjectManager());
		
		
		recBlack.attachChild(starSprite);
		recBlack.attachChild(this.gameOverText);
		
		this.mainScene.getChildByIndex(LAYER_SCORE).attachChild(recBlack);
		 */
		
		this.mainScene.getChildByIndex(LAYER_SCORE).attachChild(this.gameOverText);
		Sprite starSprite = new Sprite(32, 32, starTextureRegion, this.getVertexBufferObjectManager());
		starSprite.registerEntityModifier(new ScaleModifier(5, 0.1f, 2.0f));
		starSprite.registerEntityModifier(new RotationModifier(3, 0, 720));
		this.mainScene.getChildByIndex(LAYER_SCORE).attachChild(starSprite);
		
		createNextLevelMenuScene();
		
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
		switch (pMenuItem.getID()) {

		case MENU_RESET:
			return true;
		case MENU_OK:

			this.menuScene.back();
			return true;

		case MENU_NEXT_LEVEL:
			if (currentLevel == LEVEL_COUNT) {
				currentLevel = 1;
			} else {
				currentLevel++;
			}
			loadLevel(currentLevel);
			//this.mEngine.setScene(mainScene);
			this.menuScene.back();
			return true;

		case MENU_SKIP:
			mEngine.setScene(levelSelectScene);
			isLevelSelecting = true;
			this.menuScene.back();
			return true;

		case MENU_QUIT:	
			this.finish();

		default:
			return false;
		}

	}

	

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		if (pScene == this.levelSelectScene) {
			Log.d("SOLITAIRE", "Cena Level pressionada");
			//this.mEngine.setScene(this.scene);
		}
		
		if (isLevelSelecting) {
			this.clickDetector.onTouchEvent(pSceneTouchEvent);
			this.scrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		return true;
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
			if (facePhysicsConnector != null){
				Body body = facePhysicsConnector.getBody();
				body.setTransform((pSceneTouchEvent.getX() - face.getWidth() / 2) / 32, (pSceneTouchEvent.getY() - face.getHeight() / 2) / 32, 0);				
			}
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
					if (facePhysicsConnector != null) {						
						Body body = facePhysicsConnector.getBody();
						body.setTransform((face.getxOrigin() + face.getWidth() / 2) / 32, (face.getyOrigin() + face.getHeight() / 2) / 32, 0);
					}
					return true;
				}
			}
			
			//if (face.collidesWith((IShape) pTouchArea)){
			final PhysicsConnector facePhysicsConnector = this.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(face);
			if (facePhysicsConnector != null) {
				Body body = facePhysicsConnector.getBody();
				body.setTransform((face.getxOrigin() + face.getWidth() / 2) / 32, (face.getyOrigin() + face.getHeight() / 2) / 32, 0);
			}
			
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

		this.mainScene.unregisterTouchArea(face);
		this.mainScene.detachChild(face);
		remains--;
		face.getPlace().setOcupado(false);
		face.setPlace(null);
		faces.remove(face);
		
		System.gc();
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
	
	
	// ===========================================================
	// Menus
	// ==========================================================

	protected MenuScene createRetryLevelMenuScene() {

		loadFontTexture();

		MenuScene menuScene = new MenuScene(this.camera);

		Rectangle rect = new Rectangle(20.0f, 20.0f, CAMERA_WIDTH - 40.0f, CAMERA_HEIGHT - 350.0f, this.getVertexBufferObjectManager());
		rect.setColor(0, 0, 0);
		rect.setAlpha(0.8f);

		final Text textCenter = new Text(180.0f, 20.0f, this.font, "Oops! Try again?", this.getVertexBufferObjectManager());

		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_QUIT, this.font, "QUIT", this.getVertexBufferObjectManager()), new Color(1,0,0), new Color(0,0,0));
		quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitMenuItem);

		final IMenuItem nextLevelMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_SKIP, this.font, "Level Select", this.getVertexBufferObjectManager()), new Color(1,0,0), new Color(0,0,0));
		nextLevelMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(nextLevelMenuItem);

		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);

		menuScene.attachChild(rect);
		menuScene.attachChild(textCenter);

		return menuScene;
	}

	protected MenuScene createNextLevelMenuScene() {
		loadFontTexture();

		MenuScene menuScene = new MenuScene(this.camera);

		Rectangle rect = new Rectangle(20.0f, 20.0f, CAMERA_WIDTH - 40.0f, CAMERA_HEIGHT - 350.0f, this.getVertexBufferObjectManager());
		rect.setColor(0, 0, 0);
		rect.setAlpha(0.5f);

		final Text textCenter = new Text(200.0f, 20.0f, this.font, "Congratulations\n You made it!", this.getVertexBufferObjectManager());

		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_NEXT_LEVEL, font, "Next Level", this.getVertexBufferObjectManager()), new Color(1,0,0), new Color(1,1,1));
		menuScene.addMenuItem(quitMenuItem);

		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);

		menuScene.attachChild(rect);
		menuScene.attachChild(textCenter);

		return menuScene;
	}
	
/*	
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
*/

	protected MenuScene createMenuScene() {
		loadFontTexture();

		MenuScene menuScene = new MenuScene(this.camera);

		Rectangle rect = new Rectangle(20.0f, 20.0f, CAMERA_WIDTH - 40.0f, CAMERA_HEIGHT - 350.0f, this.getVertexBufferObjectManager());
		rect.setColor(0, 0, 0);
		rect.setAlpha(0.5f);

		//final Text textCenter = new Text(140, 15, this.font, "SOLITAIRE", this.getVertexBufferObjectManager());

		final IMenuItem okMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_OK, this.font, "Continue", this.getVertexBufferObjectManager()), new Color(1,0,0), new Color(1,1,1));
		okMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(okMenuItem);

		final IMenuItem selectLevelMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_SKIP, this.font, "Level Select", this.getVertexBufferObjectManager()), new Color(1,0,0), new Color(1,1,1));
		selectLevelMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(selectLevelMenuItem);
		
		final IMenuItem nextLevelMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_NEXT_LEVEL, this.font, "Next Level", this.getVertexBufferObjectManager()), new Color(1,0,0), new Color(1,1,1));
		nextLevelMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(nextLevelMenuItem);

		final IMenuItem quitLevelMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_QUIT, this.font, "Quit", this.getVertexBufferObjectManager()), new Color(1,0,0), new Color(1,1,1));
		quitLevelMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitLevelMenuItem);
		
		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);

		menuScene.attachChild(rect);
		//menuScene.attachChild(textCenter);

		return menuScene;
	}

	private Scene createLevelBoxes() {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(SolitaireActivity.this, "Select a level to start!", Toast.LENGTH_SHORT).show();
			}
		});
		
		loadFontTexture();
		/*
		this.levelSelectScene = new Scene(){
			@SuppressWarnings("unused")
			public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
				if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
					SolitaireActivity.this.finish();
				}
				return true;
			}
				
		};*/
		this.levelSelectScene = new Scene();
		
		this.levelSelectScene.setBackground(new Background(0.2f, 0.2f, 0.5f));

		this.scrollDetector = new SurfaceScrollDetector(this);
		this.clickDetector = new ClickDetector(this);

		this.levelSelectScene.setOnSceneTouchListener(this);
		this.levelSelectScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.levelSelectScene.setOnSceneTouchListenerBindingOnActionDownEnabled(true);

		// calculate the amount of required columns for the level count
		int totalRows = (LEVELS / LEVEL_COLUMNS_PER_SCREEN) + 1;

		// Calculate space between each level square
		float spaceBetweenRows = (CAMERA_HEIGHT / LEVEL_ROWS_PER_SCREEN) - LEVEL_PADDING;
		float spaceBetweenColumns = (CAMERA_WIDTH / LEVEL_COLUMNS_PER_SCREEN) - LEVEL_PADDING;

		//Set the wood Background
		for (int x = 0; x < CAMERA_WIDTH; x += 128) {
			for (int y = 0; y < (totalRows*150); y += 128) {
				//Sprite mBackground = new Sprite(x, y, 128, 128, this.);
				//this.levelSelectScene.attachChild(this.tableBackground);
				this.levelSelectScene.setBackground(this.tableBackground);
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
				Sprite box = new Sprite(boxX, boxY, levelSelectRegion, this.getVertexBufferObjectManager()) {

					@Override
					public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
						//SolitaireActivity.this.mEngine.setScene(SolitaireActivity.this.scene);
						if (levelToLoad >= maxLevelReached)
							iLevelClicked = -1;
						else {
							iLevelClicked = levelToLoad;
							loadLevel(iLevelClicked);
						}	
						return false;
					}
				};
				
				box.setScale(1.5f);
 
				this.levelSelectScene.attachChild(box);

				// Center for different font size
				if (iLevel < 10) {
					this.levelSelectScene.attachChild(new Text(boxX + 17.0f, boxY + 3.0f, this.font, String.valueOf(iLevel), this.getVertexBufferObjectManager()));
				} else {
					this.levelSelectScene.attachChild(new Text(boxX + 4.0f, boxY + 3.0f, this.font, String.valueOf(iLevel), this.getVertexBufferObjectManager()));
				}

				this.levelSelectScene.registerTouchArea(box);

				iLevel++;
				boxX += spaceBetweenColumns + LEVEL_PADDING;

				if (iLevel > LEVELS)
					break;
			} 

			if (iLevel > LEVELS)
				break;

			boxY += spaceBetweenRows + LEVEL_PADDING;
			boxX = LEVEL_PADDING;
		}

		// Set the max scroll possible, so it does not go over the boundaries.
		maxY = boxY - CAMERA_HEIGHT + 200;


		
		return this.levelSelectScene;
	}

	private void loadFontTexture() {
		// TODO Auto-generated method stub
		// Level Selector
		this.levelSelectorTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 64);
		this.levelSelectRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.levelSelectorTextureAtlas, this, "levelcircle.png", 0, 0);
		this.levelSelectorTextureAtlas.load();
		
		//load font texture
		FontFactory.setAssetBasePath("font/");
		this.font = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getAssets(), "tahoma.ttf", 48, true, android.graphics.Color.WHITE);
		this.font.load();

	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if (this.mEngine.getScene() == this.levelSelectScene)
				this.finish();
		}

		if((pKeyCode == KeyEvent.KEYCODE_MENU || pKeyCode == KeyEvent.KEYCODE_BACK) && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if(this.mainScene.hasChildScene()) {
				/* Remove the menu and reset it. */
				this.menuScene.back();
			} else {
				/* Attach the menu. */
				this.mainScene.setChildScene(this.menuScene, false, true, true);
			}
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	@Override
	public void onClick(ClickDetector pClickDetector, int pPointerID,
			float pSceneX, float pSceneY) {
		// TODO Auto-generated method stub
		loadLevel(iLevelClicked);
		
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
		if ( ((mCurrentY - pDistanceY) < minY) || ((mCurrentY - pDistanceY) > maxY) )
			return;

		this.camera.offsetCenter(0, -pDistanceY);

		mCurrentY -= pDistanceY;
		
	}

	@Override
	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}
	

		
}
