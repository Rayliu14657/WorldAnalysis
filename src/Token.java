public class Token {
    public String word;

    public int number;
    public String type;

    public Token() {
        this.word=null;
        this.number=-1;
        this.type=null;
    }

    public Token(String word, int number, String type) {
        this.word = word;
        this.number = number;
        this.type = type;
    }

}
