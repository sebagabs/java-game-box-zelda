package Game.Zelda.Entities.Dynamic;

import static Game.GameStates.Zelda.ZeldaGameState.worldScale;
import static Game.Zelda.Entities.Dynamic.Direction.DOWN;
import static Game.Zelda.Entities.Dynamic.Direction.UP;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import Game.GameStates.State;
import Game.GameStates.Zelda.ZeldaGameState;
import Game.Zelda.Entities.Statics.DungeonDoor;
import Game.Zelda.Entities.Statics.SectionDoor;
import Game.Zelda.Entities.Statics.SolidStaticEntities;
import Main.Handler;
import Resources.Animation;
import Resources.Images;

/**
 * Created by AlexVR on 3/15/2020
 */
public class Link extends BaseMovingEntity {


	private final int animSpeed = 120;
	int newMapX=0,newMapY=0,xExtraCounter=0,yExtraCounter=0;
	public boolean movingMap = false, attacking=false;
	public boolean hasSword = false;
	Direction movingTo;
	Animation wsUp,wsDown,wsLeft,wsRight;
	public Direction currentDirr;

	public Link(int x, int y, BufferedImage[] sprite, Handler handler) {
		super(x, y, sprite, handler);
		speed = 4;
		maxHealth = 6; // 3 hearts. Hearts = maxHealth / 2
		currentHealth = maxHealth;
		BufferedImage[] animList = new BufferedImage[4];
		animList[0] = sprite[4];
		animList[1] = sprite[5];
		animation = new Animation(animSpeed,animList);
		BufferedImage[] animList2 = new BufferedImage[4];
		animList2[0] = sprite[8];
		animList2[1] = sprite[9];
		animList2[2] = sprite[10];
		animList2[3] = sprite[11];
		animation = new Animation(animSpeed,animList2);
	}


