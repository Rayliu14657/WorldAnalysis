import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ToQuad {
    private Token SYM;
    private List<Token> words;
    private int current = 0;
    private int row = 1;
    private int number = 1;
    private boolean flag = true;
    private int count = 0;
    private int NXQ = 0;
    private List<Quad> output = new ArrayList<>();
    private HashMap<String, String> varMap = new HashMap<>();


    public ToQuad(List<Token> words) {
        this.words = words;
        if (words != null) {
            ADVANCE();
        }
    }

    private int newTemp() {
        count++;
        return count;
    }


    private void genQuad(String op, String arg1, String arg2, String result) {
        Quad q = new Quad(op, arg1, arg2, result);
        output.add(q);
        NXQ++;
    }

    private void ADVANCE() {
        if (current < words.size()) {
            SYM = words.get(current);
            current++;
            number++;
        }
    }

    private int merge(int p1, int p2) {
        int i = p2;
        if (p1 == -1) {
            return p2;
        }
        if (p2 == -1) {
            return p1;
        }
        if (p1 != 0 && p2 != 0) {
            while (Integer.parseInt(output.get(i).getRes()) != 0) {
                i = Integer.parseInt(output.get(i).getRes());
            }
            output.get(i).setRes(Integer.toString(p1));
            return p2;
        }
        return p2;
    }

    private void backPatch(int source, int target) {
        if (source == -1) {
            return;
        }
        //source 为链表，为链表的每一个表项回填target
        int i = source;
        int t = 0;
        if (target != 0) {
            while (Integer.parseInt(output.get(i).getRes()) != 0) {
                t = Integer.parseInt(output.get(i).getRes());
                output.get(i).setRes(Integer.toString(target));
                i = t;
            }
        }
        //链表表尾最后一项的res为0，单独处理
        output.get(i).setRes(Integer.toString(target));
    }

    private void ERROR(int i) {
        System.out.println("错误位于第" + row + "句,第" + number + "个单词上");
        flag = false;
        switch (i) {
            case 0:
                System.out.println("语法错，缺少分号");
            case 1:
                System.out.println("then 关键词缺失");
            case 2:
                System.out.println("do 关键词缺失");
            case 3:
                System.out.println("表达式格式错误");
            case 4:
                System.out.println("格式错误");
            case 5:
                System.out.println("赋值语句错误");
            case 6:
                System.out.println("标识符类型错误");
            case 7:
                System.out.println("标识符未定义");
            case 8:
                System.out.println("未识别到}");
            case 9:
                System.out.println("未识别到)");
        }
    }

    //E->id
    //E->const
    //E->(E)
    private String E() {
        String value = "";
        if ("标识符".equals(SYM.type)) {
            if (varMap.get(words.get(current - 1).word) != null) {
                if (!varMap.get(words.get(current - 1).word).equals("int")) {
                    ERROR(6);
                }
                value = words.get(current - 1).word;
                ADVANCE();
                value = T(value);
            } else {
                ERROR(7);
            }
        } else if ("常数".equals(SYM.type)) {
            value = words.get(current - 1).word;
            ADVANCE();
            value = T(value);
        } else {
            if ("(".equals(SYM.word)) {
                ADVANCE();
                E();
                if (")".equals(SYM.word)) {
                    ADVANCE();
                } else {
                    //未出现右括号
                    ERROR(9);
                }
            } else {
                //表达式格式错误
                ERROR(3);
            }
        }
        return value;
    }

    //T->E*E
    //T->E+E
    private String T(String value) {
        String n = "T" + newTemp();
        if ("+".equals(SYM.word) || "*".equals(SYM.word)) {
            String operator = SYM.word;
            ADVANCE();
            String arg2 = E();
            genQuad(operator, value, arg2, n);
        } else {
            n = value;
            count--;
        }
        return n;
    }

    //Q->id:=E
    private int Q() {
        if (flag) {
            if ("标识符".equals(SYM.type)) {
                if (varMap.get(words.get(current - 1).word) != null) {
                    if (!varMap.get(words.get(current - 1).word).equals("int")) {
                        //类型错误
                        ERROR(6);
                    }
                    String v = words.get(current - 1).word;
                    ADVANCE();
                    if (":=".equals(SYM.word)) {
                        ADVANCE();
                        genQuad(":=", E(), "-", v);
                        return NXQ;
                    } else {
                        ERROR(5);
                    }
                } else {
                    //变量未定义
                    ERROR(7);
                }
            } else {
                //语法错
                ERROR(4);
            }
        }
        return 0;
    }

    private TFList B() {
        int trueList = 0;
        int falseList = 0;
        if ("标识符".equals(SYM.type)) {
            if (varMap.get(words.get(current - 1).word) != null) {
                String arg1 = words.get(current - 1).word;
                ADVANCE();
                if (varMap.get(arg1).equals("bool")) {
                    trueList = NXQ;
                    genQuad("j=", arg1, "true", "0");
                    falseList = NXQ;
                    genQuad("i", "-", "-", "0");
                    if ("&".equals(SYM.word)) {
                        ADVANCE();
                        TFList l = and(trueList, falseList);
                        trueList = l.trueList;
                        falseList = l.falseList;
                    } else if ("|".equals(SYM.word)) {
                        ADVANCE();
                        TFList l = or(trueList, falseList);
                        trueList = l.trueList;
                        falseList = l.falseList;
                    }
                } else if (">".equals(SYM.word) || "<".equals(SYM.word)) {
                    if (!varMap.get(arg1).equals("int")) {
                        ERROR(6);
                    }
                    String operator = SYM.word;
                    ADVANCE();
                    if ("标识符".equals(SYM.type) || "常数".equals(SYM.type)) {
                        if ("标识符".equals(SYM.type)) {
                            String arg2 = words.get(current - 1).word;
                            if (varMap.get(arg2) == null) {
                                ERROR(7);
                            } else if (!varMap.get(arg2).equals("int")) {
                                ERROR(6);
                            }
                        }
                        trueList = NXQ;
                        String arg2 = words.get(current - 1).word;
                        genQuad("j" + operator, arg1, arg2, "0");
                        falseList = NXQ;
                        genQuad("j", "-", "-", "0");
                        ADVANCE();
                        if ("&".equals(SYM.word)) {
                            ADVANCE();
                            TFList r = and(trueList, falseList);
                            trueList = r.trueList;
                            falseList = r.falseList;
                        } else if ("|".equals(SYM.word)) {
                            ADVANCE();
                            TFList r = and(trueList, falseList);
                            trueList = r.trueList;
                            falseList = r.falseList;
                        }
                        return new TFList(trueList, falseList);
                    } else {
                        ERROR(6);
                    }
                }
            } else {
                ERROR(3);
            }
            return new TFList(trueList, falseList);
        } else if ("常数".equals(SYM.word)) {
            String arg1 = words.get(current - 1).word;
            ADVANCE();
            if (">".equals(SYM.word) || "<".equals(SYM.word)) {
                String operator = SYM.word;
                ADVANCE();
                if ("标识符".equals(SYM.type) || "常数".equals(SYM.type)) {
                    if ("常数".equals(SYM.word)) {
                        String arg2 = words.get(current - 1).word;
                        if (varMap.get(arg2) == null) {
                            ERROR(7);
                        }
                        trueList = NXQ;
                        genQuad("J" + operator, arg1, arg2, "0");
                        falseList = NXQ;
                        genQuad("j", "-", "-", "0");
                        ADVANCE();
                        if ("&".equals(SYM.word)) {
                            ADVANCE();
                            TFList r = and(trueList, falseList);
                            trueList = r.trueList;
                            falseList = r.falseList;
                        } else if ("|".equals(SYM.word)) {
                            ADVANCE();
                            TFList r = and(trueList, falseList);
                            trueList = r.trueList;
                            falseList = r.falseList;
                        }
                        return new TFList(trueList, falseList);

                    }
                }

            }
        } else if ("!".equals(SYM.word)) {
            ADVANCE();
            if ("(".equals(SYM.word)) {
                ADVANCE();
                TFList r = B();
                trueList = r.trueList;
                falseList = r.falseList;
                if (")".equals(SYM.word)) {
                    ADVANCE();
                    if ("&".equals(SYM.word)) {
                        ADVANCE();
                        TFList r2 = and(trueList, falseList);
                        trueList = r2.trueList;
                        falseList = r2.falseList;
                    } else if ("|".equals(SYM.word)) {
                        ADVANCE();
                        TFList r2 = and(trueList, falseList);
                        trueList = r2.trueList;
                        falseList = r2.falseList;
                    }
                    return new TFList(trueList, falseList);

                }
            }
        }
        return new TFList(0, 0);
    }

    private TFList or(int t, int f) {
        int m = NXQ;
        TFList r = B();
        int tl = r.trueList;
        int fl = r.falseList;
        backPatch(f, m);
        t = merge(t, tl);
        f = fl;
        return new TFList(t, f);
    }

    private TFList and(int t, int f) {
        int m = NXQ;
        TFList r = B();
        int tl = r.trueList;
        int fl = r.falseList;
        backPatch(t, m);
        t = tl;
        f = merge(f, fl);
        return new TFList(t, f);

    }

    private int P() {
        int truelist = 0;
        int falselist = 0;
        int nextlist = 0;
        if (flag) {
            if ("if".equals(SYM.word)) {
                ADVANCE();
                TFList r = B();
                truelist = r.trueList;
                falselist = r.falseList;
                if (flag) {
                    if ("then".equals(SYM.word)) {
                        ADVANCE();
                        backPatch(truelist, NXQ);
                        falselist = truelist + 1;
                        int pnext = 0;
                        if ("{".equals(SYM.word)) {
                            ADVANCE();
                            pnext = P();
                            if ("}".equals(SYM.word)) {
                                ADVANCE();
                                number = 1;
                            } else ERROR(8);
                        } else {
                            Q();
                        }
                        if (flag) {
                            if (";".equals(SYM.word)) {
                                if (pnext != 0) {
                                    nextlist = merge(falselist, pnext);
                                } else {
                                    nextlist = falselist;
                                }
                                ADVANCE();
                                row++;
                                number = 1;
                            } else if (flag) {
                                if ("else".equals(SYM.word)) {
                                    int N = NXQ;
                                    genQuad("j", "-", "-", "0");
                                    nextlist = merge(pnext, N);
                                    ADVANCE();
                                    backPatch(falselist, NXQ);
                                    if ("{".equals(SYM.word)) {
                                        ADVANCE();
                                        int a = P();
                                        if (a != 0) {
                                            nextlist = merge(N, a);
                                        } else nextlist = N;
                                        if ("}".equals(SYM.word)) {
                                            ADVANCE();
                                            number = 1;
                                        } else {
                                            ERROR(8);
                                        }
                                    } else {
                                        Q();
                                    }
                                    if (flag) {
                                        if (";".equals(SYM.word)) {
                                            ADVANCE();
                                            row++;
                                            number = 1;
                                        } else {
                                            ERROR(0);
                                        }
                                    } else {
                                        ERROR(0);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if ("while".equals(SYM.word)) {
                int back = NXQ;
                ADVANCE();
                TFList r = B();
                truelist = r.trueList;
                falselist = r.falseList;
                if (flag) {
                    if ("do".equals(SYM.word)) {
                        ADVANCE();
                        backPatch(truelist, NXQ);
                        if ("{".equals(SYM.word)) {
                            ADVANCE();
                            backPatch(P(), back);
                            if ("}".equals(SYM.word)) {
                                ADVANCE();
                                number = 1;
                            } else ERROR(8);
                        } else {
                            Q();
                        }
                        if (flag) {
                            if (";".equals(SYM.word)) {
                                ADVANCE();
                                nextlist = falselist;
                                genQuad("j", "-", "-", Integer.toString(back));
                            }
                        }
                    }
                }
            } else if ("int".equals(SYM.word)) {
                ADVANCE();
                varMap.put(SYM.word, "int");
                ADVANCE();
                if (SYM.word.equals(";")) {
                    ADVANCE();
                } else {
                    ERROR(0);
                }
            } else if ("标识符".equals(SYM.type)) {
                Q();
                ADVANCE();
                return -1;
            }
        }
        return nextlist;
    }

    public void run() {
        if (words != null) {
            while (current < words.size()) {
                if (flag) {
                    P();
                } else {
                    break;
                }
            }
        }
    }

    public void printQuad() {
        int start = 0;
        for (Quad c : output) {
            String arg1 = c.getArg1();
            String arg2 = c.getArg2();
            String op = c.getOp();
            String res = c.getRes();

            System.out.println(start + " " + op + " " + arg1 + " " + arg2 + " " + res);
            start++;
        }

    }

    public void printQuadToFile(String path) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        StringBuilder stringBuilder = new StringBuilder();
        int start = 0;
        for (Quad c : output) {
            String arg1 = c.getArg1();
            String arg2 = c.getArg2();
            String op = c.getOp();
            String res = c.getRes();
            stringBuilder.append(start);
            stringBuilder.append("\t");
            stringBuilder.append(op);
            stringBuilder.append("\t");
            stringBuilder.append(arg1);
            stringBuilder.append("\t");
            stringBuilder.append(arg2);
            stringBuilder.append("\t");
            stringBuilder.append(res);
            stringBuilder.append("\n");
            start++;
            bufferedWriter.write(stringBuilder.toString());
            stringBuilder = new StringBuilder();
        }
        bufferedWriter.close();

    }


    public static void main(String[] args) throws IOException {
        String inputPath = "Input\\data.txt";
        WordAnalyze analyze = new WordAnalyze();
        char[] input = analyze.input(inputPath);
        List<Token> tokens = analyze.analyze(input);
        ToQuad t = new ToQuad(tokens);
        t.run();
        t.printQuad();
        t.printQuadToFile("Output\\quad.txt");
    }


}
