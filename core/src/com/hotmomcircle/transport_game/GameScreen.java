package com.hotmomcircle.transport_game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hotmomcircle.transport_game.entity.Gem;
import com.hotmomcircle.transport_game.entity.Player;
import com.hotmomcircle.transport_game.object.Bicycle_OBJ;
import com.hotmomcircle.transport_game.object.Car_OBJ;
import com.hotmomcircle.transport_game.object.Transport_OBJ;
import com.hotmomcircle.transport_game.entity.Route;
import com.hotmomcircle.transport_game.tools.Camera;
import com.hotmomcircle.transport_game.entity.Node;
import com.hotmomcircle.transport_game.ui.Planning;
import com.hotmomcircle.transport_game.ui.Points;
import com.hotmomcircle.transport_game.ui.gemArrow;
import com.hotmomcircle.transport_game.ui.Pause;
//map imports below 
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
//

// Screen of the level the player is currently playing
// Separation of game and level to allow 
public class GameScreen implements Screen, Json.Serializable {

	TransportGame game;
	ParentGame parentGame;

	SpriteBatch batch;

	private int originalTileSize = 16;
	public int scale = 2;
	private int tileSize = originalTileSize * scale;
	
	//initializing map 
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;

	Texture img;
	public Player player;
	public ArrayList<Transport_OBJ> transport_OBJs = new ArrayList<Transport_OBJ>();
	   
	public Camera camera;
	
	public Array<Gem> gems;

	// list of Nodes for interaction
	public Array<Node> nodes;
	// list of Routes for planning UI
	public Array<Route> routes;
	   
   // Variables associated with the pause / game state
	private int GAME_STATE;
	private final int GAME_RUNNING = 0;
	private final int GAME_PAUSED = 1;
	public Skin skin;
	public BitmapFont font;

	public Pause pauseUI;
	public Stage pauseStage;

	//UI Skin

	// Stage for UI components
	private Stage stage;
	private Table table;

	// Planning UI
	public Planning planningUI;

	// scores need to be public so Player can modify
	public Points points;
	public Points carbon;
	public Points freshness;

	public AssetManager assetManager;

