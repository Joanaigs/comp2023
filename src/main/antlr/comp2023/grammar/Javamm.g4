grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INTEGER : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

program
    : statement+ EOF
    | (importDeclaration)* classDeclaration EOF;

importDeclaration : 'import' ID ( '.' ID )* ';';

classDeclaration  : 'class' ID ( 'extends' ID )? '{' ( varDeclaration )* ( methodDeclaration )*'}';

varDeclaration    : type ID ';';

methodDeclaration : 'public' type ID '(' ( type ID ( ','type ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'
                  | 'public' 'static' 'void' 'main' '(' 'String' '[' ']' ID ')' '{' ( varDeclaration )* ( statement )* '}' ;

type        : 'int' '[' ']'
            | 'boolean'
            | 'int'
            | ID;

statement
    :  '{' ( statement )* '}' #BrStmt
    | 'if' '(' expression ')' statement 'else' statement #IfStmt
    | 'while' '(' expression ')' statement #WhileSTmt
    | expression ';' #ExprStmt
    | var=ID '=' expression ';'  #Assignment
    | var=ID '=' value=INTEGER ';' #Assignment
    | var=ID '[' expression ']' '=' expression ';' #ArrayAssign
    ;


expression
    : 'new' 'int' '[' expression ']'        #CreateArray
    | 'new' value=ID '(' ')'                #InitializeClass
    | '!' expression                        #NegateExpr
    | '(' expression ')'                    #ParenExpr
    | expression '[' expression ']'         #ArrayExp
    | expression '.' value=ID '(' ( expression ( ',' expression )* )? ')' #CallFnc
    | expression '.' 'length'               #GetLenght
    | expression op=( '*' | '/') expression #BinaryOp
    | expression op=( '+' | '-') expression #BinaryOp
    | expression op='<' expression          #BinaryOp
    | expression op='&&' expression         #BinaryOp
    | value = INTEGER                       #Integer
    | bool = 'true'                                #Boolean
    | bool = 'false'                               #Boolean
    | value = ID                            #Identifier
    | 'this'                                #This
    ;