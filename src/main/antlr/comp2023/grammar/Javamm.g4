grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INTEGER : ([0-9] | [1-9][0-9]*) ;
ID : [$_a-zA-Z_][$_a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

COMMENT : '/*' .*? '*/' -> skip ;
COMMENT2 : '//' ~[\r\n]* -> skip ;

program
    :  (importDeclaration)* classDeclaration EOF;

importDeclaration
    :'import' library+=ID ( '.' library+=ID )* ';'
    ;

classDeclaration
    :'class' className=ID ( 'extends' extendsClass=ID )? '{' ( varDeclaration )* ( methodDeclaration )*'}'
    ;

varDeclaration
    :type name=ID ';'
    ;

methodDeclaration
    : (instanceMethodDeclaration| mainMethodDeclaration) ;

instanceMethodDeclaration
    : ('public'|'private'|'protected')?  type methodName=ID '(' ( fieldDeclaration ( ','fieldDeclaration )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'
;
mainMethodDeclaration
    : ('public')? 'static' 'void' methodName='main' '(' 'String' '[' ']' var=ID ')' '{' ( varDeclaration )* ( statement )* '}'
    ;

fieldDeclaration
    :type name=ID
    ;

type locals[boolean isArray=false, boolean isPrimitive=true]
    : typeDeclaration=('byte'|'short'|'int'|'long'|'float'|'double'|'boolean'|'char')array='[' ']' {$isArray=true;}
    | typeDeclaration=('byte'|'short'|'int'|'long'|'float'|'double'|'boolean'|'char')
    | typeDeclaration='String' {$isPrimitive=false;}
    | typeDeclaration=ID {$isPrimitive=false;}
    ;


statement
    :  '{' ( statement )* '}'                               #CodeBlockStmt
    | 'if' '(' expression ')' statement 'else' statement    #IfStmt
    | 'while' '(' expression ')' statement                  #WhileSTmt
    | expression ';'                                        #ExprStmt
    | var=ID '=' expression ';'                             #Assignment
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
    | expression op=('++'|'--')                                           #PostfixOp
    | op=('++'|'--'|'+'|'-') expression                                   #UnaryOp
    | expression op=( '*' | '/' | '%') expression                         #BinaryOp
    | expression op=( '+' | '-') expression                               #BinaryOp
    | expression op=('<<'|'>>') expression                                #BinaryOp
    | expression op=('<'|'>'|'<='|'>=') expression                        #BinaryOp
    | expression op=('=='|'!=') expression                                #BinaryOp
    | expression op='&' expression                                        #BinaryOp
    | expression op='^' expression                                        #BinaryOp
    | expression op='|' expression                                        #BinaryOp
    | expression op='&&' expression                                       #BinaryOp
    | expression op='||' expression                                       #BinaryOp
    | value = INTEGER                                                     #Integer
    | bool = 'true'                                                       #Boolean
    | bool = 'false'                                                      #Boolean
    | value = ID                                                          #Identifier
    | 'this'                                                              #This
    ;
