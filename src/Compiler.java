import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Compiler {
    public static void main(String[] args) throws Exception {
        ArrayList<TokenTable> ttb = new ArrayList<>();
        Lexer lexer = new Lexer(ttb);
        lexer.analyse();
        lexer.output();
        //Parse parse = new Parse(ttb);
        //parse.analyse();
        //parse.output();
    }

}
class FileIO{
    BufferedReader reader;
    BufferedWriter writer;
    FileIO() throws IOException {
        reader = new BufferedReader(new FileReader("testfile.txt"));
        writer = new BufferedWriter(new FileWriter("output.txt"));
    }
}
class TokenTable{
    String name;
    String category;

    int line;
    TokenTable(String category,String name,int line)
    {
        this.name = name;
        this.category =category;
        this.line = line;
    }
}
class Lexer extends FileIO{

    ArrayList<TokenTable> ttb;
    char sym;
    int line=0;
    boolean InStr = false;
    boolean InSingleNote = false;
    boolean InMulNote = false;
    boolean isIdent = false;
    Lexer(ArrayList<TokenTable> ttb) throws IOException {
        super();
        this.ttb = ttb;
    }
    void analyse() throws IOException {

        String tempLine;
        while ((tempLine = reader.readLine()) != null) {
            int length = tempLine.length();
            line++;
            for (int i = 0; i < length; i++) {
                sym = tempLine.charAt(i);
                if (InSingleNote) {
                    InSingleNote = false;
                    break;
                } else if (InMulNote) {
                    if (sym == '*') {
                        if(length>(i+1))
                            if(tempLine.charAt(i+1)=='/') {
                                InMulNote = false;
                                i++;
                            }
                    }
                } else if(InStr){
                    StringBuilder token = new StringBuilder();
                    while(i<length&&InStr) {
                        sym=tempLine.charAt(i);
                        token.append(sym);
                        if(sym=='"')
                            InStr = false;
                        i++;
                    }
                    ttb.add(new TokenTable("STRCON","\""+token.toString(),line));
                    i--;
                }else {
                    if (sym == '\n') {
                        line++;
                        break;
                    } else if (sym == ' ' || sym == '\t') {
                        continue;
                    } else if (sym == '/') {
                        if(tempLine.length()>(i+1)) {
                            if (tempLine.charAt(i+1) == '/') {
                                InSingleNote = true;
                            } else if (tempLine.charAt(i+1) == '*') {
                                InMulNote = true;
                                i++;
                            } else
                                ttb.add(new TokenTable("DIV", "/",line));
                        }
                    } else if (sym=='"') {
                        InStr=true;
                    } else if (Character.isLowerCase(sym)|| Character.isUpperCase(sym)||sym=='_') {
                        StringBuilder token = new StringBuilder();
                        while((i<length)&&(Character.isLowerCase(tempLine.charAt(i))|| Character.isUpperCase(tempLine.charAt(i))||Character.isDigit(tempLine.charAt(i))||tempLine.charAt(i)=='_')){
                            if(Character.isDigit(tempLine.charAt(i)))
                                isIdent = true;
                            sym=tempLine.charAt(i);
                            token.append(sym);
                            i++;
                        }
                        reserved(token);
                        if(isIdent) {
                            ttb.add(new TokenTable("IDENFR",token.toString(),line));
                            isIdent = false;
                        }
                        i--;
                    }
                    else if(Character.isDigit(sym)){
                        StringBuilder token = new StringBuilder();
                        while((i<length)&&Character.isDigit(tempLine.charAt(i)))
                        {
                            sym=tempLine.charAt(i);
                            token.append(sym);
                            i++;
                        }
                        ttb.add(new TokenTable("INTCON",token.toString(),line));
                        i--;
                    }   else if (sym=='!') {
                        if(i+1<length&&tempLine.charAt(i+1)=='=') {
                            ttb.add(new TokenTable("NEQ", "!=",line));
                            i++;
                        }
                        else
                            ttb.add(new TokenTable("NOT","!",line));
                    }
                    else if(sym=='|'){
                        if(i+1<length&&tempLine.charAt(i+1)=='|') {
                            ttb.add(new TokenTable("OR", "||",line));
                            i++;
                        }
                        else
                            error(line);
                    }
                    else if(sym=='&'){
                        if(i+1<length&&tempLine.charAt(i+1)=='&') {
                            ttb.add(new TokenTable("AND", "&&",line));
                            i++;
                        }
                        else
                            error(line);
                    }
                    else if(sym=='<'){
                        if(i+1<length&&tempLine.charAt(i+1)=='=') {
                            ttb.add(new TokenTable("LEQ", "<=",line));
                            i++;
                        }
                        else
                            ttb.add(new TokenTable("LSS","<",line));
                    }
                    else if(sym=='>'){
                        if(i+1<length&&tempLine.charAt(i+1)=='=') {
                            ttb.add(new TokenTable("GEQ", ">=",line));
                            i++;
                        }
                        else
                            ttb.add(new TokenTable("GRE",">",line));
                    }
                    else if(sym=='='){
                        if(i+1<length&&tempLine.charAt(i+1)=='=') {
                            ttb.add(new TokenTable("EQL", "==",line));
                            i++;
                        }
                        else
                            ttb.add(new TokenTable("ASSIGN","=",line));
                    }
                    else{
                        char str = sym;
                        if(str == '+') ttb.add(new TokenTable("PLUS","+",line));
                        else if(str == '-') ttb.add(new TokenTable("MINU","-",line));
                        else if(str == '*') ttb.add(new TokenTable("MULT","*",line));
                        else if(str == '%') ttb.add(new TokenTable("MOD","%",line));
                        else if(str == ';') ttb.add(new TokenTable("SEMICN",";",line));
                        else if(str == ',') ttb.add(new TokenTable("COMMA",",",line));
                        else if(str == '[') ttb.add(new TokenTable("LBRACK","[",line));
                        else if(str == ']') ttb.add(new TokenTable("RBRACK","]",line));
                        else if(str == '(') ttb.add(new TokenTable("LPARENT","(",line));
                        else if(str == ')') ttb.add(new TokenTable("RPARENT",")",line));
                        else if(str == '{') ttb.add(new TokenTable("LBRACE","{",line));
                        else if(str == '}') ttb.add(new TokenTable("RBRACE","}",line));
                    }
                }
            }
        }
        reader.close();
    }

