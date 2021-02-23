package Game.GameStates.Zelda;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import Game.GameStates.State;
import Game.Zelda.Entities.Dynamic.BaseMovingEntity;
import Game.Zelda.Entities.Dynamic.Direction;
import Game.Zelda.Entities.Dynamic.Enemy;
import Game.Zelda.Entities.Dynamic.Link;
import Game.Zelda.Entities.Statics.DungeonDoor;
import Game.Zelda.Entities.Statics.SectionDoor;
import Game.Zelda.Entities.Statics.SolidStaticEntities;
import Main.Handler;
import Resources.Images;

/**
 * Created by AlexVR on 3/14/2020
 */
public class ZeldaGameState extends State {

	public static int xOffset, yOffset, stageWidth, stageHeight, worldScale;
	public int cameraOffsetX, cameraOffsetY;
	// map is 16 by 7 squares, you start at x=7,y=7 starts counting at 0
	public int mapX, mapY, mapWidth, mapHeight;
	public int damageCooldown = 60;
	public int knockback = 30;

	public ArrayList<ArrayList<ArrayList<SolidStaticEntities>>> objects;
	public ArrayList<ArrayList<ArrayList<BaseMovingEntity>>> enemies;
	public Link link;
	public Enemy bouncyEnemy;
	public static boolean inCave = false;
	public ArrayList<SolidStaticEntities> caveObjects;

	public ZeldaGameState(Handler handler) {
		super(handler);
		//x and y location are top of the corner
		xOffset = handler.getWidth() / 4;
		yOffset = handler.getHeight() / 4;

		stageWidth = handler.getWidth() / 3 + (handler.getWidth() / 15);
		stageHeight = handler.getHeight() / 2;

		//changes scale of the map
		worldScale=3;

		//link's current position on the map, link starts at 7,7

		mapX = 7;
		mapY = 7;
		mapWidth = 256;
		mapHeight = 176;
		cameraOffsetX = ((mapWidth * mapX) + mapX + 1) * worldScale;
		cameraOffsetY = ((mapHeight * mapY) + mapY + 1) * worldScale;

		//3 list that are very useful(se usan para los otros objetos y enemigos)

		objects = new ArrayList<>();
		enemies = new ArrayList<>();
		caveObjects = new ArrayList<>();

		//to not get a nullpointer--do not remove pls

		//0->16
		for (int i = 0; i < 16; i++) {
			objects.add(new ArrayList<>());
			enemies.add(new ArrayList<>());
			//0->8
			for (int j = 0; j < 8; j++) {
				objects.get(i).add(new ArrayList<>());
				enemies.get(i).add(new ArrayList<>());
			}
		}

		addWorldObjects();//check addworldobjects in the bottom :)

		link = new Link(xOffset + (stageWidth / 2), yOffset + (stageHeight / 2), Images.zeldaLinkFrames, handler);
		bouncyEnemy = new Enemy((xOffset + (stageWidth / 2)) + 100, (yOffset + (stageHeight / 2)) + 100, Images.bouncyEnemyFrames, handler);
		enemies.get(mapX).get(mapY).add(bouncyEnemy);
	}

	// Method that is in charge of Link's life display.
	public void linkHeartManager(int[] a) {
		// Fills array with full hearts
		// Happens when the game first starts or when the maximumHealth.
		for (int i = 0; i < a.length; i++) {
			a[i] = 1;
		}
		if (link.currentHealth <= 0) { // If Link health is 0, draw all hearts empty
			for (int i = 0; i < a.length; i++) {
				a[i] = 0;
			}
		}
		// Checks Link's health and changes the type of heart in the array
		// 1 is a full heart, 2 is a half heart and 0 is an empty one
		else if (link.currentHealth < link.maxHealth) {
			int lastHeart = link.currentHealth/2; // Helps establish if the last active heart of current health should be a half or an empty one.
			for (int i = 0; i < a.length; i++) {
				if (link.currentHealth % 2 != 0) { // Checks to see if current health is uneven; draws half heart
					if (i < lastHeart) {
						a[i] = 1;
					}else if (i == lastHeart) { // Number of health is uneven, so last active heart is a half heart
						a[i] = 2;
					}else {
						a[i] = 0;
					}
				}else { // If health is even, draws full hearts
					if (i < lastHeart) {
						a[i] = 1;
					}else {
						a[i] = 0;
					}
				}
			}
		}
	}


