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
