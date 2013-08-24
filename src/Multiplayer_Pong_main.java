import static org.lwjgl.opengl.GL11.*;
import java.util.Random;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;
import org.lwjgl.*;

abstract class GameState{
	public abstract void draw();
	public abstract void processInput();
}

public class Multiplayer_Pong_main {

	public static final int WIDTH = 800, HEIGHT = 600;
	public static final int FPS_LIMIT = 1000;
	public static long lastFrame;
	final int PADWIDTH = 100;
	final int PADHEIGHT = 15;
	final int BALLSIZE = 20;
	final int PLAYERS = 1;
	final boolean VMULTACTIVE = false;
	public GameState game, mainMenu, pause, currentState;
	private static long getTime(){
		return(Sys.getTime()* 1000) / Sys.getTimerResolution();
	}	
	private static double getDelta(){
		long currentTime = getTime();
		double delta = (double) (currentTime - lastFrame);
		lastFrame = getTime();
		return delta;
	}
	boolean getNegative(double i){

		return(i<0?true:false);
	}
	boolean Negative = true;
	double padx[] = new double[4];
	double pady[] = new double[4];
	double x;
	double y;
	double dx;
	double dy;
	double vMult;
	double mMult;
	double delta;
	double xMult;
	double yMult;
	long lastBounce;
	int angle;
	long lastSpace;
	int score[] = new int[4];
	Random mainRandom = new Random();
	public Multiplayer_Pong_main(){
	
		mainMenu = new GameState(){
			@Override
			public void draw() {
				Draw();
			}
			@Override
			public void processInput() {
				if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
					currentState = game;
					System.out.println("The game started");
				}
				movePads();
			}
		};
		game = new GameState(){
			@SuppressWarnings("unused")
			@Override
			public void draw() {
				Draw();
				
				//"ball"
				if(y < PADHEIGHT){//top
		   			if(x+BALLSIZE > padx[0] && x < padx[0]+PADWIDTH){
		   				dy = 1;
		   				Bounce(0);
		   			}else if(y < 0){
		   				endGame(0);		   			
		   			}
				}else if(y +BALLSIZE > HEIGHT - PADHEIGHT){//bottom
					if (x + BALLSIZE > padx[1] && x < padx[1] + PADWIDTH && PLAYERS >= 2) {
						dy = -1;
						Bounce(1);
					}else if(y +BALLSIZE > HEIGHT){
				   		if (PLAYERS >= 2) {
							endGame(1);
				   		}else{
				   			dy = -1;
				   			Bounce(4);
				   		}
					}
				}else if(x < PADHEIGHT){//left
					if (y + BALLSIZE > pady[2] && y < pady[2] + PADWIDTH && PLAYERS >= 3) {
						dx = 1;
						Bounce(2);
					}else if(x < 0){
						if (PLAYERS >= 3){
							endGame(2);
						}else{
							dx = 1;
							Bounce(4);
						}	
					}
				}else if(x +BALLSIZE > WIDTH -PADHEIGHT){//right
					if (y + BALLSIZE > pady[3] && y < pady[3] + PADWIDTH && PLAYERS >= 4) {
						dx = -1;
						Bounce(3);
					}else if(x +BALLSIZE > WIDTH){
						if (PLAYERS >= 4) {
							endGame(3);
						}else{
							dx = -1;
							Bounce(4);
						}
					}
				}
				x += dx*delta*xMult*vMult;
				y += dy*delta*yMult*vMult;
			}
			@Override
			public void processInput() {
				movePads();	
				if(Keyboard.isKeyDown(Keyboard.KEY_BACK)){
					currentState = pause;
					System.out.println("Game is paused.");
					
				}
			}
		};
		pause = new GameState(){

			@Override
			public void draw() {
				Draw();
				
			}
			@Override
			public void processInput() {
				if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
					currentState = game;
					System.out.println("Game is unpaused.");
				}
				
			}
		};
		
		try{
			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			Display.setTitle("Multiplayer pong");
			Display.create();
		}catch (LWJGLException e){
			e.printStackTrace();
			Display.destroy();
			System.exit(1);
		}
		
		//OGL init
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		
		vardec();

		while(!Display.isCloseRequested()){
			//render
			glClear(GL_COLOR_BUFFER_BIT);
			delta = getDelta();			
			
			currentState.processInput();
			currentState.draw();
			
			Display.update();
			Display.sync(FPS_LIMIT);
		}
		Display.destroy();
		System.exit(0);
	}
	private void Bounce(int pad){
		if (System.currentTimeMillis()-lastBounce > 200) {
			vMult += 0.001 * delta;
			//		vMult+=(VMULTACTIVE?0.001*delta:0);
			if (pad == 0) {
				if (x + BALLSIZE / 2 < padx[pad] + PADWIDTH / 2) {
					System.out.println("left side");
					if (dx == 1) {
						if (xMult > .2) {
							xMult -= .2;
							yMult += .2;
							xMultCheck();
						}

					} else {
						if (yMult > .2) {
							xMult += .2;
							yMult -= .2;
							yMultCheck();
						}
					}
				} else {
					System.out.println("right side");
					if (dx == 1) {
						if (yMult > .2) {
							xMult += .2;
							yMult -= .2;
							yMultCheck();
						}
					} else {

						if (xMult > .2) {
							xMult -= .2;
							yMult += .2;
							xMultCheck();
						}
					}

				}
			}
			lastBounce = System.currentTimeMillis();
		}
	}
	private void xMultCheck(){
		if(xMult<0){
			xMult= (-xMult);
			dx= (-dx);
		}
	}
	private void yMultCheck(){
		if(yMult<0){
			yMult= (-yMult);
			dy= (-dy);
		}
	}
	private void endGame(int side){
		System.out.println("The " + side + " pad let the ball through");
		score[side]++;
		System.out.print("score: ");
		for(int i = 0;i<4;i++){
			System.out.print("\t" + score[i]);
		}
		System.out.println();
		currentState = mainMenu;
		
		vardec();
	}
	void Draw(){
		glRectd(x, y, x+BALLSIZE , y+BALLSIZE);
		glColor3f(1,0,1);
		for(int i=0; i<2; i++){
			glRectd(padx[i],pady[i],padx[i]+PADWIDTH,pady[i]+PADHEIGHT);
		}
		for(int i=0; i<2; i++){
			glRectd(padx[i+2],pady[i+2],padx[i+2]+PADHEIGHT,pady[i+2]+PADWIDTH);
		}
		glColor3f(1,1,1);
	}
	private void movePads(){
		//pad0
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
			if(padx[0]+PADWIDTH<WIDTH){
				padx[0] += delta*mMult;
			}
		}else if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
			if(padx[0]>0){
				padx[0] -= delta*mMult;
			}
		}
		//pad1
		if(Keyboard.isKeyDown(Keyboard.KEY_X)){
			if(padx[1]+PADWIDTH<WIDTH){
				padx[1] += delta*mMult;
			}
		}else if(Keyboard.isKeyDown(Keyboard.KEY_Z)){
			if(padx[1]>0){
				padx[1] -= delta*mMult;
			}
		}
		//pad2
		if(Keyboard.isKeyDown(Keyboard.KEY_A )){
			if(pady[2]+PADWIDTH<HEIGHT){
				pady[2] += delta*mMult;
			}
		}else if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
			if(pady[2]>0){
				pady[2] -= delta*mMult;
			}
		}
		//pad3
		if(Keyboard.isKeyDown(Keyboard.KEY_J)){
			if(pady[3]+PADWIDTH<HEIGHT){
				pady[3] += delta*mMult;
			}
		}else if(Keyboard.isKeyDown(Keyboard.KEY_I)){
			if(pady[3]>0){
				pady[3] -= delta*mMult;
			}
		}
	}
	private void vardec(){
		padx[0]=WIDTH/2 - (PADWIDTH/2);
		pady[0]=0;
		padx[1]=WIDTH/2 - (PADWIDTH/2);
		pady[1]=HEIGHT - PADHEIGHT;
		padx[2]=0;
		pady[2]=HEIGHT/2 - PADWIDTH/2;
		padx[3]=WIDTH-PADHEIGHT;
		pady[3]=HEIGHT/2 - PADWIDTH/2;
		x = mainRandom.nextInt(WIDTH/2);
		y = mainRandom.nextInt(HEIGHT/2);
		spawnBall();
		vMult = 0.2;
		//begin speed
		mMult = 0.5;
		xMult = 1;
		yMult = 1;
		lastFrame = getTime();
		currentState = mainMenu;
		
	}
	public void spawnBall(){
		int corner = mainRandom.nextInt(4);
		if (corner == 0){//left top
			dx = 1;
			dy = 1;
			System.out.println("Generated ball at corner top left corner");
		}else if(corner == 1){//right top
			dx = -1;
			dy = 1;
			x+=WIDTH/2;
			System.out.println("Generated ball at corner top right corner");
		}else if(corner == 2){//right bottom
			dx = -1;
			dy = -1;
			x+=WIDTH/2;
			y+=HEIGHT/2;
			System.out.println("Generated ball at corner bottom left corner");
		}else if(corner == 3){//left bottom
			dx = 1;
			dy = -1;
			y+=HEIGHT/2;
			System.out.println("Generated ball at corner bottom right corner");
		}
	}
	public static void main(String[] args){
		new Multiplayer_Pong_main();
	}
}
