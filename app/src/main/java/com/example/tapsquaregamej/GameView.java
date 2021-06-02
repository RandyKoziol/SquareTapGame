package com.example.tapsquaregamej;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable{

    private Thread thread;
    private boolean isPlaying, isGameOver = false, leftOrRight, firstIterationLeft = true;
    public static int screenX, screenY;
    private Paint paint;
    public static float ratioX, ratioY;
    private List<Square> squares;
    private Random random;
    private int numberOfSquares = 1, tempNumOfSquares = 1;
    private int score, firstIterationPerSquare = numberOfSquares;
    private Rect leftRect, rightRect;
    private List<Square> trash;
    private SharedPreferences preferences;
    private GameActivity activity;



    public GameView(GameActivity activity, int screenX, int screenY) {
        super(activity);

        this.activity = activity;

        //initialize the screen x and y variables
        GameView.screenX = screenX;
        GameView.screenY = screenY;

        //create a ratio to use for any screen size
        ratioX = 1920f / screenX;
        ratioY = 1080f / screenY;

        //create an array to store squares to created
        squares = new ArrayList<>();

        for (int i = 0; i < numberOfSquares; i++){
            Square square = new Square(getResources());
            squares.add(square);
        }

        random = new Random();

        paint = new Paint();

        //the square is able to skip this rect entirely if it is too small
        leftRect = new Rect(0, 0, 20, screenY);
        rightRect = new Rect(screenX, 0, screenX, screenY);

        trash = new ArrayList<>();

        leftOrRight = random.nextBoolean();

        preferences = activity.getSharedPreferences("game", Context.MODE_PRIVATE);

    }

    @Override
    public void run() {

        while(isPlaying){

            update();
            draw();
            sleep();

            squares.removeAll(trash);
            trash.clear();

            //increase the difficulty every 5 points
            if(squares.size() == 0){
                if (score % 5 == 0) {
                    numberOfSquares++;
                    firstIterationPerSquare = numberOfSquares;
                    tempNumOfSquares = numberOfSquares;
                }else{
                    firstIterationPerSquare = numberOfSquares;
                    numberOfSquares = tempNumOfSquares;
                }
                repopulateSquares();

                //alternate which side the squares spawn on
                leftOrRight = random.nextBoolean();


            }

        }

    }

    private void update() {

        if (leftOrRight){
            createRightSquare();
        }else{
            createLeftSquare();
        }

    }

    private void draw() {

        if (getHolder().getSurface().isValid()){

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawARGB(255,255,255,255);

            if (isGameOver){
                isPlaying = false;
                paint.setTextSize(100f);
                paint.setTextAlign(Paint.Align.CENTER);

                canvas.drawText("Game Over", screenX/2, screenY/2, paint);
                canvas.drawText("Score: " + score, screenX/2, screenY/5, paint);

                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();

                return;
            }



                for (Square square : squares){
                    canvas.drawBitmap(square.square, square.x, square.y, paint);

                }




            getHolder().unlockCanvasAndPost(canvas);

        }

    }

    private void saveIfHighScore() {

        if (preferences.getInt("highscore", 0) < score){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }

    }

    private void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume(){

        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause(){

        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){


        if (event.getAction() == MotionEvent.ACTION_DOWN){
            for(Square square: squares){
                if(square.getCollisionShape().contains((int) event.getX(),(int) event.getY() )){

                    trash.add(square);
                    score++;

                }
            }

            if (isGameOver){
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
            }

        }
        return true;
    }

    public void repopulateSquares(){
        for (int i = 0; i < numberOfSquares; i++){
            Square square = new Square(getResources());
            squares.add(square);
        }
    }

    public void createRightSquare(){
        for(Square square : squares){

            //moves the square
            square.x -= square.speed;

            //if the square is off screen
            if (square.x < 0){

                //create a cap for the speed of the square
                int bound = (int) (30 * ratioX);

                //apply a random speed to the square
                square.speed = random.nextInt(bound);

                //create a minimum speed of > 15
                if (square.speed < 15 * ratioX)
                    square.speed = (int) (15 * ratioX);

                //set the x and y of the square to a location on the surface view
                square.x = screenX;
                //might make is so the y values cannot overlap with a for loop and predetermined values later might not
                square.y = random.nextInt(screenY - square.height);
            }
            //end the game here if a square reaches the end


            if (firstIterationPerSquare == 0){
                if (square.x == 0){
                    isGameOver = true;
                }
            }
            if (Rect.intersects(square.getCollisionShape(), leftRect)){
                isGameOver = true;
                return;
            }
        }
    }

    // the square takes too long to be drawn the problem is likely in the if condition
    public void createLeftSquare(){


        for(Square square : squares){

            if (firstIterationPerSquare > 0){
                square.x = screenX;
                firstIterationPerSquare--;
            }


            //moves the square
            square.x += square.speed;

            //if the square is not on the screen
            if (square.x > screenX){

                //create a cap for the speed of the square
                int bound = (int) (30 * ratioX);

                //apply a random speed to the square
                square.speed = random.nextInt(bound);

                //create a minimum speed of > 15
                if (square.speed < 15 * ratioX)
                    square.speed = (int) (15 * ratioX);

                //set the x and y of the square to a location on the surface view
                square.x = 0;
                //might make is so the y values cannot overlap with a for loop and predetermined values later might not
                square.y = random.nextInt(screenY - square.height);
            }
            //end the game here if a square reaches the end

            //end the game if the square reaches the opposite side
            if (Rect.intersects(square.getCollisionShape(), rightRect)){
                isGameOver = true;
                return;
            }

        }












    }




}