    void output() throws IOException {
        for (TokenTable i :ttb
        ) {
            System.err.println(i.category+" "+i.name+" "+i.line+"\n");
            writer.write(i.category+" "+i.name+" "+i.line+"\n");
        }
        writer.close();
    }
    void reserved(StringBuilder token)
    {
        String str = token.toString();
        if(str.equals("main")) ttb.add(new TokenTable("MAINTK","main",line));
        else if(str.equals("const")) ttb.add(new TokenTable("CONSTTK","const",line));
        else if(str.equals("char")) ttb.add(new TokenTable("CHARTK","char",line));
        else if(str.equals("int")) ttb.add(new TokenTable("INTTK","int",line));
        else if(str.equals("if")) ttb.add(new TokenTable("IFTK","if",line));
        else if(str.equals("else")) ttb.add(new TokenTable("ELSETK","else",line));
        else if(str.equals("break")) ttb.add(new TokenTable("BREAKTK","break",line));
        else if(str.equals("continue")) ttb.add(new TokenTable("CONTINUETK","continue",line));
        else if(str.equals("while")) ttb.add(new TokenTable("WHILETK","while",line));
        else if(str.equals("getint")) ttb.add(new TokenTable("GETINTTK","getint",line));
        else if(str.equals("printf")) ttb.add(new TokenTable("PRINTFTK","printf",line));
        else if(str.equals("return")) ttb.add(new TokenTable("RETURNTK","return",line));
        else if(str.equals("void")) ttb.add(new TokenTable("VOIDTK","void",line));
        else isIdent = true;
    }
    void error(int line) throws IOException {
        System.out.println("error in line "+line+"\n");
    }
}
class Parse extends FileIO{
    ArrayList<TokenTable> ttb;
    String sym;
    String name;
    int index=0;

    SymbolTable root = null;

    SymbolTable current = null;
    ArrayList<String> outemp = new ArrayList<>();
    Parse(ArrayList<TokenTable> ttb) throws IOException {
        super();
        this.ttb = ttb;
    }
    void nextsym() throws IndexOutOfBoundsException, IOException {
        TokenTable temp;
        temp= ttb.get(index++);
        name = temp.name;
        sym = temp.category;
        outemp.add(sym+" "+name+"\n");
    }
    String nowsym() throws IndexOutOfBoundsException{
        return ttb.get(index).category;
    }
    String latersym() throws IndexOutOfBoundsException{
        return ttb.get(index+1).category;
    }

