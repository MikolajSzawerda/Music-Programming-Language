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

