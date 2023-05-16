package ascii_art;

import ascii_art.img_to_char.BrightnessImgCharMatcher;
import ascii_output.AsciiOutput;
import ascii_output.HtmlAsciiOutput;
import image.Image;

import java.util.*;
import java.util.stream.Stream;

public class Shell {

    private static final int MIN_PIXELS_PER_CHAR = 2;
    private static final String CMD_EXIT = "exit";
    private static final int INITIAL_CHARS_IN_ROW = 64;
    private static final String FONT_NAME = "Courier New";
    private static final String OUTPUT_FILENAME = "out.html";
    private static final int INITIAL_CHARS_IN_ROW = 64;
    private static final String INITIAL_CHARS_RANGE = "0-9";
    
    private final Image img_;
    private final Set<Character> charSet = new HashSet<>();
    private final int minCharsInRow;
    private final int maxCharsInRow;
    private final BrightnessImgCharMatcher charMatcher;
    private final AsciiOutput output;
    
    private int charsInRow;
    private boolean consoleOutput;
    
    


    /**
     * constructor
     * @param img Image object to work on
     */
    public Shell(Image img){
        img_ = img;
        minCharsInRow = Math.max(1, img.getWidth()/img.getHeight());
        maxCharsInRow = img.getWidth() / MIN_PIXELS_PER_CHAR;
        charsInRow = Math.max(Math.min(INITIAL_CHARS_IN_ROW, maxCharsInRow), minCharsInRow);
        charMatcher = new BrightnessImgCharMatcher(img, FONT_NAME);
        output = new HtmlAsciiOutput(OUTPUT_FILENAME, FONT_NAME);
        addChars(INITIAL_CHARS_RANGE); // initial chars adding.
        consoleOutput = false;
    }

    /**
     * runs the program and keep running till the user decides to exit
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean isChanged = false;
        System.out.print(">>> ");
        String cmd = scanner.nextLine().trim();
        String[] words = cmd.split("\\s+");
        while (!words[0].equals(CMD_EXIT)) {
            switch (words[0]){
                case "chars":
                    showChars();
                    break;
                case "console":
                    toConsole();
                    break;
                case "render":
                    render();
                    break;
                case "add":
                    if(words.length == 2)
                        addChars(words[1]);
                    else
                        System.out.println("Add command should be following with the chars to be add, no spaces throw" +
                                "the chars specification.");
                    break;
                case "remove":
                    if(words.length == 2)
                        removeChars(words[1]);
                    else
                        System.out.println("Remove command should be following with the chars to be remove.");

                    break;
                case "res":
                    if(words.length == 2) {
                        if (this.charsInRow >= this.maxCharsInRow && !isChanged) {
                            System.out.println("You are already using the max resolution.");
                            isChanged = true;
                        } else if (this.charsInRow <= this.minCharsInRow && !isChanged) {
                            System.out.println("You are already using the min resolution.");
                            isChanged = true;
                        } else {
                            resChange(words[1]);
                            System.out.println("Width set to " + charsInRow + ".");
                            isChanged = false;
                        }
                    }
                    else
                        System.out.println("Res command should be following with the type of resolution change.");

                    break;
                default:
                    System.out.println("Illegal input, please type again.");
                    break;
            }

            System.out.print(">>> ");

            cmd = scanner.nextLine().trim();
            words = cmd.split("\\s+");
        }
    }

    /**
     * shows the chars that were collected in the set
     */
    private void showChars(){
        charSet.stream().sorted().forEach(c-> System.out.print(c + " "));
        System.out.println("");
    }

    /**
     * parse the chars in the range that was given.
     * @param param the string to work on.
     * @return returns an array representing the chars range, null otherwise.
     */
    private static char[] parseCharRange(String param){
        if(param == null)
            return null;

        switch (param){
            case "space":
                return new char[]{' ', ' '};
            case "all":
                return new char[]{' ', '~'};
        }
        if (param.length() == 1)
            return new char[]{param.charAt(0), param.charAt(0)};

        if (param.length() == 3 && param.charAt(1) == '-'){
            if((int) param.charAt(0) > (int) param.charAt(2)){
                return new char[]{param.charAt(2), param.charAt(0)};
            }
            else return new char[]{param.charAt(0), param.charAt(2)};
        }

        return null;
    }


    /**
     * adds the chars that was given by the parser.
     * @param s string to work on.
     */
    private void addChars(String s){
        char[] range = parseCharRange(s);
        if(range != null){
            for (int i = (int) range[0]; i < range[1]+1; i++) {
                this.charSet.add((char)(i));
            }
        }
    }

    /**
     * removes the chars that was given by the parser.
     * @param s the string to work on.
     */
    private void removeChars(String s){
        char[] range = parseCharRange(s);
        if(range != null){
            for (int i = range[0]; i < range[1]+1; i++) {
                charSet.remove((char)(i));
            }
        }
    }

    /**
     * changing the rendering resolution.
     * @param s string to determine which change to do.
     */
    private void resChange(String s){
        if(s.equals("up") && this.charsInRow * 2 <= this.maxCharsInRow) {
            this.charsInRow *= 2;
        }
        else if (s.equals("down") && this.charsInRow / 2 >= this.minCharsInRow){
            this.charsInRow /= 2;
        }
    }

    /**
     * switches the console output boolean switch.
     */
    private void toConsole(){
        consoleOutput = true;
    }

    /**
     * render the image with the charset that has been collected in the chosen resolution and with
     * the console/html decision.
     */
    private void render(){
        int charSetSize = charSet.size();
        Character[] charSetArray = new Character[charSetSize];
        //convert the charSet to Array
        int i = 0;
        for(Character charToConvert : charSet){
            charSetArray[i] = charToConvert;
            i++;
        }

        char[][] chars = charMatcher.chooseChars(charsInRow, charSetArray);

        //in case of console rendering
        if(consoleOutput){
            consoleOutput = false;
            System.out.println(Arrays.deepToString(chars));
            return;
        }
        output.output(chars);
    }

}
