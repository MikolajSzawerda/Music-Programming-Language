**Tytuł**: Interpreter języka generatorów melodii midi

**Autor**: Mikołaj Szawerda

## Opis zakładanej funkcjonalności

Projekt ma na celu stworzenie interpretera języka ogólnego przeznaczenia z dodatkowymi typami i funkcjami umożliwiającymi deklaratywny odczyt i warunkowanie generowanego pliku MIDI. Język będzie udostępniał typy: wysokości nut, czasu trwania nuty, generator melodii deterministyczny i stochastyczny, tworzenie finalnego pliku midi z podanych generatorów.

### Założenia
Zakłada się że wejściowe pliki MIDI zawierają w danym punkcie czasowym pojedynczą nutę.

## Charakterystyka języka

- język typowany statycznie silnie
- język wspiera tylko wbudowane typy prymitywne oraz wbudowane typy złożone
- język wspiera operatory arytmetyczne/logiczne/znakowe/relacji porządku/specjalne muzyczne
- język wspiera dla danej jednostki kontekst globalny, nie ma konieczności deklaracji funkcji `main()`
- zmienne przesłaniane do najbliższego kontekstu
- język wspiera operację warunkową `if` oraz pętlę `for` - iteracja tylko po typach udostępniających iterator
- język wspiera deklarację funkcji:
    - typy prymitywne przekazywane przez wartość, złożone przez referencję
    - brak parametrów domyślnych
    - możliwość przeciążenia
    - wsparcie dla funkcji wyższego rzędu
    - wsparcie dla rekurencji
- język nie wspiera obsługi błędów w czasie wykonania programu - wystąpienie wyjątku kończy program z komunikatem
- język wspiera operacje odczytu i zapisu plików MIDI, oraz manipulację danymi poprzez typy wbudowane
- język wspiera operacje wypisania na standardowe wyjście

### Przykłady kontrukcji językowych

- silne statyczne typowanie

```
fun add(Int a, Int b) -> Int{
  return a+b;
}

add("5", 1); //compilation error

if("5"==5){ //compilation error
  //...
}

if(Int("5")==5){ //correct
  //...
}

Int a = 5;
a="5"; //compilation error
Int b = 5.0; //compilation error
if(Int(5.0)==5){ //correct
  //...
}
```

- wbudowane typy prymitywne

```
Int a = 5;
Double b = 4.0;
Bool c = true;
Float e;
e=4.0;
const Int f = Int(b)*a+a/2;
f=4; //compilation error
if(a+f>Int(b)){
  const Int a = round(10.0*b);
  String k = String(a);
}
```

- wbudowane typy złożone

```
Opt[Int] o = Opt.of(1);
Opt[Int] o1 = Opt.empty();
Opt[Int] o2 = o1.or(o);
Pitch a = C1;
Pitch[] a1 = [C2, D4];
Rythm b = R_Q;
Rythm[] b1 = [R_Q, R_E];
Note d = {R_Q, C1};
Note d1 = {R_Q}; //pause
Motive d = [{b, a}, {R_H, D2}, {R_Q}];
Markov e = Markov([{b, a}, {R_H, D2}, {R_Q}]);
Markov e1 = Markov(d);
Markov e2 = with "song.mid";
Markov e3 = with ("song.mid", "song1.mid");
Markov e4 = Markov(d) //panic - at least len=2
Composer f = Composer.with(e).with(d).with(e);
Song g = f.gen(125, 60); //125 BPM, 60 seconds
```

- operatory

```
Int a = 5;
Int b = 10;

a += b;
a = (a+b)*(a-b)/(-a+2*b);
Bool isBigger = a > b;

String a1 = "Hello";
String a2 = "world;

if(a1+" "+a2 == "Hello world") {
  print(a1);
} else {
  print(a2);
}

if(a % 15 == 0) {
  print("foobar_"+String(a));
}

Pitch a = D1;
a.transpose(1); // D#1
a.transpose(-2); //C#1
Rythm b = R_Q;
b.speed(2); //R_E
Motive b = [{R_Q, D1}, R_Q];
b.transpose(1); // {R_Q, D#1}, R_Q
b.speed(2); //{R_E, D#1}, R_E
```

