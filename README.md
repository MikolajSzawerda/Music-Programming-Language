**Temat**: Język operacji na typach muzycznych z możliwością generacji do pliku MIDI

**Autor**: Mikołaj Szawerda

## Charakterystyka

- silne statyczne typowanie
- obiekty są mutowalne i przekazywane przez referencję

### Funkcje

- reprezentacja zapisu nutowego(w postaci drzewiastej)
    - sekwencja nut - operator `|` `C | E | G`
    - równoczesne zagranie - operator `&` `C & E G`
- możliwość zmiany właściwości reprezentacji nutowych - składnia modifier `[(C, 4) q, C, E]{oct=2, dur=q}` - zmiana
  długości i oktawy dla wszystkich elementów, które nie mają podanych wartości wprost
- nakładanie szablonu struktury drzewiastej na liniową strukturę(Scale, Rythm, Tablica) `(0 | 1 & 0)>>[E, G, D]`
- możliwość łańcuchowania operacji - operator `|>`(wyjście przekazuje jako pierwszy argument funkcji w kolejnym
  stopniu) `[1,2,3] |> concat [1,2] |> len`
- możliwość definiowania lambdy - składnia `with(Parameters...)->ReturnType {...}`
- możliwość deklaratywnego tworzenia listy - składnia `[fun(x) <| x iterable]`
- funkcje wbudowane do deklaratywnego odczytu i zapisu midi `open("name.mid", Phrase, 1)` i `export("name.mid")`
- obsługa operacji `if(Bool){} else{}` oraz pętli `for(T x in Expr){}`

```
((E, 4) w | (G, 4) w | (D, 4) w) & ((C, 4) w | (D, 4) w | (F, 4) w);
[[E, G, D] |> mel, [C, D, F] |> mel]{dur=w, oct=4} |> harm;

let a = [E, G, D]{oct=4} |> mel;

let b = a |> 
        repeat 2.0 as Int |>
        transpose -1 |>
        concat a |>
        harm |>
        track Guitar;

let c = "song.mid" |>
        open Track 0 |>
        head+100; //shoud be parsed as head(x, 100)
        
(Int, Int)->Int NWD;

NWD = with(Int a, Int b)->Int {
    if(b!=0){
        return NWD(b, a%b);
    }
    return a;
};
    
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
    
[a |> track Piano, b, markov(["song1.mid", "song2.mid"], 1)((C, 4) q), [C, E, G] |> randGen [q, w, h] |> track BagPipe] |>
    song 120, 60 |>
    export "demo2.mid";
    
let x1 = [0, 1, 2] |> concat [3,4] |>len+2;//panic
let x2 = ([0, 1, 2] |> concat [3,4] |> dot (1+3*4^7-*1+3/6*12) |>len)+2;

```

## EBNF

