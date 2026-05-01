package ca.sfu.cmpt276.team7.board;

import ca.sfu.cmpt276.team7.cells.BarrierCell;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.cells.WallCell;
import ca.sfu.cmpt276.team7.core.Position;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads a {@link Board} from a simple ASCII map file
 * <p>
 * Legend:
 * <ul>
 *   <li># = Wall</li>
 *   <li>. = Floor</li>
 *   <li>B = Barrier</li>
 *   <li>S = Start (stored as FloorCell + board.setStartPosition)</li>
 *   <li>E = Exit  (stored as FloorCell + board.setEndPosition)</li>
 *   <li>K = Key marker (stored as FloorCell; position added to keyPositions)</li>
 *   <li>T = Trap marker (stored as FloorCell; position added to trapPositions)</li>
 *   <li>G = Goblin spawn marker (stored as FloorCell; position added to goblinSpawns)</li>
 *   <li>O = Ogre spawn marker (stored as FloorCell; position added to ogreSpawns)</li>
 * </ul>
 *
 * <p>
 * Note: 
 * This loader does NOT create reward/punishment objects (i.e. Key or Trap objects/cells)
 * It only builds the Board's structural grid (walls, floors, barriers) and records sepcial positions (keys, traps, spawns, start, and end as seen in legend)
 * Gameplay systems can later interpret markers and place rewards/traps/enemies
 * TLDR: the loader does not create the actual key, treasure, or trap objects/cells. It just remembers where they should go later
 * </p>
 */
public final class BoardLoader {

    /**
     * Container for the loaded Board plus marker/spawn positions.
     * <p>
     * the load method needs to return a lot more than just a Board
     * so we return a Result object that contains:
     *  - the board (the grid of Cells)
     *  - list of marker positions: goblin spawns, ogre spawns, key positions, trap positions 
     * TLDR: result contains the board and all the special coordinates we might need later 
     * </p>
     */
    public static final class Result { // static nested classes belong to the class whereas inner classes belong to an object of the class 
        private final Board board;
        private final List<Position> goblinSpawns;
        private final List<Position> ogreSpawns;
        private final List<Position> keyPositions;
        private final List<Position> trapPositions;

        /**
         * @param board loaded board
         * @param goblinSpawns goblin spawn marker positions
         * @param ogreSpawns ogre spawn marker positions
         * @param keyPositions key marker positions
         * @param trapPositions trap marker positions
         */
        public Result(Board board,
                      List<Position> goblinSpawns,
                      List<Position> ogreSpawns,
                      List<Position> keyPositions,
                      List<Position> trapPositions) {
            this.board = board;
            this.goblinSpawns = goblinSpawns;
            this.ogreSpawns = ogreSpawns;
            this.keyPositions = keyPositions;
            this.trapPositions = trapPositions;
        }

        /** @return loaded board */
        public Board getBoard() { return board; }

        /** @return goblin spawn marker positions */
        public List<Position> getGoblinSpawns() { return goblinSpawns; }

        /** @return ogre spawn marker positions */
        public List<Position> getOgreSpawns() { return ogreSpawns; }

        /** @return key marker positions */
        public List<Position> getKeyPositions() { return keyPositions; }

        /** @return trap marker positions */
        public List<Position> getTrapPositions() { return trapPositions; }
    }

    /**
     * prevents cilent from doing "new BoardLoader()"
     * because this BoardLoader class is only meant to be used like: BoardLoader.load(path) 
     */
    private BoardLoader() { 
        // Utility class: no instances, should not be instantiated 
    }

