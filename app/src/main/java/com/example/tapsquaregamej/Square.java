package com.example.tapsquaregamej;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import static com.example.tapsquaregamej.GameView.screenX;
import static com.example.tapsquaregamej.GameView.ratioX;
import static com.example.tapsquaregamej.GameView.ratioY;

public class Square {

    public int speed = 20;
    int x = 0, y, width, height, squareCounter = 0;



    Bitmap square;

    Square(Resources res){

        square = BitmapFactory.decodeResource(res, R.drawable.tap3);

        width = square.getWidth();
        height = square.getHeight();

        width /= 4;
        height /= 4;

        //this was bugged now it works
        width = (int) (width * ratioX);
        height = (int) (height * ratioY);

        square = Bitmap.createScaledBitmap(square, width, height, false);

        y = -height;

    }

    Bitmap getSquare () {
        return square;
    }

    Rect getCollisionShape(){
        return new Rect(x, y, x + width, y + height);
    }


}
