package ca.sfu.cmpt276.team7.unit.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.cells.WallCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.ui.RenderItem;
import ca.sfu.cmpt276.team7.ui.RenderKind;
import ca.sfu.cmpt276.team7.ui.SheetId;

public class UiTestSupport {
    private UiTestSupport() {};

    public static Board makeSimpleBoard(int width, int height) {
        Cell[][] grid = new Cell[height][width];

        Position start = new Position(0, 1);
        Position end = new Position(width - 1, height - 1);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position pos = new Position(x, y);

                // Build a simple test map with walls on the border,
                // while keeping the start and exit-related positions open.
                if ((x == 0 || x == width - 1 || y == 0 || y == height - 1)
                    && !pos.equals(start) && !pos.equals(end)) {
                    grid[y][x] = new WallCell(new Position(x, y));
                } else {
                    grid[y][x] = new FloorCell(new Position(x, y));
                }
            }
        }

        Board board = new Board(grid);
        
        // Place the start near the left edge and the end near the lower-right side
        // so tests can use a predictable, mostly enclosed layout.
        board.setStartPosition(new Position(0, 1));
        board.setEndPosition(new Position(width - 1, height - 2));

        return board;
    }

    public static List<String> getOnlyTexts(List<RenderItem> items) {
        List<String> texts = new ArrayList<>();

        // Extract only text render items so tests can assert displayed messages
        // without checking unrelated sprites.
        for (RenderItem item : items) {
            if (item.getKind() == RenderKind.TEXT) {
                texts.add(item.getText());
            }
        }

        return texts;
    }

    public static class FakeClock implements LongSupplier {
        private long nowMs = 0L;

        @Override
        public long getAsLong() {
            return nowMs;
        }

        public void advanceMs(long ms) {
            // Manually advance time to make time-based UI behavior deterministic in tests.
            nowMs += ms;
        }
    }


    private static record SpriteSpec(SheetId sheetId, int srcX, int srcY) {}

    private static final int cellWidth = 50;
    private static final int cellHeight = 50;

    private static final int gameSrcSize = 64;
    private static final int screenSrcSize = 200;
    private static final int srcPadding = 5;

    private static int srcSize(int order, int srcSize) {
        // Convert sprite order in the atlas into the actual source coordinate,
        // accounting for padding between sprite images.
        return ((srcSize + (srcPadding * 2)) * order) + srcPadding;
    }

    public static final SpriteSpec playerSprite = new SpriteSpec(SheetId.GAME_ATLAS, srcSize(7, gameSrcSize), srcPadding);
    public static final SpriteSpec goblinSprite = new SpriteSpec(SheetId.GAME_ATLAS, srcSize(6, gameSrcSize), srcPadding);
    public static final SpriteSpec ogreSprite = new SpriteSpec(SheetId.GAME_ATLAS, srcSize(5, gameSrcSize), srcPadding);

    public static final SpriteSpec wallSprite = new SpriteSpec(SheetId.GAME_ATLAS, srcSize(1, gameSrcSize), srcPadding);
    public static final SpriteSpec barrierSprite = new SpriteSpec(SheetId.GAME_ATLAS, srcSize(1, gameSrcSize), srcPadding);
    public static final SpriteSpec keySprite = new SpriteSpec(SheetId.GAME_ATLAS, srcSize(3, gameSrcSize), srcPadding);
    public static final SpriteSpec chestSprite = new SpriteSpec(SheetId.GAME_ATLAS, srcSize(4, gameSrcSize), srcPadding);
    public static final SpriteSpec trapSprite = new SpriteSpec(SheetId.GAME_ATLAS, srcSize(2, gameSrcSize), srcPadding);

    public static boolean containsSprite(List<RenderItem> items, SpriteSpec sprite) {
        // Check whether the expected sprite appears anywhere in the rendered output.
        for (RenderItem item : items) {
            if (item.getKind() == RenderKind.SPRITE && item.getSheetId() == sprite.sheetId()
                && item.getSrcX() == sprite.srcX() && item.getSrcY() == sprite.srcY()) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsSpriteAt(List<RenderItem> items, SpriteSpec sprite, int x, int y) {
        int expectedX = x * cellWidth;
        int expectedY = y * cellHeight;

        // Check whether the expected sprite is rendered at the specified grid cell.
        for (RenderItem item : items) {
            if (item.getKind() == RenderKind.SPRITE && item.getSheetId() == sprite.sheetId()
                && item.getSrcX() == sprite.srcX() && item.getSrcY() == sprite.srcY()
                && item.getX() == expectedX && item.getY() == expectedY) {
                return true;
            }
        }
        return false;
    }
}
