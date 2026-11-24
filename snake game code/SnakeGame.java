import java.util.*;
import java.io.*;

public class SnakeGame{

    static final int ROWS = 10;
    static final int COLS = 5;

    static Scanner sc = new Scanner(System.in);
    static Random random = new Random();

    
    static class Point {

        int row, col;

        Point(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public boolean equals(Object obj) {

            if (!(obj instanceof Point))
                return false;

            Point p = (Point) obj;

            return row == p.row && col == p.col;
        }

        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    
    static class ScoreEntry {

        String player;
        int score;

        ScoreEntry(String player, int score) {
            this.player = player;
            this.score = score;
        }
    }

    // ==============================
    // SNAKE CLASS
    // ==============================

    static class Snake {

        // DEQUE
        Deque<Point> body = new ArrayDeque<>();

        // HASHING
        HashSet<Point> cells = new HashSet<>();

        char direction = 'D';

        Snake() {

            body.addFirst(new Point(5, 3));
            body.addLast(new Point(5, 2));
            body.addLast(new Point(5, 1));

            cells.addAll(body);
        }

        Point getHead() {
            return body.peekFirst();
        }

        Point getTail() {
            return body.peekLast();
        }

        boolean occupies(Point p) {
            return cells.contains(p);
        }

        int getLength() {
            return body.size();
        }

        boolean setDirection(char d) {

            if (direction == 'W' && d == 'S')
                return false;

            if (direction == 'S' && d == 'W')
                return false;

            if (direction == 'A' && d == 'D')
                return false;

            if (direction == 'D' && d == 'A')
                return false;

            direction = d;

            return true;
        }

        Point nextPosition() {

            Point head = getHead();

            int r = head.row;
            int c = head.col;

            switch (direction) {

                case 'W':
                    r--;
                    break;

                case 'S':
                    r++;
                    break;

                case 'A':
                    c--;
                    break;

                case 'D':
                    c++;
                    break;
            }

            return new Point(r, c);
        }

        boolean move(Point next, boolean grow) {

            Point tail = getTail();

            if (cells.contains(next) &&
                !(next.equals(tail) && !grow)) {

                return false;
            }

            body.addFirst(next);
            cells.add(next);

            if (!grow) {

                Point removed = body.removeLast();

                cells.remove(removed);
            }

            return true;
        }
    }

    
    static class ScoreBoard {

        ArrayList<ScoreEntry> scores = new ArrayList<>();

        String FILE_NAME = "highscores.txt";

        

        void addScore(String name, int score) {

            if (name.trim().isEmpty()) {
                System.out.println("Invalid player name!");
                return;
            }

            scores.add(new ScoreEntry(name, score));

            sortScores();

            saveScores();
        }

        

        void sortScores() {

            Collections.sort(scores,
                (a, b) -> Integer.compare(b.score, a.score));
        }

        

        void displayScores() {

            sortScores();

            System.out.println("\n===== HIGH SCORES =====");

            if (scores.isEmpty()) {
                System.out.println("No scores available.");
                return;
            }

            for (int i = 0; i < scores.size(); i++) {

                ScoreEntry s = scores.get(i);

                System.out.println(
                    (i + 1) + ". " +
                    s.player + " | " + s.score
                );
            }
        }

        

        void searchPlayer(String name) {

            boolean found = false;

            for (int i = 0; i < scores.size(); i++) {

                ScoreEntry s = scores.get(i);

                if (s.player.toLowerCase()
                    .contains(name.toLowerCase())) {

                    System.out.println(
                        "Found: " + s.player +
                        " | Score: " + s.score
                    );

                    found = true;
                }
            }

            if (!found) {
                System.out.println("Player not found.");
            }
        }

        

        void saveScores() {

            try {

                BufferedWriter bw =
                    new BufferedWriter(
                        new FileWriter(FILE_NAME)
                    );

                for (ScoreEntry s : scores) {

                    bw.write(s.player + "|" + s.score);

                    bw.newLine();
                }

                bw.close();

            } catch (IOException e) {

                System.out.println("Error saving scores.");
            }
        }

        

        void loadScores() {

            File file = new File(FILE_NAME);

            if (!file.exists())
                return;

            try {

                BufferedReader br =
                    new BufferedReader(
                        new FileReader(FILE_NAME)
                    );

                String line;

                while ((line = br.readLine()) != null) {

                    String[] data = line.split("\\|");

                    if (data.length == 2) {

                        scores.add(
                            new ScoreEntry(
                                data[0],
                                Integer.parseInt(data[1])
                            )
                        );
                    }
                }

                br.close();

                sortScores();

            } catch (Exception e) {

                System.out.println("Error loading scores.");
            }
        }
    }

    
    static class Game {

        Snake snake;

        Point food;

        int score = 0;

        Game() {

            snake = new Snake();

            placeFood();
        }

        

        void placeFood() {

            ArrayList<Point> freeCells = new ArrayList<>();

            for (int i = 0; i < ROWS; i++) {

                for (int j = 0; j < COLS; j++) {

                    Point p = new Point(i, j);

                    if (!snake.occupies(p)) {
                        freeCells.add(p);
                    }
                }
            }

            if (freeCells.isEmpty()) {
                food = null;
                return;
            }

            food = freeCells.get(
                random.nextInt(freeCells.size())
            );
        }

        

        void drawBoard() {

            char[][] board = new char[ROWS][COLS];

            for (int i = 0; i < ROWS; i++) {

                Arrays.fill(board[i], ' ');
            }

            if (food != null) {
                board[food.row][food.col] = '*';
            }

            for (Point p : snake.body) {

                board[p.row][p.col] = 'o';
            }

            Point head = snake.getHead();

            board[head.row][head.col] = '@';

            System.out.println("\nScore: " + score);

            System.out.println("+----------+");

            for (int i = 0; i < ROWS; i++) {

                System.out.print("|");

                for (int j = 0; j < COLS; j++) {

                    System.out.print(board[i][j] + " ");
                }

                System.out.println("|");
            }

            System.out.println("+----------+");
        }

        

        void play(String player, ScoreBoard sb) {

            boolean gameOver = false;

            while (!gameOver) {

                drawBoard();

                if (food == null) {
                    System.out.println("You won! Board completed.");
                    break;
                }

                System.out.print(
                    "Enter move (W/A/S/D) or Q to quit: "
                );

                String input = sc.nextLine()
                                 .trim()
                                 .toUpperCase();

                if (input.isEmpty()) {
                    System.out.println("Invalid input.");
                    continue;
                }

                char move = input.charAt(0);

                if (move == 'Q')
                    break;

                if ("WASD".indexOf(move) == -1) {

                    System.out.println("Invalid input.");
                    continue;
                }

                if (!snake.setDirection(move)) {

                    System.out.println(
                        "Cannot reverse direction!"
                    );

                    continue;
                }

                Point next = snake.nextPosition();

                

                if (next.row < 0 ||
                    next.row >= ROWS ||
                    next.col < 0 ||
                    next.col >= COLS) {

                    System.out.println("Game Over! Wall collision.");

                    gameOver = true;

                    break;
                }

                

                boolean grow = next.equals(food);

                

                if (!snake.move(next, grow)) {

                    System.out.println("Game Over! Snake collision.");

                    gameOver = true;

                    break;
                }

                

                if (grow) {

                    score += 10;

                    System.out.println("Food eaten! +10");

                    placeFood();
                }
            }

            System.out.println("\nFinal Score: " + score);

            sb.addScore(player, score);
        }
    }

    

    public static void main(String[] args) {

        ScoreBoard sb = new ScoreBoard();

        sb.loadScores();

        while (true) {

            System.out.println("\n====== SNAKE GAME ======");

            System.out.println("1. Start Game");

            System.out.println("2. View High Scores");

            System.out.println("3. Search Player");

            System.out.println("4. Exit");

            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {

                case "1":

                    System.out.print("Enter player name: ");

                    String name = sc.nextLine().trim();

                    if (name.isEmpty()) {

                        System.out.println("Name cannot be empty.");

                        break;
                    }

                    Game game = new Game();

                    game.play(name, sb);

                    break;

                case "2":

                    sb.displayScores();

                    break;

                case "3":

                    System.out.print("Enter player name: ");

                    String search = sc.nextLine();

                    sb.searchPlayer(search);

                    break;

                case "4":

                    sb.saveScores();

                    System.out.println("Thank you for playing!");

                    return;

                default:

                    System.out.println("Invalid choice!");
            }
        }
    }
}