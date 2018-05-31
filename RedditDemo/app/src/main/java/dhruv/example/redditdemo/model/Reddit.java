package dhruv.example.redditdemo.model;

public class Reddit {
    int score;
    String author;
    String title;

    public Reddit(int score, String author, String title) {
        this.score = score;
        this.author = author;
        this.title = title;
    }

    public int getScore() {
        return score;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }
}
