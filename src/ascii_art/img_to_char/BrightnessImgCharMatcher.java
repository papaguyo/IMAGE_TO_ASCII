package ascii_art.img_to_char;

import image.Image;
import java.lang.*;

import java.awt.*;
import java.util.HashMap;

public class BrightnessImgCharMatcher {

    private static final int MAX_RGB = 255;
    private final Image img_;
    private final String fontName_;
    private final HashMap<Image, Double> cache = new HashMap<>();

    /**
     * constructor
     * @param img Image object to work on
     * @param fontName the font name to load
     */
    public BrightnessImgCharMatcher(Image img, String fontName){
        img_ = img;
        fontName_ = fontName;
    }

    /**
     * the "main" function that build every thing together
     * @param numCharsInRow the number of char to be in a row
     * @param charSet the chars array
     * @return returns two-dimensional array of the result
     */
    public char[][] chooseChars(int numCharsInRow, Character[] charSet){
        double[] charBrightness = getBrightness(charSet);
        double[] stretchCharBrightness = brightnessStretch(charBrightness);
        return convertImageToAscii(charSet, stretchCharBrightness, numCharsInRow);
    }

    /**
     *
     * @param charSet the characters array to work on
     * @return returns an array with the chars' brightness values
     */
    private double[] getBrightness(Character[] charSet){
        int pixelCount = 0;
        double[] charBrightness = new double[charSet.length];
        for (int i = 0; i < charSet.length; i++) {
            boolean[][] imagePixels = CharRenderer.getImg(charSet[i], 16, this.fontName_);
            for (int j = 0; j < imagePixels.length; j++) {
                for (int k = 0; k < imagePixels[0].length; k++) {
                    if(imagePixels[j][k])
                        pixelCount++;
                }
            }

            charBrightness[i] = ((double) pixelCount / (double) (imagePixels.length * imagePixels[0].length));
            pixelCount = 0;
        }
        return charBrightness;
    }

    /**
     *
     * @param brightness the chars' brightness array
     * @return returns the char brightnesses array after normalized stretch
     */
    private double[] brightnessStretch(double[] brightness){
        double[] returnArr = new double[brightness.length];
        double minBrightness = 1;
        double maxBrightness = 0;
        for(double brightLevel : brightness){
            if (brightLevel > maxBrightness)
                maxBrightness = brightLevel;
            if(brightLevel < minBrightness)
                minBrightness = brightLevel;
        }

        for(int i = 0; i < brightness.length; i++){
            double newBright = (double) (brightness[i] - minBrightness) / (double) (maxBrightness - minBrightness);
            returnArr[i] = newBright;
        }
        return returnArr;
    }

    /**
     *
     * @param newImage an Image object
     * @return returns the image average brightness
     */
    private double getAverageImageBrightness(Image newImage){
        if(cache.containsKey(newImage)){
            return cache.get(newImage);
        }
        double brightCount = 0;
        int numOfCells = 0;
        double greyPixel = 0;
        for(Color pixel : newImage.pixels()){
            greyPixel = ((pixel.getRed()*0.2126) + (pixel.getGreen()*0.7152) + (pixel.getBlue()*0.0722)) / MAX_RGB;
            brightCount += greyPixel;
            numOfCells++;
        }

        if(numOfCells == 0)
            cache.put(newImage, 0.0);
        else
            cache.put(newImage, brightCount / (newImage.getHeight() * newImage.getWidth()));

        return cache.get(newImage);
    }

    /**
     *
     * @param charSet the characters array to work on
     * @param brightnessAfterStretch the characters brightnesses after normalized
     * @param charsInRow the number of chars in a row
     * @return returns a two-dimensional array with the matching brightness characters
     */
    private char[][] convertImageToAscii(Character[] charSet, double[] brightnessAfterStretch, int charsInRow){
        int pixels = img_.getWidth() / charsInRow; // num of pixels calculation
        char[][] asciiArt = new char[img_.getHeight()/pixels][img_.getWidth()/pixels];
        double averageBright;
        int rowIndex = 0;
        int colIndex = 0;

        for(Image subImage : img_.squareSubImagesOfSize(pixels)){
            averageBright = getAverageImageBrightness(subImage);
            double maxMatch = brightnessAfterStretch[0];
            int matchIndex = 0;

            for (int i = 0; i < brightnessAfterStretch.length; i++) {
                // calculate the maximum brightness match
                if (Math.abs(averageBright - brightnessAfterStretch[i]) < Math.abs(averageBright - maxMatch)) {
                    maxMatch = brightnessAfterStretch[i];
                    matchIndex = i;
                }
            }

            asciiArt[rowIndex][colIndex] = charSet[matchIndex];
            colIndex++;

            if(colIndex >= asciiArt[0].length){ // keeping the indexes of asciiArt array in bounds
                rowIndex++;
                colIndex = 0;
            }
        }
        return asciiArt;
    }
}
