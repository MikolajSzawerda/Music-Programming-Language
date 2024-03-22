Język umożliwiający wygenerowanie utworów muzycznych w postaci MIDI. Język posiada specjalny zapis nutowy oraz udostępnia generatory.

```
((E, 4) w | (G, 4) w | (D, 4) w) & ((C, 4) w | (D, 4) w | (F, 4) w);
[[E, G, D] |> mel, [C, D, F] |> mel]{dur=w, oct=4} |> harm;

let a = [E, G, D]{oct=4} |> mel;

let b = a |> 
        repeat 2 |>
        transpose -1 |>
        concat a; |>
        harm |>
        track Guitar;
    
let randGen = with(Scale scale, Rythm rythm) -> Phrase {
    if(scale |> isEmpty || rythm |> isEmpty){
        "Provided scale or rythm is empty" |> panic;
    }
    
    let seed = (scale |> len * rythm |> len) % 3+1;
    let maxLen = [scale |> len, rythm |> len] |> max;
    let lowestNote = scale |> argmin;
    Template form;
    for(Int i in 1->seed){
        let line = [rand() % maxLen <| dumb_temp 1->(rand()%4+1)*maxLen] |> mel;
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
Statement           := DeclOrAssig |
                       Expression;

DeclOrAssig         := Type identifier ["=" Expression] |
                       identifier assig_op Expression;
Expression          := (LambdaExpression | ValueExpression) [PipeExpression];

PipeExpression      := "|>" { inline_func_call "|>"} inline_func_call;
inline_func_call    := identifier [arguments_list];
arguments_list      := Expression {"," Expression};

LambdaExpression    := "with" parameters_list "->" Type Block
parameters_list     := "(" [parameter {"," parameter}] ")";
parameter           := Type identifier;
Block               := "{" {Statement | ControlStatement} ";" "}"
ControlStatement    := IfStmt |
                       ForStmt |
                       ReturnStmt;

IfStmt              := "if" "(" Expression ")" Block ["else" IfStmt | Block];
ForStmt             := "for" "(" Type identifier "in" Expression ")" Block;

ValueExpression     := MathExpr [ModifierExpr]; 

ModifierExpr        := "{" modifier_item {"," modifier_item } "}"; 
modifier_item       := identifier "=" Expression; 

MathExpr            := and_term {or_op and_term};
and_term            := add_term {and_op add_term};
add_term            := term {add_op term};
term                := factor {mul_op factor};
factor              := hfactor {h_op hfactor};
hfactor             := value | "(" value ")";
value               := unary_value |
                       ArrayExpr;
unary_value         := ([unary_op] IdOrFuncCall | literal) | NoteExpr;                       
IdOrFuncCall        := identifier ["(" arguments_list ")"]
NoteExpr            := Pitch [Duration];
Pitch               := "(" pitch_name "," int_lit ")" | pitch_name;
Duration            := rythm_lit;
ArrayExpr           := "[" Expression ({"," Expression} | ComprExpr) ]";          
ComprExpr           := "<|" identifier Expression;

Type                := LitType |
                       CpxType |
                       FuncType |
                       let;
FuncType            := "(" [type_list] ")" "->" (Type|"Void");
type_list           := Type {"," Type};

LitType             := Int | 
                       Double |
                       Bool |
                       String;
CpxType             := Scale |
                       Rythm |
                       Phrase |
                       Track |
                       Song |
                       []Type;

h_op                := ">>" | "^" | "->";
mul_op              := "*" | "/" | "%" | "&";
add_op              := "+" | "-" | "|";
rel_op              := "==" | "<=" | ">=" | "!=";
and_op              := "&&";
or_op               := "||";
unary_op            := "-" | "+";
assig_op            := "|=" | "&=" | "*=" | "^=" | "%=" | "+=" | "-=" | "/=";                                              
```
