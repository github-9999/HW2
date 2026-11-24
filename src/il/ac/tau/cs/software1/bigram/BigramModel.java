package il.ac.tau.cs.software1.bigram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class BigramModel {
    public static final int MAX_VOCABULARY_SIZE = 14500;
    public static final String VOC_FILE_SUFFIX = ".voc";
    public static final String COUNTS_FILE_SUFFIX = ".counts";
    public static final String SOME_NUM = "some_num";
    public static final int ELEMENT_NOT_FOUND = -1;

    String[] mVocabulary;
    int[][] mBigramCounts;

    // DO NOT CHANGE THIS !!!
    public void initModel(String fileName) throws IOException {
        mVocabulary = buildVocabularyIndex(fileName);
        mBigramCounts = buildCountsArray(fileName, mVocabulary);
    }

    //////////////////////////////////////////////////////////////////////
    // Helper methods - already implemented for you. Use them!
    //////////////////////////////////////////////////////////////////////

    /*
     * Converts a single token (a whitespace-free piece of text from the file)
     * into the form in which it is stored in the vocabulary:
     * - a token with at least one English letter -> the token in lowercase
     * - a token made only of digits (an integer) -> SOME_NUM
     * - anything else is not a legal word -> null
     *
     * @pre: token != null
     */
    public static String normalizeWord(String token) {
        boolean hasLetter = false;
        boolean allDigits = !token.isEmpty();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                hasLetter = true;
            }
            if (c < '0' || c > '9') {
                allDigits = false;
            }
        }
        if (hasLetter) {
            return token.toLowerCase(Locale.ROOT);
        }
        if (allDigits) {
            return SOME_NUM;
        }
        return null;
    }

    /*
     * @pre: vocabulary != null
     * 
     * @post: $ret = the index of word in vocabulary, or ELEMENT_NOT_FOUND if
     * word is null or does not appear in it
     */
    public static int findIndex(String[] vocabulary, String word) {
        if (word == null) {
            return ELEMENT_NOT_FOUND;
        }
        for (int i = 0; i < vocabulary.length; i++) {
            if (word.equals(vocabulary[i])) {
                return i;
            }
        }
        return ELEMENT_NOT_FOUND;
    }

    //////////////////////////////////////////////////////////////////////
    // Questions 1 - 5: implement these
    //////////////////////////////////////////////////////////////////////

    /*
     * Splits a line into tokens (separated by whitespace). Returns an empty
     * array for an empty / whitespace-only line.
     */
    private static String[] splitToTokens(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return new String[0];
        }
        return trimmed.split("\\s+");
    }

    /*
     * Index of word among the first `size` cells of arr, or ELEMENT_NOT_FOUND.
     */
    private static int findIndexInPrefix(String[] arr, int size, String word) {
        for (int i = 0; i < size; i++) {
            if (word.equals(arr[i])) {
                return i;
            }
        }
        return ELEMENT_NOT_FOUND;
    }

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * 
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public String[] buildVocabularyIndex(String fileName) throws IOException { // Question 1
        String[] temp = new String[MAX_VOCABULARY_SIZE];
        int size = 0;

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        try {
            String line;
            while ((line = reader.readLine()) != null && size < MAX_VOCABULARY_SIZE) {
                String[] tokens = splitToTokens(line);
                for (int t = 0; t < tokens.length && size < MAX_VOCABULARY_SIZE; t++) {
                    String word = normalizeWord(tokens[t]);
                    if (word == null) {
                        continue;
                    }
                    if (findIndexInPrefix(temp, size, word) == ELEMENT_NOT_FOUND) {
                        temp[size] = word;
                        size++;
                    }
                }
            }
        } finally {
            reader.close();
        }

        String[] vocabulary = new String[size];
        for (int i = 0; i < size; i++) {
            vocabulary[i] = temp[i];
        }
        return vocabulary;
    }

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * 
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public int[][] buildCountsArray(String fileName, String[] vocabulary) throws IOException { // Question 2
        int[][] counts = new int[vocabulary.length][vocabulary.length];

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = splitToTokens(line);
                int prev = ELEMENT_NOT_FOUND; // a new line starts a new sentence
                for (int t = 0; t < tokens.length; t++) {
                    int cur = findIndex(vocabulary, normalizeWord(tokens[t]));
                    if (prev != ELEMENT_NOT_FOUND && cur != ELEMENT_NOT_FOUND) {
                        counts[prev][cur]++;
                    }
                    // if cur is not in the vocabulary, prev becomes -1: no pair across it
                    prev = cur;
                }
            }
        } finally {
            reader.close();
        }
        return counts;
    }

    /*
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @pre: fileName is a legal file path
     */
    public void saveModel(String fileName) throws IOException { // Question 3
        BufferedWriter vocWriter = new BufferedWriter(new FileWriter(fileName + VOC_FILE_SUFFIX));
        try {
            vocWriter.write(mVocabulary.length + " words\n");
            for (int i = 0; i < mVocabulary.length; i++) {
                vocWriter.write(i + "," + mVocabulary[i] + "\n");
            }
        } finally {
            vocWriter.close();
        }

        BufferedWriter countsWriter = new BufferedWriter(new FileWriter(fileName + COUNTS_FILE_SUFFIX));
        try {
            for (int i = 0; i < mBigramCounts.length; i++) {
                for (int j = 0; j < mBigramCounts[i].length; j++) {
                    if (mBigramCounts[i][j] != 0) {
                        countsWriter.write(i + "," + j + ":" + mBigramCounts[i][j] + "\n");
                    }
                }
            }
        } finally {
            countsWriter.close();
        }
    }

    /*
     * @pre: fileName is a legal file path
     */
    public void loadModel(String fileName) throws IOException { // Question 4
        String[] vocabulary;
        BufferedReader vocReader = new BufferedReader(new FileReader(fileName + VOC_FILE_SUFFIX));
        try {
            String first = vocReader.readLine().trim();
            int size = Integer.parseInt(first.split(" ")[0]);
            vocabulary = new String[size];
            for (int i = 0; i < size; i++) {
                String line = vocReader.readLine();
                // the word itself may contain commas, so split on the FIRST comma only
                int comma = line.indexOf(',');
                int index = Integer.parseInt(line.substring(0, comma));
                vocabulary[index] = line.substring(comma + 1);
            }
        } finally {
            vocReader.close();
        }

        int[][] counts = new int[vocabulary.length][vocabulary.length];
        BufferedReader countsReader = new BufferedReader(new FileReader(fileName + COUNTS_FILE_SUFFIX));
        try {
            String line;
            while ((line = countsReader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                int comma = line.indexOf(',');
                int colon = line.indexOf(':');
                int i = Integer.parseInt(line.substring(0, comma));
                int j = Integer.parseInt(line.substring(comma + 1, colon));
                int c = Integer.parseInt(line.substring(colon + 1).trim());
                counts[i][j] = c;
            }
        } finally {
            countsReader.close();
        }

        mVocabulary = vocabulary;
        mBigramCounts = counts;
    }

    /*
     * @pre: sentence is in lowercase
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @pre: each two words in the sentence are separated with a single space
     * 
     * @post: if sentence is probable, according to the model, $ret = true, else,
     * $ret = false
     */
    public boolean isLegalSentence(String sentence) { // Question 5
        if (sentence.isEmpty()) {
            return true;
        }
        String[] words = sentence.split(" ");
        if (getWordIndex(words[0]) == ELEMENT_NOT_FOUND) {
            return false;
        }
        for (int i = 0; i + 1 < words.length; i++) {
            // getBigramCount returns 0 if either word is not in the vocabulary
            if (getBigramCount(words[i], words[i + 1]) == 0) {
                return false;
            }
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////
    // Queries on the model - already implemented for you.
    // They start working once Questions 1 - 2 are done.
    //////////////////////////////////////////////////////////////////////

    /*
     * @pre: word is in lowercase
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @post: $ret = -1 if word is not in vocabulary, otherwise $ret = the index of
     * word in vocabulary
     */
    public int getWordIndex(String word) {
        return findIndex(mVocabulary, word);
    }

    /*
     * @pre: word1, word2 are in lowercase
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @post: $ret = the count for the bigram <word1, word2>. if one of the words
     * does not
     * exist in the vocabulary, $ret = 0
     */
    public int getBigramCount(String word1, String word2) {
        int index1 = getWordIndex(word1);
        int index2 = getWordIndex(word2);
        if (index1 == ELEMENT_NOT_FOUND || index2 == ELEMENT_NOT_FOUND) {
            return 0;
        }
        return mBigramCounts[index1][index2];
    }

    /*
     * @pre: word is in lowercase, and is in mVocabulary
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @post: $ret = the word with the lowest vocabulary index that appears most
     * frequently after word
     * (if a bigram starting with word was never seen, $ret will be null)
     */
    public String getMostFrequentProceeding(String word) {
        int[] row = mBigramCounts[getWordIndex(word)];
        int best = ELEMENT_NOT_FOUND;
        for (int i = 0; i < row.length; i++) {
            if (row[i] > 0 && (best == ELEMENT_NOT_FOUND || row[i] > row[best])) {
                best = i;
            }
        }
        return best == ELEMENT_NOT_FOUND ? null : mVocabulary[best];
    }

    /*
     * @pre: arr1.length = arr2.length
     * 
     * @post: if arr1 or arr2 are only filled with zeros, $ret = -1, otherwise
     * calculates CosineSim
     */
    public static double calcCosineSim(int[] arr1, int[] arr2) {
        double dot = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (int i = 0; i < arr1.length; i++) {
            dot += (double) arr1[i] * arr2[i];
            norm1 += (double) arr1[i] * arr1[i];
            norm2 += (double) arr2[i] * arr2[i];
        }
        if (norm1 == 0 || norm2 == 0) {
            return -1;
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /*
     * @pre: word is in vocabulary
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @post: $ret = w implies that w is the word with the largest
     * cosineSimilarity(vector for word, vector for w)
     * among all the other words in vocabulary (the lowest index wins a tie)
     */
    public String getClosestWord(String word) {
        int wordIndex = getWordIndex(word);
        int best = ELEMENT_NOT_FOUND;
        double bestSim = 0;
        for (int i = 0; i < mVocabulary.length; i++) {
            if (i == wordIndex) {
                continue;
            }
            double sim = calcCosineSim(mBigramCounts[wordIndex], mBigramCounts[i]);
            if (best == ELEMENT_NOT_FOUND || sim > bestSim) {
                best = i;
                bestSim = sim;
            }
        }
        // A vocabulary with a single word: the closest word is the word itself
        return best == ELEMENT_NOT_FOUND ? word : mVocabulary[best];
    }
}