	@Override
	public void tick() {
		link.tick();
		bouncyEnemy.tick();
		damageCooldown--;
		if (!link.movingMap) {
			for (SolidStaticEntities entity : objects.get(mapX).get(mapY)) { //gets the blocks of the current map link is in
				entity.tick();
			}
			for (BaseMovingEntity entity : enemies.get(mapX).get(mapY)) {//gets the enemies of the current map
				entity.tick();
				if (damageCooldown < 0) {
					if (entity.getInteractBounds().intersects(link.getInteractBounds())) {//if the enemy touches link, the enemy will take 1 hp, so collision with the enemy is done, we can add if link can die or whatever.
						if (link.attacking == false) {
							link.damage(1);
							switch (link.currentDirr) { // Causes knock back Link when he is damaged. 
							case DOWN:
								link.y -= knockback;
								break;
							case UP:
								link.y += knockback;
								break;
							case RIGHT:
								link.x -= knockback;
								break;
							case LEFT:
								link.x += knockback;
								break;
							}
							damageCooldown = 60; // Gives invincibility to Link for a second after he is damaged.
						}else {
							bouncyEnemy.kill();
							enemies.get(mapX).get(mapY).remove(bouncyEnemy);
						}
					}
				}
			}
		}
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_Q)) {
			bouncyEnemy.kill();
			enemies.get(mapX).get(mapY).remove(bouncyEnemy);
		}

	}

	@Override
	public void render(Graphics g) {
		if (inCave) {//if in cave render this:
			for (SolidStaticEntities entity : caveObjects) {
				entity.render(g);
			}
			// Will draw the sword in the cave until Link picks it up
			if (!link.hasSword) { 
				g.drawImage(Images.woodenSword, (int) ((3.5 * (ZeldaGameState.stageWidth/7.5)) + ZeldaGameState.xOffset), (4 * (ZeldaGameState.stageHeight/7)) + ZeldaGameState.yOffset- ((16*worldScale)/2), (16*worldScale), (16*worldScale),null);
			}
			// Will stop drawing the sword when Link "picks it up"
			if (link.getInteractBounds().intersects((int) ((3.5 * (ZeldaGameState.stageWidth/7.5)) + ZeldaGameState.xOffset), (4 * (ZeldaGameState.stageHeight/7)) + ZeldaGameState.yOffset- ((16*worldScale)/2), (16*worldScale), (16*worldScale))) {
				link.hasSword = true;
			}
			g.setColor(Color.WHITE);
			g.setFont(new Font("TimesRoman", Font.BOLD, 32));
			g.drawString("  IT ' S  DANGEROUS  TO  GO", (3 * (ZeldaGameState.stageWidth / 16)) + ZeldaGameState.xOffset,
					(2 * (ZeldaGameState.stageHeight / 11)) + ZeldaGameState.yOffset + ((16 * worldScale)));
			g.drawString("  ALONE !   TAKE  THIS", (4 * (ZeldaGameState.stageWidth / 16)) + ZeldaGameState.xOffset,
					(4 * (ZeldaGameState.stageHeight / 11)) + ZeldaGameState.yOffset - ((16 * worldScale) / 2));

			// Cave entrance
			g.drawImage(Images.oldMan, (int) ((3.5 * (ZeldaGameState.stageWidth/7.5)) + ZeldaGameState.xOffset), (4 * (ZeldaGameState.stageHeight/10)) + ZeldaGameState.yOffset- ((16*worldScale)/2), (16*worldScale), (16*worldScale),null);
			g.drawImage(Images.fire, (int) ((3.5 * (ZeldaGameState.stageWidth/12)) + ZeldaGameState.xOffset), (4 * (ZeldaGameState.stageHeight/10)) + ZeldaGameState.yOffset- ((16*worldScale)/2), (16*worldScale), (16*worldScale),null);
			g.drawImage(Images.fire, (int) ((3.5 * (ZeldaGameState.stageWidth/5.5)) + ZeldaGameState.xOffset), (4 * (ZeldaGameState.stageHeight/10)) + ZeldaGameState.yOffset- ((16*worldScale)/2), (16*worldScale), (16*worldScale),null);

			link.render(g);
		} else {
			g.drawImage(Images.zeldaMap, -cameraOffsetX + xOffset, -cameraOffsetY + yOffset,
					Images.zeldaMap.getWidth() * worldScale, Images.zeldaMap.getHeight() * worldScale, null);
			if (!link.movingMap) {
				for (SolidStaticEntities entity : objects.get(mapX).get(mapY)) {
					entity.render(g);
				}
				for (BaseMovingEntity entity : enemies.get(mapX).get(mapY)) {
					entity.render(g);
				}
			}
			link.render(g);
			bouncyEnemy.render(g);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, xOffset, handler.getHeight());
			g.fillRect(xOffset + stageWidth, 0, handler.getWidth(), handler.getHeight());
			g.fillRect(0, 0, handler.getWidth(), yOffset);
			g.fillRect(0, yOffset + stageHeight, handler.getWidth(), handler.getHeight());
		}

		// Draws Link's life meter
		g.setColor(Color.RED);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 32));
		g.drawString(" - L I F E -", ((handler.getWidth() / 3) + handler.getWidth() / 6)+ (((handler.getPacman().width + 8) * 2)),200);

		// Array that contains Link's hearts (to be drawn).
		int[] drawableHearts = new int[link.maxHealth/2];
		linkHeartManager(drawableHearts); // Method that is in charge of Link's life display.


		// Draws Link's current health (hearts)
		for (int i = 0; i < drawableHearts.length; i++) {
			if (drawableHearts[i] == 1) { // draws full heart
				g.drawImage(Images.hearts[0],((handler.getWidth() / 3) + handler.getWidth() / 5)+ (((handler.getPacman().width) * 2) * (int)i),225, handler.getWidth() / 70, handler.getHeight() / 70, null);
			}
			else if (drawableHearts[i] == 2) { // draws half heart
				g.drawImage(Images.hearts[1],((handler.getWidth() / 3) + handler.getWidth() / 5)+ (((handler.getPacman().width) * 2) * (int)i),225, handler.getWidth() / 70, handler.getHeight() / 70, null);
			}
			else { // 0: draws empty heart
				g.drawImage(Images.hearts[2],((handler.getWidth() / 3) + handler.getWidth() / 5)+ (((handler.getPacman().width) * 2) * (int)i),225, handler.getWidth() / 70, handler.getHeight() / 70, null);
			}
		}
	}

	private void addWorldObjects() {//everything per map is added here, if we want to add enemies, walls, objects etc it all has to be here:
		// cave,adds walls to the cave
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 11; j++) {
				if (i >= 2 && i <= 13 && j >= 2 && j < 9) {
					continue;
				} else {
					if (j >= 9) {
						if (i > 1 && i < 14) {
							if ((i == 7 || i == 8)) {
								continue;
							} else {
								caveObjects.add(new SolidStaticEntities(i, j, Images.caveTiles.get(2), handler));
							}
						} else {
							caveObjects.add(new SolidStaticEntities(i, j, Images.caveTiles.get(5), handler));
						}
					} else {
						caveObjects.add(new SolidStaticEntities(i, j, Images.caveTiles.get(5), handler));
					}
				}
			}
		}
		caveObjects.add(new DungeonDoor(7, 9, 16 * worldScale * 2, 16 * worldScale * 2, Direction.DOWN,
				"caveStartLeave", handler, (4 * (ZeldaGameState.stageWidth / 16)) + ZeldaGameState.xOffset,
				(2 * (ZeldaGameState.stageHeight / 11)) + ZeldaGameState.yOffset));

		// 7,7 (mapx,mapy) everything here renders under 7,7 in the map
		ArrayList<SolidStaticEntities> solids = new ArrayList<>();
		ArrayList<BaseMovingEntity> monster = new ArrayList<>();
		solids.add(new SectionDoor(0, 5, 16 * worldScale, 16 * worldScale, Direction.LEFT, handler));
		solids.add(new SectionDoor(7, 0, 16 * worldScale * 2, 16 * worldScale, Direction.UP, handler));
		solids.add(new DungeonDoor(4, 1, 16 * worldScale, 16 * worldScale, Direction.UP, "caveStartEnter", handler,
				(7 * (ZeldaGameState.stageWidth / 16)) + ZeldaGameState.xOffset,
				(9 * (ZeldaGameState.stageHeight / 11)) + ZeldaGameState.yOffset));
		solids.add(new SectionDoor(15, 5, 16 * worldScale, 16 * worldScale, Direction.RIGHT, handler));
		solids.add(new SolidStaticEntities(6, 0, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(5, 1, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(6, 1, Images.forestTiles.get(6), handler));
		solids.add(new SolidStaticEntities(3, 2, Images.forestTiles.get(6), handler));
		solids.add(new SolidStaticEntities(2, 3, Images.forestTiles.get(6), handler));
		solids.add(new SolidStaticEntities(1, 4, Images.forestTiles.get(6), handler));
		solids.add(new SolidStaticEntities(1, 6, Images.forestTiles.get(3), handler));
		solids.add(new SolidStaticEntities(1, 7, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(1, 8, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(2, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(3, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(4, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(5, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(6, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(7, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(8, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(9, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(10, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(11, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(12, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(13, 9, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(14, 8, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(14, 7, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(14, 6, Images.forestTiles.get(2), handler));
		solids.add(new SolidStaticEntities(14, 4, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(13, 4, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(12, 4, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(11, 4, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(10, 4, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(9, 4, Images.forestTiles.get(4), handler));
		solids.add(new SolidStaticEntities(9, 3, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(9, 2, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(9, 1, Images.forestTiles.get(5), handler));
		solids.add(new SolidStaticEntities(9, 0, Images.forestTiles.get(5), handler));
		monster.add(bouncyEnemy);
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_Q)) {
			monster.remove(bouncyEnemy);
		}
		objects.get(7).set(7, solids);//lo que lo añade (todos estos bloques a 7,7)

		// 6,7
		monster = new ArrayList<>();
		solids = new ArrayList<>();
		solids.add(new SectionDoor(0, 2, 16 * worldScale, 16 * worldScale * 7, Direction.LEFT, handler));
		solids.add(new SectionDoor(12, 0, 16 * worldScale * 2, 16 * worldScale, Direction.UP, handler));
		solids.add(new SectionDoor(15, 5, 16 * worldScale, 16 * worldScale, Direction.RIGHT, handler));
		objects.get(6).set(7, solids);

		// 7,6
		monster = new ArrayList<>();
		solids = new ArrayList<>();
		solids.add(new SectionDoor(0, 4, 16 * worldScale, 16 * worldScale * 3, Direction.LEFT, handler));
		solids.add(new SectionDoor(7, 10, 16 * worldScale * 2, 16 * worldScale, Direction.DOWN, handler));
		solids.add(new SectionDoor(15, 4, 16 * worldScale, 16 * worldScale * 3, Direction.RIGHT, handler));
		objects.get(7).set(6, solids);

		// 8,7
		monster = new ArrayList<>();
		solids = new ArrayList<>();
		solids.add(new SectionDoor(0, 5, 16 * worldScale, 16 * worldScale, Direction.LEFT, handler));
		solids.add(new SectionDoor(2, 0, 16 * worldScale * 13, 16 * worldScale, Direction.UP, handler));
		solids.add(new SectionDoor(15, 2, 16 * worldScale, 16 * worldScale * 7, Direction.RIGHT, handler));
		objects.get(8).set(7, solids);

		//if we want to create a new section of the map it would have to be:
		//monster = new ArrayList<>();
		//solids = new ArrayList<>();
		//solids.add.....
		//objects(add map number here) 

	}

	@Override
	public void refresh() {

	}
}
