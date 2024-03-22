**Temat**: Język operacji na typach muzycznych z możliwością generacji do pliku MIDI
**Autor**: Mikołaj Szawerda

## Charakterystyka
- silne statyczne typowanie
- obiekty są niemutowalne i przekazywane przez wartość

### Funkcjonalności

- reprezentacja zapisu nutowego(w postaci drzewiastej)
    - sekwencja nut - operator `|` `C | E | G`
    - równoczesne zagranie - operator `&` `C & E G`
- możliwość zmiany właściwości reprezentacji nutowych - składnia modifier `[(C, 4) q, C, E]{oct=2, dur=q}` - zmiana długości i oktawy dla wszystkich elementów, które nie mają podanych wartości wprost
- nakładanie szablonu struktury drzewiastej na liniową strukturę(Scale, Rythm, Phrase, Tablica) `(0 | 1 & 0)>>[E, G, D]`
- możliwość łańcuchowania operacji - operator `|>`(wyjście przekazuje jako pierwszy argument funkcji w kolejnym stopniu) `[1,2,3] |> concat [1,2] |> len`
- możliwość definiowania lambdy - składnia `with(Parameters...)->ReturnType {...}`
- możliwość deklaratywnego tworzenia listy - składnia `[fun(x) <| x iterable]`
- funkcje wbudowane do deklaratywnego odczytu i zapisu midi `open("name.mid", Phrase, 1)` i `export("name.mid")`

```
((E, 4) w | (G, 4) w | (D, 4) w) & ((C, 4) w | (D, 4) w | (F, 4) w);
[[E, G, D] |> mel, [C, D, F] |> mel]{dur=w, oct=4} |> harm;

let a = [E, G, D]{oct=4} |> mel;

let b = a |> 
        repeat 2.0 as Int |>
        transpose -1 |>
        concat a; |>
        harm |>
        track Guitar;

let c = "song.mid" |>
        open Track 0 |>
        head+100; //shoud be parsed as head(x, 100)
    
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
    
let x1 = [0, 1, 2] |> concat [3,4] |>len+2;//panic
let x2 = ([0, 1, 2] |> concat [3,4] |> dot (1+3*4^7-*1+3/6*12) |>len)+2;

```

## EBNF

```
Program             := {Statement ";"};
Statement           := DeclOrAssig |
                       Expression;

DeclOrAssig         := Type identifier ["=" Expression] |
                       identifier assig_op Expression;

Expression          := LambdaExpression |
                       ValueExpression;

LambdaExpression    := LambdaDecl | "(" LambdaDecl ")";
LambdaDecl          := "with" parameters_list "->" Type Block {PipeExpression}
parameters_list     := "(" [parameter {"," parameter}] ")";
parameter           := Type identifier;
Block               := "{" {Statement | ControlStatement} ";" "}"
ControlStatement    := IfStmt |
                       ForStmt |
                       ReturnStmt;

PipeExpression      := "|>" inline_func_call;
inline_func_call    := identifier [arguments_list];
arguments_list      := Expression {"," Expression};                       

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
hfactor             := casted_value | "(" casted_value ")";
casted_value        := value "as" Type;
value               := (unary_value | ArrayExpr) {PipeExpression};
unary_value         := ([unary_op] (IdOrFuncCall | literal)) | NoteExpr;                       
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


## Analiza wymagań

### Zapis utworu w postaci drzewa

| Typ liniowy | Interpretacja                     |
|-------------|-----------------------------------|
| Scale       | Uporządkowana lista wysokości nut |
| Rythm       | Uporządkowana lista długości nut  |
| []T         | Lista elementów typu T            |

| Typ drzewiasty | Interpretacja                                |                                
|----------------|----------------------------------------------|
| Progression    | Wysokości nut drzewo elementów Scale         |
| Groove         | Długości nut drzewo elementów Rythm          |
| Phrase         | Nuty(wysokość, długość)                      |                      
| Template       | Indeksy elementu z branej liniowej struktury |

- operator `|` tworzy listę w której pierwszy węzeł jest "wartownikiem" posiada domyślne wartości, dla węzłów, które któreś z pól mają NULL
- operator `&` tworzy listę, której elementy mają wspólny węzęł pełniący rolę korzenia(oraz wartownika z domyślnymi wartościami)
- węzły `Phrase` posiadają wysokość oraz długość, węzły `Template` posiadają wszystkie dostępne pola typów wbudowanych
- operacja `>>` splotu szablonu z strukturą liniową przechodzi drzewo i buduje strukturę identyczną jak struktura szablonu
gdzie węzłami są elementy z liniowej struktury o indeksach z danego węzła szablonu z nałożonymi modyfikatorami(mutacją pól) z szablonu
- operacja złożenia `*` Progression z Groove przechodzi równocześnie oba drzewa i tworzy Phrase o wysokościach z Progression i długościach z Groove

### Zapis i odczyt pliku midi

- funkcja `open(String filename, Type t, [Int trackNumber]` czyta podany plik midi i parsuje go do podanego typu muzycznego
- funkcja `export(Song s|Track t, String fileName)` zapisuje obiekt piosenki lub ścieżki do pliku midi - typy `Song` oraz `Track`
posiadają pola tempo i długość 