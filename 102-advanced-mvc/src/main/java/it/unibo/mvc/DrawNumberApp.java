package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;

    private int min = MIN;
    private int max = MAX;
    private int attempts = ATTEMPTS;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        String fileName = "config.yml";
        InputStream in = ClassLoader.getSystemResourceAsStream(fileName);

        if (in == null){
            throw new IllegalStateException("Impossibile trovare il file: " + fileName);
        }

        try (
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader buffReader = new BufferedReader(reader);
        ) {
            String line;
            while ((line = buffReader.readLine()) != null){
                StringTokenizer tokenizer = new StringTokenizer(line, ":");
        
                if(tokenizer.hasMoreTokens()){
                    String title = tokenizer.nextToken().trim();
                    String value = tokenizer.nextToken().trim();

                    if(title == "minimum"){
                        min = Integer.parseInt(value);
                    }
                    else if(title == "maximum"){
                        max = Integer.parseInt(value);
                    }
                    else{
                        attempts = Integer.parseInt(value);
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        this.model = new DrawNumberImpl(min, max, attempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
