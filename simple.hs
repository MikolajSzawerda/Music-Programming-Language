Int a = 10;
let b = with(Int a)->Int{
    if(a==1){
        Int a = 20;
        for(Int i in 1->20){
            a += 1;
        }
        return a;
    }
    return a+1;
};

print(b(1));
print(a);
Int tt = 5.0 as Int;
print(tt);
print(5.0 as Int);

let lambda = with(Int a)->lam(Int)->lam(Int)->Int{
    return with(Int d)->lam(Int)->Int{
        return with(Int c)->Int{
               return a+d+c;
        };
    };
};
let test = 4;
print("Lambda execution: "+lambda(test)(2)(test) as String);

let testVoid = with()->Void{
};
let kkl = [1,2,3,4];
let g = [x*2 <| x kkl];
Template tem = 0 as Template;
for(Int i in g){
    tem |= i;
}
Phrase jk = (C, 4) q as Phrase;
print(g);
print("Head: "+(kkl |> head) as String);

let example_fun = with(Int external)->Int{
    if(true){
        Int a = 0;
        for(Int i in [1,2,3]){
            a += 1;
            if(i>=2){
            external = a;
                return a;
            }
        }
        print("Num iter: "+ a as String);
    }
    external = 20;
    return -1;
};
Int ext = 1;
print(example_fun(ext));
print(ext);

let funfun = with(Int a)->Int{
    return a+2;
};
print([funfun(x) <| x kkl]);

let notes2 = [C, E, G#]{dur=e, oct=2};
let tkl = 0 | 1 & 2 | 2 | 1;

print(notes2 |> len);
print(rand()%10);

"Hello" |> panic;