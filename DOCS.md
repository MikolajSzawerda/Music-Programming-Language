Język umożliwiający wygenerowanie utworów muzycznych w postaci MIDI. Język posiada specjalny zapis nutowy oraz udostępnia generatory.

```
((E, 4) w | (G, 4) w | (D, 4) w) & ((C, 4) w | (D, 4) w | (F, 4) w);
[[E, G, D] |> mel, [C, D, F] |> mel]{dur=w, oct=4} |> harm;

a = [E, G, D]{oct=4} |> mel;

b = a |> 
    repeat 2 |>
    transpose -1 |>
    concat a; |>
    harm |>
    track Guitar;
    
randGen = with(Scale scale, Rythm rythm) -> Phrase {
    if(scale |> isEmpty || rythm |> isEmpty){
        "Provided scale or rythm is empty" |> panic;
    }
    
    seed = (scale |> len * rythm |> len) % 3+1;
    maxLen = [scale |> len, rythm |> len] |> max;
    lowestNote = scale |> argmin;
    Template form;
    for(Int i in 1->seed){
        line = [rand() % maxLen <| dumb_temp 1->(rand()%4+1)*maxLen] |> mel;
        form &= line;
    }
    
    return form>>scale*form>>rythm;
} 

    
[a |> track Piano, b, markov(["song1.mid", "song2.mid"], 2), [C, E, G] |> randGen [q, w, h] |> track BagPipe] |>
    song 120, 60 |>
    export "demo2.mid";
```

## EBNF

```
Program             := {Statement ";"};
Statement           := Declaration |
                       Assigment |
                       Expression;

Declaration         := [Type] identifier ["=" Expression];
Assigment           := identifier assig_op Expression;
Expression          := LambdaExpression |
                       ValueExpression [PipeExpression];

PipeExpression      := "|>" { inline_func_call "|>"} inline_func_call;
inline_func_call    := identifier [arguments_list];
arguments_list      := ValueExpression {"," ValueExpression};

LambdaExpression    := "with" parameters_list "->" Type Block
parameters_list     := "(" [parameter {"," parameter}] ")";
parameter           := Type identifier;
Block               := "{" {Statement | ControlStatement} ";" "}"
ControlStatement    := IfStmt |
                       ForStmt |
                       ReturnStmt;

IfStmt              := "if" "(" ValueExpression ")" Block ["else" IfStmt | Block];
ForStmt             := "for" "(" Type identifier "in" ValueExpression ")" Block;

ValueExpression     := MathExpr [ModifierExpr]; 

ModifierExpr        := "{" modifier_item {"," modifier_item } "}"; 
modifier_item       := identifier "=" ValueExpression; 

MathExpr            := hfactor {h_op hfactor};
h_op                := ">>" | "^" | "->";
hfactor             := factor | "(" factor ")";
factor              := term {mul_op term}
mul_op              := "*" | "/" | "%" | "&&" | "&";
term                := value {add_op value};
add_op              := "+" | "-" | rel_op | "|";
rel_op              := "==" | "<=" | ">=" | "!=";
assig_op            := "=" | "|=" | "&=" | "*=" | "^=" | "%=" | "+=" | "-=" | "/=";
value               := simple_value |
                       ArrayExpr;
simple_value        := IdOrFuncCall |
                       literal |
                       NoteExpr;
IdOrFuncCall        := identifier ["(" arguments_list ")"]
NoteExpr            := Pitch [Duration];
Pitch               := "(" pitch_name "," int_lit ")" | pitch_name;
Duration            := rythm_lit;
ArrayExpr           := "[" ValueExpression ({"," ValueExpression} | ComprExpr) ]";          
ComprExpr           := "<|" identifier ValueExpression;
```
