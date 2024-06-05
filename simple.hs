((E, 4) w | (G, 4) w | (D, 4) w) & ((C, 4) w | (D, 4) w | (F, 4) w) |> export "test.mid";
[([E, G, D] |> mel), ([C, D, F] |> mel)]{dur=w, oct=4} |> harm;

