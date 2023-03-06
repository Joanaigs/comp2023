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
    :'import' library+=ID ( '.' library+=ID )* ';'
    ;

classDeclaration
    :'class' className=ID ( 'extends' extendsClass=ID )? '{' ( varDeclaration )* ( methodDeclaration )*'}'
    ;

varDeclaration
    :varType=type name=ID ';'
    ;

methodDeclaration
    : (('public'|'private'|'protected') instanceMethodDeclaration|'public' mainMethodDeclaration) ;

instanceMethodDeclaration
    : type methodName=ID '(' ( fieldDeclaration ( ','fieldDeclaration )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'
;
mainMethodDeclaration
    : 'static' 'void' methodName='main' '(' 'String' '[' ']' var=ID ')' '{' ( varDeclaration )* ( statement )* '}'
    ;

fieldDeclaration
    :type name=ID
    ;

type locals[boolean isArray=false, boolean isPrimitive=true]
    : typeDeclaration=('byte'|'short'|'int'|'long'|'float'|'double'|'boolean'|'char')array='[' ']' {$isArray=true;}
    | typeDeclaration=('byte'|'short'|'int'|'long'|'float'|'double'|'boolean'|'char')
    | typeDeclaration='String'
    | typeDeclaration=ID
    ;

statement
    :  '{' ( statement )* '}'                               #CodeBlockStmt
    | 'if' '(' expression ')' statement 'else' statement    #IfStmt
    | 'while' '(' expression ')' statement                  #WhileSTmt
    | expression ';'                                        #ExprStmt
    | var=ID '=' expression ';'                             #Assignment
    | var=ID '=' value=INTEGER ';'                          #Assignment
    | var=ID '[' expression ']' '=' expression ';'          #ArrayAssignStmt
    ;


expression
    : 'new' 'int' '[' expression ']'                                      #CreateArray
    | 'new' value=ID '(' ')'                                              #InitializeClass
    | '!' expression                                                      #NegateExpr
    | '(' expression ')'                                                  #ParenthesisExpr
    | expression '[' expression ']'                                       #ArrayExp
    | expression '.' value=ID '(' ( expression ( ',' expression )* )? ')' #CallFnc
    | expression '.' 'length'                                             #GetLenght
    | expression op=( '++' |'--')                                         #PostfixOp
    | op=( '++' |'--'|'+'|'-'|'~') expression                             #UnaryOp
    | expression op=( '*' | '/') expression                               #BinaryOp
    | expression op=( '+' | '-') expression                               #BinaryOp
    | expression op=('<<'|'>>'|'>>>') expression                          #BinaryOp
    | expression op=('<'|'>'|'<='|'>='|'instanceof') expression           #BinaryOp
    | expression op=('=='|'!=') expression                                #BinaryOp
    | expression op='&' expression                                        #BinaryOp
    | expression op='^' expression                                        #BinaryOp
    | expression op='|' expression                                        #BinaryOp
    | expression op='&&' expression                                       #BinaryOp
    | expression op='||' expression                                       #BinaryOp
    | expression op='? :' expression                                      #BinaryOp
    | value = INTEGER                                                     #Integer
    | bool = 'true'                                                       #Boolean
    | bool = 'false'                                                      #Boolean
    | value = ID                                                          #Identifier
    | 'this'                                                              #This
    ;