- kontekst globalny

```
Markov a;

fun sum(Int a, Int b) -> Int {
  return a+b;
}

fun enrichWithLick(Motive b) -> Void{
  a << b;
}

a << [{R_Q, C1}];
enrichWithLick([{R_Q}]);
print(a);
```

- pętla for

```
Range a = Range(3);
Motive b = [{R_Q, D1}, R_Q, {R_Q, C1}];
Markov c = Markov(b);

for(Int i in a){
  print(i);
} // 1 2

for(Note n in b) {
  print(n);
} //{R_Q, D1}, R_Q, {R_Q, D1}

for(Note n in b.gen()) {
  print(n);
} //{R_Q, D1}, R_Q, {R_Q, C1}, ... inf loop(b[i%len(b)])

for(Note n in c){
  print(n);
}//compilation error

for(Note n in c.gen({R_Q, C1})){
  print(n);
} //{R_Q, C1}, {?}, ... inf loop
```

- deklaracja funkcji

```
Pitch a = C1;
fun transpose(Pitch a) -> Void {
  a.transpose(1);
}

print(transpose(a)); //C#1

fun transpose2(val Pitch a) -> Void {
  a.transpose(1);
}

print(transpose2(a)); //C#1

fun transpose3(Pitch a) -> Void {
  a = B3;
}

print(transpose3(a)); //B3

fun transpose4(const Pitch a) -> Void {
  a = C4; //compilation error
}

fun nwd(Int a, Int b) -> Int
{
    if(b != 0) {
    	return nwd(b,a%b);
    }
    return a;
}

fun buissnesLogic(Int age) -> Opt[Motive]{
  if(age > 18){
    return Opt.of([{R_Q, C1}, R_Q]);
  }
  return Opt.empty();
}

Motive lick = buisnesLogic(29)
  .map(Motive (Motive a)->{
    a.transpose(1);
    return a;
  })
  .or([{R_Q, D1}]);

fun transformMelody(const Motive a, (Motive)->Motive transformation) -> Motive {
  Motive b = Motive(a);
  return transformation(b);
}

```

- sytuacje wyjątkowe

```

Markov a = with "lick1.mid" //panic("File not found");

fun detonateProgram() -> Void {
  panic "Nuke nuke";
}

detonateProgram(); //panic("Nuke nuke");
```

- język wspiera deklaratywny odczyt plików midi

```
Pitch[] a = with "lick.mid";
print(a); //C1, C2, C3
Rythm[] b = with "lick.mid";
print(b); //Q E H
Motive c = with "lick.mid";
print(c); //{R_Q, C1}, {R_E, C2}, {R_H, C3}
Markov d = with "lick.mid"; // d << c;
print(d); //potentialy will print matrix line by line?
```

- język wspiera zapis wynikowego pliku midi

```
Motive a = with "lick.mid";
a.transpose(1);
Markov b = Markov(a);

Composer c = Composer
  .with(a)
  .with(b, {R_Q, C1});

Song d = c.gen(125, 60); //125BPM 60 seconds

d.toFile("song.mid");

b << [{R_Q, D1}, R_Q]; //next song gen will use new values

c.with([{R_Q, C1}, R_Q]).gen(60, 10).toFile("song2.mid");
```

## Gramatyka

