package sgf.model.game;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import sgf.utilities.Pair;


/**
 * Class that managed the {@link Leaderboard}.
 */
public class LeaderboardImpl implements Leaderboard {
    private final Map<String, Pair<String, Integer>> mapScore = new HashMap<>();
    private final Path p = FileSystems.getDefault().getPath("res" + File.separator + "classification.json");

    @Override
    public Map<String, Pair<String, Integer>> getMapScore() {
        return this.mapScore;
    }

    @Override
    public Path getPath() {
        return this.p;
    }

    @Override
    public void addRecord(final String date, final String name, final int score) {
        this.mapScore.put(date, new Pair<>(name, score));
    }
}
