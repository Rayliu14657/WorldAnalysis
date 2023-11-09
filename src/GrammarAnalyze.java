import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrammarAnalyze {


    public GrammarAnalyze() {
    }

    List<Token> subToken(List<Token> tokens, int str, int end){
        List<Token>subToken = new ArrayList<>();
        for(int i=str;i<=end;i++){
           subToken.add(tokens.get(i));
        }
        return subToken;
    }

    //检查表达式
    boolean checkE(List<Token> tokens) {
        Token token = new Token();
        tokens.add(0, token);
        tokens.add(token);
        int size = tokens.size();
        size = size - 1;
        boolean flag = true;
        for (int i = 0; i < size; i++) {
            int next = i + 1;
            token = tokens.get(i);
            if (token.type == null) {
                flag = emptyFirst(tokens.get(next));
            } else if (token.type.equals("标识符")) {
                flag = symbolFirst(tokens.get(next));
            } else if (token.type.equals("运算符")) {
                flag = signFirst(tokens.get(next));
            } else if (token.type.equals("常数")) {
                flag = constFirst(tokens.get(next));
            }
            if (!flag) {
                if (token.word != null) {
                    System.out.println("出错！错误位于：" + token.word);
                }

                return false;
            }
        }
        return true;
    }

    boolean checkAssign(List<Token> tokens){
        if(tokens.get(0).type.equals("标识符")){
            if(tokens.get(1).word.equals(":=")){
                int tokenLength = tokens.size();
                List<Token>expToken = subToken(tokens,2,tokenLength-1);
                return checkE(expToken);
            }
        }
        return false;
    }

    int checkIF(List<Token> tokens) {
        Token token = tokens.get(0);
        if (token == null) {
            System.out.println("当前语句为空");
            return -1;
        } else if ("if".equals(token.word)) {
            int len = tokens.size();
            List<Token> condition = new ArrayList<>();
            int index = 0;
            for (int i = 1; i < len; i++) {
                Token tokenNow = tokens.get(i);
                if (tokenNow.word.equals("then")) {
                    index = i;
                    break;
                }
                condition.add(tokenNow);
            }
            if (condition.size() != 1 || !Objects.equals(condition.get(0).type, "标识符")) {
                System.out.println("if语句条件出错");
                return -1;
            } else {
                List<Token> assign = new ArrayList<>();
                int index_else = 0;
                for (int i = index + 1; i < len; i++) {
                    Token tokenNow = tokens.get(i);
                    if (tokenNow.word.equals("else")) {
                        index_else = i;
                        break;
                    }
                    assign.add(tokenNow);
                }
                Token addToken = new Token();
                addToken.word = ";";
                addToken.type = "分界符";
                if(index_else!=0){
                    assign.add(addToken);
                }
                boolean assignFlag = checkE(assign);
                if (assignFlag) {
                    List<Token> assign_else = new ArrayList<>();
                    if (index_else == 0) {
                        System.out.println("if-then分支语句，嵌套赋值语句");
                        return 1;
                    } else {
                        for (int i = index_else + 1; i < len; i++) {
                            Token tokenNow = tokens.get(i);
                            assign_else.add(tokenNow);
                        }
                        boolean elseFlag = checkE(assign_else);
                        if (elseFlag) {
                            System.out.println("if-then-else分支语句，嵌套赋值语句");
                            return 1;
                        } else {
                            System.out.println("赋值语句出错");
                            return -1;
                        }
                    }

                } else {
                    System.out.println("赋值语句出错");
                    return -1;
                }
            }
        } else {
            return 0;
        }
    }

    int checkWhile(List<Token> tokens) {
        Token token = tokens.get(0);
        if (token == null) {
            System.out.println("当前语句为空");
            return -1;
        } else if ("while".equals(token.word)) {
            int len = tokens.size();
            List<Token> condition = new ArrayList<>();
            int index = 0;
            for (int i = 1; i < len; i++) {
                Token tokenNow = tokens.get(i);
                if (tokenNow.word.equals("do")) {
                    index = i;
                    break;
                }
                condition.add(tokenNow);
            }
            if (condition.size() != 1 || !Objects.equals(condition.get(0).type, "标识符")) {
                System.out.println("while语句条件出错");
                return -1;
            } else {
                List<Token> assign = new ArrayList<>();
                for (int i = index + 1; i < len; i++) {
                    Token tokenNow = tokens.get(i);
                    assign.add(tokenNow);
                }
                boolean assignFlag = checkE(assign);
                if (assignFlag) {
                    System.out.println("while-do循环语句，嵌套赋值语句");
                    return 1;
                } else {
                    System.out.println("赋值语句出错");
                    return -1;
                }
            }
        } else {
            return 0;
        }


    }

    //定义方法用于检查表达式

    private boolean emptyFirst(Token next) {
        if (next.word != null) {
            return next.type.equals("标识符")|| next.type.equals("常数");
        }
        return false;
    }

    private boolean symbolFirst(Token next) {
        if (next.word != null) {
            return next.type.equals("运算符") || next.word.equals(";");
        }
        return true;
    }

    private boolean signFirst(Token next) {
        if (next.word != null) {
            return next.type.equals("标识符") || next.type.equals("常数");
        }
        return false;
    }

    private boolean constFirst(Token next) {
        if (next.word != null) {
            return next.type.equals("运算符") || next.word.equals(";");
        }
        return true;
    }

    private boolean endFirst(Token next) {
        return next.type == null;
    }

    public void check(List<Token> tokenList) {
        int whileRes = checkWhile(tokenList);
        int ifRes = checkIF(tokenList);
        if (whileRes == 0 && ifRes == 0) {
            if (checkE(tokenList) ){
                System.out.println("赋值语句");
            }
        }
    }


    public static void main(String[] args) {
        WordAnalyze wordAnalyze = new WordAnalyze();
        String s = "A:=3&3";
        s = s + " ";
        System.out.println(s);
        List<Token> tokenList = wordAnalyze.analyze(s.toCharArray());
        GrammarAnalyze grammarAnalyze = new GrammarAnalyze();
        System.out.println(grammarAnalyze.checkAssign(tokenList));


    }


}