```
identifier      = letter { letter | unicode_digit } .
decimal_digit   = "0" … "9" ;
decimal_digits  = decimal_digit { decimal_digit };
int_lit         = "0" |
                  ( "1" … "9" ) [ decimal_digits ];
float_lit       = decimal_digits "." [ decimal_digits ];
string_lit      = \" {unicode_char} \";
pitch_lit       = ([A-G](#)?[1-7]);
rythm_lit       = R_((D|T)_)(DL|L|W|H|Q|E|S|T)
type_name       = Int | Double | Bool |
                  Pitch | Rythm | Note | Motive |
                  Markov | Composer | Song | Gen;
gen_type_name   = Opt |
                  Iter;
Type            = type_name |
                  ArrayType |
                  GenericType |
                  FuncType;
ArrayType       = type_name "[" "]";
GenericType     = gen_type_name "[" type_name "]";
FuncType        = "(" Type { "," Type } ")" "->" Type;

Program         = StatementList | { FunctionDecl };

FunctionDecl    = "fun" FunctionName Parameters "->" (Type|"Void") Block;
FunctionName    = identifier .
Parameters      = "(" [ ParameterList ] ")";
ParameterList   = ParameterDecl { "," ParameterDecl };
ParameterDecl   = [ "val" |  "const" ] Type identifier;

Block           = "{" StatementList "}";
StatementList   = { Statement ";" };
Statement       = Declaration |
                  SimpleStmt |
	              ReturnStmt |
                  BreakStmt |
                  ContinueStmt |
                  PanicStmt |
                  IfStmt |
                  ForStmt;
                  
Declaration     = ConstDecl |
                  VarDecl;
VarDecl         = Type identifier ("=" Expression) ;
ConstDecl       = "const" VarDecl;

SimpleStmt      = ExpressionStmt |
                  IncDecStmt |
                  Assignment;

ExpressionStmt  = Expression;

Expression      = UnaryExpr |
                  Expression binary_op Expression |
                  MidiReadExpr;

MidiReadExpr    = "with" (string_lit | "("string_lit {"," string_lit} ")");
UnaryExpr       = PrimaryExpr |
                  unary_op UnaryExpr;

binary_op       = "||" | "&&" | rel_op | add_op | mul_op;
rel_op          = "==" | "!=" | "<" | "<=" | ">" | ">=";
add_op          = "+" | "-";
mul_op          = "*" | "/" | "%" | "<<";
unary_op        = "+" | "-";

PrimaryExpr     = Operand |
                  PrimaryExpr Selector |
                  PrimaryExpr Index |
                  PrimaryExpr Arguments;

Operand         = PrimitLit | ObjectLit | NoteLit | ArrayLit | LambdaLit;
PrimitLit       = int_lit | float_lit | string_lit | pitch_lit | rythm_lit;
ObjectLit       = Type Arguments;
NoteLit         = "{" rythm_lit ["," pitch_lit] "}";
ArrayLit        = "[" [ ArrayLitList ]  "]";
ArrayLitList    =  ArrayElem {"," ArrayElem};
ArrayElem       = PrimitLit | ObjectLit | NoteLit;

LambdaLit       = Type "fun" Parameters "->" Block;


Selector        = "." identifier;
Index           = "[" Expression "]";
Arguments       = "(" [ ExpressionList ] ")";
ExpressionList  = Expression { "," Expression };

IncDecStmt      = Expression ( "++" | "--" );

Assignment      = identifier [ add_op | mul_op ] "=" Expression;

ReturnStmt      = "return" [ Expression ];

BreakStmt       = "break";

ContinueStmt    = "continue";

PanicStmt       = "panic" string_lit;

IfStmt          = "if" "(" ExpressionStmt ")" Block [ "else" ( IfStmt | Block ) ];

ForStmt         = "for" "(" type_name identifier "in" identifier ")" Block;
```


## Analiza wymagań

### Język typowany statycznie silnie
- śledzenie typu wyrażenia
- zapewnienie operacji konwersji

#### Konwersja

|                                         | Result         |
|-----------------------------------------|----------------|
| Motive(R_Q)                             | [{EMPTY, R_Q}] |
| Pitch({C1, R_Q})                        | C1             |
| Rythm(Note(C1, R_Q))                    | R_Q            |
| Int(2.0)                                | 2              |
| Double(Int(1))                          | 1.0            |
| Pitch[](Motive{[{C1, R_Q}, {C2, R_E}]}) | [C1, C2]       |