	//gemArrow instance 
	private gemArrow gemArrowUI;

// New level
	public GameScreen(TransportGame game, ParentGame parentGame) {
		this.game = game;
		this.parentGame = parentGame;
		
		loadAssets();
		player = new Player(this, 700, 300, 32, 32, "./foot/player_down1.png");
		
		gems = new Array<Gem>();
		gems.add(new Gem(this, 400, 400, 16, 16));
		gems.add(new Gem(this, 200, 200, 16, 16));
		gems.add(new Gem(this, 300, 300, 16, 16));

		initializeGame();
	}
	
//	Load level from json
	public GameScreen(TransportGame game, ParentGame parentGame, JsonValue jsonMap) {
		this.game = game;
		this.parentGame = parentGame;
		loadAssets();
//		Read in the serializable data
		read(null, jsonMap);
		
//		For now write the gems in manually, these will be serialized too
		gems = new Array<Gem>();
		gems.add(new Gem(this, 400, 400, 16, 16));
		gems.add(new Gem(this, 200, 200, 16, 16));
		gems.add(new Gem(this, 300, 300, 16, 16));
		initializeGame();
		
		
	}
	
//		Load assets - Load all textures, maps, etc here with the assetManager before going to the game screen.
//		Separated from initialize game as assets need to be loaded before player is loaded, player needs to be loaded before rest of game is initialized
	public void loadAssets() {
//		In the mean time show a loading screen
		assetManager = parentGame.assetManager;

		//loading map 
		assetManager.setLoader(TiledMap.class,  new TmxMapLoader());
		assetManager.load("bigMap.tmx", TiledMap.class);
		
//		Load in the player transport
		String[] transportPaths = {
			    "./foot/player_up1.png", "./foot/player_up2.png",
			    "./foot/player_down1.png", "./foot/player_down2.png",
			    "./foot/player_left1.png", "./foot/player_left2.png",
			    "./foot/player_right1.png", "./foot/player_right2.png",
			    "./bicycle/bike_up1.png", "./bicycle/bike_up2.png",
			    "./bicycle/bike_down1.png", "./bicycle/bike_down2.png",
			    "./bicycle/bike_left1.png", "./bicycle/bike_left2.png",
			    "./bicycle/bike_right1.png", "./bicycle/bike_right2.png",
			    "./car/car_up.png", "./car/car_up.png",
			    "./car/car_down.png", "./car/car_down.png",
			    "./car/car_left.png", "./car/car_left.png",
			    "./car/car_right.png", "./car/car_right.png"
			};
		
		for(String path: transportPaths) {
			assetManager.load(path, Texture.class);
			
		}
		
//		Load in the objects (gem, bike_OBJ, car_OBJ
		String[] objectPaths = {
				"gem.png",
				"./objects/bicycle.png",
				"./objects/car_left.png"
		};
		
		for(String path: objectPaths) {
			assetManager.load(path, Texture.class);
			
		}
		
		assetManager.finishLoading();
	}
	
//	Initializes the game. Put into separate function to allow multiple constructors to call it
	public void initializeGame() {
		this.font = game.font;
		this.skin = game.skin;

		this.batch = game.batch;
		
		// for the pause / play feature
		GAME_STATE = GAME_RUNNING;
		
		try {
			map = assetManager.get("bigMap.tmx", TiledMap.class);
			System.out.println("Map loaded successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// routes for node testing
		routes = new Array<Route>();
		for (int i = 1; i < 4; i++) {
			routes.add(new Route(this, 0, 0, 32, 32, "gem.png", 900, i * 100 + 100));
		}

		// initialise Node array
		nodes = new Array<Node>();

		for (MapLayer layer : map.getLayers()) {
            // Check if the layer contains objects
			// AND create Node(s) for the object layer
            if (layer.getObjects() != null && layer.getName().equals("rec_layer")) {
                // Retrieve objects from the layer
                for (MapObject object : layer.getObjects()) {
					// get X and Y for each object
                    float locX = object.getProperties().get("x", Float.class);
                    float locY = object.getProperties().get("y", Float.class);
					// pass to Node constructor
					nodes.add(new Node(this, locX, locY, 16, 16, "gem.png", routes));
                }
            }
		}


		renderer = new OrthogonalTiledMapRenderer(map,3);
		//

		
		
		transport_OBJs.add(new Bicycle_OBJ(this, 300, 100, true));
		transport_OBJs.add(new Bicycle_OBJ(this, 400, 100, true));
		transport_OBJs.add(new Bicycle_OBJ(this, 500, 100, true));
		
		transport_OBJs.add(new Car_OBJ(this, 400, 150, true));
		
		// create the camera and the SpriteBatch
		camera = new Camera(game, player);

		// Stage is the layer on which we draw the UI
		// Likely keeps things cleaner as we make
		// the map more complicated and add objects
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		// Asset manager instansiation
		assetManager.load("uiskin.json", Skin.class);


		// table to hold UI elements
		table = new Table();
		table.setFillParent(true);
		table.defaults().width(game.SCREEN_WIDTH / 6).expandX().fillX();
		table.setWidth(game.SCREEN_WIDTH / 6);
		table.left().top();

		// UI scores
		points = new Points("0", skin);
		carbon = new Points("0", skin);
		freshness = new Points("100", skin);
		
		gemArrowUI = new gemArrow(skin, player, gems, table); 

		table.add(gemArrowUI).top().left();

		// fill table with UI scores
		table.add(new Label("Points: ", skin));
		table.add(points).fillX().uniformX();
		table.add(new Label("Carbon: ", skin));
		table.add(carbon).fillX().uniformX();
		table.add(new Label("Freshness: ", skin));
		table.add(freshness).fillX().uniformX();

		//initalise gemArrow 

		// add table to stage
		stage.addActor(table);

		// Planning UI
		planningUI = new Planning(game, this, stage, skin, player);
		

		// Pause UI
		pauseStage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(pauseStage);

		pauseUI = new Pause(game, this, pauseStage, skin);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta) {

		// pauses the game if it isnt already paused - prevents multiple inputs
		if(Gdx.input.isKeyPressed(Input.Keys.P) && GAME_STATE != GAME_PAUSED) {
			pause();
			pauseUI.showPause();
		} 
		// resumes game if it isn't already running
		if(Gdx.input.isKeyPressed(Input.Keys.R) && GAME_STATE != GAME_RUNNING) {
			resume();
		} 
		if (GAME_STATE == GAME_PAUSED){
			pauseStage.act(delta);
			pauseStage.draw();

			

		} else {

		
			ScreenUtils.clear(0, 0, 0.2f, 1);
			
			// map render 
			renderer.setView(camera);
			camera.setPosition();
			// camera.position.set(player.getX(),player.getY(), 0);

			renderer.render();
			//

			for (Gem gem : gems) {
				if (player.getRectangle().overlaps(gem.getRectangle())) {
					gems.removeValue(gem, true);
				points.setText("50");
				}
			}

		
		
		for(int i = 0; i < transport_OBJs.size(); i++) {
				transport_OBJs.get(i).update(i);
		}

		// tell the camera to update its matrices.
		camera.update();



		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		try {
			for (Transport_OBJ transport: transport_OBJs) {
				if (transport != null) {					
					transport.render(batch);
				}
			}
			
			for (Gem gem : gems) {
				gem.render(batch);
			}
			
			for (Node node: nodes) {
				node.render(batch);
			}
			
//			Render the player last so they appear on top of everything
			
			if (!planningUI.active) {
				player.render(batch);
			}
			
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		batch.end();

		// UI draw
		stage.act(delta);
		stage.draw();

	}
		 // Update the gemArrow UI with the current player and gem positions
		gemArrowUI.update(player, gems);
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		System.out.println("Game Paused");
		GAME_STATE = GAME_PAUSED;
		
	}

	@Override
	public void resume() {
		GAME_STATE = GAME_RUNNING;
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

		// TODO check if right place for this disposal
		map.dispose();
		renderer.dispose();
		stage.dispose();
		assetManager.dispose(); // This will have to be removed from gamescreen when we have multiple levels and put into ParentGame

	}

	public int getTileSize() {
		return tileSize;
	}
	
	public void addBike(int x, int y) {
		transport_OBJs.add(new Bicycle_OBJ(this, x, y, true));
	}
	
	public void addCar(int x, int y) {
		transport_OBJs.add(new Car_OBJ(this, x, y, true));
	}

	@Override
	public void write(Json json) {
		json.writeValue("playerX", player.getX());
		json.writeValue("playerY", player.getY());
		
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		// TODO Auto-generated method stub
		int x = jsonData.getInt("playerX");
		int y = jsonData.getInt("playerY");
		player = new Player(this, x, y, 32, 32, "./foot/player_down1.png");
		
	}

	
}