    /**
     * Loads a Board from an ASCII map file
     *
     * <p>This is the real loader; it contains many phases:</p>
     *
     * <p><b>Phase 1: validate i/p and read file:</b></p>
     * <ul>
     *   <li>reject null path</li>
     *   <li>read file lines and ignore blank lines</li>
     *   <li>reject empty map</li>
     * </ul>
     *
     * <p><b>Phase 2: validate shape (rectangle):</b></p>
     * <ul>
     *   <li>height = number of lines</li>
     *   <li>width = length of first line</li>
     *   <li>ensure every line has the same width, so that the grid is a rectangle</li>
     * </ul>
     *
     * <p><b>Phase 3: prepare storage:</b></p>
     * <ul>
     *   <li>create Cell[][] grid to hold the final board cells</li>
     *   <li>create start and end positions (initially null so you can detect missing ones)</li>
     *   <li>create list for marker positions</li>
     * </ul>
     *
     * <p><b>Phase 4: parse every character into either:</b></p>
     * <ul>
     *   <li>a real structural Cell (wall, barrier, floor) and/or</li>
     *   <li>a recorded marker position (key, trap, spawn) and/or</li>
     *   <li>start/end position values</li>
     * </ul>
     *
     * <p><b>Mechanically:</b></p>
     * <pre>
     * - loop over y (rows)
     * - loop over x (columns)
     * - read character c
     * - create Position(x, y)
     * - decide what cell to place in the grid using switch(c)
     * - if it's a marker character (J, T, G, O) add position to the relevant list
     * - store cell in grid[y][x]
     * </pre>
     *
     * <p><b>Important design choice to be aware of:</b>
     * markers like K, T, G, O become FloorCell in the grid.
     * So, the tile is walkable floor structurally, but later game logic
     * will interpret it as containing something.</p>
     *
     * <p><b>Phase 5: post check required markers:</b></p>
     * <ul>
     *   <li>if start or end never got set, throw error</li>
     * </ul>
     *
     * <p><b>Phase 6: build board + apply metadata:</b></p>
     * <pre>
     * - Board board = new Board(grid)
     * - board.setStartPosition(start)
     * - board.setEndPosition(end)
     * - return everything in new Result(...)
     * </pre>
     *
     * @param mapPath path to the map file
     * @return result containing board + marker lists
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if map is invalid (non-rectangular, missing S/E, unknown chars)
     */
    public static Result load(Path mapPath) throws IOException { // throws IOException if something goes wrong during file or stream operations
        if (mapPath == null) throw new IllegalArgumentException("mapPath cannot be null"); // fail if caller passed null instead of a path

        List<String> lines = readNonEmptyLines(mapPath); // reads file lines while skipping blank lines and trimming trailing spaces
        if (lines.isEmpty()) throw new IllegalArgumentException("Map file is empty"); // if there's nothing left after skipping blanks the map is not valid 

        int height = lines.size(); // num rows in text file 
        int width = lines.get(0).length(); // num of columns aka length of first row string

        for (int i = 0; i < lines.size(); i++) { // check rectangular grid constraint, ensure every row has the same width 
            if (lines.get(i).length() != width) { throw new IllegalArgumentException( "Map must be rectangular. Line " + (i + 1) + " has different length"); }
        }

        Cell[][] grid = new Cell[height][width]; // actual board grid: grid[y][x] holds a cell object for that tile 

        // will store the coordinates of S and E as we find them 
        // keeping them null initially lets us detect if we they were inproperly set later 
        Position start = null;
        Position end = null;

        // these lists store marker coordinates discovered during parsing 
        // later game systems will use these coordinates to place enemies/items/traps 
        List<Position> goblinSpawns = new ArrayList<Position>();
        List<Position> ogreSpawns = new ArrayList<Position>();
        List<Position> keyPositions = new ArrayList<Position>();
        List<Position> trapPositions = new ArrayList<Position>();

        // traverse the ASCII map row by row (y) and column by column (x)
        for (int y = 0; y < height; y++) {
            String row = lines.get(y);

            for (int x = 0; x < width; x++) {
                char c = row.charAt(x); // extract the map character at (x,y)
                Position pos = new Position(x, y); // position is your own class representing coordinates (x,y) on the board 

                Cell cell;
                switch (c) {
                    case '#': // # = wall
                        cell = new WallCell(pos); 
                        break;
                    case 'B': // B = barrier
                        cell = new BarrierCell(pos); 
                        break;
                    case '.': // . = walkable tile 
                        cell = new FloorCell(pos); 
                        break;

                    case 'S': // S = start position (need to record) (strcturally behaves like a walkable tile)
                        start = pos;
                        cell = new FloorCell(pos);
                        break;

                    case 'E': // E = exit position (need to record) (strcturally behaves like a walkable tile, maybe change later)
                        end = pos;
                        cell = new FloorCell(pos); 
                        break;

                    case 'K': // key marker: record the coordinate for later, and the tile is walkable 
                        keyPositions.add(pos);
                        cell = new FloorCell(pos);
                        break;

                    case 'T': // T = trap, do same pattern as key
                        trapPositions.add(pos);
                        cell = new FloorCell(pos);
                        break;

                    case 'G': // G = goblin, record coordinate for later enemy spawning 
                        goblinSpawns.add(pos);
                        cell = new FloorCell(pos);
                        break;

                    case 'O': // O = Ogre spawn marker 
                        ogreSpawns.add(pos);
                        cell = new FloorCell(pos);
                        break;

                    default: // if the map has a character that was not defined in legend (and not a switch case) throw error 
                        throw new IllegalArgumentException("Unknown map char '" + c + "' at (" + x + "," + y + ")");
                }

                grid[y][x] = cell; // store the build cell in the grid 
            }
        }

        // if we never encountered S or E the map is invalid for gameplay 
        if (start == null) throw new IllegalArgumentException("Map missing start 'S'");
        if (end == null) throw new IllegalArgumentException("Map missing exit 'E'");

        Board board = new Board(grid); // create the board object using the structural grid built 

        //save the start and end positions into the board's metha data 
        board.setStartPosition(start);
        board.setEndPosition(end);

        return new Result(board, goblinSpawns, ogreSpawns, keyPositions, trapPositions); // return Board and marker positions so other systems can use em 
    }

    /**
     * Reads all non-empty lines from the map file
     * <p>
     * reads the map file line by line
     * skips lines that are empty/whitespace
     * applies rstrip so trailing spaces don't mess up the rectangular width constraint for grid 
     * returns clean List<string> 
     * </P>
     * @param mapPath map file path
     * @return list of non-empty lines
     * @throws IOException if file cannot be read
     */
    private static List<String> readNonEmptyLines(Path mapPath) throws IOException { // throws IOException if something goes wrong during file or stream operations
        List<String> lines = new ArrayList<String>();

        BufferedReader br = Files.newBufferedReader(mapPath); // Files.newBufferedReader(Path) creates a BufferedReader for the file at mapPath 
        try {
            String line;

            while ((line = br.readLine()) != null) { // br.readline() reads one line at a time until it returns null (end of file)
                if (!line.trim().isEmpty()) { // line.trim().isEmpty() checks if the line is all white space, if it's not empty we keep it
                    lines.add(rstrip(line)); // rstrip removes trailing spaces so the map widths are consistent 
                }
            }
        } finally { // ensure file handle is closed even if an exception occurs while reading it 
            br.close();
        }
        return lines;
    }

    /**
     * Removes trailing spaces without destroying leading spaces (if any)
     * we need this because if someone edits the map and accidentally leaves spaces at the end of lines we don't want the map width to look longer and fail the rectangular check
     *
     * @param s input string
     * @return string without trailing whitespace
     */
    private static String rstrip(String s) {
        int end = s.length();

        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) { // walk backward until we find a non whitespace char 
            end--;
        }
        return s.substring(0, end); // substring(0, end) returns the string without those trailing whitespace chars
    }
}