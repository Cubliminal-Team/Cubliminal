package net.limit.cubliminal.event.backrooms.skindatabase;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.Arrays;
import java.util.List;

public class PlayerMessageProcessor {
    /**
     * Process the message and return the best message intent,
     *
     * @param message player message
     * @return {@link ProcessedMessage} or {@code null} if the message doesn't have an intent
     */
    public static ProcessedMessage process(Text message) {
        String content = message.getString();

        Cubliminal.LOGGER.info("Processing Player Message: '%s'".formatted(content));

        Intent bestIntent = null;
        double bestScore = 0.0;

        for (Intent intent : Intent.values()) {
            for (String phrase : intent.getPhrases()) {
                double score = testMessage(content, phrase);

                if (score >= intent.getThreshold() && score > bestScore) {
                    bestScore = score;
                    bestIntent = intent;
                }
            }
        }

        Cubliminal.LOGGER.info("Best score: '%s'".formatted(bestScore));
        Cubliminal.LOGGER.info("Best intent: '%s'".formatted(bestIntent));

        if (bestIntent == null) return null;

        return new ProcessedMessage(bestIntent, message.copy());
    }

    private static double testMessage(String message, String phrase) {
        message = message.toLowerCase().trim();
        phrase = phrase.toLowerCase().trim();

        String[] words = message.split("\\s+");
        String[] phraseWords = phrase.split("\\s+");

        double maxScore = 0.0;

        // Test #1 - test typos
        for (int i = 0; i < words.length; i++) {
            int end = Math.min(i + phraseWords.length, words.length);
            String window = String.join(" ", Arrays.asList(words).subList(i, end));
            double score = similarity(window, phrase);
            maxScore = Math.max(maxScore, score);
        }

        // Test #2 - test by words
        double wordScoreSum = 0.0;
        for (String pw : phraseWords) {
            double maxWordScore = 0.0;
            for (String w : words) {
                double wordScore = similarity(w, pw);
                maxWordScore = Math.max(maxWordScore, wordScore);
            }
            wordScoreSum += maxWordScore;
        }

        double avgWordScore = wordScoreSum / phraseWords.length;
        maxScore = Math.max(maxScore, avgWordScore);

        return maxScore;
    }

    /**
     * Score between [0, 1]. 1 is the same, 0 entirely different
     *
     * @param a string a
     * @param b string b
     * @return score
     */
    private static double similarity(String a, String b) {
        int maxLength = Math.max(a.length(), b.length());

        if (maxLength == 0) return 1.0;

        int dist = levenshtein(a.toLowerCase(), b.toLowerCase());
        return 1.0 - (dist / (double) maxLength);
    }

    // https://en.wikipedia.org/wiki/Levenshtein_distance
    private static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j - 1],
                            Math.min(
                                    dp[i - 1][j],
                                    dp[i][j - 1]
                            )
                    );
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    public record ProcessedMessage(PlayerMessageProcessor.Intent intent, Text message) {
        public static final Codec<ProcessedMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PlayerMessageProcessor.Intent.CODEC.fieldOf("intent").forGetter(ProcessedMessage::intent),
                TextCodecs.CODEC.fieldOf("content").forGetter(ProcessedMessage::message)
        ).apply(instance, ProcessedMessage::new));
    }

    public enum Intent {
        GREETING(
                "greeting",
                0.75f,
                "hello", "hi there", "hey friend", "hello there", "hiya", "howdy", "hey mate", "hola"
        ),
        COME_HERE(
                "come_here",
                0.75f,
                "come here", "come closer", "follow me", "get over here", "this way"
        ),
        HELP(
                "help",
                0.75f,
                "help me", "please help", "i need help", "can you help me", "i'm stuck"
        ),
        TRUST(
                "trust",
                0.75f,
                "i'm human", "i'm a survivor", "don't be afraid", "it's just me", "i'm your friend"
        )
        ;

        public static final Codec<Intent> CODEC = Codec.STRING.comapFlatMap(
                s -> {
                    Intent intent = fromId(s);
                    if (intent == null) {
                        return DataResult.error(() -> "Not a valid intent: " + s);
                    }

                    return DataResult.success(intent);
                },
                Intent::getId
        );

        private final String id;
        private final float threshold;
        private final List<String> phrases;

        Intent(String id, float threshold, String... phrases) {

            this.id = id;
            this.threshold = threshold;
            this.phrases = Arrays.stream(phrases).toList();
        }

        public String getId() {
            return id;
        }

        public float getThreshold() {
            return threshold;
        }

        public List<String> getPhrases() {
            return phrases;
        }

        public static Intent fromId(String id) {
            return Arrays.stream(values()).filter(intent -> intent.getId().equals(id)).findFirst().orElse(null);
        }
    }
}