### Język wspiera tylko wbudowane typy prymitywne oraz wbudowane typy złożone

| Token        | Typ | Opis                                                           |
|--------------|-----|----------------------------------------------------------------|
| Int          | p   | int32                                                          |
| Double       | p   | IEEE 64b                                                       |
| Bool         | p   | true,false                                                     |
| Unit         | p   | void - używany do procedur                                     |
| Opt[T]       | z   | Optional - udostepnia lepsze API do wartości null              |
| Iter[T]      | z   | iterator dla kolekcji typy T                                   |
| T[]          | z   | mutowalna lista                                                |
| Pitch        | z   | Wysokość nuty, tworzony z zastrzeżonych słów kluczowych        |
| Rythm        | z   | Długość trwania nuty, tworzony z zastrzeżonych słów kluczowych |
| Note         | z   | Nuta - określona wysokość i długość                            |
| Motive       | z   | lista nut, generator deterministyczny i iterator               |
| Markov       | z   | impl łańcucha markova, generator stochastyczny                 |
| Composer     | z   | builder, określa generatory z których zostanie stworzony Song  |
| Song         | z   | wewnętrznie Motive[], umożliwia zapis do midi                  |
| Gen          | z   | Nieskończony Iter[Note]                                        |
| ()->T1       | z   | funkcja anonimowa bez argumentowa                              |
| (T1)->T2     | z   | funkcja anonimowa z jednym argumentem                          |
| (T1, T2)->T3 | z   | funkcja anonimowa z dwoma argumentami                          |

#### Opt
- implementacja Optional'a
- `fun of(T a)->Opt[T]` - where `T in [Int, Double, Bool, Opt, Pitch, Rythm, Note, Motive, Markov, Composer, Song, T[]]` - metoda wytwórcza
- `fun map((T1)->T2)->Opt[T2]` - transformacja
- `fun filter((T1)->Bool)->Opt[T1]` - filtrowanie przez warunek
- `fun get()->T1` - gdy puste `panic`
- `fun or(()->T1)->T1` - gdy puste rezultat `()->T1`, w przeciwnym wypadku zawartość `this`

#### Iter
- implementacja iteratora
- `Iter[T]` where `T in [Int, Double, Bool, Opt, Pitch, Rythm, Note, Motive, Markov, Composer, Song, K[]]`
- `fun next()->T`, zwraca aktualny element i przechodzi do kolejnego węzła, `panic` gdy empty
- `fun hasNext()->Bool`

#### Pitch
- wysokośc nuty `([A-G](#)?[1-7])|EMPTY`, `EMPTY` umożliwia utworzenie pauzy
- `fun transpose(Int a)->Unit` - zmienia wysokość nuty o `a`, `panic`, gdy brak wspieranej wysokości
- `Pitch[]` - udostępnia `fun iter()->Iter[Pitch]`

#### Rythm
- długość trwania nuty

| Token | Długość      |
|-------|--------------|
| R_DL  | cztery takty |
| R_L   | dwa takty    |
| R_W   | takt         |
| R_H   | pół taktu    |
| R_Q   | ćwierć taktu |
| R_E   | 1/8 taktu    |
| R_S   | 1/16 taktu   |
| X_T   | 1/3 X        |
| X_D   | 3/2 X        |

- `fun speed(Int a)->Rythm` - zmiana długości rytmu o `a`, `panic` gdy nie wspierany

#### Note
- udostępnia metedy dla Rythm i Pitch


#### Gen
- generator nut, udostępnia nieskończony iterator
- przechowuje funkcję tworzącą iterator `()->Iter[Note]`, obecny iterator i gdy `hasNext()` zwraca false, tworzy nowy iterator
- `fun iter()->Iter[Note]`

#### Motive
- generator deterministyczny i iterator po Note[]
- `fun iter()->Iter[Note]`
- `fun gen()->Gen`

