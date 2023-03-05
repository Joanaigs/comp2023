grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INTEGER : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

COMMENT : '/*' .*? '*/' -> skip ;
COMMENT2 : '//' ~[\r\n]* -> skip ;

program
    : statement+ EOF
    | (importDeclaration)* classDeclaration EOF;

importDeclaration
    :'import' library=ID ( '.' method=ID )* ';'
    ;

classDeclaration
    :'class' className=ID ( 'extends' extendsClass=ID )? '{' ( varDeclaration )* ( methodDeclaration )*'}'
    ;

varDeclaration
    :varType=type name=ID ';'
    ;

//adicionei aqui private public protected and none
methodDeclaration
    : ('public' | 'private' | 'protected') type methodName=ID '(' ( type var=ID ( ','type var=ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'
    | 'public' 'static' 'void' methodName='main' '(' 'String' '[' ']' var=ID ')' '{' ( varDeclaration )* ( statement )* '}'
    |  type methodName=ID '(' ( type var=ID ( ','type var=ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'
    |  'static' 'void' methodName='main' '(' 'String' '[' ']' var=ID ')' '{' ( varDeclaration )* ( statement )* '}' 

    ;
    //static void main(String[] args) {}

type
    : typeDeclaration='int'array='[' ']'
    | typeDeclaration='boolean'
    | typeDeclaration='int'
    | typeDeclaration=ID
    ;

statement
    :  '{' ( statement )* '}' #CodeBlockStmt
    | 'if' '(' expression ')' statement 'else' statement #IfStmt
    | 'while' '(' expression ')' statement #WhileSTmt
    | expression ';' #ExprStmt
    | var=ID '=' expression ';'  #Assignment
    | var=ID '=' value=INTEGER ';' #Assignment
    | var=ID '[' expression ']' '=' expression ';' #ArrayAssignStmt
    ;


expression
    : 'new' 'int' '[' expression ']'        #CreateArray
    | 'new' value=ID '(' ')'                #InitializeClass
    | '!' expression                        #NegateExpr
    | '(' expression ')'                    #ParenthesisExpr
    | expression '[' expression ']'         #ArrayExp
    | expression '.' value=ID '(' ( expression ( ',' expression )* )? ')' #CallFnc
    | expression '.' 'length'               #GetLenght
    | expression op=( '*' | '/') expression #BinaryOp
    | expression op=( '+' | '-') expression #BinaryOp
    | expression op='<' expression          #BinaryOp
    | expression op='&&' expression         #BinaryOp
    | value = INTEGER                       #Integer
    | bool = 'true'                         #Boolean
    | bool = 'false'                        #Boolean
    | value = ID                            #Identifier
    | 'this'                                #This
    ;

//falta aceitar metodo vazio