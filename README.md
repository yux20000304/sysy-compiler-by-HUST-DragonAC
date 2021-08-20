华中科技大学 DragonAC队作品

参赛队员：[刘汉鹏](https://github.com/showstarpro)、[张鹏](https://github.com/zippermonkey)、[刘美](https://github.com/rabbicat30)、[杨雨鑫](https://github.com/yux20000304)

# Sysy

A compiler that translates Sysy into ARMv7a

[GitHub项目地址](https://github.com/showstarpro/sysy.git)

[Gitlab项目地址](https://gitlab.com/yux20000304/sysy.git)

## Build

cd ./parser;

jflex Lexer.flex;

java -cp .:java-cup-11b.jar java_cup.Main  < ycalc.cup;

cd ../;

javac -cp .:java-cup-11b.jar Main.java;


## Run

功能测试：compiler -S -o testcase.s testcase.sy

性能测试：compiler -S -o testcase.s testcase.sy -O2

## Grade

排名：17

功能测试：100

性能测试：23.1285

总分：40.2093

## 总结

> 1、前端采用的jflex-javacup自动化工具，实现起来较为的方便快捷
>
> 2、IR的设计是本次大赛的一个难点，我们查看了很多的资料，最后参考LLVM框架的IR进行了相应的修改和改动
>
> 3、本次大赛在功能测试点上进行了相应的加强，由于是多人协作完成的，对代码质量没有进行严格的把控，导致在写代码的时候产生了很多莫名的bug，在debug上花费了大量的时间。最后在一个隐藏样例上，队内没有人会使用fuzz等测开工具，导致无法准确的定位Bug所在，毫无头绪的浪费了2周左右的时间。在初赛的最后也没能找到Bug所在，导致后面的性能测试无法进行评测。最后能以外卡进入大赛也是很荣幸，在初赛结束后，官方解锁了隐藏样例，我们进行了Bug的修改，能够顺利的进行后面的测评。
>
> 4、在优化方向上，我们采用了较为常见的循环外提、死代码去除、公共表达式的删除。
>
> 5、在本次大赛上，很多优胜队伍采用了2到3层的IR，能够进行较为深度的代码优化。
>
> 6、在优化技术上，我们没有采用更多更加新颖的技术手段，这是一大缺陷。官方希望看到更加优秀的优化技术出现，如：多向量、多线程等。包括利用树莓派的硬件进行gpu加速等技术。