	@Override
	public void tick() {
		if (movingMap){
			switch (movingTo) {
			case RIGHT:
				handler.getZeldaGameState().cameraOffsetX+=8;
				newMapX+=8;
				if (xExtraCounter>2){
					x+=1;
					xExtraCounter-=8;
					animation.tick();

				}else{
					x-=7;
					if (newMapX > 0) newMapX = 0;
				}
				break;
			case LEFT:
				handler.getZeldaGameState().cameraOffsetX-=8;
				newMapX-=8;
				if (xExtraCounter>2){
					x-=1;
					xExtraCounter-=8;
					animation.tick();

				}else{
					x+=7;
					if (newMapX < 0) newMapX = 0;
				}
				break;
			case UP:
				handler.getZeldaGameState().cameraOffsetY-=8;
				newMapY+=8;
				if (yExtraCounter>2){
					y-=1;
					yExtraCounter-=8;
					animation.tick();

				}else{
					y+=7;
					if (newMapY > 0) newMapY = 0;
				}
				break;
			case DOWN:
				handler.getZeldaGameState().cameraOffsetY+=8;
				newMapY-=8;
				if (yExtraCounter>2){
					y+=1;
					yExtraCounter-=8;
					animation.tick();
				}else{
					y-=7;
					if (newMapY < 0) newMapY = 0;
				}
				break;
			}
			bounds = new Rectangle(x,y,width,height);
			changeIntersectingBounds();
			if (newMapX == 0 && newMapY == 0){
				movingMap = false;
				movingTo = null;
				newMapX = 0;
				newMapY = 0;
			}
		}else {
			if (handler.getKeyManager().up) {
				if (direction != UP) {
					BufferedImage[] animList = new BufferedImage[2];
					animList[0] = sprites[4];
					animList[1] = sprites[5];
					animation = new Animation(animSpeed, animList);
					direction = UP;
					sprite = sprites[4];
				}
				animation.tick();
				move(direction);

			} else if (handler.getKeyManager().down) {
				if (direction != DOWN) {
					BufferedImage[] animList = new BufferedImage[2];
					animList[0] = sprites[0];
					animList[1] = sprites[1];
					animation = new Animation(animSpeed, animList);
					direction = DOWN;
					sprite = sprites[0];
				}
				animation.tick();
				move(direction);
			} else if (handler.getKeyManager().left) {
				if (direction != Direction.LEFT) {
					BufferedImage[] animList = new BufferedImage[2];
					animList[0] = Images.flipHorizontal(sprites[2]);
					animList[1] = Images.flipHorizontal(sprites[3]);
					animation = new Animation(animSpeed, animList);
					direction = Direction.LEFT;
					sprite = Images.flipHorizontal(sprites[3]);
				}
				animation.tick();
				move(direction);
			} else if (handler.getKeyManager().right) {
				if (direction != Direction.RIGHT) {
					BufferedImage[] animList = new BufferedImage[2];
					animList[0] = (sprites[2]);
					animList[1] = (sprites[3]);
					animation = new Animation(animSpeed, animList);
					direction = Direction.RIGHT;
					sprite = (sprites[3]);
				}
				animation.tick();
				move(direction);
			} else {
				moving = false;
			}

			// Pause State
			if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_SPACE)){
				State.setState(handler.getPauseState());

			}
		}

		// Debugging commands (maximumtHealth)
		// Pressing / adds extra hearts - raises maximum health.
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_SLASH)) {
			if (currentHealth < maxHealth) {
				currentHealth++;

			}
		}
		// Pressing \ reduces maximum health - deletes hearts.
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_BACK_SLASH)) {
			if (currentHealth > 0) {
				currentHealth--;
			}
		}

		// Debugging commands (currentHealth)
		// Pressing H raises the amount current health.
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_H)) {
			maxHealth+=2;
			currentHealth = maxHealth;
		}
		// Pressing G reduces the amount of current health.
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_G)) {
			if (maxHealth >= 2) {
				maxHealth-=2;
			}
		}

		// Attack animations
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_ENTER) && hasSword == true && direction == Direction.UP) {
			attacking = true;
			BufferedImage[] animList2 = new BufferedImage[4];
			animList2[0] = sprites[8];
			animList2[1] = sprites[9];
			animList2[2] = sprites[10];
			animList2[3] = sprites[11];
			animation = new Animation(animSpeed, animList2);
			sprite = (sprites[8]);
			if (!animation.end) {
				animation.tick();	
			}
		}
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_ENTER) && hasSword == true && direction == Direction.DOWN) {
			attacking = true;
			BufferedImage[] animList2 = new BufferedImage[4];
			animList2[0] = sprites[12];
			animList2[1] = sprites[13];
			animList2[2] = sprites[14];
			animList2[3] = sprites[15];
			animation = new Animation(animSpeed, animList2);
			sprite = (sprites[12]);
			if (!animation.end) {
				animation.tick();	
			}	
		}
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_ENTER) && hasSword == true && direction == Direction.RIGHT) {
			attacking = true;
			BufferedImage[] animList2 = new BufferedImage[4];
			animList2[0] = sprites[16];
			animList2[1] = sprites[17];
			animList2[2] = sprites[18];
			animList2[3] = sprites[19];
			animation = new Animation(animSpeed, animList2);
			sprite = (sprites[16]);
			if (!animation.end) {
				animation.tick();	
			}	
		}
		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_ENTER) && hasSword == true && direction == Direction.LEFT) {
			attacking = true;
			BufferedImage[] animList2 = new BufferedImage[4];
			animList2[0] = sprites[20];
			animList2[1] = sprites[21];
			animList2[2] = sprites[22];
			animList2[3] = sprites[23];
			animation = new Animation(animSpeed, animList2);
			sprite = (sprites[20]);
			if (!animation.end) {
				animation.tick();	
			}	
		}
		if (!animation.end) {
			animation.tick();	
		}
		attacking = false;		
	}



	@Override
	public void render(Graphics g) {
		if (moving) {
			g.drawImage(animation.getCurrentFrame(),x , y, width , height  , null);

		} else {
			if (movingMap){
				g.drawImage(animation.getCurrentFrame(),x , y, width, height  , null);
			}
			g.drawImage(sprite, x , y, width , height , null);
		}
		if(attacking==true && moving==false) {
			g.drawImage(wsRight.getCurrentFrame(),x , y, width, height  , null);
		}
	}

	@Override
	public void move(Direction direction) {
		moving = true;
		changeIntersectingBounds();
		//check for collisions
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
					//dont move
					return;
				}
			}
		}
		else {
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
							ZeldaGameState.inCave = true;
							x = ((DungeonDoor) objects).nLX;
							y = ((DungeonDoor) objects).nLY;
							direction = UP;
						}
					}
				}
				else if (!(objects instanceof SectionDoor) && objects.bounds.intersects(interactBounds)) {
					//dont move
					return;
				}
			}
		}
		switch (direction) {
		case RIGHT:
			currentDirr = Direction.RIGHT;
			x += speed;
			break;
		case LEFT:
			currentDirr = Direction.LEFT;
			x -= speed;
			break;
		case UP:
			currentDirr = Direction.UP;
			y -= speed;
			break;
		case DOWN:
			currentDirr = Direction.DOWN;
			y += speed;
			break;
		}
		bounds.x = x;
		bounds.y = y;
		changeIntersectingBounds();

	}
}
