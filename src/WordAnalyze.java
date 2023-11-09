import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordAnalyze {
    //定义一个保留字数组
    private String[] keyWord = { "var", "int", "if", "then", "else", "do", "while"};
    //定义一个用于移动指示的索引
    private char ch;

    private HashMap<String, Integer> tokenMap  = new HashMap<String, Integer>(){{
        put("var",1);
        put("int",2);
        put("if",3);
        put("then",4);
        put("else",5);
        put("do",6);
        put("while",7);
        //标识符8
        //常数9
        put("+",10);
        put("*",11);
        put("(",12);
        put(")",13);
        put("{",14);
        put("}",15);
        put("=",16);
        put(":",17);
        put(":=",18);
        put(">",19);
        put("<",20);
        put(",",21);
        put(";",22);
        put("&",23);
        put("|",24);
        put("!",25);
    }};

    //判断是否是关键字
    boolean isKey(String str) {
        for (int i = 0; i < keyWord.length; i++) {
            if (keyWord[i].equals(str))
                return true;
        }
        return false;
    }

    //判断是否是字母
    boolean isLetter(char letter) {
        if ((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z'))
            return true;
        else
            return false;
    }

    //判断是否是数字
    boolean isDigit(char digit) {
        if (digit >= '0' && digit <= '9')
            return true;
        else
            return false;
    }

    //词法分析
    //将程序转化为chars输入分析器
    List<Token> analyze(char[] chars){
        StringBuilder arr;
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            ch = chars[i];
            arr = new StringBuilder();
            //读取到文件换行符，制表符和回车时，什么都不做
            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                //什么都不做
            }
            //如果是字母
            else if (isLetter(ch)) {
                //如果以字母开头，之后又读取到字母或者数字，将这些字母和数字组合成一个字符串
                while (isLetter(ch) || isDigit(ch)) {
                    arr.append(ch);
                    ch = chars[++i];
                }
                //结束while循环之后的i被额外增加一次，回退一个字符
                i--;
                //对字符串进行判断，判断是否属于保留字；
                if (isKey(arr.toString())) {
                    //关键字
                    Token token = new Token(arr.toString(),tokenMap.get(arr.toString()),"关键字");
                    tokens.add(token);

                } else {
                    //标识符
                    Token token = new Token(arr.toString(),11,"标识符");
                    tokens.add(token);
                }
            }
            //如果当前读取到数字
            else if (isDigit(ch)) {
                //进入循环判断，直到数字结束
                //当前读取到的是数字
                while (isDigit(ch)) {
                    arr.append(ch);
                    ch = chars[++i];
                }
                i--;
                //属于无符号常数
                Token token = new Token(arr.toString(),12,"常数");
                tokens.add(token);
            }
            else {
                switch (ch) {
                //运算符
                case '+':
                case '*':
                    Token token = new Token(String.valueOf(ch),tokenMap.get(String.valueOf(ch)),"运算符");
                    tokens.add(token);
                    break;
                //分界符
                case '(':
                case ';':
                case ')':
                case '{':
                case '}':
                    token = new Token(String.valueOf(ch),tokenMap.get(String.valueOf(ch)),"分界符");
                    tokens.add(token);
                    break;
                //运算符
                case '=':
                case '&':
                case'|':
                case'!':
                    token = new Token(String.valueOf(ch),tokenMap.get(String.valueOf(ch)),"运算符");
                    tokens.add(token);
                break;
                case ':': {
                    //如果:后紧接着就是=，将:=一起输出为赋值符号
                    ch = chars[++i];
                    if (ch == '='){
                        token = new Token(":=",tokenMap.get(":="),"运算符");
                        tokens.add(token);
                    }
                    else {
                        //:后的不是=, 只输出:
                        token = new Token(":",tokenMap.get(":"),"运算符");
                        tokens.add(token);
                        //将索引回退
                        i--;
                    }
                }
                break;
                //与判断是":"还是":="相同
                case '>': {
                    token = new Token(">",tokenMap.get(">"),"运算符");
                    tokens.add(token);
                }
                break;
                //同上
                case '<': {
                    token = new Token("<",tokenMap.get("<"),"运算符");
                    tokens.add(token);
                }
                break;

                case ',': {
                    token = new Token(",",tokenMap.get(","),"运算符");
                    tokens.add(token);
                }
                break;

                //当运行至字符串尾部时，输出分析结束，不进入default
                case '\0': {

                }
                break;

                //无识别
                default:
                    token = new Token(String.valueOf(ch),-1,"非法字符");
                    tokens.add(token);
            }
            }
        }
        return tokens;
    }

    char[] input(String path) {
        File file = new File(path);//定义一个file对象，用来初始化FileReader
        try {
            FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
            int length = (int) file.length();
            //这里定义字符数组的时候需要多定义一个,因为词法分析器会遇到超前读取一个字符的时候
            // 如果是最后一个字符被读取,在读取下一个字符就会出现越界的异常
            char[] buf = new char[length + 1];
            reader.read(buf);
            reader.close();
            return buf;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void output(String path, List<Token> Tokens) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
        File file = new File(path);
        if(!file.exists()){
            file.createNewFile();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(Token t:Tokens){
            stringBuilder.append(t.word);
            stringBuilder.append("\t");
            stringBuilder.append(t.number);
            stringBuilder.append("\t");
            stringBuilder.append(t.type);
            stringBuilder.append("\n");
            bufferedWriter.write(stringBuilder.toString());
            stringBuilder = new StringBuilder();
        }
        bufferedWriter.close();

    }

    public static void main(String[] args) throws Exception {
        String outputPath = "Output\\out.txt";
        String inputPath = "Input\\data.txt";
        WordAnalyze analyze = new WordAnalyze();
        char[] input = analyze.input(inputPath);
        List<Token> tokens = analyze.analyze(input);
        analyze.output(outputPath,tokens);



    }
}
