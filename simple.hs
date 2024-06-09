let template = 0 | 1 & 2 |0 |3;
let notes = [C, E, G, F]{dur=q, oct=2};
let treeA = template >> notes;
treeA |>export "test2.mid";

//[([E, G, D] |> mel), ([C, D, F] |> mel)]{dur=w, oct=4} |> harm;