#### Markov
- generator stochastyczny
- `fun gen()->Gen`
- wewnętrznie reprezentowany przez dwa słowniki o typie `Map<Pitch, Pair<Int, List<Pair <Pitch, Int>>>` i `Map<Rythm, Pair<Int, List<Pair <Rythm, Int>>>`, gdzie np `map.get(Pitch.C1).first()` zawiera akumulowaną sumę `map.get(Pitch.C1).second()`
- `fun gen(Note initialState)->Gen` - przy utworzeniu generatora macierze zostają skopiowane do iteratora, przy czym każda wartość jest dzielona przez akumulowaną sumę, otrzymując w ten sposób macierz prawdopodobieństwa `Map<Pitch, List<Pair <Pitch, Double>>>`, `hasNext()` zawsze zwraca true, iterator posiada aktualny stan `(Pitch, Rythm)`, wywołanie `next()`, wybiera wiersz z macierzy i zadanymi prawdopodobieństwami losuje stan następny modifkując `(Pitch, Rythm)` w iteratorze (w implementacji złożone typy w strukturach danych oczywiście zostaną zastąpione własnymi typami), wywołanie `gen(Note)` rzuca wyjątkiem, gdy z podanego stanu początkowego nie można przejśc do innego - obiekt `Markov` nie został wypełniony/stan początkowy nie pojawił się w wprowadzonych danych

#### Composer
- pozwala wskazać które generatory będą użyte do stworzenia piosenki - przechowuje referencje
- `fun with(Motive m) ->Composer` oraz `fun with(Markov m, Note initState)` - zostają dołączeni odpowiednio do `List<Motive>` i `List<Pair<Markov, Note>>` w obiekcie
- `fun gen(Int tempo, Int duration) -> Song` - wewnętrznie dla  przypisanych obiektów do generacji tworzy `List<Gen>`, następnie dla określonego `tempo, duration` określa długość trwania odpowiednich tokenów nut i w pętli z każdego generatora uzyskuje kolejne nuty, finalnie otrzymując `List<List<Note>>` konstruując `Song`


#### Song
- obiekt przechowujący metadane, oraz nuty piosenki, użyty zostaje do wygenerowania midi

### Język wspiera operatory arytmetyczne/logiczne/znakowe/relacji porządku/specjalne muzyczne

#### Operatory


|                                        | Result                                 |
|----------------------------------------|----------------------------------------|
| C1 + C2                                | [C1, C2]                               |
| R_Q + R_H                              | [R_Q, R_H]                             |
| -[C1, C2]                              | [C2, C1]                               |
| [C1, C2, C3]*[R_Q, R_H]                | [{C1, R_Q}, {C2, R_H}]//Note           |
| [C1]+[C2]                              | [C1, C2]                               |
| [{R_Q, C1}]+[{R_Q, C2}]                | [{R_Q, C1}, {R_Q, C2}]                 |
| [{R_Q, C1}]*2                          | [{R_Q, C1}, {R_Q, C1}]                 |
| "a"*2                                  | "aa"                                   |
| "a"+" "+"a"                            | "a a"                                  |
| Markov()<< [{R_Q, C1}, {R_Q, C2}]      | Increase count for C1->C2, R_Q->R_Q    |
| Markov()<< [{R_Q}, {R_Q, C2}]          | Increase count for EMPTY->C2, R_Q->R_Q |
| Markov()+Markov()                      | Sum count matricies                    |
| [{R_Q}, {R_Q, C2}]==[{R_Q}, {R_Q, C2}] | true                                   |
| [{R_Q}]==[{R_Q}, {R_Q, C2}]            | false                                  |

### Język wspiera deklarację funkcji:
- typy prymitywne przekazywane przez wartość, złożone przez referencję
- wsparcie dla funkcji wyższego rzędu, wsparcie dla rekurencji - konieczność utworzenia domknięcia funkcji, oraz ramki stosu, oraz określenia maksymalnej głębokości

