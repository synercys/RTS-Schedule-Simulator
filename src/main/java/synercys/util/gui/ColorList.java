package synercys.util.gui;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jjs on 2/13/17.
 */
public class ColorList {
    private static int MAX_COLOR_LENGTH = 50;

    private ArrayList<Color> colorList = new ArrayList<Color>();    // ArrayList index starts from 0.
    private int colorIndex = 0;

    public ColorList()
    {
        final int center = 128;
        final int width = 127;
        final double frequency = 2.4;
        colorList.clear();
        //generateColorList(frequency, frequency, frequency, 0, 2, 4, center, width, MAX_COLOR_LENGTH);
        generateColorList(frequency, frequency, frequency, 1, 2, 4, center, width, MAX_COLOR_LENGTH);
        colorIndex = 0;
    }

    /* Source from: http://krazydad.com/tutorials/makecolors.php */
    private void generateColorList(double frequency1, double frequency2, double frequency3,
                                   int phase1, int phase2, int phase3,
                                   int center, int width, int len)
    {
        if (center ==0)   center = 128;
        if (width == 0)    width = 127;
        if (len == 0)      len = MAX_COLOR_LENGTH;

        for (int i = 0; i < len; ++i)
        {
            double red = (Math.sin(frequency1*i + phase1) * width + center)/256.0;
            double grn = (Math.sin(frequency2*i + phase2) * width + center)/256.0;
            double blu = (Math.sin(frequency3*i + phase3) * width + center)/256.0;
            colorList.add(new Color(red, grn, blu, 1));
            //System.out.println(colorList.get(i).toString());
        }
    }

    public Color getNextColor() {
        // Reset color index if it is going to be overflown.
        if (colorIndex == MAX_COLOR_LENGTH) {
            colorIndex = 0;
        }

        colorIndex++;
        return colorList.get(colorIndex-1); // ArrayList index starts from 0.


        //to get rainbow, pastel colors
//        Random random = new Random();
//        final float hue = random.nextFloat();
//        final float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
//        final float luminance = 1.0f; //1.0 for brighter, 0.0 for black
//        return Color.hsb(hue, saturation, luminance);
    }

    public void resetColorIndex(Integer index) {
        if (index == null) {
            colorIndex = 0;
        } else {
            colorIndex = index;
        }
    }

    public Color getColorByIndex(int index)
    {
        // ArrayList index starts from 0.
        return colorList.get(index);
    }

}
