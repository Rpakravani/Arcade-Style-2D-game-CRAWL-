package ca.sfu.cmpt276.team7.ui;


import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import ca.sfu.cmpt276.team7.Game;
import ca.sfu.cmpt276.team7.board.Board; // for game loop 

/**
 * GameWindow opens the window, and starts the panel within it
 */
public class GameWindow {

	/**
     * Opens the game window and starts the UI event loop for the given game.
     *
     * @param game the game controller to run
     * @param board the board to render in the game panel
     */
	public static void start(Game game, Board board) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Dungeon Crawl");

			GamePanel panel = new GamePanel(game, board);

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(panel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			frame.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					game.handleInput(e.getKeyCode());
					panel.repaint();
				}
			});

			// === GAME LOOP ===
			Timer timer = new Timer(200, e -> {
				game.updateTick();
				panel.repaint();
			});

			timer.start();
		});
	}
}