```
Program             := {Statement ";"};
Statement           := DeclOrAssig |
                       Expression |
                       IfStmt |
                       ForStmt;

DeclOrAssig         := (Type|"let") identifier ["=" Expression] |
                       identifier assig_op Expression;

Expression          := LambdaExpression |
                       ValueExpression;

LambdaExpression    := LambdaDecl | "(" LambdaDecl ")";
LambdaDecl          := "with" parameters_list "->" (Type|"Void") Block {PipeExpression}
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
ReturnStmt          := "return" [Expression];
ValueExpression     := MathExpr [ModifierExpr]  {PipeExpression}; 

ModifierExpr        := "{" modifier_item {"," modifier_item } "}"; 
modifier_item       := identifier "=" Expression; 

MathExpr            := and_term {or_op and_term};
and_term            := rel_term {and_op rel_term};
rel_term            := add_term {rel_op add_term};
add_term            := term {add_op term};
term                := factor {mul_op factor};
factor              := hfactor {h_op hfactor};
hfactor             := (value | "(" ValueExpression ")" ) ["as" Type];
value               := (unary_value | ArrayExpr);
unary_value         := ([unary_op] (IdOrFuncCall | literal)) | NoteExpr;                       
IdOrFuncCall        := identifier ["(" arguments_list ")"]
NoteExpr            := Pitch [Duration] | "rest" rythm_lit;
Pitch               := "(" pitch_lit "," Expression ")" | identifier;
Duration            := rythm_lit;
ArrayExpr           := "[" Expression ({"," Expression} | ComprExpr) ]";          
ComprExpr           := "<|" identifier Expression;

Type                := LitType |
                       CpxType |
                       FuncType;
FuncType            := "lam" (" [type_list] ")" "->" (Type|"Void");
type_list           := Type {"," Type};

LitType             := Int | Double |
                       Bool | String;
CpxType             := Scale | Rythm | Progression | Groove |
                       Phrase | Track | Song |
                       []Type;

h_op                := ">>" | "^" | "->";
mul_op              := "*" | "/" | "%" | "&";
add_op              := "+" | "-" | "|";
rel_op              := "==" | "<=" | ">=" | "!=" | ">" | "<";
and_op              := "&&";
or_op               := "||";
unary_op            := "-" | "+";
assig_op            := "|=" | "&=" | "*=" | "^=" | "%=" | "+=" | "-=" | "/=" | "=";

identifier          := letter { letter | digit };
literal             := int_lit | float_lit | string_lit;
int_lit             := "0" | non_zero_digit {digit};
float_lit           := int_lit "." [ int_lit ];
string_lit          := \" {char} \";
pitch_lit           := [A-G](#);
rythm_lit           := (dl|l|w|h|q|e|s|t)(_(d|t));
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
| Progression    | Wysokość nut drzewo elementów Scale          |
| Groove         | Długości nut drzewo elementów Rythm          |
| Phrase         | Nuty(wysokość, długość)                      |                      
| Template       | Indeksy elementu z branej liniowej struktury |

- operator `|` tworzy listę w której pierwszy węzeł jest "wartownikiem" posiada domyślne wartości, dla węzłów, które
  któreś z pól mają NULL
- operator `&` tworzy listę, której elementy mają wspólny węzęł pełniący rolę korzenia(oraz wartownika z domyślnymi
  wartościami)
- przy użyciu składni modyfikatora `{id=x,id2=x1}` zmianie ulegają wartownicy
- węzły `Phrase` posiadają wysokość oraz długość, węzły `Template` posiadają wszystkie dostępne pola typów wbudowanych
- operacja `>>` splotu szablonu z strukturą liniową przechodzi drzewo i buduje strukturę identyczną jak struktura
  szablonu
  gdzie węzłami są elementy z liniowej struktury o indeksach z danego węzła szablonu z nałożonymi modyfikatorami(mutacją
  pól) z szablonu
- operacja złożenia `*` Progression z Groove przechodzi równocześnie oba drzewa i tworzy Phrase o wysokościach z
  Progression i długościach z Groove

### Zapis i odczyt pliku midi

- funkcja `open(String filename, Type t, [Int trackNumber]` czyta podany plik midi i parsuje go do podanego typu
  muzycznego
- funkcja `export(Song s|Track t, String fileName)` zapisuje obiekt piosenki lub ścieżki do pliku midi - typy `Song`
  oraz `Track`
  posiadają pola tempo i długość, przejście przez drzewo BFS

### Funkcje wbudowane

```
mel([]T)                        := tworzy z listy sekwencję |
harm([]T)                       := tworzy z listy drzewo &
concat(T, [])                   := dołącza do listy element
isEmpty/len/head/tail/repeat    := operacje na listach
panic(String msg)               := rzuca wyjątkiem
transpose/speed                 := operacje muzyczne
at([]T, Int)                    := zwraca element z listy
```

### Operatory

| Operator | Interpretacja                             |
|----------|-------------------------------------------|
| a->b     | tworzy listę o zakresie od a do b         |
| a>>b     | nałożenie templat a na b                  |
| a*b      | mnożenie, lub złożenie                    |
| a        | \|>b b1,b2,...     pipe b(a, b1, b2, ...) |

### Dostępne modyfikatory dla typów

| Typ         | Modyfikatory                     |
|-------------|----------------------------------|
| Scale       | oct                              |
| Rythm       | dur                              |
| Progression | oct                              |
| Groove      | dur                              |
| Phrase      | oct, dur                         |
| Track       | oct, dur, instrument, tempo, len |
| Song        | tempo, len                       |

oct - oktawa, dur długość nuty, tempo - BPM, len - długość w sekundach

### Rzutowanie

Język zapewnia składnię `x as Type`, która realizuje rzutowanie, po za oczywistymi konwersjami dostępne są

| old         | new              |
|-------------|------------------|
| Scale       | Phrase(dur=NULL) |
| Progression | Phrase(dur=NULL) |
| Rythm       | Groove           |
| Scale       | Progression      |
| Phrase      | Progression      |
| Phrase      | Groove           |

### Obsługa błędów

Język nie zapewnia mechanizmu przechwytywania wyjątków, istnieje natomiast możliwość rzucenia wyjątku przy pomocy
funkcji `panic(String)`, która skutkuje wyświetleniem treści komunikatu oraz zatrzymaniem interpretera

## Technologie

### Język

Java 21

### Budowanie

Gradle

### Testowanie

JUnit5, Mockito, assertj

### Zewnętrzne zależności

- `javax.sound.midi` - wbudowana biblioteka w java pozwalająca wykonywać niskopoziomowe operacje na pliku midi -
  operowanie na poziomie Event'ow KeyOn KeyOff

- `log4j` - bardziej przejrzyste logowanie niż System.out.println()

- `lombok` - generacja Builder'ow i boilerplatu, oraz null checkow

## Struktura i testowanie

### Lexer

odpowiedzialny za zamianę reprezentacji tekstowej na tokenową

Testy:

- rozponawanie literałów liczbowych/tekstowych/nutowych
- rozpoznawanie słów kluczowych języka
- rozpoznawanie identyfikatorów
- rozpoznawanie komentarzy, tokeny podawane z pozycją w pliku

### Parser

budowa tablicy symboli i budowa AST
testy

- budowa tablicy z odpowiednimi wartościami
- rozpoznawanie produkcji i odpowiednia struktura drzewa

### AST

- drzewiasta reprezentacja kodu
- przechowuje produkcje w postaci obiektów, w raz z dostępnym dla nich kontekstem

### SemanticAnalyzer

- sprawdzenie poprawności sekwencji produkcji oraz prosta optymalizacja kodu

Testy:

- wywołanie dla przypadków pozytywnych i negatywnych
- optymalizacja kodu nieosiągalnego, nieużywanego

### Interpreter

- wykonuje program poprzez przechodzenie AST
- implementacja własnych typów
  Testy:
- wykonanie przykładowych kodów źródłowych wraz z oczekiwanymi zmianami stanu
- obsługa wystąpienia "panic" i wypisanie komunikatu

### MusicTree

moduł implementujący drzewo typów muzycznych z iteratorem wg kolejności wystąpienia

### MidiReader

konwertuje plik midi na reprezentacje użytą w AST/interpreterze

Testy:

- odczyt pliku midi i konwersja na podany typ Progression/Groove/Phrase/Track/Song

### Markov

moduł odpowiedzialny za reprezentację i operacje na łańcuchach markova

Testy

- Inicjalizacja z podaną macierzą
- Przyjęcie Phrase

Generowanie kolejnych nut na podstawie aktualnego stanu i macierzy przejść

### MidiWriter

moduł odpowiedzialny za generowanie pliku midi z podanego obiektu Song/Track

Testy:

- generacja midi z zachowaniem ustalonego tempa/długości
