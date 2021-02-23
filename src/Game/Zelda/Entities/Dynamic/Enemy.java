package Game.Zelda.Entities.Dynamic;

import static Game.GameStates.Zelda.ZeldaGameState.worldScale;
import static Game.Zelda.Entities.Dynamic.Direction.DOWN;
import static Game.Zelda.Entities.Dynamic.Direction.UP;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;

import Game.GameStates.Zelda.ZeldaGameState;
import Game.Zelda.Entities.Statics.DungeonDoor;
import Game.Zelda.Entities.Statics.SectionDoor;
import Game.Zelda.Entities.Statics.SolidStaticEntities;
import Main.Handler;
import Resources.Animation;

public class Enemy extends BaseMovingEntity {

	private final int animSpeed = 120;
	private int moveSpeed = 120;
	private boolean movingMap = false;
	private Direction movingTo;
	private int newMapX = 0;
	private int newMapY = 0;
	private int xExtraCounter = 0;
	private int yExtraCounter = 0;
	private Random random = new Random();
	private int moveTo = random.nextInt(4);
	private Direction dirr = Direction.LEFT;

	public Enemy(int x, int y, BufferedImage[] sprite, Handler handler) {
		super(x, y, sprite, handler);
		animation = new Animation(256,sprite);
		bounds = new Rectangle((x * (ZeldaGameState.stageWidth/16)) + ZeldaGameState.xOffset,(y * (ZeldaGameState.stageHeight/11)) + ZeldaGameState.yOffset,width,height);
		speed=32;
		direction = UP;
		sprites = sprite;
		interactBounds = (Rectangle) bounds.clone();
		interactBounds.y+=(height/2);
		interactBounds.height/=2;
		maxHealth = 1;
		currentHealth = maxHealth;
		BufferedImage[] animList = new BufferedImage[4];
		animList[0] = sprite[0];
		animList[1] = sprite[1];
		animation = new Animation(animSpeed,animList);
		BufferedImage[] animList2 = new BufferedImage[2];
		animList2[0] = sprite[0];
		animList2[1] = sprite[1];
		animation = new Animation(animSpeed,animList2);
	}

	@Override
	public void tick() {
		if (!dead) {
			moveSpeed--;
			if (moveSpeed < 0) {
				move(dirr);
				moveSpeed = 120;
			}
		}
	}

	@Override 
	public void move(Direction direction) {
		moveTo = random.nextInt(4);
		moving = true;
		changeIntersectingBounds();
		animation.tick();
		if (ZeldaGameState.inCave){
			for (SolidStaticEntities objects : handler.getZeldaGameState().caveObjects) {
				if ((objects instanceof DungeonDoor) && objects.bounds.intersects(bounds) && direction == ((DungeonDoor) objects).direction) {
					if (((DungeonDoor) objects).name.equals("caveStartLeave")) {
						ZeldaGameState.inCave = false;
						x = ((DungeonDoor) objects).nLX;
						y = ((DungeonDoor) objects).nLY;
						direction = DOWN;
					}
				} else if (!(objects instanceof DungeonDoor) && objects.bounds.intersects(interactBounds)) {
					// Don't move
					return;
				}
			}
		}
		else {
			changeIntersectingBounds();
			for (SolidStaticEntities objects : handler.getZeldaGameState().objects.get(handler.getZeldaGameState().mapX).get(handler.getZeldaGameState().mapY)) {
				if ((objects instanceof SectionDoor) && objects.bounds.intersects(bounds) && direction == ((SectionDoor) objects).direction) {
					if (!(objects instanceof DungeonDoor)) {
						movingMap = true;
						movingTo = ((SectionDoor) objects).direction;
						switch (((SectionDoor) objects).direction) {
						case RIGHT:
							newMapX = -(((handler.getZeldaGameState().mapWidth) + 1) * worldScale);
							newMapY = 0;
							handler.getZeldaGameState().mapX++;
							xExtraCounter = 8 * worldScale + (2 * worldScale);
							break;
						case LEFT:
							newMapX = (((handler.getZeldaGameState().mapWidth) + 1) * worldScale);
							newMapY = 0;
							handler.getZeldaGameState().mapX--;
							xExtraCounter = 8 * worldScale + (2 * worldScale);
							break;
						case UP:
							newMapX = 0;
							newMapY = -(((handler.getZeldaGameState().mapHeight) + 1) * worldScale);
							handler.getZeldaGameState().mapY--;
							yExtraCounter = 8 * worldScale + (2 * worldScale);
							break;
						case DOWN:
							newMapX = 0;
							newMapY = (((handler.getZeldaGameState().mapHeight) + 1) * worldScale);
							handler.getZeldaGameState().mapY++;
							yExtraCounter = 8 * worldScale + (2 * worldScale);
							break;
						}
						return;
					}
					else {
						if (((DungeonDoor) objects).name.equals("caveStartEnter")) {
							return;
						}
					}
				}
				else if (!(objects instanceof SectionDoor) && objects.bounds.intersects(interactBounds)) {
					// Don't move
//					switch (direction) {
//					case DOWN:
//						y -= speed;
//						break;
//					case UP:
//						y += speed;
//						break;
//					case RIGHT:
//						x -= speed;
//						break;
//					case LEFT:
//						x += speed;
//						break;
//					}
					return;
				}
			}
		}
		switch (moveTo) {
		case 0:
			direction = Direction.UP;
			break;
		case 1:
			direction = Direction.DOWN;
			break;
		case 2:
			direction = Direction.RIGHT;
			break;
		case 3:
			direction = Direction.LEFT;
			break;
		}
		switch (direction) {
		case RIGHT:
			x += speed;
			changeIntersectingBounds();
			break;
		case LEFT:
			x -= speed;
			changeIntersectingBounds();
			break;
		case UP:
			y -= speed;
			changeIntersectingBounds();
			break;
		case DOWN:
			y += speed;
			changeIntersectingBounds();
			break;
		}
		bounds.x = x;
		bounds.y = y;
		changeIntersectingBounds();
	}

	@Override 
	public void render(Graphics g) {
		if (!dead) {
			super.render(g);
			g.drawImage(animation.getCurrentFrame(),x , y, width , height  , null);
		}
	}


}