    void error(String fname) throws Exception {
        System.out.println("error in "+fname);
        output();
        throw new Exception();
    }
    void analyse() throws Exception {
        nextsym();
        CompUnit();
    }
    void output() throws IOException {

        for (String i:outemp
        ) {
            writer.write(i);
        }
        writer.close();
    }
    void CompUnit() throws Exception {
        root = new SymbolTable();
        current = root;
        while((sym.equals("INTTK")&&nowsym().equals("IDENFR")&&!latersym().equals("LPARENT"))||sym.equals("CONSTTK"))
        {
            Decl();
            nextsym();
        }
        while((sym.equals("INTTK")||sym.equals("VOIDTK"))&&nowsym().equals("IDENFR")&&latersym().equals("LPARENT")){
            FuncDef();
            nextsym();
        }
        if(sym.equals("INTTK")&&nowsym().equals("MAINTK")&&latersym().equals("LPARENT")) {
            MainFuncDef();
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
        outemp.add("<CompUnit>\n");

    }
    void Decl() throws Exception {
        if(sym.equals("CONSTTK")) {
            nextsym();
            ConstDecl();
        }
        else if(sym.equals("INTTK")) {
            nextsym();
            VarDecl();
        }
    }
    void ConstDecl() throws Exception {
        if(sym.equals("INTTK")){
            nextsym();
            ConstDef();
            nextsym();
            while (sym.equals("COMMA"))
            {
                nextsym();
                ConstDef();
                nextsym();
            }
            if (sym.equals("SEMICN"))
                outemp.add("<ConstDecl>\n");
            else
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void ConstDef() throws Exception {
        if(sym.equals("IDENFR")){
            nextsym();
            int dimension = 0;
            while (sym.equals("LBRACK")){
                dimension=dimension+1;
                nextsym();
                ConstExp();
                nextsym();
                if(sym.equals("RBRACK"))
                    nextsym();
                else
                    error(Thread.currentThread().getStackTrace()[1].getMethodName());
            }
            if(current.isContain(name))
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
            else
                current.addSymbol(new Symbol());
            if(sym.equals("ASSIGN")){
                nextsym();
                ConstInitVal();
            }
            outemp.add("<ConstDef>\n");
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void ConstExp() throws Exception {
        AddExp();
        outemp.add("<ConstExp>\n");
    }
    void ConstInitVal() throws Exception {
        if(sym.equals("INTCON")||sym.equals("PLUS")||sym.equals("MINU")||sym.equals("IDENFR")||sym.equals("LPARENT")) {
            ConstExp();
            outemp.add("<ConstInitVal>\n");
        }
        else if (sym.equals("LBRACE")) {
            nextsym();
            ConstInitVal();
            nextsym();
            while(sym.equals("COMMA")){
                nextsym();
                ConstInitVal();
                nextsym();
            }
            if(sym.equals("RBRACE"))
                outemp.add("<ConstInitVal>\n");
            else
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void VarDecl() throws Exception {
        VarDef();
        nextsym();
        while (sym.equals("COMMA")){
            nextsym();
            VarDef();
            nextsym();
        }
        if(sym.equals("SEMICN"))
            outemp.add("<VarDecl>\n");
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void VarDef() throws Exception {
        if(sym.equals("IDENFR"))
        {
            if(nowsym().equals("SEMICN")||nowsym().equals("COMMA")) {
                outemp.add("<VarDef>\n");
                return;
            }
            nextsym();
            while (sym.equals("LBRACK")) {
                nextsym();
                ConstExp();
                nextsym();
                if (sym.equals("RBRACK")) {
                    if(nowsym().equals("LBRACK"))
                        nextsym();
                    else if (nowsym().equals("ASSIGN")) {
                        nextsym();
                    }
                }
                else
                    error(Thread.currentThread().getStackTrace()[1].getMethodName());
            }
            if(sym.equals("ASSIGN")){
                nextsym();
                InitVal();
            }
            outemp.add("<VarDef>\n");

        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void InitVal() throws Exception {
        if(sym.equals("INTCON")||sym.equals("IDENFR")||sym.equals("PLUS")||sym.equals("MINU")||sym.equals("LPARENT")) {
            Exp();
            outemp.add("<InitVal>\n");
        }
        else if (sym.equals("LBRACE")) {
            nextsym();
            InitVal();
            nextsym();
            while(sym.equals("COMMA")){
                nextsym();
                InitVal();
                nextsym();
            }
            if(sym.equals("RBRACE"))
                outemp.add("<InitVal>\n");
            else
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void MainFuncDef() throws Exception {
        if(sym.equals("INTTK"))
        {
            nextsym();
            if(sym.equals("MAINTK")){
                nextsym();
                if(sym.equals("LPARENT")) {
                    nextsym();
                    if(sym.equals("RPARENT")) {
                        nextsym();
                        Block();
                        outemp.add("<MainFuncDef>\n");
                    }
                }
            }
        }
    }
    void Block() throws Exception {
        if(sym.equals("LBRACE"))
        {
            nextsym();
            while(!sym.equals("RBRACE")){
                BlockItem();
                nextsym();
            }
            outemp.add("<Block>\n");
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void BlockItem() throws Exception {
        if(sym.equals("CONSTTK")||sym.equals("INTTK")){
            Decl();
        }
        else
            Stmt();
    }
    void Stmt() throws Exception {
        if(sym.equals("PRINTFTK")){
            nextsym();
            Printf();
        }
        else if(sym.equals("RETURNTK")){
            nextsym();
            Return();
        }
        else if(sym.equals("BREAKTK")||sym.equals("CONTINUETK")) {
            nextsym();
            BreakAndContinue();
        }
        else if(sym.equals("WHILETK")){
            nextsym();
            While();
        }
        else if(sym.equals("IFTK")){
            nextsym();
            If();
        }
        else if(sym.equals("LBRACE")){
            Block();
            outemp.add("<Stmt>\n");
        }
        else {
            int i =index-1;
            boolean flag=false;
            while(i<ttb.size()&&!ttb.get(i).category.equals("SEMICN")){
                if (ttb.get(i).category.equals("ASSIGN")) {
                    flag = true;
                    break;
                }
                i++;
            }
            if(flag)
                Assign();
            else {
                if(sym.equals("SEMICN"))
                    outemp.add("<Stmt>\n");
                else {
                    Exp();
                    nextsym();
                    if(sym.equals("SEMICN"))
                        outemp.add("<Stmt>\n");
                    else
                        error(Thread.currentThread().getStackTrace()[1].getMethodName());
                }
            }
        }
    }
    void Assign() throws Exception {
        Lval();
        nextsym();
        if(sym.equals("ASSIGN")){
            nextsym();
            if(sym.equals("GETINTTK")){
                nextsym();
                if(sym.equals("LPARENT")){
                    nextsym();
                    if(sym.equals("RPARENT")){
                        nextsym();
                        if(sym.equals("SEMICN")){
                            outemp.add("<Stmt>\n");
                        }
                        else
                            error(Thread.currentThread().getStackTrace()[1].getMethodName());
                    }
                    else
                        error(Thread.currentThread().getStackTrace()[1].getMethodName());
                }
                else
                    error(Thread.currentThread().getStackTrace()[1].getMethodName());
            }
            else {
                Exp();
                nextsym();
                if (sym.equals("SEMICN"))
                    outemp.add("<Stmt>\n");
                else
                    error(Thread.currentThread().getStackTrace()[1].getMethodName());
            }
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void Lval() throws Exception {
        if(sym.equals("IDENFR")){
            if(nowsym().equals("LBRACK")){
                nextsym();
                while (sym.equals("LBRACK")) {
                    nextsym();
                    Exp();
                    nextsym();
                    if (sym.equals("RBRACK")) {
                        if (nowsym().equals("LBRACK"))
                            nextsym();
                    }
                    else
                        error(Thread.currentThread().getStackTrace()[1].getMethodName());
                }
            }
            outemp.add("<LVal>\n");
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void If() throws Exception {
        if(sym.equals("LPARENT"))
        {
            nextsym();
            Cond();
            nextsym();
            if(sym.equals("RPARENT"))
            {
                nextsym();
                Stmt();
                if(nowsym().equals("ELSETK")){
                    nextsym();
                    nextsym();
                    Stmt();
                }
                outemp.add("<Stmt>\n");
            }
            else
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void While() throws Exception {
        if(sym.equals("LPARENT"))
        {
            nextsym();
            Cond();
            nextsym();
            if(sym.equals("RPARENT"))
            {
                nextsym();
                Stmt();
                outemp.add("<Stmt>\n");
            }
            else
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void BreakAndContinue() throws Exception {
        if(sym.equals("SEMICN")){
            outemp.add("<Stmt>\n");
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void Return() throws Exception {
        if(sym.equals("SEMICN")){
            outemp.add("<Stmt>\n");
        }
        else {
            Exp();
            nextsym();
            if(sym.equals("SEMICN")) {
                outemp.add("<Stmt>\n");
            }
            else
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
        }
    }
    void Printf() throws Exception {
        if(sym.equals("LPARENT")){
            nextsym();
            if(sym.equals("STRCON")){
                nextsym();
                while (sym.equals("COMMA")){
                    nextsym();
                    Exp();
                    nextsym();
                }
                if(sym.equals("RPARENT"))
                {
                    nextsym();
                    if (sym.equals("SEMICN"))
                        outemp.add("<Stmt>\n");
                    else
                        error(Thread.currentThread().getStackTrace()[1].getMethodName());
                }
                else
                    error(Thread.currentThread().getStackTrace()[1].getMethodName());
            }
            else
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void FuncDef() throws Exception {
        FuncType();
        nextsym();
        if(sym.equals("IDENFR")){
            nextsym();
            if(sym.equals("LPARENT")){
                nextsym();
                if(sym.equals("INTTK")) {
                    FuncFParams();
                    nextsym();
                }
                if(sym.equals("RPARENT")) {
                    nextsym();
                    Block();
                    outemp.add("<FuncDef>\n");
                }
                else
                    error(Thread.currentThread().getStackTrace()[1].getMethodName());
            }
        }
    }
    void FuncFParams() throws Exception {
        FuncFParam();
        if(nowsym().equals("COMMA")) {
            nextsym();
            while (sym.equals("COMMA")) {
                nextsym();
                FuncFParam();
                if (nowsym().equals("COMMA"))
                    nextsym();
                else {
                    break;
                }
            }
        }
        outemp.add("<FuncFParams>\n");
    }
    void FuncFParam() throws Exception {
        if(sym.equals("INTTK")){
            nextsym();
            if(sym.equals("IDENFR")) {

                if (nowsym().equals("LBRACK")) {
                    nextsym();
                    nextsym();
                    if (sym.equals("RBRACK")) {
                        if(nowsym().equals("LBRACK")) {
                            nextsym();
                            while (sym.equals("LBRACK")) {
                                nextsym();
                                ConstExp();
                                nextsym();
                            }
                            if (sym.equals("RBRACK")) {
                                outemp.add("<FuncFParam>\n");
                            } else
                                error(Thread.currentThread().getStackTrace()[1].getMethodName());
                        }
                        else
                            outemp.add("<FuncFParam>\n");
                    } else
                        error(Thread.currentThread().getStackTrace()[1].getMethodName());
                }
                else{
                    outemp.add("<FuncFParam>\n");
                }
            }
            else
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    void FuncType() throws Exception {
        if(sym.equals("VOIDTK")||sym.equals("INTTK"))
            outemp.add("<FuncType>\n");
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    void PrimaryExp() throws Exception {
        if(sym.equals("LPARENT"))
        {
            nextsym();
            Exp();
            nextsym();
            if(sym.equals("RPARENT")){
                outemp.add("<PrimaryExp>\n");
            }
            else
                error(Thread.currentThread().getStackTrace()[1].getMethodName());
        } else if (sym.equals("INTCON")) {
            Number();
            outemp.add("<PrimaryExp>\n");
        } else if (sym.equals("IDENFR")) {
            Lval();
            outemp.add("<PrimaryExp>\n");
        }
    }
    void Exp() throws Exception {
        AddExp();
        outemp.add("<Exp>\n");
    }
    void Cond() throws Exception {
        LorExp();
        outemp.add("<Cond>\n");
    }
    void LorExp() throws Exception {
        LAndExp();
        if(nowsym().equals("OR")) {
            outemp.add("<LOrExp>\n");
            nextsym();
            while (sym.equals("OR")){
                nextsym();
                LAndExp();
                if(nowsym().equals("OR")) {
                    outemp.add("<LOrExp>\n");
                    nextsym();
                }
            }
        }
        outemp.add("<LOrExp>\n");
    }
    void LAndExp() throws Exception {
        EqExp();
        if(nowsym().equals("AND")){
            outemp.add("<LAndExp>\n");
            nextsym();
            while (sym.equals("AND")){
                nextsym();
                EqExp();
                if(nowsym().equals("AND")) {
                    outemp.add("<LAndExp>\n");
                    nextsym();
                }
            }
        }
        outemp.add("<LAndExp>\n");
    }
    void EqExp() throws Exception {
        RelExp();
        if(nowsym().equals("EQL")||nowsym().equals("NEQ")){
            outemp.add("<EqExp>\n");
            nextsym();
            while (sym.equals("EQL")||sym.equals("NEQ")){
                nextsym();
                RelExp();
                if(nowsym().equals("EQL")||nowsym().equals("NEQ")) {
                    outemp.add("<EqExp>\n");
                    nextsym();
                }
            }
        }
        outemp.add("<EqExp>\n");
    }
    void RelExp() throws Exception {
        AddExp();
        if(nowsym().equals("LSS")||nowsym().equals("LEQ")||nowsym().equals("GRE")||nowsym().equals("GEQ")){
            outemp.add("<RelExp>\n");
            nextsym();
            while (sym.equals("LSS")||sym.equals("LEQ")||sym.equals("GRE")||sym.equals("GEQ")){
                nextsym();
                AddExp();
                if(nowsym().equals("LSS")||nowsym().equals("LEQ")||nowsym().equals("GRE")||nowsym().equals("GEQ")){
                    outemp.add("<RelExp>\n");
                    nextsym();
                }
            }
        }
        outemp.add("<RelExp>\n");
    }
    void AddExp() throws Exception {
        MulExp();
        if(nowsym().equals("PLUS")||nowsym().equals("MINU")){
            outemp.add("<AddExp>\n");
            nextsym();
            while (sym.equals("PLUS")||sym.equals("MINU")){
                nextsym();
                MulExp();
                if(nowsym().equals("PLUS")||nowsym().equals("MINU")) {
                    outemp.add("<AddExp>\n");
                    nextsym();
                }
            }
        }
        outemp.add("<AddExp>\n");
    }
    void MulExp() throws Exception {
        UnaryExp();
        if(nowsym().equals("MULT")||nowsym().equals("DIV")||nowsym().equals("MOD")){
            outemp.add("<MulExp>\n");
            nextsym();
            while (sym.equals("MULT")||sym.equals("DIV")||sym.equals("MOD")){
                nextsym();
                UnaryExp();
                if(nowsym().equals("MULT")||nowsym().equals("DIV")||nowsym().equals("MOD")) {
                    outemp.add("<MulExp>\n");
                    nextsym();
                }
            }
        }
        outemp.add("<MulExp>\n");
    }
    void UnaryExp() throws Exception {
        if(sym.equals("IDENFR")&&nowsym().equals("LPARENT")) {
            nextsym();
            nextsym();
            if(sym.equals("RPARENT")){
                outemp.add("<UnaryExp>\n");
            }
            else {
                FuncRParams();
                if(sym.equals("RPARENT")){
                    outemp.add("<UnaryExp>\n");
                }
                else
                    error(Thread.currentThread().getStackTrace()[1].getMethodName());
            }
        } else if ((sym.equals("IDENFR")&&!nowsym().equals("LPARENT"))||sym.equals("LPARENT")||sym.equals("INTCON")) {
            PrimaryExp();
            outemp.add("<UnaryExp>\n");
        } else if (sym.equals("PLUS")||sym.equals("MINU")||sym.equals("NOT")) {
            UnaryOp();
            nextsym();
            UnaryExp();
            outemp.add("<UnaryExp>\n");
        }
    }
    void FuncRParams() throws Exception {
        Exp();
        if(nowsym().equals("COMMA")){
            nextsym();
            while (sym.equals("COMMA")) {
                nextsym();
                Exp();
                if(nowsym().equals("COMMA"))
                    nextsym();
                else {
                    outemp.add("<FuncRParams>\n");
                    nextsym();
                }
            }

        }
        else {
            outemp.add("<FuncRParams>\n");
            nextsym();
        }
    }
    void Number() throws Exception {
        if(sym.equals("INTCON"))
            outemp.add("<Number>\n");
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    void UnaryOp() throws Exception {
        if (sym.equals("PLUS")||sym.equals("MINU")||sym.equals("NOT")) {
            outemp.add("<UnaryOp>\n");
        }
        else
            error(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
}

class Symbol{
        String name;
        int dimension;
        int decLine;
        int useLine;
}

class SymbolTable{
    SymbolTable parent = null;
    HashMap<String,Symbol> symbols = new HashMap<>();

    boolean isContain(String name){
        if(symbols.containsKey(name))
            return true;
        return false;
    }

    void addSymbol(Symbol s){
        symbols.put(s.name,s);
    }
}