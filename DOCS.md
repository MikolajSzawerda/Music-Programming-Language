Język umożliwiający wygenerowanie utworów muzycznych w postaci MIDI. Język posiada specjalny zapis nutowy oraz udostępnia generatory.

- wysokość i oktawa `(C, 4)`
- długość `q`
- nuta `(C, 4) q`, pauza `rest q`
- sekwencja nut `(C, 4) q | rest e | (B, 4) q` - operator `|`
  - może zostać skrócone do `mel [C rest e B]{dur=q, oct=4}`
- jednoczesne zagranie `(C, 4) q & (E, 4) q & (G, 4) q` - operator `&`
  - może zostać skrócone do `harm [C E G]{oct=4, dur=q}`
```
harm [mel [(C, 4) rest (D, 3) (E, 5)]{dur=q} (E, 4) q mel [(F, 2) h]]
```

- obiekty posiadają pola, które są mutowalne - Motive ma strukturę drzewiastą, gdzie pola dur, oct są dziedziczone od najbliższej nie pustej wartości rodzica(chyba że sam obiekt ma niepuste pole)

```
(C,4){oct=1} //(C,1)
(C,4) q{dur=e, oct=2} //(C,2) e

//at this moment they have oct and dur null
a = harm [C E G]

a{dur=e, oct=1} //now all of them will be trated as having dur=e and oct=1

b = a | (C,4){dur=h}
```

- funkcja bez i jedno, wielo argumentowa

```
factoryFunc = with()->Motive {
    produce (C, 4) q | rest e | (B, 4) q;
}

//treated as value
factoryFunc | (C, 4) q

doubleFunc = with(Int number)->Int {
    produce number*2;
}

doubleFunc doubleFunc doubleFunc doubleFunc 2;

addFunc = with(Int a, Int b)->Int {
    produce a+b;
}
addFunc [addFunc [addFunc [2 3] 2] addFunc [3 2] ]
```


- tworzenie uporządkowanych zbiorów z możliwością indeksowania w artymetyce modularnej

```
a = scale [C D# E G# F]{oct=3}
a[0] //(C, 3)
a[-1] //(F, 3)
a[5] //(C, 3)
b = groove [q e h e]
```

- mechanizm splotu indeksów z obiektem sekwencji

```
c = interval (0 | 1 | 2 & -1) & 0{oct=2};
Motive a;
a = scale [C E G F]{oct=3};
b = groove [q e e];

//C3 | E3| G3| F3 |
//C2 |
a<-c;

//q | e | e | e |
//q |
b<-c;

//(C, 3) q | (E, 3) e | (G, 3) e | (F, 3) e
//(C, 2) q | 
(a<-c)*(b<-c) ;
```

- mechanizm komponowania

```
exampleMotive = with()->Motive {
    m1 = mel [C rest e B]{dur=q, oct=4};
    i1 = mel [0 1 2];
    s1 = scale [(C,1), (D,3), (E,4)];
    produce m1 | s1<-i1{dur=q} & m1{oct=2};
}

motiveRepeat = with(Int motiveLength)->Int{
    c = rand*(double) motiveLength;
    if(c>10.0) {
        produce 1;
    }
    if(c>5.0){
        produce 2;
    }
    produce 3;
}

track [repeat [motiveRepeat len exampleMotive transpose [2 exampleMotive]]]{instrument=Piano, tempo=120, dur=20}->"melody.mid";
```

- mechanizm generacji MIDI

```
exampleMotive = //...

s = with()->Song {
    t1 = track [exampleMotive*2]{instrument=Piano};
    t2 = track [exampleMotive{oct=1} | exampleMotive{oct=2}*2]{instrument=Bass};
    produce  mel [harm [t1 t2]{tempo=120} t1{tempo=60}];

}
    
s{dur=60}->"example_song.mid"
```