### Język nie wspiera obsługi błędów w czasie wykonania programu
- wystąpienie wyjątku kończy program z komunikatem
- wsparcie dla operacji `panic String` pozwalającej rzucić wyjątkiem w czasie wykonania, oraz obsługa przez interpreter końca programu

### Język wspiera operacje odczytu i zapisu plików MIDI, oraz manipulację danymi poprzez typy wbudowane

#### Odczyt

- `with (String, String, ...)` lub `with String` - konieczność przypisania do zmiennej by wskazać typ
- założenie, że wskazany plik zawiera tylko jedną nutę w danym czasie - przy braku spełnienia założenia brana będzie najdłuższa z nut
- mając wskazany typ, wewnętrznie interpreter czytając plik:

|Typ|Wynik|
|-|-|
|Pitch[]|Przeczyta tylko wysokości nut
|Rythm[]|Przeczyta tylko długości nut
|Motive|Utworzy listę nut o wskazanej wysokości i długości
|Markov|Utworzy listę nut, a następnie przy użyciu `<<` załaduje dane do Markova


## Technologie

### Język
Java 21

### Budowanie
Gradle

### Testowanie
JUnit5, Mockito, assertj

### Zewnętrzne zależności
- javax.sound.midi
    - wbudowana biblioteka w java pozwalająca wykonywać niskopoziomowe operacje na pliku midi - operowanie na poziomie Event'ow KeyOn KeyOff
- log4j
    - bardziej przejrzyste logowanie niż `System.out.println()`
- lombok
    - generacja `Builder'ow` i boilerplatu, oraz null checkow

## Struktura i testowanie

### Stream reader
- odpowiedzialny za podawanie znaku z podanego pliku źródłowego
- usuwanie komentarzy i zamiana wieloktroności białych znaków na pojedynczą spację
- testy
  - podawanie znaku wraz z jego pozycją
  - obsługa różnych znaków białych
  - obsługa komentarzy

### Lexer
- odpowiedzialny za zamianę reprezentacji tekstowej na tokenową
- testy
  - rozponawanie literałów liczbowych/tekstowych/nutowych
  - rozpoznawanie słów kluczowych języka
  - rozpoznawanie identyfikatorów

### Parser
- budowa tablicy symboli i budowa AST
- testy
  - budowa tablicy z odpowiednimi wartościami
  - rozpoznawanie produkcji i odpowiednia struktura drzewa

#### AST
- drzewiasta reprezentacja kodu
- przechowuje produkcje w postaci obiektów, w raz z dostępnym dla nich kontekstem 

#### SemanticAnalyzer
- sprawdzenie poprawności sekwencji produkcji oraz prosta optymalizacja kodu
- testy
  - wywołanie dla przypadków pozytywnych i negatywnych
  - optymalizacja kodu nieosiągalnego, nieużywanego

### Interpreter
- wykonuje program poprzez przechodzenie AST
- implementacja własnych typów 'Rythm/Pitch/Motive/Composer/Iter/Gen'
- testy
  - wykonanie przykładowych kodów źródłowych wraz z oczekiwanymi zmianami stanu
  - obsługa wystąpienia "panic" i wypisanie komunikatu

### MidiReader
- konwertuje plik midi na reprezentacje użytą w AST/interpreterze
- testy
  - odczyt pliku midi i konwersja na podany typ `Rythm[]/Pitch[]/Motive`

### Markov
- moduł odpowiedzialny za reprezentację i operacje na łańcuchach markova
- testy
  - Inicjalizacja z podaną macierzą
  - Przyjęcie listy `Rythm[]/Pitch[]/Motive`
  - Generowanie kolejnych nut na podstawie aktualnego stanu i macierzy przejść(iterator markov)

### MidiWriter
- moduł odpowiedzialny za generowanie pliku midi z podanego obiektu `Song`
- testy
  - generacja midi z zachowaniem ustalonego tempa/długości
  - ilość ścieżek odpowiadająca ilości generatorów


