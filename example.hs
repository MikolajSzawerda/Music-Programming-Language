((E, 4) w | (G, 4) w | (D, 4) w) & ((C, 4) w | (D, 4) w | (F, 4) w);
[([E, G, D] |> mel), ([C, D, F] |> mel)]{dur=w, oct=4} |> harm;

let a = [E, G, D]{oct=4} |> mel;

let b = a |> 
        repeat 2.0 as Int |>
        transpose -1 |>
        concat a |>
        harm |>
        track Guitar;

let c = "song.mid" |>
        open Track, 0 |>
        head+100; //shoud be parsed as head(x, 100)
        
lam(Int, Int)->Int NWD;

NWD = with(Int a, Int b)->Int {
    if(b!=0){
        return NWD(b, a%b);
    }
    return a;
};
a(1, with()->Int{b(1);c(2);});
let randGen = with(Scale scale, Rythm rythm) -> Phrase {
    if((scale |> isEmpty) || (rythm |> isEmpty)){
        "Provided scale or rythm is empty" |> panic;
    }
    
    let seed = ((scale |> len) * (rythm |> len)) % 3+1;
    let maxLen = [(scale |> len), rythm |> len] |> max;
    let lowestNote = scale |> argmin;
    Template form;
    for(Int i in 1->seed){
        let line = [rand() % maxLen <| dumb_temp 1->(rand()%4+1)*maxLen] |> mel;
        form &= line;
    }
    
    return form>>scale*form>>rythm;
};
    //Hello world
[(a |> track Piano), b, markov(["song1.mid", "song2.mid"], 1)((C, 4) q), [C, E, G] |> randGen [q, w, h] |> track BagPipe] |>
    song 120, 60 |>
    export "demo2.mid";
    
//let x1 = [0, 1, 2] |> concat [3,4] |>len+2;//panic
let x2 = ([0, 1, 2] |> concat [3,4] |> dot (1+3*4^7-1+3/6*12) |>len)+2;
